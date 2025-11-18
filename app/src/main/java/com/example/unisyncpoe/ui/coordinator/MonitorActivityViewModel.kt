package com.example.unisyncpoe.ui.coordinator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.unisyncpoe.data.local.dao.AnnouncementDao
import com.example.unisyncpoe.data.local.dao.AssignmentDao
import com.example.unisyncpoe.data.local.dao.AttendanceDao
import com.example.unisyncpoe.data.local.dao.UserDao
import com.example.unisyncpoe.data.model.*
import com.example.unisyncpoe.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data classes for activity monitoring
 */
data class LecturerActivity(
    val id: String,
    val lecturerId: String,
    val lecturerName: String,
    val action: String,
    val moduleCode: String? = null,
    val timestamp: Long,
    val details: String? = null
)

data class StudentEngagement(
    val id: String,
    val studentId: String,
    val studentName: String,
    val moduleCode: String,
    val metric: String, // e.g., "Attendance", "Submissions", "Participation"
    val value: String // e.g., "85%", "12/15", "High"
)

data class ActivityStatistics(
    val totalLecturers: Int,
    val activeLecturers: Int,
    val totalStudents: Int,
    val engagedStudents: Int,
    val averageAttendance: Int,
    val submissionRate: Int
)

@HiltViewModel
class MonitorActivityViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userDao: UserDao,
    private val attendanceDao: AttendanceDao,
    private val assignmentDao: AssignmentDao,
    private val announcementDao: AnnouncementDao
) : ViewModel() {
    
    companion object {
        private const val TAG = "MonitorActivityVM"
    }
    
    private val _lecturerActivity = MutableStateFlow<List<LecturerActivity>>(emptyList())
    val lecturerActivity: StateFlow<List<LecturerActivity>> = _lecturerActivity.asStateFlow()
    
    private val _studentEngagement = MutableStateFlow<List<StudentEngagement>>(emptyList())
    val studentEngagement: StateFlow<List<StudentEngagement>> = _studentEngagement.asStateFlow()
    
    private val _activityStats = MutableStateFlow<ActivityStatistics>(
        ActivityStatistics(0, 0, 0, 0, 0, 0)
    )
    val activityStats: StateFlow<ActivityStatistics> = _activityStats.asStateFlow()
    
    init {
        loadActivity()
    }
    
    fun loadActivity() {
        viewModelScope.launch {
            calculateStatistics()
            loadLecturerActivities()
            loadStudentEngagement()
        }
    }
    
    private suspend fun loadLecturerActivities() {
        try {
            val activities = mutableListOf<LecturerActivity>()
            
            // Get all users first for name resolution
            val users = mutableListOf<User>()
            var usersLoaded = false
            userRepository.getAllUsers().collect { userList ->
                if (!usersLoaded) {
                    usersLoaded = true
                    users.addAll(userList)
                }
            }
            val userMap = users.associateBy { it.id }
            
            // Get lecturer activities from attendance records
            val attendances = attendanceDao.getRecentAttendances()
            // Group by lecturer and course to avoid duplicates
            attendances.groupBy { "${it.lecturerId}_${it.courseId}_${it.classDate}" }
                .values.forEach { group ->
                    val attendance = group.first()
                    val lecturer = userMap[attendance.lecturerId]
                    activities.add(
                        LecturerActivity(
                            id = "attendance_${attendance.id}",
                            lecturerId = attendance.lecturerId,
                            lecturerName = lecturer?.name ?: attendance.lecturerId,
                            action = "Marked attendance",
                            moduleCode = attendance.courseName,
                            timestamp = attendance.markedAt,
                            details = "Marked attendance for ${group.size} students in ${attendance.courseName}"
                        )
                    )
                }
            
            // Get lecturer activities from announcements
            val announcements = announcementDao.getRecentAnnouncements()
            announcements.forEach { announcement ->
                val lecturer = userMap[announcement.authorId]
                activities.add(
                    LecturerActivity(
                        id = "announcement_${announcement.id}",
                        lecturerId = announcement.authorId,
                        lecturerName = lecturer?.name ?: announcement.authorName,
                        action = "Created announcement",
                        moduleCode = announcement.courseName ?: "General",
                        timestamp = announcement.createdAt,
                        details = announcement.title
                    )
                )
            }
            
            // Get lecturer activities from assignments - group by lecturer and course
            val assignments = assignmentDao.getRecentAssignments()
            // Group by lecturer and course to show unique assignments
            assignments.groupBy { "${it.lecturerId}_${it.courseId}_${it.title}" }
                .values.forEach { group ->
                    val assignment = group.first()
                    activities.add(
                        LecturerActivity(
                            id = "assignment_${assignment.id}",
                            lecturerId = assignment.lecturerId,
                            lecturerName = assignment.lecturerName,
                            action = "Created assignment",
                            moduleCode = assignment.courseName,
                            timestamp = assignment.createdAt,
                            details = "${assignment.title} (${group.size} student submissions)"
                        )
                    )
                }
            
            // Update with sorted activities (limit to 50 most recent)
            _lecturerActivity.value = activities.sortedByDescending { it.timestamp }.take(50)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading lecturer activities", e)
        }
    }
    
    private suspend fun loadStudentEngagement() {
        try {
            val engagements = mutableListOf<StudentEngagement>()
            
            // Get all users first for name resolution
            val users = mutableListOf<User>()
            var usersLoaded = false
            userRepository.getAllUsers().collect { userList ->
                if (!usersLoaded) {
                    usersLoaded = true
                    users.addAll(userList)
                }
            }
            val userMap = users.associateBy { it.id }
            
            // Get student engagement from attendance records
            val attendances = attendanceDao.getAllAttendances()
            // Group by student and course
            val studentCourseMap = attendances.groupBy { "${it.studentId}_${it.courseId}" }
            
            studentCourseMap.forEach { (key, records) ->
                val firstRecord = records.first()
                val presentCount = records.count { it.status == AttendanceStatus.PRESENT }
                val totalCount = records.size
                val percentage = if (totalCount > 0) ((presentCount.toDouble() / totalCount) * 100).toInt() else 0
                
                val student = userMap[firstRecord.studentId]
                engagements.add(
                    StudentEngagement(
                        id = "attendance_${key}",
                        studentId = firstRecord.studentId,
                        studentName = student?.name ?: firstRecord.studentName,
                        moduleCode = firstRecord.courseName,
                        metric = "Attendance",
                        value = "$percentage% ($presentCount/$totalCount)"
                    )
                )
            }
            
            // Get student engagement from assignments - calculate per student
            val assignments = assignmentDao.getAllAssignmentsList()
            // Extract student ID from assignment ID pattern: "assignment_lecturerId_studentId_i"
            assignments.groupBy { assignment ->
                val parts = assignment.id.split("_")
                if (parts.size >= 3) "${parts[2]}_${assignment.courseId}" else "unknown_${assignment.courseId}"
            }.forEach { (key, studentAssignments) ->
                val firstAssignment = studentAssignments.first()
                val studentId = key.split("_").firstOrNull() ?: "unknown"
                val submitted = studentAssignments.count { 
                    it.submissionStatus in listOf(SubmissionStatus.SUBMITTED, SubmissionStatus.GRADED) 
                }
                val total = studentAssignments.size
                
                if (studentId != "unknown" && total > 0) {
                    val student = userMap[studentId]
                    engagements.add(
                        StudentEngagement(
                            id = "submission_$key",
                            studentId = studentId,
                            studentName = student?.name ?: "Student $studentId",
                            moduleCode = firstAssignment.courseName,
                            metric = "Submissions",
                            value = "$submitted/$total"
                        )
                    )
                }
            }
            
            // Update with all engagements (limit to 100 most relevant)
            _studentEngagement.value = engagements.take(100)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading student engagement", e)
        }
    }
    
    private suspend fun calculateStatistics() {
        try {
            // Sync users first to ensure we have latest data
            userRepository.getUserStatistics()
            
            // Get user counts
            val totalLecturers = userDao.getLecturerCount()
            val totalStudents = userDao.getStudentCount()
            
            // Calculate active lecturers (lecturers with activity in last 7 days)
            val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            val activeLecturers = attendanceDao.getActiveLecturersCount(weekAgo)
            
            // Calculate engaged students (students with activity in last 7 days)
            val engagedStudents = attendanceDao.getEngagedStudentsCount(weekAgo)
            
            // Calculate average attendance percentage
            val totalPresent = attendanceDao.getTotalPresentCount()
            val totalAttendance = attendanceDao.getTotalAttendanceCount()
            val averageAttendance = if (totalAttendance > 0) {
                ((totalPresent.toDouble() / totalAttendance) * 100).toInt()
            } else {
                0
            }
            
            // Calculate submission rate
            val submittedCount = assignmentDao.getSubmittedCount()
            val totalAssignments = assignmentDao.getTotalAssignmentsCount()
            val submissionRate = if (totalAssignments > 0) {
                ((submittedCount.toDouble() / totalAssignments) * 100).toInt()
            } else {
                0
            }
            
            // Update statistics
            _activityStats.value = ActivityStatistics(
                totalLecturers = totalLecturers,
                activeLecturers = activeLecturers.coerceAtMost(totalLecturers), // Cap at total
                totalStudents = totalStudents,
                engagedStudents = engagedStudents.coerceAtMost(totalStudents), // Cap at total
                averageAttendance = averageAttendance,
                submissionRate = submissionRate
            )
        } catch (e: Exception) {
            // On error, keep default values (0, 0, 0, 0, 0, 0)
            android.util.Log.e("MonitorActivityVM", "Error calculating statistics", e)
        }
    }
    
    fun initializeDummyData() {
        viewModelScope.launch {
            injectSampleData()
            loadActivity()
        }
    }
    
    private suspend fun injectSampleData() {
        try {
            // Check if data already exists using direct queries
            val attendances = attendanceDao.getAllAttendances()
            if (attendances.isEmpty()) {
                injectSampleAttendance()
            }
            
            val assignments = assignmentDao.getAllAssignmentsList()
            if (assignments.isEmpty()) {
                injectSampleAssignments()
            }
            
            val announcements = announcementDao.getAllAnnouncementsList()
            if (announcements.isEmpty()) {
                injectSampleAnnouncements()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for existing data", e)
        }
    }
    
    private suspend fun injectSampleAttendance() {
        // Get lecturers and students from database
        val users = mutableListOf<User>()
        var usersLoaded = false
        userRepository.getAllUsers().collect { userList ->
            if (!usersLoaded) {
                usersLoaded = true
                users.addAll(userList)
            }
        }
        
        val lecturers = users.filter { it.role == UserRole.LECTURER }
        val students = users.filter { it.role == UserRole.STUDENT }
        
        if (lecturers.isEmpty() || students.isEmpty()) {
            Log.d(TAG, "No lecturers or students found, skipping sample attendance injection")
            return
        }
        
        val sampleAttendances = mutableListOf<Attendance>()
        val now = System.currentTimeMillis()
        
        // Create attendance records for the last 2 weeks
        for (day in 0..13) {
            val classDate = now - (day * 24 * 60 * 60 * 1000L)
            lecturers.forEachIndexed { lecturerIndex, lecturer ->
                students.take(10).forEachIndexed { studentIndex, student ->
                    val status = when {
                        studentIndex % 10 == 0 -> AttendanceStatus.ABSENT
                        studentIndex % 10 == 1 -> AttendanceStatus.LATE
                        studentIndex % 10 == 2 -> AttendanceStatus.EXCUSED
                        else -> AttendanceStatus.PRESENT
                    }
                    
                    sampleAttendances.add(
                        Attendance(
                            id = "attendance_${lecturer.id}_${student.id}_$day",
                            studentId = student.id,
                            studentName = student.name,
                            courseId = "course_${lecturerIndex % 3}",
                            courseName = when (lecturerIndex % 3) {
                                0 -> "CS101"
                                1 -> "CS201"
                                else -> "MATH101"
                            },
                            lecturerId = lecturer.id,
                            classDate = classDate,
                            markedAt = classDate + (60 * 60 * 1000L), // 1 hour after class
                            status = status
                        )
                    )
                }
            }
        }
        
        attendanceDao.insertAttendances(sampleAttendances)
        Log.d(TAG, "Injected ${sampleAttendances.size} sample attendance records")
    }
    
    private suspend fun injectSampleAssignments() {
        val users = mutableListOf<User>()
        var usersLoaded = false
        userRepository.getAllUsers().collect { userList ->
            if (!usersLoaded) {
                usersLoaded = true
                users.addAll(userList)
            }
        }
        
        val lecturers = users.filter { it.role == UserRole.LECTURER }
        val students = users.filter { it.role == UserRole.STUDENT }
        
        if (lecturers.isEmpty() || students.isEmpty()) {
            Log.d(TAG, "No lecturers or students found, skipping sample assignments injection")
            return
        }
        
        val sampleAssignments = mutableListOf<Assignment>()
        val now = System.currentTimeMillis()
        
        lecturers.forEachIndexed { lecturerIndex, lecturer ->
            val courseName = when (lecturerIndex % 3) {
                0 -> "CS101"
                1 -> "CS201"
                else -> "MATH101"
            }
            
            // Create 5 assignments per lecturer
            for (i in 1..5) {
                val dueDate = now + (i * 7 * 24 * 60 * 60 * 1000L) // Due in i weeks
                val createdAt = now - ((5 - i) * 7 * 24 * 60 * 60 * 1000L) // Created (5-i) weeks ago
                
                // Create assignment for each student with varying submission status
                students.take(15).forEachIndexed { studentIndex, student ->
                    val submissionStatus = when {
                        studentIndex % 5 == 0 -> SubmissionStatus.NOT_SUBMITTED
                        studentIndex % 5 == 1 -> SubmissionStatus.SUBMITTED
                        studentIndex % 5 == 2 -> SubmissionStatus.GRADED
                        studentIndex % 5 == 3 -> SubmissionStatus.LATE
                        else -> SubmissionStatus.SUBMITTED
                    }
                    
                    sampleAssignments.add(
                        Assignment(
                            id = "assignment_${lecturer.id}_${student.id}_$i",
                            title = "$courseName Assignment $i",
                            description = "Assignment $i for $courseName",
                            courseId = "course_${lecturerIndex % 3}",
                            courseName = courseName,
                            lecturerId = lecturer.id,
                            lecturerName = lecturer.name,
                            dueDate = dueDate,
                            createdAt = createdAt,
                            submissionStatus = submissionStatus,
                            submittedAt = if (submissionStatus != SubmissionStatus.NOT_SUBMITTED) {
                                createdAt + (3 * 24 * 60 * 60 * 1000L)
                            } else null,
                            score = if (submissionStatus == SubmissionStatus.GRADED) {
                                (70 + (studentIndex % 30))
                            } else null
                        )
                    )
                }
            }
        }
        
        assignmentDao.insertAssignments(sampleAssignments)
        Log.d(TAG, "Injected ${sampleAssignments.size} sample assignment records")
    }
    
    private suspend fun injectSampleAnnouncements() {
        val users = mutableListOf<User>()
        var usersLoaded = false
        userRepository.getAllUsers().collect { userList ->
            if (!usersLoaded) {
                usersLoaded = true
                users.addAll(userList)
            }
        }
        
        val lecturers = users.filter { it.role == UserRole.LECTURER }
        
        if (lecturers.isEmpty()) {
            Log.d(TAG, "No lecturers found, skipping sample announcements injection")
            return
        }
        
        val sampleAnnouncements = mutableListOf<Announcement>()
        val now = System.currentTimeMillis()
        
        lecturers.forEachIndexed { lecturerIndex, lecturer ->
            val courseName = when (lecturerIndex % 3) {
                0 -> "CS101"
                1 -> "CS201"
                else -> "MATH101"
            }
            
            // Create 3 announcements per lecturer
            for (i in 1..3) {
                sampleAnnouncements.add(
                    Announcement(
                        id = "announcement_${lecturer.id}_$i",
                        title = "$courseName Announcement $i",
                        content = "This is announcement $i for $courseName",
                        courseId = "course_${lecturerIndex % 3}",
                        courseName = courseName,
                        authorId = lecturer.id,
                        authorName = lecturer.name,
                        createdAt = now - ((3 - i) * 2 * 24 * 60 * 60 * 1000L), // Created (3-i)*2 days ago
                        priority = when (i) {
                            1 -> AnnouncementPriority.HIGH
                            2 -> AnnouncementPriority.NORMAL
                            else -> AnnouncementPriority.LOW
                        }
                    )
                )
            }
        }
        
        announcementDao.insertAnnouncements(sampleAnnouncements)
        Log.d(TAG, "Injected ${sampleAnnouncements.size} sample announcement records")
    }
}

