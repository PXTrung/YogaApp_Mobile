import { initializeApp } from "firebase/app";
import { getDatabase } from "firebase/database";

const firebaseConfig = {
  apiKey: "AIzaSyDxUID3zWxIQUJqdPx6-7CtT7q7OkcCYBw",
  databaseURL: "https://yogauniversity-e94b9-default-rtdb.firebaseio.com",
  projectId: "yogauniversity-e94b9",
  storageBucket: "yogauniversity-e94b9.firebasestorage.app",
  messagingSenderId: "545467880894",
  appId: "1:545467880894:android:f927a098f908a2042c55ef",
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Get a reference to the database
export const database = getDatabase(app);