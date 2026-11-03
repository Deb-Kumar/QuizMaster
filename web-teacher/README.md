# QuizMaster Web Portal (Teacher & Certificate Engine)

A high-performance **React + Vite + TypeScript + Tailwind CSS** web application designed for QuizMaster educators and public credential verification.

## 🚀 Features

- 📜 **Open Certificate Verifier**: Zero-login, zero-registration verification portal. Enter any certificate ID (or query parameter `?certId=...`) to view authenticated credentials, student information, grades, and cryptographic QR code with celebratory confetti animations.
- 🎨 **Android Dev / Material 3 Vibes**: Glassmorphic dark theme, elevation layers, smooth shimmer skeleton loaders, and responsive components.
- ✍️ **Teacher Quiz Studio**: Author comprehensive single and multiple-choice quizzes, configure timers, pass marks, and publish directly to Cloud Firestore.
- 👥 **Batch & Student Manager**: Create classrooms, generate 6-character access codes (`KT3921`), and manage student enrollments.

## 💻 Local Development

```bash
cd web-teacher
npm install
npm run dev
```

## 🌐 Build & Production

```bash
npm run build
```

The production output will be generated in `dist/`. Ready for deployment on **Vercel** or **Firebase Hosting**.
