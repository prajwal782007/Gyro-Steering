# Gyro Steering Windows Receiver

This component receives UDP packets sent from the Gyro Steering Android app.

## Requirements
* Python 3

## Usage

1. Open a terminal in this directory (`windows-receiver`).
2. Run the receiver:

```bash
python src/main.py
```

3. The receiver listens on `0.0.0.0` (all interfaces) by default on UDP port `5005`.
4. It expects to receive a test packet from the Android app with the exact payload:
   `TEST|HELLO_FROM_PHONE|1`

## Windows Firewall Troubleshooting

If the Android phone successfully sends the packet but this receiver does not print anything, Windows Defender Firewall might be blocking incoming UDP packets.

1. Ensure both devices are on the same local Wi-Fi network or hotspot.
2. Open a command prompt and run `ipconfig` to find the IPv4 address of the currently active Wi-Fi adapter.
3. Ensure you entered that exact laptop IPv4 address in the Android app.
4. Verify that UDP port `5005` is allowed through the Windows Firewall for Python.
5. Make sure this receiver script is running *before* tapping "SEND TEST PACKET" on the phone.
