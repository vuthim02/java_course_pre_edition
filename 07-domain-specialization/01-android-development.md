# Domain Specialization — Lesson 1: Android Development with Java

> **INTRODUCTORY OVERVIEW** — This section provides a high-level introduction to the domain. Each topic warrants its own dedicated course for professional mastery.

## Why Android?

Android is the world's most popular mobile OS with 3+ billion active devices. Java is the original (and still primary) language for Android development alongside Kotlin.

```
┌─────────────────────────────────────────────────────────────┐
│                     ANDROID APPLICATION                       │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                   Activities                             │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────┐ │ │
│  │  │ Login    │  │ Home     │  │ Profile  │  │Settings│ │ │
│  │  │ Activity │  │ Activity │  │ Activity │  │Activity│ │ │
│  │  └──────────┘  └──────────┘  └──────────┘  └────────┘ │ │
│  └─────────────────────────────────────────────────────────┘ │
│                               │                               │
│  ┌────────────────────────────▼────────────────────────────┐ │
│  │                   ViewModel + LiveData                    │ │
│  └────────────────────────────┬────────────────────────────┘ │
│                               │                               │
│  ┌────────────────────────────▼────────────────────────────┐ │
│  │                   Repository + Room DB                    │ │
│  └─────────────────────────────────────────────────────────┘ │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ Retrofit     │  │ Firebase     │  │ Dependency       │   │
│  │ (Network)    │  │ (Auth/Push)  │  │ Injection (Dagger)│   │
│  └──────────────┘  └──────────────┘  └──────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Setup

```xml
<!-- build.gradle (Project-level) -->
buildscript {
    dependencies {
        classpath 'com.android.tools.build:gradle:8.2.0'
    }
}
```

```xml
// build.gradle (App-level)
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'  // Optional for Kotlin
}

android {
    compileSdk 34
    defaultConfig {
        applicationId "com.example.myapp"
        minSdk 26
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}

dependencies {
    implementation 'androidx.core:core:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.lifecycle:lifecycle-viewmodel:2.7.0'
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
}
```

## Activity — Entry Point

```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find views
        Button submitButton = findViewById(R.id.btn_submit);
        EditText nameInput = findViewById(R.id.et_name);
        TextView greetingText = findViewById(R.id.tv_greeting);

        // Set click listener
        submitButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            greetingText.setText("Hello, " + name + "!");
        });
    }
}
```

### Activity Lifecycle

```
              ┌─────────────┐
              │  onCreate() │
              └──────┬──────┘
                     │
              ┌──────▼──────┐
        ┌─────│  onStart()  │
        │     └──────┬──────┘
        │            │
   ┌────┴────┐  ┌────▼──────┐
   │onRestart│  │ onResume()│  ← App is visible and interactive
   └────┬────┘  └────┬──────┘
        │            │
        │     ┌──────▼──────┐
        │     │ onPause()   │  ← Partially obscured
        │     └──────┬──────┘
        │            │
        │     ┌──────▼──────┐
        └─────│ onStop()    │  ← Not visible
              └──────┬──────┘
                     │
              ┌──────▼──────┐
              │ onDestroy() │
              └─────────────┘
```

## ViewModel — Survives Configuration Changes

```java
public class UserViewModel extends ViewModel {

    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private final UserRepository repository = new UserRepository();

    public LiveData<List<User>> getUsers() {
        return users;
    }

    public void loadUsers() {
        repository.fetchUsers(new Callback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                users.setValue(result);
            }

            @Override
            public void onError(Exception e) {
                users.setValue(Collections.emptyList());
            }
        });
    }
}

// In Activity
UserViewModel viewModel = new ViewModelProvider(this).get(UserViewModel.class);
viewModel.getUsers().observe(this, userList -> {
    // Update UI — survives rotation!
});
```

## Room Database (SQLite)

```java
@Entity
public class User {
    @PrimaryKey
    public long id;
    public String name;
    public String email;
}

@Dao
public interface UserDao {
    @Query("SELECT * FROM user")
    LiveData<List<User>> getAll();

    @Insert
    void insert(User user);

    @Delete
    void delete(User user);
}

@Database(entities = {User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
}

// Usage
AppDatabase db = Room.databaseBuilder(
    getApplicationContext(),
    AppDatabase.class,
    "myapp-db"
).build();
db.userDao().insert(new User());
```

## Retrofit — HTTP Client

```java
public interface ApiService {
    @GET("api/v1/users")
    Call<List<User>> getUsers();

    @POST("api/v1/users")
    Call<User> createUser(@Body User user);
}

// Setup
Retrofit retrofit = new Retrofit.Builder()
    .baseUrl("https://api.myapp.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build();

ApiService api = retrofit.create(ApiService.class);

// Usage
api.getUsers().enqueue(new Callback<List<User>>() {
    @Override
    public void onResponse(Call<List<User>> call, Response<List<User>> response) {
        if (response.isSuccessful()) {
            List<User> users = response.body();
            // Update UI
        }
    }

    @Override
    public void onFailure(Call<List<User>> call, Throwable t) {
        // Handle error
    }
});
```

## Exercises

1. Create an Android app with an Activity, layout XML, and button click handler.
2. Add a ViewModel that loads data and survives rotation.
3. Implement a RecyclerView to display a list of items.
4. Add Room database to persist data locally.
5. Use Retrofit to call a REST API and display results.
