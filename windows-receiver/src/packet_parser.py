from dataclasses import dataclass
from typing import Optional

@dataclass
class ControlPacket:
    angle: float
    throttle: float
    brake: float
    sequence: int

def parse_steer_packet(payload: str) -> Optional[ControlPacket]:
    """
    Parses a CONTROL|angle|throttle|brake|sequence or STEER|angle|sequence packet.
    Returns ControlPacket if valid, None if invalid.
    """
    parts = payload.split('|')
    if len(parts) == 5 and parts[0] == "CONTROL":
        try:
            angle = float(parts[1])
            throttle = float(parts[2])
            brake = float(parts[3])
            sequence = int(parts[4])
        except ValueError:
            return None
    elif len(parts) == 4 and parts[0] == "CONTROL": # Fallback for old format
        try:
            angle = float(parts[1])
            throttle = float(parts[2])
            brake = 0.0
            sequence = int(parts[3])
        except ValueError:
            return None
    elif len(parts) == 3 and parts[0] == "STEER":
        try:
            angle = float(parts[1])
            throttle = 0.0
            brake = 0.0
            sequence = int(parts[2])
        except ValueError:
            return None
    else:
        return None
        
    # Validate ranges
    if not (-90.000 <= angle <= 90.000):
        return None
    if not (0.000 <= throttle <= 1.000):
        return None
    if not (0.000 <= brake <= 1.000):
        return None
        
    return ControlPacket(angle, throttle, brake, sequence)
