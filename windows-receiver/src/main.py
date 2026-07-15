import sys
import time
import os
from udp_receiver import UdpReceiver

def print_status(receiver):
    receiver.update_pps()
    with receiver.lock:
        ip = receiver.phone_ip
        port = receiver.phone_port
        angle = receiver.current_angle
        seq = receiver.latest_sequence
        pps = receiver.packets_sec
        missing = receiver.estimated_missing
        invalid = receiver.invalid_packets
        last_time = receiver.last_packet_time
        
    now = time.monotonic()
    
    if last_time == 0.0:
        age_ms = 0
        status = "WAITING FOR PACKETS"
    else:
        age_ms = int((now - last_time) * 1000)
        if age_ms > 250:
            status = "STREAM TIMEOUT"
        else:
            status = "RECEIVING LIVE STEERING"

    direction = "CENTER"
    if angle < 0:
        direction = "LEFT"
    elif angle > 0:
        direction = "RIGHT"
        
    output = []
    output.append("========================================")
    output.append("       GYRO STEERING LIVE RECEIVER      ")
    output.append("========================================")
    output.append("")
    output.append(f"Listening:           0.0.0.0:5005")
    output.append(f"Phone IP:            {ip}")
    output.append(f"Phone source port:   {port}")
    output.append("")
    output.append(f"Current angle:       {angle:.3f}°")
    output.append(f"Direction:           {direction}")
    output.append(f"Sequence:            {seq}")
    output.append("")
    output.append(f"Packets/sec:         {pps}")
    output.append(f"Estimated missing:   {missing}")
    output.append(f"Invalid packets:     {invalid}")
    output.append("")
    output.append(f"Last packet age:     {age_ms} ms")
    output.append("")
    output.append(f"Status:              {status}")
    
    return "\n".join(output)

def main():
    # Enable ANSI escape sequences on Windows command prompt
    os.system('')

    receiver = UdpReceiver(host="0.0.0.0", port=5005)
    
    if not receiver.start():
        sys.exit(1)
        
    # Print the first block
    print(print_status(receiver))
    
    try:
        while True:
            time.sleep(0.1) # 10 Hz refresh
            # Move cursor up 19 lines and reprint
            sys.stdout.write("\033[19F")
            sys.stdout.write(print_status(receiver) + "\n")
            sys.stdout.flush()
    except KeyboardInterrupt:
        print("\nStopping Gyro Steering UDP receiver...")
    finally:
        receiver.stop()

if __name__ == "__main__":
    main()
