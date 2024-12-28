# FamilySafe

A safety alert application for families and groups.

## Setup

1. Clone the repository
2. Copy `app/google-services.json.template` to `app/google-services.json`
3. Replace the placeholder values in `google-services.json` with your actual Firebase configuration
4. Build and run the application

## Security Note

The `google-services.json` file contains sensitive API keys and configuration. It is excluded from version control for security reasons. Make sure to:
- Never commit the actual `google-services.json` file
- Keep your API keys and credentials secure
- Use the template file as a reference for required configuration

## Features

- Real-time safety alerts
- Group management
- Location sharing with group members
- Chat functionality
- Interactive map with alert zones

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
