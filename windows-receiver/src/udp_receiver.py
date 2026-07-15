import socket
import threading
import time
import sys
from packet_parser import parse_steer_packet

class UdpReceiver:
    def __init__(self, host="0.0.0.0", port=5005):
        self.host = host
        self.port = port
        self.sock = None
        self._running = False
        self._thread = None
        
        # Live stats
        self.lock = threading.Lock()
        self.phone_ip = "Unknown"
        self.phone_port = 0
        self.current_angle = 0.0
        self.current_throttle = 0.0
        self.latest_sequence = -1
        
        self.packets_sec = 0
        self.estimated_missing = 0
        self.invalid_packets = 0
        
        self.last_packet_time = 0.0
        
        # Internal counters for PPS
        self._pps_counter = 0
        self._pps_last_time = time.monotonic()

    def start(self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.sock.settimeout(0.5)
        try:
            self.sock.bind((self.host, self.port))
        except Exception as e:
            print(f"ERROR: Could not bind to UDP port {self.port}.")
            print("The port may already be in use by another application.")
            return False
            
        self._running = True
        self._thread = threading.Thread(target=self._receive_loop, daemon=True)
        self._thread.start()
        return True

    def _receive_loop(self):
        while self._running:
            try:
                data, addr = self.sock.recvfrom(1024)
            except socket.timeout:
                continue
            except Exception as e:
                if self._running:
                    pass
                break
                
            ip, sender_port = addr
            
            try:
                payload = data.decode('utf-8')
            except UnicodeDecodeError:
                with self.lock:
                    self.invalid_packets += 1
                continue
                
            # Stage A functionality preservation
            if payload == "TEST|HELLO_FROM_PHONE|1":
                # We simply print this directly, but since we are doing live terminal refresh,
                # we pad it with newlines and reset the cursor position appropriately if we wanted.
                # Since the test packet is rarely sent during streaming, we'll just print it.
                sys.stdout.write("\n\n----------------------------------------\n")
                sys.stdout.write("Packet received\n\n")
                sys.stdout.write(f"From IP:   {ip}\n")
                sys.stdout.write(f"From Port: {sender_port}\n")
                sys.stdout.write(f"Payload:   {payload}\n")
                sys.stdout.write(f"Bytes:     {len(data)}\n")
                sys.stdout.write("----------------------------------------\n")
                sys.stdout.write("STATUS: Android UDP test packet received successfully.\n\n")
                sys.stdout.flush()
                continue
                
            # Stage B
            packet = parse_steer_packet(payload)
            with self.lock:
                if not packet:
                    self.invalid_packets += 1
                    continue
                    
                # New phone source check
                if self.phone_ip != ip:
                    self.phone_ip = ip
                    self.phone_port = sender_port
                    self.latest_sequence = packet.sequence
                    self.estimated_missing = 0
                
                self.phone_port = sender_port
                self.current_angle = packet.angle
                self.current_throttle = packet.throttle
                self.last_packet_time = time.monotonic()
                self._pps_counter += 1
                
                # Sequence tracking
                if self.latest_sequence != -1:
                    if packet.sequence < self.latest_sequence:
                        # Sender restart or reset
                        self.latest_sequence = packet.sequence
                    elif packet.sequence > self.latest_sequence + 1:
                        # Gap detected
                        self.estimated_missing += (packet.sequence - self.latest_sequence - 1)
                        self.latest_sequence = packet.sequence
                    elif packet.sequence == self.latest_sequence + 1:
                        self.latest_sequence = packet.sequence
                else:
                    self.latest_sequence = packet.sequence
                    
    def update_pps(self):
        with self.lock:
            now = time.monotonic()
            if now - self._pps_last_time >= 1.0:
                self.packets_sec = self._pps_counter
                self._pps_counter = 0
                self._pps_last_time = now

    def stop(self):
        self._running = False
        if self._thread:
            self._thread.join(timeout=1.0)
        if self.sock:
            self.sock.close()
            print("\nSocket closed successfully.")
