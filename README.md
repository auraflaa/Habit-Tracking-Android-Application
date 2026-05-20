# HabitFlow

HabitFlow is an Android-based habit tracking application engineered to facilitate consistent routine management and goal tracking. The application provides intuitive activity monitoring, analytical progress visualization, and robust cloud synchronization to ensure data availability across devices.

## Core Capabilities

- **Authentication System**: Secure identity management integrating Google Sign-In and standard Email/Password authentication protocols.
- **Real-Time Synchronization**: Bidirectional data synchronization utilizing Firebase Realtime Database for seamless cross-device state management.
- **Offline-First Architecture**: Continuous operational capability in offline environments via local SQLite persistence, with automated background synchronization upon network restoration.
- **Data Isolation**: Strict multi-tenant data architecture ensuring complete privacy and isolation of individual user data.
- **Analytical Insights**: Comprehensive progress tracking through visual heatmaps and streak analytics for performance assessment.
- **Customizable Interface**: Dynamic user interface supporting multiple display themes (Dark, Light, Ocean, and AMOLED).

## Technical Architecture

- **Languages**: Java, Kotlin
- **Local Storage**: SQLite
- **Backend Infrastructure**: Firebase Authentication, Firebase Realtime Database
- **Architectural Pattern**: Singleton Pattern for centralized data and state management
- **UI Frameworks**: Material Design 3, ViewPager2, RecyclerView

## Development Setup

1. **Repository Configuration**: Clone the project repository to your local development environment.
2. **Firebase Integration**:
   - Establish a new project within the [Firebase Console](https://console.firebase.google.com/).
   - Register an Android application using the package identifier `com.habitflow`.
   - Download the `google-services.json` configuration file and position it within the `app/` directory.
   - Provision **Authentication** services (Email/Password & Google providers) and instantiate the **Realtime Database**.
   - Register your development **SHA-1 certificate fingerprint** in the Firebase project settings.
   - Apply the following security rules to the Realtime Database to enforce data isolation:
     ```json
     {
       "rules": {
         "users": {
           "$userId": {
             ".read": "auth != null && auth.uid === $userId",
             ".write": "auth != null && auth.uid === $userId"
           }
         }
       }
     }
     ```
3. **Compilation**: Open the project utilizing Android Studio, synchronize Gradle dependencies, and deploy to a target device or emulator.

## Security Considerations

Sensitive configuration artifacts, such as `google-services.json`, and generated build outputs are explicitly excluded from source control mechanisms via `.gitignore` to prevent credential exposure.
