import sys
import time
import os
import threading
from udp_receiver import UdpReceiver
from virtual_controller import VirtualController

def print_status(receiver, controller):
    receiver.update_pps()
    with receiver.lock:
        ip = receiver.phone_ip
        port = receiver.phone_port
        angle = receiver.current_angle
        throttle = receiver.current_throttle
        brake = receiver.current_brake
        seq = receiver.latest_sequence
        pps = receiver.packets_sec
        missing = receiver.estimated_missing
        invalid = receiver.invalid_packets
        last_time = receiver.last_packet_time
        
    now = time.monotonic()
    
    if last_time == 0.0:
        age_ms = 0
        status = "WAITING FOR PACKETS"
        failsafe = "CENTERED"
    else:
        age_ms = int((now - last_time) * 1000)
        if age_ms > 250:
            status = "STREAM TIMEOUT"
            failsafe = "CENTERED + THROTTLE RELEASED"
        else:
            status = "RECEIVING LIVE CONTROL"
            failsafe = "NORMAL"

    direction = "CENTER"
    if angle < 0:
        direction = "LEFT"
    elif angle > 0:
        direction = "RIGHT"
        
    ctrl_status = "ACTIVE" if controller.available else "UNAVAILABLE"
    ctrl_steer = f"{controller.current_normalized_x:+.3f}" if controller.available else "+0.000"
    ctrl_throttle = f"{controller.current_throttle:.3f}" if controller.available else "0.000"
    ctrl_brake = f"{controller.current_brake:.3f}" if controller.available else "0.000"
        
    output = []
    output.append("========================================")
    output.append("       GYRO STEERING LIVE RECEIVER      ")
    output.append("========================================")
    output.append("")
    output.append(f"Listening:           0.0.0.0:5005")
    output.append(f"Phone IP:            {ip}")
    output.append(f"Phone source port:   {port}")
    output.append("")
    output.append(f"Current angle:       {angle:+.3f}°")
    output.append(f"Direction:           {direction}")
    output.append(f"Throttle:            {throttle * 100:.1f}%")
    output.append(f"Brake:               {brake * 100:.1f}%")
    output.append(f"Sequence:            {seq}")
    output.append("")
    output.append(f"Packets/sec:         {pps}")
    output.append(f"Estimated missing:   {missing}")
    output.append(f"Invalid packets:     {invalid}")
    output.append("")
    output.append(f"Last packet age:     {age_ms} ms")
    output.append("")
    output.append(f"Status:              {status}")
    output.append("")
    output.append(f"Virtual controller:  {ctrl_status}")
    output.append(f"Controller steering: {ctrl_steer}")
    output.append(f"Controller throttle: {ctrl_throttle}")
    output.append(f"Controller brake:    {ctrl_brake}")
    output.append(f"Fail-safe:           {failsafe}")
    
    return "\n".join(output)

def controller_worker(receiver, controller):
    while getattr(receiver, '_running', False):
        with receiver.lock:
            last_time = receiver.last_packet_time
            angle = receiver.current_angle
            throttle = receiver.current_throttle
            brake = receiver.current_brake
            
        now = time.monotonic()
        if last_time == 0.0 or (now - last_time) > 0.250:
            controller.center()
        else:
            controller.set_control_state(angle, throttle, brake)
            
        time.sleep(0.01) # 100 Hz

def main():
    # Enable ANSI escape sequences on Windows command prompt
    os.system('')

    receiver = UdpReceiver(host="0.0.0.0", port=5005)
    controller = VirtualController()
    
    if not receiver.start():
        sys.exit(1)
        
    if controller.available:
        worker = threading.Thread(target=controller_worker, args=(receiver, controller), daemon=True)
        worker.start()
        
    # Print the first block
    print(print_status(receiver, controller))
    
    try:
        while True:
            time.sleep(0.1) # 10 Hz refresh
            # Move cursor up 27 lines and reprint
            sys.stdout.write("\033[27F")
            sys.stdout.write(print_status(receiver, controller) + "\n")
            sys.stdout.flush()
    except KeyboardInterrupt:
        print("\nStopping Gyro Steering UDP receiver...")
    finally:
        receiver.stop()
        controller.stop()

if __name__ == "__main__":
    main()
