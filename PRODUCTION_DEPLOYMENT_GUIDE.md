# Production Deployment Guide

This guide will help you prepare and deploy the UniSync app for production.

## 📋 Pre-Deployment Checklist

### 1. **API Configuration**

#### Update Production API URL

1. Open `app/build.gradle.kts`
2. Find the `release` build type
3. Update the `API_BASE_URL` to your production API endpoint:

```kotlin
release {
    buildConfigField("String", "API_BASE_URL", "\"https://your-production-api.com/api/\"")
}
```

**Important:**
- Use HTTPS only for production
- Ensure your API server has a valid SSL certificate
- Test the API endpoint before building

#### API Server Requirements

- ✅ HTTPS enabled with valid SSL certificate
- ✅ CORS configured for your app domain
- ✅ JWT_SECRET set in environment variables
- ✅ Firebase Admin SDK configured
- ✅ All endpoints tested and working

### 2. **Firebase Configuration**

#### Verify Firebase Setup

1. **Firebase Console:**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Select your project
   - Verify Authentication is enabled
   - Verify Cloud Messaging is enabled
   - Verify Firestore is enabled

2. **google-services.json:**
   - Ensure `app/google-services.json` is from your production Firebase project
   - Verify the package name matches: `com.example.unisyncpoe`

3. **Web Client ID:**
   - Go to Firebase Console → Authentication → Sign-in method → Google
   - Copy the Web client ID
   - Update `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="default_web_client_id">YOUR_WEB_CLIENT_ID</string>
   ```

### 3. **App Signing**

#### Generate Signing Key

1. Create a keystore file:
   ```bash
   keytool -genkey -v -keystore unisync-release.keystore -alias unisync -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Create `keystore.properties` in the project root (add to `.gitignore`):
   ```properties
   storePassword=your_store_password
   keyPassword=your_key_password
   keyAlias=unisync
   storeFile=unisync-release.keystore
   ```

3. Update `app/build.gradle.kts` to add signing config:
   ```kotlin
   android {
       // ... existing code ...
       
       signingConfigs {
           create("release") {
               val keystorePropertiesFile = rootProject.file("keystore.properties")
               val keystoreProperties = java.util.Properties()
               if (keystorePropertiesFile.exists()) {
                   keystoreProperties.load(java.io.FileInputStream(keystorePropertiesFile))
                   storeFile = file(keystoreProperties["storeFile"] as String)
                   storePassword = keystoreProperties["storePassword"] as String
                   keyAlias = keystoreProperties["keyAlias"] as String
                   keyPassword = keystoreProperties["keyPassword"] as String
               }
           }
       }
       
       buildTypes {
           release {
               signingConfig = signingConfigs.getByName("release")
               // ... existing release config ...
           }
       }
   }
   ```

### 4. **Build Configuration**

#### Verify Build Settings

✅ **Already Configured:**
- ProGuard/R8 minification enabled for release
- Resource shrinking enabled
- Debuggable set to false for release
- Comprehensive ProGuard rules
- HTTPS-only network security config

#### Build Release APK

```bash
./gradlew assembleRelease
```

The APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

#### Build Release AAB (for Google Play)

```bash
./gradlew bundleRelease
```

The AAB will be generated at:
`app/build/outputs/bundle/release/app-release.aab`

### 5. **Version Management**

#### Current Version
- **Version Code:** 1
- **Version Name:** 1.0.0

#### Updating Version

Before each release, update in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 2  // Increment for each release
    versionName = "1.0.1"  // Update version name
}
```

**Version Code Rules:**
- Must be incremented for each release
- Cannot be decreased
- Used by Google Play to determine updates

### 6. **Security Checklist**

✅ **Completed:**
- ProGuard/R8 enabled (code obfuscation)
- HTTPS-only network traffic
- No hardcoded secrets
- Encrypted credential storage
- Secure token handling

⚠️ **Verify:**
- API keys are not committed to version control
- Firebase service account keys are secure
- JWT secrets are strong and unique
- Firestore security rules are properly configured

### 7. **Testing**

#### Pre-Release Testing

- [ ] Test all authentication flows (email/password, SSO, biometric)
- [ ] Test offline mode and sync functionality
- [ ] Test push notifications
- [ ] Test all CRUD operations
- [ ] Test role-based access control
- [ ] Test on multiple Android versions (API 24+)
- [ ] Test on different screen sizes
- [ ] Test with slow/unstable network
- [ ] Test app behavior after force stop
- [ ] Test app updates (if updating existing app)

#### Performance Testing

- [ ] App startup time < 3 seconds
- [ ] No memory leaks
- [ ] Smooth scrolling in lists
- [ ] Efficient database queries
- [ ] Proper image loading and caching

### 8. **Google Play Store Preparation**

#### Required Assets

1. **App Icon:** 512x512px PNG
2. **Feature Graphic:** 1024x500px PNG
3. **Screenshots:** 
   - Phone: At least 2, up to 8
   - Tablet (if supported): At least 2, up to 8
4. **App Description:** Up to 4000 characters
5. **Short Description:** Up to 80 characters
6. **Privacy Policy URL:** Required for apps with user data

#### Content Rating

Complete the content rating questionnaire in Google Play Console.

#### Store Listing

- App name: "UniSync"
- Category: Education
- Tags: Education, University, Student Management
- Contact details: Your support email

### 9. **Post-Deployment**

#### Monitor

- [ ] Crash reports (Firebase Crashlytics)
- [ ] Analytics (Firebase Analytics)
- [ ] User feedback (Google Play reviews)
- [ ] API performance and errors
- [ ] Push notification delivery

#### Updates

- Plan regular updates for bug fixes and features
- Increment version code for each update
- Test updates thoroughly before release

## 🚀 Deployment Steps

### Step 1: Final Checks

1. Update API URL in `build.gradle.kts`
2. Verify Firebase configuration
3. Update version code/name if needed
4. Run all tests

### Step 2: Build Release

```bash
# Clean previous builds
./gradlew clean

# Build release AAB (for Google Play)
./gradlew bundleRelease

# Or build APK (for direct distribution)
./gradlew assembleRelease
```

### Step 3: Test Release Build

1. Install the release build on a test device
2. Test all critical features
3. Verify API connectivity
4. Test push notifications

### Step 4: Upload to Google Play

1. Go to [Google Play Console](https://play.google.com/console)
2. Create new app or select existing
3. Upload AAB file
4. Complete store listing
5. Submit for review

### Step 5: Monitor

- Monitor crash reports
- Respond to user reviews
- Track analytics
- Plan updates

## 📝 Important Notes

### API URL Configuration

The app uses different API URLs for debug and release builds:

- **Debug:** `http://10.0.2.2:3000/api/` (localhost for emulator)
- **Release:** `https://your-production-api.com/api/` (UPDATE THIS)

### Network Security

- Production builds use HTTPS only
- Cleartext traffic is disabled
- Network security config enforces SSL certificates

### ProGuard

- Code is obfuscated and minified in release builds
- All necessary rules are configured
- Test release builds thoroughly to catch any ProGuard issues

### Firebase

- Ensure `google-services.json` is from production project
- Verify all Firebase services are enabled
- Test push notifications before release

## 🔒 Security Reminders

1. **Never commit:**
   - `keystore.properties`
   - `*.keystore` files
   - `.env` files
   - Firebase service account keys

2. **Always use:**
   - HTTPS for production API
   - Strong JWT secrets
   - Secure password hashing
   - Encrypted credential storage

3. **Review:**
   - Firestore security rules
   - API authentication
   - User data handling

## 📞 Support

For issues or questions:
- Check logs in Firebase Console
- Review crash reports
- Monitor API server logs
- Check Google Play Console for user feedback

---

**Last Updated:** 2024
**App Version:** 1.0.0
**Minimum Android Version:** API 24 (Android 7.0)

