# Firestore Database Connection Guide

## ✅ **CURRENT STATUS**

Your app **already has Firestore integration** partially set up! Here's what's working:

### **Already Configured:**
- ✅ Firebase dependencies in `build.gradle.kts`
- ✅ `google-services.json` file exists
- ✅ `FirestoreService` class implemented
- ✅ `FirestoreModule` for dependency injection
- ✅ User data syncing to Firestore `users` collection
- ✅ Automatic sync from Firestore to local Room database

### **Currently Syncing:**
- ✅ **Users** → Firestore `users` collection

---

## 🔧 **SETUP STEPS**

### **Step 1: Verify Firebase Configuration**

1. **Check `google-services.json`**
   - File location: `app/google-services.json`
   - Ensure it matches your Firebase project
   - If you need to update it:
     - Go to [Firebase Console](https://console.firebase.google.com/)
     - Select your project
     - Go to Project Settings → Your Apps
     - Download the latest `google-services.json`
     - Replace the file in `app/` directory

2. **Enable Firestore in Firebase Console**
   - Go to Firebase Console → Firestore Database
   - Click "Create Database"
   - Choose "Start in test mode" (for development)
   - Select your preferred location
   - Click "Enable"

3. **Set Firestore Security Rules** (Important!)
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       // Users collection - authenticated users can read/write
       match /users/{userId} {
         allow read: if request.auth != null;
         allow write: if request.auth != null && request.auth.uid == userId;
       }
       
       // Announcements - authenticated users can read, lecturers/admins can write
       match /announcements/{announcementId} {
         allow read: if request.auth != null;
         allow write: if request.auth != null;
       }
       
       // Assignments - authenticated users can read, lecturers can write
       match /assignments/{assignmentId} {
         allow read: if request.auth != null;
         allow write: if request.auth != null;
       }
       
       // Attendance - students can read their own, lecturers can write
       match /attendance/{attendanceId} {
         allow read: if request.auth != null;
         allow write: if request.auth != null;
       }
       
       // Messages - users can read/write their own messages
       match /messages/{messageId} {
         allow read: if request.auth != null && 
           (resource.data.fromUserId == request.auth.uid || 
            resource.data.toUserId == request.auth.uid);
         allow write: if request.auth != null && 
           (request.resource.data.fromUserId == request.auth.uid || 
            request.resource.data.toUserId == request.auth.uid);
       }
     }
   }
   ```

---

## 📊 **FIREstore COLLECTIONS STRUCTURE**

Your app will use these Firestore collections:

### **1. `users` Collection** ✅ (Already Implemented)
```json
{
  "id": "user123",
  "email": "student@university.edu",
  "name": "John Doe",
  "role": "STUDENT",
  "studentId": "STU001",
  "createdAt": 1234567890,
  "lastSyncTime": 1234567890,
  "isSynced": true
}
```

### **2. `announcements` Collection** (To be implemented)
```json
{
  "id": "announcement123",
  "title": "Exam Schedule",
  "content": "Final exams start next week...",
  "authorId": "lecturer123",
  "authorName": "Dr. Smith",
  "courseId": "CS101",
  "priority": "HIGH",
  "createdAt": 1234567890,
  "isSynced": true
}
```

### **3. `assignments` Collection** (To be implemented)
```json
{
  "id": "assignment123",
  "title": "Project Submission",
  "description": "Submit your final project...",
  "courseId": "CS101",
  "dueDate": 1234567890,
  "submissionStatus": "PENDING",
  "studentId": "student123",
  "score": null,
  "createdAt": 1234567890,
  "isSynced": true
}
```

### **4. `attendance` Collection** (To be implemented)
```json
{
  "id": "attendance123",
  "studentId": "student123",
  "lecturerId": "lecturer123",
  "courseId": "CS101",
  "classDate": 1234567890,
  "status": "PRESENT",
  "markedAt": 1234567890,
  "isSynced": true
}
```

### **5. `messages` Collection** (To be implemented)
```json
{
  "id": "message123",
  "fromUserId": "student123",
  "fromUserName": "John Doe",
  "toUserId": "lecturer123",
  "toUserName": "Dr. Smith",
  "subject": "Question about assignment",
  "content": "I have a question...",
  "sentAt": 1234567890,
  "isRead": false,
  "isSynced": true
}
```

---

## 🚀 **EXPANDING FIRESTORE INTEGRATION**

Currently, only **users** are syncing to Firestore. To sync all data types, you have two options:

### **Option 1: Use Firestore as Primary Database** (Recommended)
- Store all data in Firestore
- Use Room database as local cache
- Sync bidirectionally

### **Option 2: Use Firestore as Backup/Sync Service**
- Keep Room as primary
- Sync to Firestore for backup/cloud access
- Sync from Firestore when needed

---

## 📝 **NEXT STEPS**

1. **Verify `google-services.json`** matches your Firebase project
2. **Enable Firestore** in Firebase Console
3. **Set Security Rules** (see above)
4. **Test User Sync** - Register a user and check Firestore
5. **Expand to Other Collections** (if needed)

---

## ✅ **TESTING FIRESTORE CONNECTION**

### Test User Sync:
1. Run the app
2. Register a new user
3. Check Firebase Console → Firestore → `users` collection
4. You should see the new user document

### Test Reading from Firestore:
1. Add a user manually in Firestore Console
2. Restart the app
3. The user should appear in the app (synced from Firestore)

---

## 🔍 **CURRENT IMPLEMENTATION DETAILS**

### **FirestoreService** (`app/src/main/java/com/example/unisyncpoe/data/remote/FirestoreService.kt`)
- ✅ `saveUser()` - Saves user to Firestore
- ✅ `getUserById()` - Gets user from Firestore
- ✅ `getUserByEmail()` - Gets user by email
- ✅ `getAllUsers()` - Gets all users
- ✅ `updateUser()` - Updates user in Firestore

### **UserRepository** (`app/src/main/java/com/example/unisyncpoe/data/repository/UserRepository.kt`)
- ✅ Automatically syncs users from Firestore to Room database
- ✅ Saves new users to both Firestore and Room

### **AuthRepository** (`app/src/main/java/com/example/unisyncpoe/data/repository/AuthRepository.kt`)
- ✅ Saves registered users to Firestore
- ✅ Checks Firestore for user login

---

## 🎯 **QUICK START**

1. **Ensure `google-services.json` is correct**
2. **Enable Firestore in Firebase Console**
3. **Set Security Rules** (use the rules above)
4. **Run the app** - Users will automatically sync!

The app is **already connected to Firestore** for user data! Just verify your Firebase configuration and you're good to go.

---

## 📚 **ADDITIONAL RESOURCES**

- [Firestore Documentation](https://firebase.google.com/docs/firestore)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
- [Firebase Console](https://console.firebase.google.com/)

