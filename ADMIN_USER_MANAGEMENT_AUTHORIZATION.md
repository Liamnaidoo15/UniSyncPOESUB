# Admin User Management Authorization

## ✅ **Implementation Complete**

Admins now have full authorization to edit and delete users in the "Manage User Accounts" feature.

## **Authorization Layers**

### **1. Activity-Level Authorization**
**File:** `app/src/main/java/com/example/unisyncpoe/ui/admin/UserManagementActivity.kt`
- ✅ `checkAdminAccess()` method verifies user is ADMIN before allowing access
- ✅ Activity closes immediately if user is not admin
- ✅ Only admins can access the User Management screen

### **2. ViewModel-Level Authorization**
**File:** `app/src/main/java/com/example/unisyncpoe/ui/admin/UserManagementViewModel.kt`

#### **Register User**
- ✅ Checks if current user is ADMIN before allowing registration
- ✅ Returns error: "Only administrators can register users"

#### **Update User**
- ✅ Checks if current user is ADMIN before allowing update
- ✅ Returns error: "Only administrators can update users"
- ✅ Updates user in both Firestore and local database

#### **Delete User**
- ✅ Checks if current user is ADMIN before allowing deletion
- ✅ Returns error: "Only administrators can delete users"
- ✅ Prevents admins from deleting their own account
- ✅ Returns error: "Cannot delete your own account"
- ✅ Deletes user from both Firestore and local database

### **3. Firestore Security Rules**
**File:** `firestore.rules`

#### **Users Collection Rules:**
```javascript
match /users/{userId} {
  // All authenticated users can read user data
  allow read: if isAuthenticated();
  
  // Users can create their own profile during registration
  // Admins can create any user
  allow create: if isAuthenticated() && 
                   (getUserId() == userId || isAdmin());
  
  // Users can update their own profile
  // Admins can update any user
  allow update: if isAuthenticated() && 
                   (getUserId() == userId || isAdmin());
  
  // Only admins can delete users
  allow delete: if isAdmin();
}
```

### **4. API-Level Authorization**
**File:** `api/routes/users.js`

#### **Update User Endpoint** (`PUT /api/users/:id`)
- ✅ Checks if user is updating their own profile OR is ADMIN
- ✅ Returns 403 if non-admin tries to update another user

#### **Delete User Endpoint** (`DELETE /api/users/:id`)
- ✅ Requires ADMIN role via `requireRole('ADMIN')` middleware
- ✅ Prevents admins from deleting themselves
- ✅ Returns 400 if admin tries to delete their own account

## **Features Available to Admins**

### **1. View All Users**
- ✅ View complete list of all registered users
- ✅ Filter users by role (Student, Lecturer, Coordinator, Admin)
- ✅ See user details: name, email, role, and ID

### **2. Register New Users**
- ✅ Create new user accounts with any role
- ✅ Set user-specific IDs (studentId, lecturerId, coordinatorId)
- ✅ Assign passwords during registration

### **3. Edit Users**
- ✅ Update user name
- ✅ Change user role
- ✅ Update user-specific IDs
- ✅ Changes are synced to both Firestore and local database

### **4. Delete Users**
- ✅ Delete any user account (except own account)
- ✅ Confirmation dialog before deletion
- ✅ User is removed from both Firestore and local database
- ✅ Cannot delete own account (safety measure)

## **Security Measures**

1. **Multi-Layer Authorization:**
   - Activity-level check (prevents UI access)
   - ViewModel-level check (prevents function execution)
   - Firestore rules (prevents unauthorized database operations)
   - API-level check (if using API endpoints)

2. **Self-Protection:**
   - Admins cannot delete their own account
   - Prevents accidental account lockout

3. **Error Handling:**
   - Clear error messages for unauthorized actions
   - Logging of all admin operations

## **Testing Checklist**

- [x] Non-admin users cannot access User Management screen
- [x] Admin can view all users
- [x] Admin can register new users
- [x] Admin can edit any user's information
- [x] Admin can change user roles
- [x] Admin can delete any user (except themselves)
- [x] Admin cannot delete their own account
- [x] Non-admin users cannot edit other users (via ViewModel check)
- [x] Non-admin users cannot delete users (via ViewModel check)
- [x] Firestore rules prevent unauthorized operations
- [x] Changes are synced to both Firestore and local database

## **Files Modified**

1. `app/src/main/java/com/example/unisyncpoe/ui/admin/UserManagementViewModel.kt`
   - Added admin check to `updateUser()`
   - Added admin check to `deleteUser()`

2. `api/routes/users.js`
   - Added `DELETE /api/users/:id` endpoint with admin-only authorization

3. `firestore.rules`
   - Already configured correctly (no changes needed)

## **Usage**

### **For Admins:**
1. Navigate to Admin Dashboard
2. Click "Manage User Accounts"
3. View, edit, or delete users as needed
4. Use "Register New User" button to create accounts

### **For Non-Admins:**
- User Management screen is not accessible
- If somehow accessed, all operations will fail with authorization errors

## **Notes**

- All admin operations are logged for audit purposes
- User deletions are permanent and cannot be undone
- Admins should exercise caution when deleting users
- The system prevents admins from deleting themselves to avoid account lockout

