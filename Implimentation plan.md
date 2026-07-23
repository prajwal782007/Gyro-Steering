ow that the requirement is finally nailed down, here's the full implementation plan, from empty folders to actually steering a car in a Windows game.

The target behavior is:

Phone placed flat on table
        ↓
User confirms current position as CENTER
        ↓
Rotate phone 45° left
        ↓
Game receives 50% left steering

Rotate phone 90° left
        ↓
Game receives 100% left steering

Rotate phone 90° right
        ↓
Game receives 100% right steering

The project consists of two applications:

1. Android app → Reads orientation + sends steering data
2. Windows app → Receives data + creates virtual controller input
1. Final system architecture
┌─────────────────────────────────────┐
│           📱 ANDROID PHONE          │
│                                     │
│  TYPE_ROTATION_VECTOR sensor        │
│              ↓                      │
│  Calculate yaw angle                │
│              ↓                      │
│  User sets current position CENTER  │
│              ↓                      │
│  Calculate relative rotation        │
│              ↓                      │
│  Clamp: -90° to +90°                │
│              ↓                      │
│  Send UDP packets at 100 Hz         │
└──────────────────┬──────────────────┘
                   │
                   │ Local Wi-Fi
                   │ UDP Port 5005
                   ▼
┌─────────────────────────────────────┐
│           💻 WINDOWS LAPTOP         │
│                                     │
│  UDP Receiver                       │
│              ↓                      │
│  Validate packet                    │
│              ↓                      │
│  Apply optional smoothing           │
│              ↓                      │
│  Map -90°...+90° → -1.0...+1.0      │
│              ↓                      │
│  Virtual Xbox Controller            │
│              ↓                      │
│  Left Analog Stick X-axis           │
└──────────────────┬──────────────────┘
                   ▼
┌─────────────────────────────────────┐
│               🎮 GAME               │
│                                     │
│  Detects virtual Xbox controller    │
│              ↓                      │
│  Left analog stick controls car     │
└─────────────────────────────────────┘
Phase 1: Define the exact control behavior

Before touching code, freeze the behavior. Otherwise, three days later we'll be debating whether 67° should mean 74% steering because apparently software development needed more suffering.

Steering mapping

We'll use:

Phone relative rotation	Virtual controller output
≤ -90°	-1.000
-75°	-0.833
-45°	-0.500
-20°	-0.222
0°	0.000
+20°	+0.222
+45°	+0.500
+75°	+0.833
≥ +90°	+1.000

Formula:

relativeAngle = shortestDifference(currentYaw, centerYaw)

clampedAngle = clamp(relativeAngle, -90, +90)

steering = clampedAngle / 90
Sign convention

We need one fixed convention:

Left  = -90° → -1.0
Center = 0°  →  0.0
Right = +90° → +1.0

If the sensor reports the opposite direction, simply multiply by -1.

Phase 2: Technology stack
Android application

Use:

Language: Kotlin
IDE: Android Studio
Minimum Android: Android 8 or newer
Sensor: TYPE_ROTATION_VECTOR
Network: UDP DatagramSocket
Update rate: Approximately 100 Hz

I'd choose Kotlin, not Java, even though you've used Java before. The app is small enough that Kotlin won't create unnecessary complexity, and Android development has largely moved there.

Windows receiver

For version 1:

Language: Python 3.11+
Network: Python socket module
Virtual controller: ViGEm-compatible Python gamepad library
Interface: CLI initially

Later, convert it into:

Windows .exe
+
GUI
+
Automatic device discovery

Do not build the GUI first. That's how people end up with a beautiful Connect button connected to absolutely nothing. 💀

Phase 3: Android app structure

Use this architecture:

app/
├── ui/
│   └── MainActivity.kt
│
├── sensor/
│   └── SteeringSensorManager.kt
│
├── network/
│   └── UdpSender.kt
│
├── model/
│   └── SteeringData.kt
│
└── utils/
    └── AngleUtils.kt

Each component has one job.

MainActivity.kt

Responsible for:

UI
Connection controls
Laptop IP input
Port input
Set Center button
Start/Stop steering
Display current angle
Display connection status
SteeringSensorManager.kt

Responsible for:

Register TYPE_ROTATION_VECTOR
Receive sensor updates
Calculate orientation matrix
Extract yaw/azimuth
Calculate relative rotation from center
UdpSender.kt

Responsible for:

Create UDP socket
Convert steering data to bytes
Send packets to laptop
Run network operations off main UI thread
AngleUtils.kt

Responsible for:

Angle wrapping
Shortest angular difference
Clamping
Optional filtering
Phase 4: Android sensor implementation
Step 4.1: Access the rotation-vector sensor

The application requests:

Sensor.TYPE_ROTATION_VECTOR

Then receives:

SensorEvent.values[]

Convert it to a rotation matrix:

SensorManager.getRotationMatrixFromVector(
    rotationMatrix,
    event.values
)

Then calculate orientation:

SensorManager.getOrientation(
    rotationMatrix,
    orientationAngles
)

The yaw/azimuth is typically:

orientationAngles[0]

It's returned in radians, so convert it:

degrees = radians × 180 / π

Result:

-180° to +180°
Phase 5: Center calibration

This is one of the most important pieces.

When the app opens:

┌──────────────────────────────────┐
│                                  │
│     Place phone flat on table    │
│                                  │
│   Keep it in your comfortable    │
│       middle position.           │
│                                  │
│      [ SET AS CENTER ]           │
│                                  │
└──────────────────────────────────┘

When the user presses the button:

centerYaw = currentYaw

Example:

Actual phone yaw: 137.4°

User presses:
SET AS CENTER

Therefore:

centerYaw = 137.4°
relativeAngle = 0°

Now phone moves to:

currentYaw = 167.4°

Result:

relativeAngle = +30°
Phase 6: Correctly handle ±180° wraparound

This is essential.

Imagine:

Center = +170°
Current = -170°

Naive calculation:

-170 - 170 = -340°

Obviously wrong. The phone only moved 20°.

Use:

fun shortestAngleDifference(current: Float, center: Float): Float {
    var difference = current - center

    while (difference > 180f) {
        difference -= 360f
    }

    while (difference < -180f) {
        difference += 360f
    }

    return difference
}

Now:

Center  = +170°
Current = -170°

Difference = +20°

Civilization survives another sensor calculation.

Phase 7: Clamp steering range

Your desired range is:

-90° → Maximum left
  0° → Center
+90° → Maximum right

So:

val clampedAngle = relativeAngle.coerceIn(-90f, 90f)

Then:

Phone rotates +110°
        ↓
clamped to +90°
        ↓
Maximum right steering
Phase 8: UDP communication protocol

For the first version, keep packets extremely simple.

Recommended packet

Send:

-42.73

That's enough.

At 100 Hz, sending JSON like this:

{
  "angle": -42.73,
  "timestamp": 1784112567123,
  "device": "Prajwal's phone",
  "status": "steering magnificently"
}

would be pointless.

Better production packet format

Once the basic prototype works:

STEER|-42.73|4821

Fields:

STEER = packet type
-42.73 = angle
4821 = sequence number

This lets the laptop detect:

Missing packets
Out-of-order packets
Invalid packets

Final format:

STEER|angle|sequence

Example:

STEER|-37.482|10328
Phase 9: UDP sending frequency

Target:

100 Hz = one packet every 10 milliseconds

But don't blindly send on every sensor event, because Android sensor events may arrive at irregular rates.

Better architecture:

Sensor events
    ↓
Always update latestAngle
    ↓
Dedicated 100 Hz sending loop
    ↓
Read latestAngle
    ↓
Send UDP packet

This gives stable networking behavior.

Use:

Sensor update rate: SENSOR_DELAY_GAME or fastest suitable rate
UDP send rate: 100 Hz

Later, benchmark:

60 Hz
100 Hz
120 Hz

My starting recommendation is 100 Hz.

Phase 10: Android UI

The MVP screen should show:

┌──────────────────────────────────┐
│       PHONE STEERING             │
│                                  │
│ Laptop IP                        │
│ [ 192.168.1.5             ]      │
│                                  │
│ Port                             │
│ [ 5005                    ]      │
│                                  │
│ Current angle                    │
│            -37.4°                │
│                                  │
│ Steering                         │
│      ███████░░░░ LEFT            │
│                                  │
│       [ SET CENTER ]             │
│                                  │
│       [ START STEERING ]         │
│                                  │
│ Status: ● Sending                │
└──────────────────────────────────┘
Required controls
Laptop IP address
Port
Set Center
Start/Stop steering
Recenter
Current relative angle
Current steering percentage
Packet rate
Connection status

Remember: UDP is connectionless. So "Connected" is technically misleading unless the laptop sends an acknowledgement. Initially use:

Sending
Stopped

Later implement a handshake for true connection status.

Phase 11: Windows UDP receiver

The laptop program listens on:

0.0.0.0:5005

Basic flow:

Start application
      ↓
Create UDP socket
      ↓
Bind port 5005
      ↓
Wait for packets
      ↓
Receive:
STEER|-37.482|10328
      ↓
Validate
      ↓
Extract angle
      ↓
Convert to virtual controller axis

The receiver should track:

Latest angle
Latest packet time
Packets per second
Sequence number
Phone IP
Packet loss estimate
Phase 12: Mapping to the virtual controller

Received:

angle = -45°

Calculate:

steering = angle / 90.0

Result:

-0.5

The virtual Xbox controller receives:

Left analog stick X = -50%

For the full range:

-90° → -1.0
  0° →  0.0
+90° → +1.0

The game sees:

Xbox 360 Controller
Left Stick X Axis

Then inside the game's control settings:

Steering Axis → Left Stick X
Phase 13: Fail-safe timeout

This is mandatory.

Suppose:

Phone angle = -80°
       ↓
Packet sent
       ↓
Wi-Fi disconnects

Without a fail-safe, the game could remain at:

80° left steering forever

So the laptop should check:

If no packet received for > 250 ms:
    steering = 0

Recommended initial timeout:

250 ms

You can later adjust between:

100–500 ms
Phase 14: Smoothing

Do not add heavy smoothing immediately.

Heavy smoothing creates input lag.

Start with raw data. If the steering shakes, add an exponential moving average:

filtered = α × newValue + (1 - α) × previousValue

Example:

α = 0.5

Higher alpha:

More responsive
Less smooth

Lower alpha:

Smoother
More latency

I'd test:

0.4
0.5
0.6
0.7

Don't blindly choose one. Measure how it feels.

Phase 15: Deadzone

Phones naturally have tiny sensor fluctuations.

You may see:

0.00°
0.13°
-0.08°
0.21°
-0.15°

So add a small center deadzone:

If absolute angle < 1°:
    steering = 0

Recommended starting point:

Deadzone = ±1°

Make it configurable later.

Phase 16: Network discovery

For the MVP, manually enter:

Laptop IP: 192.168.1.5
Port: 5005

Later, make it automatic.

The laptop broadcasts:

STEERING_SERVER|PRAJWAL-LAPTOP|5005

The phone discovers it and displays:

Available computers:

💻 PRAJWAL-LAPTOP
192.168.1.5

[ CONNECT ]

That's much better than making users hunt for IP addresses like it's 1997.

Phase 17: Handshake system

Production version:

PHONE → Laptop:
HELLO|deviceId|deviceName

Laptop → PHONE:
WELCOME|sessionId

PHONE → Laptop:
STEER|angle|sequence

Laptop → PHONE every second:
PING

PHONE → Laptop:
PONG

This allows:

Real connection status
Latency measurement
Automatic reconnection
Session management
Phase 18: Development milestones
Milestone	Goal	Success condition
M1	Sensor reading	Phone displays correct rotation
M2	Calibration	Current position becomes 0°
M3	Correct mapping	±90° works correctly
M4	UDP sending	Laptop receives angle
M5	Stable 100 Hz	Continuous smooth packets
M6	Virtual controller	Windows sees Xbox controller
M7	Game test	Car steers using phone
M8	Safety	Disconnect returns steering to center
M9	Polish	GUI, discovery, settings
Phase 19: Recommended project folder structure
phone-steering-controller/
│
├── android-app/
│   ├── app/
│   │   └── src/main/java/com/prajwal/steering/
│   │
│   ├── MainActivity.kt
│   ├── SteeringSensorManager.kt
│   ├── UdpSender.kt
│   └── AngleUtils.kt
│
├── windows-receiver/
│   ├── main.py
│   ├── udp_receiver.py
│   ├── controller.py
│   ├── config.py
│   └── requirements.txt
│
├── docs/
│   ├── protocol.md
│   └── architecture.md
│
└── README.md
Phase 20: Testing plan

We should test each layer independently.

Android sensor test

Check:

Center → 0°
Rotate approximately 45° left → approximately -45°
Rotate approximately 90° left → approximately -90°
Return center → approximately 0°
Network test

Laptop terminal:

Phone: 192.168.1.8
Packets/sec: 99
Angle: -37.42°
Sequence: 8421
Packet loss: 0.2%
Controller test

Before opening any game:

Windows Game Controllers
        ↓
Virtual Xbox Controller
        ↓
Properties
        ↓
Watch X-axis move

Test:

Phone left → X-axis left
Phone center → X-axis center
Phone right → X-axis right
Game test

Start with a game that has proper Xbox controller support. Configure:

Steering → Left Analog Stick X

Then evaluate:

Input latency
Sensor jitter
Return-to-center accuracy
Maximum steering
Wi-Fi stability
Final MVP specification

The first genuinely working version should contain only this:

ANDROID
✓ Rotation-vector sensor
✓ Set Center button
✓ Relative angle calculation
✓ ±90° clamp
✓ UDP transmission at 100 Hz
✓ Laptop IP and port input

WINDOWS
✓ UDP receiver
✓ Packet validation
✓ Angle → analog axis mapping
✓ Virtual Xbox controller output
✓ 250 ms disconnect fail-safe

RESULT
✓ 0° phone rotation = straight
✓ 45° left = 50% left steering
✓ 90° left = maximum left
✓ 45° right = 50% right steering
✓ 90° right = maximum right
My implementation recommendation

Build this in three practical stages:

STAGE A
Phone sensor → Show correct angle on Android screen

STAGE B
Phone → UDP → Laptop terminal displays exact angle

STAGE C
Laptop → Virtual Xbox controller → Actual game steering
