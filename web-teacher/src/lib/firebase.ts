import { initializeApp, getApps, getApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';
import { getAuth } from 'firebase/auth';

const firebaseConfig = {
  apiKey: "AIzaSyBRWA47UN-IlO5MXRZVICy9_o3WVLaXK2U",
  authDomain: "quizmaster-dd2f8.firebaseapp.com",
  projectId: "quizmaster-dd2f8",
  storageBucket: "quizmaster-dd2f8.firebasestorage.app",
  messagingSenderId: "475100234962",
  appId: "1:475100234962:android:c824cf3118bf0245e1990b"
};

const app = !getApps().length ? initializeApp(firebaseConfig) : getApp();
export const db = getFirestore(app);
export const auth = getAuth(app);
export default app;

