import sys
from udp_receiver import UdpReceiver

def main():
    print("========================================")
    print("       GYRO STEERING UDP RECEIVER       ")
    print("========================================")
    print()
    print("Listening on UDP 0.0.0.0:5005")
    print("Waiting for Android phone packets...")
    print()
    print("Press Ctrl+C to stop.")

    receiver = UdpReceiver(host="0.0.0.0", port=5005)
    
    if not receiver.start():
        sys.exit(1)

    try:
        receiver.receive_loop()
    except KeyboardInterrupt:
        print("\nStopping Gyro Steering UDP receiver...")
    finally:
        receiver.stop()

if __name__ == "__main__":
    main()
