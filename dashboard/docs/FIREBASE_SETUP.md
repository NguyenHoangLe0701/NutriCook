# Firebase Setup Guide für NutriCook Dashboard

## Current Status
- ✅ Application running on `http://localhost:8080`
- ✅ H2 in-memory database configured for development
- ✅ Firebase support added to project dependencies
- ❌ Firebase initialization currently **disabled** (needs valid credentials)

## Steps to Connect Your Firebase Database

### 1. Get Your Firebase Service Account Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project: **nutricook-fff8f**
3. Navigate to **Service Accounts** (IAM & Admin > Service Accounts)
4. Click the email address of the default service account
5. Go to the **Keys** tab
6. Click **Add Key** > **Create new key**
7. Choose **JSON** format and download the file
8. The file will be named something like `nutricook-fff8f-xxxxx.json`

### 2. Replace the Service Account Key File

1. Rename your downloaded JSON file to `serviceAccountKey.json`
2. Replace the file at: `src/main/resources/serviceAccountKey.json`
   - This file currently contains incomplete configuration

### 3. Enable Firebase in Application

Update `src/main/resources/application.properties`:

```properties
# Change this line from:
firebase.enabled=false

# To:
firebase.enabled=true
```

### 4. Disable H2 Database (Optional for Production)

If you want to use Firebase as your primary database instead of H2:

In `src/main/resources/application.properties`, comment out the H2 configuration:

```properties
# spring.datasource.url=jdbc:h2:mem:nutricookdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
# spring.datasource.driver-class-name=org.h2.Driver
# ... (comment out other H2 properties)

# firebase.enabled=true
```

### 5. Restart the Application

```bash
mvn clean spring-boot:run
```

## Firestore Structure

This project uses **Cloud Firestore** (document/collection model), not the Realtime Database.

Your Firestore data is stored in the project: `nutricook-fff8f`. Firestore does not use a single URL
the way Realtime Database does — it uses a project ID and collections. Example collections the app expects:

- `categories` — documents representing food categories
- `foodItems` — documents representing individual food items
- `foodUpdates` — documents for updates/announcements
- `users` — user documents and profiles

Each collection contains documents; each document is a map of fields. Example document path:
```
categories/{categoryId}
foodItems/{foodItemId}
```

### Example: reading/writing from Firestore in Spring
If you inject the `Firestore` bean (provided by `FirebaseConfig`), you can use it like this in a
Spring service:

```java
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.ApiFuture;
import java.util.Map;

@Service
public class FoodService {
   private final Firestore firestore;

   public FoodService(Firestore firestore) {
      this.firestore = firestore;
   }

   public void addFoodItem(String id, Map<String, Object> data) throws Exception {
      DocumentReference docRef = firestore.collection("foodItems").document(id);
      ApiFuture<WriteResult> result = docRef.set(data);
      result.get(); // wait for completion
   }
}
```

Ensure the service account JSON you place in `src/main/resources/serviceAccountKey.json` has
permissions for Firestore (typically the "Cloud Datastore User" or appropriate Firestore roles).

## Development Notes

### Using H2 Console (Development Only)
While running the app, access the H2 console at:
```
http://localhost:8080/h2-console
```

Database URL in H2 Console: `jdbc:h2:mem:nutricookdb`
Username: `sa`
Password: (empty)

### Environment Configuration

#### Development (`application.properties`)
- Uses H2 in-memory database
- Firebase: Can be enabled/disabled
- Recommended for local development

#### Production (`application-prod.properties`)
- Switch to MySQL or cloud database
- Enable Firebase for backend services
- Disable H2 console
- Update CSP headers

## Test Setup

Tests automatically use H2 database (configured in `src/test/resources/application.properties`) with Firebase disabled to avoid credential issues during testing.

## Troubleshooting

### Issue: "Error reading credentials from stream, 'type' field not specified"
**Solution:** Make sure you replaced `serviceAccountKey.json` with the actual Firebase service account key from Google Cloud Console.

### Issue: "Failed to initialize Firebase"
**Solution:** 
1. Verify the `serviceAccountKey.json` file is valid JSON
2. Check that your Firebase project ID matches in the file
3. Ensure credentials file is in `src/main/resources/`

### Issue: Database connection errors
**Solution:** If using MySQL instead of H2, update these properties:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/nutricook_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

## Resources

- [Firebase Admin SDK for Java](https://firebase.google.com/docs/database/admin/start)
- [Google Cloud Console](https://console.cloud.google.com/)
- [Spring Boot Firebase Integration](https://spring.io/guides/gs/spring-boot/)
- [H2 Database Console](http://www.h2database.com/html/main.html)

