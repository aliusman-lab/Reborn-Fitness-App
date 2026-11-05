REBORN 🏋️‍♂️🤖 – AI-Powered Fitness & Food Scanner App

REBORN is a modern, privacy-focused native Android application designed for personalized health tracking, dynamic caloric management, and real-time food classification using On-Device TensorFlow Lite (TFLite) Machine Learning.

🌟 Key Features

📱 User Onboarding & Local Persistence: Save physical metrics (Name, Age, Height, Weight, Fitness Goals) locally using SharedPreferences.

📊 Dynamic Health Engine: Automatically computes Body Mass Index (BMI), Basal Metabolic Rate (BMR via Mifflin-St Jeor), Daily Caloric Targets, and Macro Splits (Protein, Carbs, Fats).

📷 On-Device TFLite Food Scanner: Scans food images via camera or gallery to predict items offline using MobileNetV2 without cloud APIs.

🤖 Smart AI Assistant Chat: Interactive fitness assistant providing real-time workout and nutrition guidance.

⚙️ Profile Settings: View, update, or reset physical metrics and goals dynamically.

🛠️ Tech Stack & Architecture

Language: Java (Android Native)

IDE: Android Studio

Machine Learning Engine: TensorFlow Lite (org.tensorflow:tensorflow-lite:2.14.0)

Local Storage: SharedPreferences API

UI Style: Modern Dark Glassmorphism (#07101f background, #ffcc00 accent gold)

Minimum SDK: API 24 (Android 7.0 Nougat)

📁 Project Architecture & Assets

REBORN/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── assets/
│   │       │   ├── mobilenet_v2.tflite   # Pre-trained ML model
│   │       │   └── labels.txt           # Image class labels
│   │       ├── java/com/reborn/fitness/
│   │       │   ├── SplashActivity.java
│   │       │   ├── OnboardingActivity.java
│   │       │   ├── MainActivity.java
│   │       │   ├── DashboardFragment.java
│   │       │   ├── FoodScannerFragment.java
│   │       │   ├── AiAssistantFragment.java
│   │       │   └── ProfileFragment.java
│   │       └── res/layout/              # Glassmorphism XML layouts
└── build.gradle                         # Gradle dependencies setup


🚀 Getting Started & Setup

Clone the Repository:

git clone https://github.com/your-username/REBORN-Fitness-App-TFLite.git


Open in Android Studio:

Launch Android Studio and click Open.

Navigate to the cloned REBORN directory.

Sync Gradle:

Let Gradle sync dependencies automatically (ensure internet connection for first sync).

Verify Assets:

Ensure mobilenet_v2.tflite and labels.txt exist under app/src/main/assets/.

Run the App:

Select an Emulator or Physical Android Device (API 24+) and click Run (Shift + F10).

🔒 Privacy & Offline ML Inference

Unlike traditional apps that upload user camera shots to remote server APIs, REBORN executes neural network inference entirely on-device. This guarantees:

Zero Internet Requirement for food scanning.

Ultra-Low Latency (~40ms inference time).

Complete User Data Privacy.

👨‍💻 Developer Information

Developer: Ali Usman

Degree Program: BS Computer Science (BSCS)

Project Type: Mobile Machine Learning Assignment (Assignment 2)