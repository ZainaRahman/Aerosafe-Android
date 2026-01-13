# AeroSafe - Air Quality Monitoring Android App


<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform">
  <img src="https://img.shields.io/badge/Language-Java-orange.svg" alt="Language">
  <img src="https://img.shields.io/badge/Backend-Firebase-yellow.svg" alt="Backend">
  <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License">
</p>

## 📱 Overview

**AeroSafe** is a comprehensive Android application designed for real-time air quality monitoring, prediction, and management. The app provides three distinct user roles with specialized features for tracking, analyzing, and reporting air quality data.

Based on the desktop application: https://github.com/ZainaRahman/AeroSafe

## ✨ Features

### 👤 For Users
- 🌡️ **Real-time AQI Monitoring** - Get current air quality index for your location
- 🗺️ **Interactive Maps** - View AQI data on Google Maps with location markers
- 📍 **Location-based Data** - Automatic location detection and air quality fetching
- 📝 **Issue Reporting** - Report air quality issues with photos and descriptions
- 🔔 **Air Quality Alerts** - Receive notifications about poor air quality
- 📊 **AQI History** - View historical air quality trends

### 🔬 For Researchers
- 📈 **Data Analytics** - Access comprehensive air quality datasets
- 📥 **Data Export** - Export data in CSV format for analysis
- 📊 **Visualization Tools** - Charts and graphs using MPAndroidChart
- 🔍 **Research Data Collection** - Collect and manage research data
- 📑 **Historical Analysis** - Analyze air quality trends over time
- 🌍 **Location-based Research** - Filter data by geographic location

### 👨‍💼 For Administrators
- 👥 **User Management** - Manage user accounts and roles
- 📋 **Report Management** - Review and respond to user reports
- 🚨 **Alert System** - Create and broadcast air quality alerts
- 📊 **System Monitoring** - Monitor app usage and data
- ✅ **Issue Resolution** - Track and resolve reported issues
- 📈 **Analytics Dashboard** - Overview of system statistics

## 🛠️ Technologies Used

### Frontend
- **Platform:** Android (API 24+, Android 7.0+)
- **Language:** Java
- **UI Framework:** Material Design Components
- **Architecture:** Repository Pattern with MVVM principles
- **View Binding:** Enabled for type-safe view access

### Backend & APIs
- **Database:** Firebase Realtime Database
- **Authentication:** Firebase Authentication (Email/Password)
- **Storage:** Firebase Storage (for images)
- **Air Quality Data:** OpenWeatherMap Air Pollution API
- **Maps:** Google Maps Android API
- **Geocoding:** Google Geocoding API
- **Charts:** MPAndroidChart library

### Networking
- **HTTP Client:** Retrofit 2
- **JSON Parsing:** Gson
- **Image Loading:** Glide

## 📋 Prerequisites

Before you begin, ensure you have:

- ✅ Android Studio Arctic Fox (2020.3.1) or later
- ✅ JDK 11 or higher
- ✅ Android SDK (API 24+)
- ✅ Firebase account (free)
- ✅ Google Cloud Platform account (free tier)
- ✅ OpenWeatherMap account (free tier)

## 🚀 Getting Started

### Step 1: Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/AeroSafe-Android.git
cd AeroSafe-Android
```

### Step 2: Open in Android Studio

1. Open Android Studio
2. Select **File → Open**
3. Navigate to the cloned repository folder
4. Click **OK**
5. Wait for Gradle sync to complete

### Step 3: Setup Firebase

1. **Create Firebase Project:**
   - Go to https://console.firebase.google.com/
   - Click "Add project"
   - Name: `aerosafe` (or your choice)
   - Follow setup wizard

2. **Enable Authentication:**
   - In Firebase Console: **Build → Authentication**
   - Click "Get started"
   - Enable "Email/Password" sign-in method

3. **Enable Realtime Database:**
   - In Firebase Console: **Build → Realtime Database**
   - Click "Create Database"
   - Start in **test mode** (for development)
   - Set security rules:
   ```json
   {
     "rules": {
       "users": {
         "$uid": {
           ".read": "auth != null",
           ".write": "$uid === auth.uid || root.child('users').child(auth.uid).child('role').val() === 'admin'"
         }
       },
       "reports": {
         ".read": "auth != null",
         "$reportId": {
           ".write": "auth != null"
         }
       },
       "issues": {
         ".read": "auth != null",
         "$issueId": {
           ".write": "auth != null"
         }
       }
     }
   }
   ```

4. **Download google-services.json:**
   - In Firebase Console: Project Settings
   - Download `google-services.json`
   - Place in `app/` folder

### Step 4: Setup API Keys

#### OpenWeatherMap API (Air Quality Data)

1. Sign up at: https://openweathermap.org/api
2. Get your API key from dashboard
3. Wait 15 minutes for activation
4. Open `app/src/main/java/com/example/aerotutorial/repository/AQIRepository.java`
5. Replace line 27:
   ```java
   private static final String API_KEY = "YOUR_OPENWEATHER_API_KEY";
   ```

#### Google Maps API

1. Go to: https://console.cloud.google.com/
2. Enable **Maps SDK for Android**
3. Enable **Geocoding API**
4. Create API Key in Credentials
5. Restrict to Android app (package: `com.example.aerotutorial`)
6. Get SHA-1 fingerprint:
   ```bash
   cd android
   ./gradlew signingReport
   ```
7. Add SHA-1 to API key restrictions
8. Open `app/src/main/AndroidManifest.xml`
9. Replace line 72:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_GOOGLE_MAPS_API_KEY" />
   ```

### Step 5: Build and Run

1. Connect Android device or start emulator
2. Click **Run** (▶️) in Android Studio
3. Select your device
4. App will build and install

## 📊 Project Structure

```
AeroSafe-Android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/aerotutorial/
│   │   │   │   ├── LoginActivity.java
│   │   │   │   ├── SignupActivity.java
│   │   │   │   ├── UserDashboardActivity.java
│   │   │   │   ├── ResearcherDashboardActivity.java
│   │   │   │   ├── AdminDashboardActivity.java
│   │   │   │   ├── AeroTutorialApp.java
│   │   │   │   │
│   │   │   │   ├── fragments/
│   │   │   │   │   ├── ResearcherDataViewFragment.java
│   │   │   │   │   ├── ResearcherHubFragment.java
│   │   │   │   │   ├── AdminOverviewFragment.java
│   │   │   │   │   ├── AdminUsersFragment.java
│   │   │   │   │   ├── AdminReportsFragment.java
│   │   │   │   │   └── AdminAlertsFragment.java
│   │   │   │   │
│   │   │   │   ├── adapters/
│   │   │   │   │   ├── ResearchDataAdapter.java
│   │   │   │   │   ├── ReportsAdapter.java
│   │   │   │   │   ├── AlertsAdapter.java
│   │   │   │   │   └── UsersAdapter.java
│   │   │   │   │
│   │   │   │   ├── models/
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Issue.java
│   │   │   │   │   ├── Report.java
│   │   │   │   │   ├── Alert.java
│   │   │   │   │   ├── Prediction.java
│   │   │   │   │   ├── AirQualityData.java
│   │   │   │   │   └── AirPollutionResponse.java
│   │   │   │   │
│   │   │   │   ├── repository/
│   │   │   │   │   ├── AuthRepository.java
│   │   │   │   │   ├── AQIRepository.java
│   │   │   │   │   ├── IssueRepository.java
│   │   │   │   │   ├── ReportRepository.java
│   │   │   │   │   ├── PredictionRepository.java
│   │   │   │   │   └── AlertRepository.java
│   │   │   │   │
│   │   │   │   ├── utils/
│   │   │   │   │   ├── PreferencesManager.java
│   │   │   │   │   ├── AQICalculator.java
│   │   │   │   │   ├── DateUtils.java
│   │   │   │   │   └── PermissionUtils.java
│   │   │   │   │
│   │   │   │   └── api/
│   │   │   │       └── RetrofitClient.java
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── drawable/
│   │   │   │   ├── values/
│   │   │   │   └── xml/
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── test/ & androidTest/
│   │
│   ├── build.gradle.kts
│   └── google-services.json
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── .gitignore
└── README.md
```

## 🔐 Security & Best Practices

### API Keys Protection

For production or public repositories:

1. **Never commit API keys** to version control
2. **Use BuildConfig** for sensitive data
3. **Use environment variables** or `local.properties`
4. **Restrict API keys** in Google Cloud Console
5. **Add `google-services.json`** to `.gitignore` for public repos

### Firebase Security Rules

Update rules for production:

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

## 🧪 Testing

### Run Unit Tests

```bash
./gradlew test
```

### Run Instrumentation Tests

```bash
./gradlew connectedAndroidTest
```

## 📱 User Roles & Credentials

### Default Admin Account
- Email: `admin@aerosafe.com`
- Password: `admin123456`
- Role: Administrator

### Test Accounts
- **User:** `user@test.com` / `test123456`
- **Researcher:** `researcher@test.com` / `test123456`

## 🐛 Troubleshooting

### Common Issues

**Issue:** "Signup failed - internal error"
- **Solution:** Enable Firebase Authentication and Realtime Database
- **Guide:** See `FIREBASE_NOT_ENABLED_FIX.md`

**Issue:** Google Maps shows gray screen
- **Solution:** Check API key and SHA-1 fingerprint
- **Guide:** See `GOOGLE_MAPS_NO_CREDIT_CARD.md`

**Issue:** "Cannot resolve symbol"
- **Solution:** Sync Gradle and rebuild project
- **Command:** `./gradlew clean build`

**Issue:** AQI data not loading
- **Solution:** Check OpenWeatherMap API key is active (takes 15 min)
- **Test:** https://api.openweathermap.org/data/2.5/air_pollution?lat=23.81&lon=90.41&appid=YOUR_KEY

## 📚 Documentation

Complete setup guides available:
- `API_SETUP_COMPLETE_GUIDE.md` - All API setup instructions
- `FIREBASE_NOT_ENABLED_FIX.md` - Firebase setup and troubleshooting
- `GOOGLE_MAPS_NO_CREDIT_CARD.md` - Google Maps setup without billing
- `HOW_TO_GET_SHA1_FINGERPRINT.md` - SHA-1 fingerprint guide
- `HOW_TO_CREATE_GITHUB_REPO.md` - GitHub repository creation

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Zaina Rahman** - *Initial work* - [ZainaRahman](https://github.com/ZainaRahman)

## 🙏 Acknowledgments

- [OpenWeatherMap](https://openweathermap.org/) - Air quality data API
- [Firebase](https://firebase.google.com/) - Backend services
- [Google Maps Platform](https://developers.google.com/maps) - Location services
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) - Chart library
- Original desktop application: [AeroSafe](https://github.com/ZainaRahman/AeroSafe)

## 📞 Support

For issues and questions:
- Open an [Issue](https://github.com/YOUR_USERNAME/AeroSafe-Android/issues)
- Check [Documentation](./docs/)
- Contact: your.email@example.com

## 🚀 Roadmap

- [ ] Add ML-based AQI prediction model
- [ ] Implement push notifications
- [ ] Add data visualization dashboard
- [ ] Support for multiple languages
- [ ] Offline mode support
- [ ] Widget for home screen
- [ ] Dark mode theme

## 💰 Cost

All services used are **FREE** for development:
- Firebase: Free tier (1GB storage, 10GB/month transfer)
- OpenWeatherMap: Free tier (60 calls/min, 1M/month)
- Google Maps: Free tier ($200 credit/month, ~28K loads)

---

<p align="center">
  Made with ❤️ for cleaner air
</p>

