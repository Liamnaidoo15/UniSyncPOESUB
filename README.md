# UniSync - University Management System

UniSync is a comprehensive mobile application designed to centralize key university operations for students, lecturers, and administrators. It provides streamlined access to timetables, announcements, assignments, attendance tracking, and campus networking — all in one unified platform.

## Features

### Core Features
- ✅ **Secure Authentication**
  - Firebase Authentication with SSO (Google Sign-In)
  - Biometric authentication (Fingerprint/Face ID)
  - Password-based authentication
  - Role-based access control (Student, Lecturer, Admin)

- ✅ **QR Code Technology**
  - Instant QR code scanning for attendance
  - Dynamic QR code generation for classes
  - Offline scanning with automatic sync when online

- ✅ **Real-time Analytics**
  - Live attendance tracking
  - Comprehensive reporting system
  - GPS location verification
  - Geo-fencing capabilities

- ✅ **Offline Support**
  - Works without internet connection
  - Automatic synchronization when online
  - Local data storage with RoomDB
  - Sync queue for pending operations

- ✅ **Modern UI/UX**
  - Material Design 3 principles
  - Responsive mobile interface
  - Dark/Light theme support
  - Smooth animations and transitions

- ✅ **Push Notifications**
  - Firebase Cloud Messaging (FCM)
  - Real-time notifications for announcements, assignments, and attendance

- ✅ **Multi-language Support**
  - English
  - isiZulu
  - Afrikaans

### Student Portal
- View personal timetables and upcoming classes
- Receive announcements from lecturers and faculty
- Track assignments with due dates and submission status
- Monitor attendance records and progress analytics
- Connect with peers and lecturers via discussion/networking hub

### Lecturer Portal
- Post and manage announcements for courses
- Create and update assignments with deadlines
- Record and monitor student attendance
- Communicate with students through the UniSync network hub
- View analytics and student participation summaries

## Technical Stack

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt
- **Database**: Room Database (SQLite)
- **Networking**: Retrofit + OkHttp
- **Authentication**: Firebase Auth + Google Sign-In
- **Push Notifications**: Firebase Cloud Messaging
- **Image Loading**: Glide
- **QR Code**: ZXing
- **Location Services**: Google Play Services Location
- **Biometric**: AndroidX Biometric Library

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/unisyncpoe/
│   │   │   ├── data/
│   │   │   │   ├── local/          # Room database, DAOs
│   │   │   │   ├── model/          # Data models
│   │   │   │   ├── remote/         # API service, Retrofit
│   │   │   │   └── repository/     # Repository layer
│   │   │   ├── di/                 # Dependency injection modules
│   │   │   ├── service/            # Firebase services
│   │   │   ├── ui/                 # UI components
│   │   │   │   ├── auth/           # Login, Registration
│   │   │   │   ├── dashboard/     # Main dashboard
│   │   │   │   ├── fragments/     # Tab fragments
│   │   │   │   └── settings/      # Settings screen
│   │   │   └── util/              # Utilities
│   │   └── res/
│   │       ├── values/            # English strings
│   │       ├── values-zu/         # isiZulu strings
│   │       └── values-af/         # Afrikaans strings
│   └── test/                      # Unit tests
```

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11 or later
- Android SDK (API 24+)
- Firebase project with Authentication and Cloud Messaging enabled

### Configuration

1. **Firebase Setup**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Authentication (Email/Password and Google Sign-In)
   - Enable Cloud Messaging
   - Download `google-services.json` and place it in `app/` directory

2. **API Configuration**
   - Update `API_BASE_URL` in `app/build.gradle.kts` with your backend API URL
   - Or set it via build config field

3. **Google Sign-In**
   - Update `default_web_client_id` in `strings.xml` with your Firebase Web Client ID

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

## API Endpoints

The app expects a REST API with the following endpoints:

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/me` - Get current user
- `GET /api/announcements` - Get announcements
- `POST /api/announcements` - Create announcement
- `GET /api/assignments` - Get assignments
- `POST /api/assignments` - Create assignment
- `GET /api/attendance` - Get attendance records
- `POST /api/attendance` - Mark attendance
- `POST /api/qr-codes/generate` - Generate QR code
- `POST /api/qr-codes/scan` - Scan QR code

## Testing

### Unit Tests
Run unit tests with:
```bash
./gradlew test
```

### Instrumented Tests
Run instrumented tests with:
```bash
./gradlew connectedAndroidTest
```

## Building for Release

1. Generate a signing key:
   ```bash
   keytool -genkey -v -keystore unisync-release.keystore -alias unisync -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Create `keystore.properties` in project root:
   ```
   storePassword=your_store_password
   keyPassword=your_key_password
   keyAlias=unisync
   storeFile=unisync-release.keystore
   ```

3. Update `app/build.gradle.kts` with signing config

4. Build release APK:
   ```bash
   ./gradlew assembleRelease
   ```

## Release Notes

### Version 1.0.0 (Initial Release)
- ✅ User registration and login with SSO
- ✅ Biometric authentication support
- ✅ Offline mode with RoomDB synchronization
- ✅ Push notifications via FCM
- ✅ Multi-language support (English, isiZulu, Afrikaans)
- ✅ QR code attendance scanning
- ✅ Timetables, Announcements, Assignments, Attendance tracking
- ✅ Network hub for campus communication
- ✅ Settings screen with theme and language options
- ✅ GPS location services integration

## Contributing

This is a POE (Portfolio of Evidence) project. For contributions or questions, please contact the development team.

## License

This project is developed for educational purposes as part of a Portfolio of Evidence submission.

## Acknowledgments

- Material Design Components
- Firebase
- AndroidX Libraries
- ZXing for QR code support

---

**Developed for POE Submission**
**UniSync - Connecting University Communities**

