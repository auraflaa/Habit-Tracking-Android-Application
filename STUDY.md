# HabitFlow: Product and Project Metadata

## Problem Statement
Maintaining consistent personal habits and routines can be challenging in a fast-paced world. Individuals often lose motivation without clear visibility into their progress and struggle to track habits across multiple devices seamlessly. HabitFlow addresses this by providing an intuitive, offline-first habit tracking application that ensures individuals can monitor daily tasks, visualize their streaks via heatmaps, and synchronize their progress across devices in real-time, thereby fostering accountability and consistency.

## 4. Tools and Technologies

### 4.1 Programming Language
- **Java**: Primary programming language utilized for application architecture and business logic.
- **Kotlin**: Utilized in Gradle configuration and secondary module logic.
- **XML**: Used extensively for designing responsive and dynamic Android UI layouts and vector drawables.

### 4.2 IDE
- **Android Studio**: The primary Integrated Development Environment (IDE) used to write, compile, profile, and debug the application.

### 4.3 Database
- **SQLite (Local)**: Used via `SQLiteOpenHelper` to persist habit data locally, ensuring complete offline functionality.
- **Firebase Realtime Database (Cloud)**: Used as the primary backend infrastructure to synchronize user habits bidirectionally across devices in real-time.

### 4.4 APIs and Technologies Used
- **Firebase Authentication**: For managing secure user identity and session states via Email/Password and Google OAuth.
- **ZenQuotes API**: An external REST API used to fetch daily motivational quotes dynamically.
- **Android AlarmManager & BroadcastReceiver**: Native Android framework tools used for the precision scheduling of local device notifications.

## 5. Implementation

### 5.1 Project Modules
The project architecture is organized by feature and functionality into the following distinct packages:
- `activities`: Core UI entry points (e.g., `MainActivity`, `SplashActivity`).
- `adapters`: Components responsible for binding data arrays to UI lists (e.g., `HabitAdapter`, `MainPagerAdapter`).
- `data`: Database architecture and cloud synchronization management (`HabitStore`, `HabitDao`, `FirebaseSyncManager`).
- `fragments`: Modular user interface screens (`HomeFragment`, `CalendarFragment`, `SettingsFragment`).
- `model`: Plain Old Java Objects (POJOs) representing business entities (`Habit`, `ChecklistItem`).
- `util`: Global utilities and background services (`ReminderReceiver`, `ThemeManager`).

### 5.2 Application Features
- Multi-user secure authentication and data isolation.
- Habit and subtask creation with customizable tracking frequencies, emojis, and priority levels.
- Granular analytical tracking through heatmap progress visualization and active streak calculation.
- Daily automated system-level reminder notifications.
- Extensive theming support (Light, Dark, Ocean, AMOLED) driven by dynamic attributes.

### 5.3 UI Screens and Navigation
- **Bottom Navigation Bar**: Anchors the user experience, allowing quick swapping of modular fragments.
- **Home Screen (`HomeFragment`)**: A daily interactive checklist displaying habits for the current day. Features swipe-to-delete with Undo capabilities.
- **Calendar Screen (`CalendarFragment`)**: A chronological overview showing monthly completions via an interactive grid system.
- **Progress Screen (`ProgressFragment`)**: Statistical data visualization representing overall app usage and specific habit adherence metrics.
- **Settings Screen (`SettingsFragment`)**: Configuration options for themes, profile management, and notification schedules.

### 5.4 API & Backend Integration
- **REST Integration**: The application executes an HTTP GET request to `https://zenquotes.io/api/random` utilizing `HttpURLConnection` within a background `ExecutorService`. The resulting JSON payload is parsed to display daily inspiration without blocking the main UI thread.
- **Firebase Integration**: The application implements real-time listeners (`ValueEventListener`) via the Firebase SDK to monitor changes in the cloud and reflect them immediately in the local SQLite cache.

## 6. Testing

### 6.1 Test Cases
- **Authentication Resilience**: Validating successful login, secure handling of invalid credentials, and account recovery paths.
- **Offline Mode Integrity**: Disabling internet connectivity, modifying habit states, and validating that changes sync automatically and accurately upon network restoration without data collision.
- **Theme Persistence**: Dynamically changing display themes and ensuring correct color attributes load consistently on application restart.
- **Notification Precision**: Scheduling custom reminder intervals and validating that the `BroadcastReceiver` intercepts the alarm precisely on time, regardless of whether the app is active or killed.

### 6.2 Results and Debugging
- Extensively utilized Android Studio's **Logcat** to trace Firebase connection states, monitor HTTP API responses, and diagnose synchronization latency.
- Debugged complex UI layout alignments using the **Layout Inspector**, particularly refining the Calendar grid weightings to ensure an even geometric distribution of days across devices with varying screen densities.

---

# Android Concepts Study Guide: HabitFlow

This section explores core Android development concepts, ranging from basic UI components to advanced backend synchronization. Each concept includes a description, a reference to its implementation in this project, and an explanation of how the code works.

## 1. Basic: Efficient List Rendering with RecyclerView

**Description:**
Android uses `RecyclerView` to display long lists of data efficiently. Instead of creating a new view for every item (which would consume too much memory and cause lag), it "recycles" views that scroll off the screen to display new incoming data.

**Code Reference:** `app/src/main/java/com/habitflow/adapters/HabitAdapter.java`

```java
public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitVH> {
    // ...
    @NonNull
    @Override
    public HabitVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflates the XML layout for a single item (item_habit.xml)
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new HabitVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitVH holder, int position) {
        // Binds the data to the view holder
        Habit h = habits.get(position);
        holder.tvName.setText(h.name);
        holder.tvEmoji.setText(h.emoji);
        // ... (updates UI state for completion, streaks, etc.)
    }
}
```

**Explanation:**
- `onCreateViewHolder`: Called only when the RecyclerView needs a new view to represent an item. It inflates `item_habit.xml`.
- `onBindViewHolder`: Called much more frequently. As the user scrolls, views that disappear are passed back into this method with a new `position` so they can be updated with new data (e.g., a different habit's name and completion status).
- `ViewHolder (HabitVH)`: Caches the `findViewById` lookups to improve scrolling performance.

## 2. Intermediate: Local Persistence with SQLite

**Description:**
To allow an app to work offline, data must be saved to the device's local storage. Android provides built-in SQLite database support. Apps use `SQLiteOpenHelper` to manage database creation and version management.

**Code Reference:** `app/src/main/java/com/habitflow/data/db/HabitDbHelper.java` and `HabitDao.java`

```java
public class HabitDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "habitflow.db";
    private static final int DATABASE_VERSION = 1;

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Defines the schema and creates the table
        String createTable = "CREATE TABLE " + TABLE_HABITS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_COMPLETED_TODAY + " INTEGER)";
        db.execSQL(createTable);
    }
}
```

**Explanation:**
- `SQLiteOpenHelper`: A helper class that handles the lifecycle of the database. 
- `onCreate`: Runs only once when the database is first created. Here, we execute raw SQL to create our tables (like `habits` and `checklists`).
- `onUpgrade`: (Not shown, but present) Handles schema migrations if `DATABASE_VERSION` is incremented in future app updates.
- Data Access Object (DAO): In `HabitDao.java`, we use methods like `db.query()` and `db.insert()` to safely interact with this SQLite database without writing manual SQL for every operation.

## 3. Intermediate: Background Processing (BroadcastReceivers)

**Description:**
Sometimes the app needs to execute code even when it is closed, such as triggering a daily reminder. `BroadcastReceiver` is an Android component that responds to system-wide broadcast announcements.

**Code Reference:** `app/src/main/java/com/habitflow/util/ReminderReceiver.java`

```java
public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Called when the AlarmManager triggers the intent
        String habitName = intent.getStringExtra("habit_name");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "habit_reminders")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Habit Reminder")
                .setContentText("Don't forget to: " + habitName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(notificationId, builder.build());
    }
}
```

**Explanation:**
- We use Android's `AlarmManager` (in `ReminderSettingAdapter.java`) to schedule exact times.
- When the time hits, the system broadcasts an `Intent` that wakes up the `ReminderReceiver`.
- `onReceive`: The entry point for the receiver. We extract the `habit_name` from the intent extras and use `NotificationCompat.Builder` to construct and display a system notification, reminding the user to complete their habit.

## 4. Advanced: Modular UI with Fragments and ViewPager2

**Description:**
Modern Android apps use a single Activity (`MainActivity`) that swaps out different "Screens" known as `Fragments`. `ViewPager2` allows users to swipe horizontally between these fragments.

**Code Reference:** `app/src/main/java/com/habitflow/activities/MainActivity.java`

```java
ViewPager2 viewPager = findViewById(R.id.view_pager);
BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

// Adapter that provides Fragments to the ViewPager
MainPagerAdapter pagerAdapter = new MainPagerAdapter(this);
viewPager.setAdapter(pagerAdapter);

// Syncs ViewPager swipes with the Bottom Navigation Bar
viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
    @Override
    public void onPageSelected(int position) {
        bottomNav.getMenu().getItem(position).setChecked(true);
    }
});
```

**Explanation:**
- `MainPagerAdapter`: Returns a different `Fragment` instance (e.g., `HomeFragment`, `CalendarFragment`, `NotesFragment`) based on the current page position.
- `registerOnPageChangeCallback`: Listens for physical swipe gestures by the user. When the user swipes to a new page, it programmatically highlights the corresponding icon in the `BottomNavigationView`.
- Fragments have their own lifecycle (e.g., `onCreateView`, `onViewCreated`), keeping the code for each tab modular and isolated.

## 5. Advanced: Real-time Cloud Synchronization

**Description:**
To allow users to access their data on multiple devices, the app syncs the local SQLite database with a cloud database (Firebase Realtime Database).

**Code Reference:** `app/src/main/java/com/habitflow/data/FirebaseSyncManager.java`

```java
public class FirebaseSyncManager {
    private DatabaseReference getHabitsRef() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("habits");
    }

    public void syncHabitToCloud(Habit habit) {
        getHabitsRef().child(habit.id).setValue(habit)
            .addOnSuccessListener(aVoid -> Log.d("Sync", "Successfully synced: " + habit.name))
            .addOnFailureListener(e -> Log.e("Sync", "Failed to sync: " + habit.name));
    }
}
```

**Explanation:**
- `FirebaseAuth`: Verifies the user's identity.
- `DatabaseReference`: Points to a specific location in the JSON tree of the Firebase Database. Here, data is strictly isolated under `users -> {user_id} -> habits`.
- `setValue(habit)`: Firebase automatically serializes the Java `Habit` object into JSON and pushes it to the cloud. Because Firebase supports offline capabilities out-of-the-box, if the user has no internet, the push is queued locally and executed automatically when connectivity is restored.
