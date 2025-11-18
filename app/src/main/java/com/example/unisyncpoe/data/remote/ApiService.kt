package com.example.unisyncpoe.data.remote

import com.example.unisyncpoe.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * REST API service interface for UniSync backend
 * All endpoints return ApiResponse<T> wrapper
 */
interface ApiService {
    
    // Authentication
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<User>>
    
    @POST("auth/login")
    suspend fun login(@Body credentials: LoginRequest): Response<ApiResponse<AuthResponse>>
    
    @POST("auth/sso")
    suspend fun ssoLogin(@Body request: SSORequest): Response<ApiResponse<AuthResponse>>
    
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<ApiResponse<User>>
    
    // Users
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<ApiResponse<User>>
    
    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: User): Response<ApiResponse<User>>
    
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<ApiResponse<Unit>>
    
    // Announcements
    @GET("announcements")
    suspend fun getAnnouncements(@Query("courseId") courseId: String? = null): Response<ApiResponse<List<Announcement>>>
    
    @GET("announcements/{id}")
    suspend fun getAnnouncement(@Path("id") id: String): Response<ApiResponse<Announcement>>
    
    @POST("announcements")
    suspend fun createAnnouncement(@Body announcement: Announcement): Response<ApiResponse<Announcement>>
    
    @PUT("announcements/{id}")
    suspend fun updateAnnouncement(@Path("id") id: String, @Body announcement: Announcement): Response<ApiResponse<Announcement>>
    
    @DELETE("announcements/{id}")
    suspend fun deleteAnnouncement(@Path("id") id: String): Response<ApiResponse<Unit>>
    
    // Assignments
    @GET("assignments")
    suspend fun getAssignments(@Query("courseId") courseId: String? = null): Response<ApiResponse<List<Assignment>>>
    
    @GET("assignments/{id}")
    suspend fun getAssignment(@Path("id") id: String): Response<ApiResponse<Assignment>>
    
    @POST("assignments")
    suspend fun createAssignment(@Body assignment: Assignment): Response<ApiResponse<Assignment>>
    
    @PUT("assignments/{id}")
    suspend fun updateAssignment(@Path("id") id: String, @Body assignment: Assignment): Response<ApiResponse<Assignment>>
    
    @PUT("assignments/{id}/submit")
    suspend fun submitAssignment(@Path("id") id: String): Response<ApiResponse<Assignment>>
    
    // Attendance
    @GET("attendance")
    suspend fun getAttendance(
        @Query("studentId") studentId: String? = null,
        @Query("courseId") courseId: String? = null
    ): Response<ApiResponse<List<Attendance>>>
    
    @POST("attendance")
    suspend fun markAttendance(@Body attendance: Attendance): Response<ApiResponse<Attendance>>
    
    @GET("attendance/stats/{studentId}/{courseId}")
    suspend fun getAttendanceStats(
        @Path("studentId") studentId: String,
        @Path("courseId") courseId: String
    ): Response<ApiResponse<AttendanceStats>>
    
    // Timetables
    @GET("timetables")
    suspend fun getTimetables(@Query("dayOfWeek") dayOfWeek: Int? = null): Response<ApiResponse<List<Timetable>>>
    
    @POST("timetables")
    suspend fun createTimetable(@Body timetable: Timetable): Response<ApiResponse<Timetable>>
    
    // QR Codes
    @POST("qr-codes/generate")
    suspend fun generateQRCode(@Body request: QRCodeRequest): Response<ApiResponse<QRCode>>
    
    @POST("qr-codes/scan")
    suspend fun scanQRCode(@Body request: QRCodeScanRequest): Response<ApiResponse<Attendance>>
    
    // Network Posts
    @GET("network/posts")
    suspend fun getNetworkPosts(): Response<ApiResponse<List<NetworkPost>>>
    
    @POST("network/posts")
    suspend fun createNetworkPost(@Body post: NetworkPost): Response<ApiResponse<NetworkPost>>
    
    @POST("network/posts/{id}/like")
    suspend fun likePost(@Path("id") id: String): Response<ApiResponse<NetworkPost>>
    
    // Sync
    @POST("sync/pending")
    suspend fun syncPending(@Body operations: List<SyncOperation>): Response<ApiResponse<SyncResult>>
    
    @GET("sync/status")
    suspend fun getSyncStatus(): Response<ApiResponse<SyncStatus>>
    
    // Notifications
    @POST("notifications/register-token")
    suspend fun registerFCMToken(@Body request: FCMTokenRequest): Response<ApiResponse<Unit>>
}

// Request/Response DTOs
data class RegisterRequest(
    val email: String,
    val name: String,
    val role: String,
    val password: String,
    val studentId: String? = null,
    val lecturerId: String? = null,
    val coordinatorId: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val user: User,
    val token: String
)

data class QRCodeRequest(
    val courseId: String,
    val lecturerId: String,
    val classDate: Long,
    val durationMinutes: Int = 15
)

data class QRCodeScanRequest(
    val qrData: String,
    val studentId: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class AttendanceStats(
    val totalClasses: Int,
    val presentCount: Int,
    val absentCount: Int,
    val lateCount: Int,
    val attendancePercentage: Double
)

data class SSORequest(
    val idToken: String
)

data class SyncOperation(
    val operation: String, // "CREATE", "UPDATE", "DELETE"
    val entityType: String,
    val entityId: String,
    val entityData: Map<String, Any>
)

data class SyncResult(
    val synced: Int,
    val failed: Int,
    val results: List<SyncOperationResult>,
    val errors: List<SyncOperationError>
)

data class SyncOperationResult(
    val operation: String,
    val entityType: String,
    val entityId: String,
    val status: String
)

data class SyncOperationError(
    val operation: String,
    val entityType: String,
    val entityId: String,
    val error: String
)

data class SyncStatus(
    val userId: String,
    val lastSyncTime: Long,
    val isOnline: Boolean
)

data class FCMTokenRequest(
    val fcmToken: String
)

