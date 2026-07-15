from dataclasses import dataclass
from typing import Optional

@dataclass
class SteerPacket:
    angle: float
    sequence: int

def parse_steer_packet(payload: str) -> Optional[SteerPacket]:
    """
    Parses a STEER|angle|sequence packet.
    Returns SteerPacket if valid, None if invalid.
    """
    parts = payload.split('|')
    if len(parts) != 3:
        return None
        
    if parts[0] != "STEER":
        return None
        
    try:
        angle = float(parts[1])
        sequence = int(parts[2])
    except ValueError:
        return None
        
    # Validate angle range
    if not (-90.000 <= angle <= 90.000):
        return None
        
    return SteerPacket(angle, sequence)
