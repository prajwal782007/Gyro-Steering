import socket

class UdpReceiver:
    def __init__(self, host="0.0.0.0", port=5005):
        self.host = host
        self.port = port
        self.sock = None

    def start(self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        try:
            self.sock.bind((self.host, self.port))
            return True
        except Exception as e:
            print(f"ERROR: Could not bind to UDP port {self.port}.")
            print("The port may already be in use by another application.")
            return False

    def receive_loop(self):
        try:
            while True:
                data, addr = self.sock.recvfrom(1024)
                ip, sender_port = addr
                
                try:
                    payload = data.decode('utf-8')
                except UnicodeDecodeError:
                    print("\nWARNING: Received packet that could not be decoded as UTF-8.")
                    continue

                print("\n----------------------------------------")
                print("Packet received")
                print()
                print(f"From IP:   {ip}")
                print(f"From Port: {sender_port}")
                print(f"Payload:   {payload}")
                print(f"Bytes:     {len(data)}")
                print("----------------------------------------")
                
                if payload == "TEST|HELLO_FROM_PHONE|1":
                    print("STATUS: Android UDP test packet received successfully.")
                    
        except KeyboardInterrupt:
            # Let the caller handle the keyboard interrupt for clean shutdown messages
            raise
        except Exception as e:
            # Handle socket closure gracefully during an interrupt
            if getattr(e, 'errno', None) == 10038:
                pass
            else:
                print(f"\nERROR: Unexpected socket error: {e}")

    def stop(self):
        if self.sock:
            self.sock.close()
            print("Socket closed successfully.")
