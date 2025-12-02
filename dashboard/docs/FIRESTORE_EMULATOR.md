Firestore emulator setup

1. Install the Firebase CLI and emulator:
   - https://firebase.google.com/docs/emulator-suite

2. Start the emulator for Firestore:
   - `firebase emulators:start --only firestore`

3. Export env vars before running the Spring Boot app (PowerShell):
   ```powershell
   $env:FIRESTORE_EMULATOR_HOST = 'localhost:8080' # example port from emulator output
   $env:FIREBASE_PROJECT_ID = 'your-project-id'
   mvn spring-boot:run
   ```
   The app detects `FIRESTORE_EMULATOR_HOST` and will use the emulator without requiring service account JSON.

Notes:
- When using the emulator, do not provide the service account file; the code will skip Firebase Admin initialization and use the emulator client.
- For CI, set `FIRESTORE_EMULATOR_HOST` and `FIREBASE_PROJECT_ID` in your pipeline environment.

