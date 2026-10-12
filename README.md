# QuizMaster (OnlineQuizApp) 🎓

An advanced, full-featured modern Android Quiz & Assessment Application built with **Jetpack Compose**, **Kotlin**, and **Firebase**.

Designed for both **Students** and **Teachers**, featuring interactive MCQ quizzes, real-time leaderboards, batch management, analytics, and official PDF certificate generation.

---

## ✨ Features

### 👨‍🎓 For Students
- **Explore & Take Quizzes**: Filter by topic, difficulty, or assigned batches. Supports both timed (countdown) and untimed modes.
- **In-Progress Resumption**: Auto-saves active quiz drafts locally so students can resume interrupted quizzes without losing progress.
- **Instant Result & Comprehensive Review**: View circular score breakdown, question status indicators (Correct, Incorrect, Skipped/Left), and detailed explanations.
- **Certificate of Achievement (PDF)**: Automatically awards official downloadable PDF certificates for scores $\ge 80\%$, complete with verification IDs and custom branding.
- **Personal Quiz History & Performance Analytics**: View past attempts, average score percentage, completion rate, and pass/fail metrics.
- **Leaderboard Podium**: View top-performing peers across classes, batches, and individual quizzes.

### 👩‍🏫 For Teachers
- **Quiz Creator & Editor**: Multi-step quiz builder with customizable questions, MCQ options, correct answer tagging, time limits, and custom icons.
- **Batch & Class Management**: Group students into batches, review student join requests, and assign quizzes to specific batches or publicly.
- **Class Leaderboards & Analytics**: View student completion rates, average scores, and individual quiz performance rankings.
- **Notifications**: Dynamic alerts when students submit quizzes or request batch enrollment.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 design and dark/light theme support
- **Architecture**: MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF)
- **Language**: Kotlin 2.x
- **Asynchronous & Reactive**: Kotlin Coroutines & StateFlow
- **Backend / Cloud**: Firebase Firestore & Firebase Authentication
- **Local Persistence**: SharedPreferences offline caching & session draft storage
- **PDF Generation**: Native Android `PdfDocument` rendering for printable certificates
- **Build System**: Gradle with Kotlin DSL / Groovy

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK with API 34+

### Setup
1. **Clone the repository:**
   ```bash
   git clone <YOUR_REPOSITORY_URL>
   cd OnlineQuizApp
   ```

2. **Firebase Setup:**
   - Create a project on the [Firebase Console](https://console.firebase.google.com/).
   - Enable **Firebase Authentication** (Email/Password & Google Sign-In) and **Cloud Firestore**.
   - Download `google-services.json` and place it in the `app/` directory.

3. **Build and Run:**
   - Open the project in Android Studio.
   - Sync Gradle files.
   - Run on an Android emulator or physical device.

---

## 📄 License
This project is licensed under the MIT License.
