Gyro-Steering/
│
├── main-implementation-plan.md
│
├── app/                              ← Android Studio has access ONLY here
│   │
│   ├── android-implementation-plan.md
│   │
│   ├── app/                          ← Android's actual app module
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/
│   │   │   │   │   └── com/
│   │   │   │   │       └── prajwal/
│   │   │   │   │           └── gyrosteering/
│   │   │   │   │               ├── MainActivity.kt
│   │   │   │   │               ├── sensor/
│   │   │   │   │               │   └── SteeringSensorManager.kt
│   │   │   │   │               ├── model/
│   │   │   │   │               │   └── SteeringState.kt
│   │   │   │   │               └── utils/
│   │   │   │   │                   └── AngleUtils.kt
│   │   │   │   │
│   │   │   │   ├── res/
│   │   │   │   │   ├── drawable/
│   │   │   │   │   ├── layout/
│   │   │   │   │   │   └── activity_main.xml
│   │   │   │   │   └── values/
│   │   │   │   │       ├── colors.xml
│   │   │   │   │       ├── strings.xml
│   │   │   │   │       └── themes.xml
│   │   │   │   │
│   │   │   │   └── AndroidManifest.xml
│   │   │   │
│   │   │   ├── test/
│   │   │   └── androidTest/
│   │   │
│   │   ├── build.gradle.kts
│   │   └── proguard-rules.pro
│   │
│   ├── gradle/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradle.properties
│   ├── gradlew
│   └── gradlew.bat
│
└── windows-receiver/                 ← Later, Antigravity works here
    ├── src/
    │   ├── main.py
    │   ├── udp_receiver.py
    │   ├── controller.py
    │   └── config.py
    └── requirements.txt
