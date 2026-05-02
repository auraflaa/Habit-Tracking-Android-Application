# HabitFlow

Build your best self, one day at a time.

HabitFlow is a modern Android habit tracker designed for consistency and ease of use. It helps you stay on track with your goals through intuitive daily tracking, progress visualization, and seamless cloud synchronization.

## ✨ Features

- **Multi-Account Support**: Secure login with Google Sign-In or Email/Password.
- **Cloud Sync**: Real-time synchronization of habits and progress via **Firebase Realtime Database**.
- **Data Privacy**: Strict data isolation ensuring each user only sees their own habits.
- **Offline First**: Track habits even without internet; changes sync automatically when back online via local persistence.
- **Personalized Experience**: Dynamic greetings and motivational quotes.
- **Progress Insights**: Heatmaps and streak tracking to keep you motivated.
- **Customizable Themes**: Multiple color themes including Dark, Light, Ocean, and AMOLED.

## 🛠️ Tech Stack

- **Language**: Java & Kotlin
- **Local Database**: SQLite
- **Backend**: Firebase Authentication & Realtime Database
- **Architecture**: Singleton Pattern for Data Management
- **UI Components**: Material Design 3, ViewPager2, RecyclerView

## 🚀 Setup Instructions

1. **Clone the project** to your local machine.
2. **Firebase Setup**:
   - Create a new project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.habitflow`.
   - Download the `google-services.json` and place it in the `app/` directory.
   - Enable **Authentication** (Email/Password & Google) and **Realtime Database**.
   - Add your **SHA-1 fingerprint** to the Firebase project settings.
   - Set the following rules in your Realtime Database:
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
3. **Build and Run**: Open in Android Studio and deploy to your device.

## 🛡️ Security

Sensitive information like `google-services.json` and build artifacts are excluded from version control via `.gitignore`.
