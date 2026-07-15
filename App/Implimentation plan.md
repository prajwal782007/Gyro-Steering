Detailed Android Studio Implementation Plan for Gyro Steering 📱🏎️

Yes. For now, we build only the Android phone app. No laptop receiver, no virtual Xbox controller, no game integration. Those can wait patiently while we make sure the phone actually understands which direction it's pointing.

The Android app's final responsibility will be:

Read phone orientation
↓
Ask user to confirm center position
↓
Save current orientation as 0°
↓
Track rotation relative to that center
↓
Map movement between -90° and +90°
↓
Display live steering angle
↓
Later: send that data to laptop using UDP
1. Android app requirements

The phone lies flat on a table, with the screen facing upward.

The user opens the app and sees:

┌─────────────────────────────────────┐
│          GYRO STEERING              │
│                                     │
│        Place phone flat             │
│                                     │
│    Current raw angle: 137.4°        │
│                                     │
│      Is this your center?           │
│                                     │
│         [ SET CENTER ]              │
└─────────────────────────────────────┘

When the user taps Set Center, the current physical position becomes:

Relative steering angle = 0°

Then:

Phone movement	App output
90° left from center	-90°
45° left	-45°
Center	0°
45° right	+45°
90° right	+90°

If the phone goes beyond ±90°, the steering output remains clamped:

Phone: -130° → Steering: -90°
Phone: +120° → Steering: +90°
2. Recommended Android technology

Use:

Language: Kotlin
UI: XML Views
Minimum SDK: API 26
Sensor: TYPE_ROTATION_VECTOR
Orientation axis: Yaw / Azimuth
Sensor update rate: SENSOR_DELAY_GAME

I recommend TYPE_ROTATION_VECTOR, rather than relying only on TYPE_GYROSCOPE.

Why? The gyroscope gives angular velocity, not absolute orientation. Integrating it over time introduces drift. The rotation-vector sensor gives an orientation estimate based on available device sensors and is much better suited to your calibration-based steering system.

3. Project architecture

For the first clean version:

app/src/main/java/com/prajwal/phonesteering/
│
├── MainActivity.kt
│
├── sensor/
│   └── SteeringSensorManager.kt
│
├── model/
│   └── SteeringState.kt
│
└── utils/
└── AngleUtils.kt

And resources:

app/src/main/res/
│
├── layout/
│   └── activity_main.xml
│
├── drawable/
│   └── Various UI backgrounds/icons
│
├── values/
│   ├── colors.xml
│   ├── strings.xml
│   └── themes.xml

Don't over-engineer this into seventeen layers of repositories, dependency injection, use cases, and spiritual abstractions. It's a steering sensor app, not an international banking system.

Phase 1: Build the basic UI

The first screen should contain these components:

┌────────────────────────────────────────┐
│                                        │
│             GYRO STEERING              │
│        Motion Steering Controller      │
│                                        │
│              ┌────────┐                │
│              │  📱    │                │
│              └────────┘                │
│                                        │
│             STEERING ANGLE             │
│                                        │
│                 -37.4°                 │
│                                        │
│       LEFT  ←──────────────→  RIGHT     │
│                                        │
│            Center: Calibrated ✓         │
│                                        │
│            [ SET AS CENTER ]            │
│                                        │
└────────────────────────────────────────┘
UI components

We need:

App title
Sensor availability status
Current steering angle
Steering direction: LEFT, CENTER, or RIGHT
Visual steering indicator
Set as Center button
Calibration status
Optional raw yaw angle for debugging

For development, show both:

Raw yaw: 143.72°
Relative angle: -36.28°
Output angle: -36.28°

Later, hide raw debugging information.

Phase 2: Create AngleUtils.kt

This class handles all angle mathematics.

Its responsibilities:

1. Convert radians to degrees
2. Calculate shortest angle difference
3. Handle +180° / -180° wraparound
4. Clamp steering output to ±90°

The essential function:

fun shortestAngleDifference(
currentAngle: Float,
centerAngle: Float
): Float {
var difference = currentAngle - centerAngle

    while (difference > 180f) {
        difference -= 360f
    }

    while (difference < -180f) {
        difference += 360f
    }

    return difference
}

Then clamping:

fun clampSteeringAngle(angle: Float): Float {
return angle.coerceIn(-90f, 90f)
}

Expected behavior:

Center: 170°
Current: -170°

Naive calculation:
-170 - 170 = -340° ❌

Correct calculation:
+20° ✅

This is important because Android's orientation angle wraps around at ±180°.

Phase 3: Build SteeringSensorManager.kt

This is the heart of the Android app.

It will:

Get Android SensorManager
↓
Find TYPE_ROTATION_VECTOR sensor
↓
Register SensorEventListener
↓
Receive orientation updates
↓
Convert rotation vector to rotation matrix
↓
Extract yaw angle
↓
Send current yaw to MainActivity

The internal flow:

SensorEvent.values
↓
SensorManager.getRotationMatrixFromVector()
↓
Rotation matrix
↓
SensorManager.getOrientation()
↓
orientationAngles[0]
↓
Yaw in radians
↓
Convert to degrees

The result is approximately:

-180° to +180°

The manager should expose callbacks such as:

onOrientationChanged(yawDegrees: Float)

The UI shouldn't care about rotation matrices or sensor internals. It should just receive the current angle.

Phase 4: Sensor availability handling

Not every Android phone exposes exactly the same sensor setup, because apparently hardware consistency was too much to ask.

When starting the app:

Check TYPE_ROTATION_VECTOR
↓
Available?
↙          ↘
YES           NO
↓             ↓
Start sensor   Show error

If unavailable, display:

Rotation vector sensor is not available on this device.

Later, we can add fallback support using:

TYPE_GAME_ROTATION_VECTOR

or other sensor combinations.

For the MVP, first test TYPE_ROTATION_VECTOR.

Phase 5: Implement center calibration

Before calibration:

Raw yaw = 137.4°
Center yaw = Not set
Relative angle = 0°

When the user presses:

SET AS CENTER

Save:

centerYaw = currentYaw

Example:

Current yaw: 137.4°
User taps Set Center

centerYaw = 137.4°
relativeAngle = 0°

Then if the phone rotates to:

currentYaw = 167.4°

Calculate:

167.4 - 137.4 = +30°

Output:

+30° right

If the direction is physically reversed during testing:

relativeAngle *= -1

We determine this only after testing on the real phone. Sensor coordinate systems love making simple directions feel like a philosophical disagreement.

Phase 6: Startup calibration popup

When the app starts, don't immediately assume some arbitrary orientation is the center.

Show a dialog:

┌────────────────────────────────────┐
│         SET CENTER POSITION        │
│                                    │
│   Place your phone flat on the     │
│   table in your comfortable        │
│   middle steering position.        │
│                                    │
│   Is this your center position?    │
│                                    │
│       [ CANCEL ]   [ YES, SET ]    │
└────────────────────────────────────┘

Important behavior:

Wait until the sensor has received a valid orientation.
Show the dialog.
User places the phone correctly.
User taps Yes, Set.
Save the latest yaw as centerYaw.
Set calibration status to true.
Steering output immediately becomes 0°.

Also keep a permanent Recenter button on the main screen.

Phase 7: Calculate relative steering

Every time a new yaw value arrives:

currentYaw
↓
Is center calibrated?
↓ YES
shortestAngleDifference(currentYaw, centerYaw)
↓
relativeAngle
↓
Clamp to -90°...+90°
↓
steeringAngle

Pseudo-code:

if (centerYaw != null) {

    val relativeAngle =
        AngleUtils.shortestAngleDifference(
            currentYaw,
            centerYaw!!
        )

    val steeringAngle =
        relativeAngle.coerceIn(-90f, 90f)
}

Example:

Center yaw: 80°

Current yaw: 35°
Difference: -45°

Displayed steering:
-45° LEFT
Phase 8: Direction detection

The app should display:

LEFT
CENTER
RIGHT

Use a small deadzone:

-1° to +1° → CENTER
Less than -1° → LEFT
Greater than +1° → RIGHT

Logic:

val direction = when {
steeringAngle < -1f -> "LEFT"
steeringAngle > 1f -> "RIGHT"
else -> "CENTER"
}

This prevents the UI from flickering:

LEFT
CENTER
RIGHT
CENTER
LEFT

because of tiny sensor fluctuations around 0°.

Phase 9: Add visual steering indicator

A simple first version can use a horizontal bar:

LEFT                 CENTER                 RIGHT
│                      │                      │
▼                      ▼                      ▼

-90° ────────────────── 0° ───────────────── +90°
▲
Current

Or a rotating phone/steering-wheel graphic:

          CENTER
             │
             │
      ╲      │      ╱
       ╲     │     ╱
        ╲    │    ╱

       -90°  0°  +90°

For MVP, a horizontal indicator is enough. Fancy animation comes after sensor accuracy works, because a beautifully animated wrong angle is still wrong.

Phase 10: Sensor update rate

Start with:

SensorManager.SENSOR_DELAY_GAME

This typically provides sufficiently frequent updates for game-like interaction.

Do not immediately use:

SENSOR_DELAY_FASTEST

More updates don't automatically mean better steering. It can increase CPU usage and deliver sensor data faster than we need.

The target is approximately:

60–100 useful steering updates per second

Later, when we add UDP, the sender can operate at a fixed rate such as 100 Hz.

Phase 11: App lifecycle

This matters for battery and correct behavior.

When the app becomes active:

onResume()
↓
Register sensor listener

When it goes into background:

onPause()
↓
Unregister sensor listener

This prevents unnecessary sensor use when the app isn't active.

Phase 12: Handle screen orientation

For this project, I'd lock the app to portrait orientation initially.

Why?

Because the phone's physical rotation is our steering input. We don't want Android itself rotating the entire UI when the phone reaches a different orientation.

In AndroidManifest.xml:

android:screenOrientation="portrait"

However, because the phone is lying flat, Android's auto-rotation behavior can be unpredictable depending on device orientation. Locking the UI avoids unnecessary configuration changes.

Phase 13: Keep screen awake

During gameplay, the phone screen should not turn off.

Add:

window.addFlags(
WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
)

Or use the XML equivalent on the main view.

When the steering session is active:

Screen remains awake

Later, when we add Start/Stop steering, we can keep the screen awake only during an active session.

Phase 14: Save calibration or don't?

For your project, do not permanently save the center angle between app launches.

Why? Because every time the user places their phone on the table, its starting physical orientation may be different.

So every new session should ask:

Is this your middle position?

That's exactly the behavior you originally wanted.

Phase 15: Add sensor smoothing carefully

Initially, use raw relative angle values.

Test:

0.0°
0.1°
-0.1°
0.2°
-0.2°

If there is noticeable jitter, add lightweight smoothing:

filteredAngle =
alpha * newAngle +
(1 - alpha) * previousAngle

Start with:

alpha = 0.6

Higher alpha:

Faster response
More jitter

Lower alpha:

Smoother movement
More input delay

For gaming, latency matters. Don't bury the input under aggressive smoothing until steering feels like it's arriving by registered post.

Phase 16: Debug information

During development, display:

Sensor: Rotation Vector ✓

Raw yaw:
137.42°

Center yaw:
102.16°

Relative angle:
35.26°

Clamped steering:
35.26°

Direction:
RIGHT

Sensor updates/sec:
78

This makes debugging much easier.

Later, production UI only needs:

Current angle: 35.3°
Direction: RIGHT
Status: Ready
Phase 17: Recommended implementation order

Follow this exact sequence:

Create the basic XML UI with angle text and Set Center button.
Check TYPE_ROTATION_VECTOR availability.
Read live yaw from the sensor.
Display raw yaw on screen.
Implement AngleUtils.shortestAngleDifference().
Implement Set Center calibration.
Display relative angle.
Clamp the output to ±90°.
Verify left/right direction physically.
Add ±1° center deadzone.
Add startup calibration dialog.
Add Recenter button.
Lock screen orientation.
Keep screen awake during use.
Test for jitter and add minimal smoothing only if necessary.
Add debug information and update-rate counter.
Test extensively on the physical phone.
Final Android-only success criteria

Before touching UDP or laptop code, the Android app must pass these tests:

Test	Expected result
Launch app	Center calibration prompt appears
Set center	Display immediately becomes 0°
Rotate 45° left	Approximately -45°
Rotate 90° left	-90°, maximum
Rotate beyond 90° left	Remains -90°
Return to center	Approximately 0°
Rotate 45° right	Approximately +45°
Rotate 90° right	+90°, maximum
Cross ±180° sensor boundary	No sudden jump
Small movement around center	Stable due to deadzone
Background app	Sensor listener stops
Return to app	Sensor tracking resumes
Relaunch app	Asks for center calibration again
The first actual coding task

The first implementation milestone is extremely specific:

Create UI
↓
Read TYPE_ROTATION_VECTOR
↓
Extract yaw angle
↓
Display raw live yaw on screen

Do not implement calibration, clamping, smoothing, or networking before raw yaw works correctly on your actual phone. First prove the sensor layer works. Then build upward, one layer at a time. This saves us from debugging six things simultaneously, a cherished human tradition that produces mainly coffee consumption and profanity. 🔧📱