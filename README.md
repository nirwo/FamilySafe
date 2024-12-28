# Safety Alert App

A safety alert application that helps users stay connected with their family and friends during emergencies. The app integrates with the Red Alert API to provide real-time alerts and allows users to mark themselves as safe or unsafe, notifying their group members.

## Features

- Google Sign-In Authentication
- Create and manage groups (family/friends)
- Real-time alerts based on location
- Mark safety status (safe/unsafe)
- Push notifications for group members
- Modern and intuitive UI

## Setup Instructions

1. Clone the repository
2. Create a Firebase project and add your `google-services.json` file to the app directory
3. Enable Google Sign-In in Firebase Console and add your SHA-1 fingerprint
4. Update the web client ID in `SignInActivity.kt`
5. Enable Firebase Cloud Messaging in Firebase Console
6. Build and run the project

## Requirements

- Android Studio Arctic Fox or newer
- Android SDK 33
- Kotlin 1.8.0
- Google Play Services
- Firebase Account

## Dependencies

All dependencies are managed through Gradle and are listed in the app-level `build.gradle` file.

## Configuration

1. Firebase Configuration:
   - Add your `google-services.json` file to the app directory
   - Update the web client ID in `SignInActivity.kt`

2. Red Alert API:
   - The app uses the public Red Alert API (https://github.com/Zontex/python-red-alert)
   - No additional configuration needed

## Testing

Run the included test suite:
```bash
./gradlew test
```

## License

MIT License
