import unittest
from packet_parser import parse_steer_packet

class TestPacketParser(unittest.TestCase):
    def test_valid_control_packets(self):
        packet = parse_steer_packet("CONTROL|0.000|0.000|1")
        self.assertIsNotNone(packet)
        self.assertEqual(packet.angle, 0.0)
        self.assertEqual(packet.throttle, 0.0)
        self.assertEqual(packet.sequence, 1)

        packet = parse_steer_packet("CONTROL|-45.000|0.500|2")
        self.assertIsNotNone(packet)
        self.assertEqual(packet.angle, -45.0)
        self.assertEqual(packet.throttle, 0.5)
        self.assertEqual(packet.sequence, 2)

        packet = parse_steer_packet("CONTROL|90.000|1.000|3")
        self.assertIsNotNone(packet)
        self.assertEqual(packet.angle, 90.0)
        self.assertEqual(packet.throttle, 1.0)
        self.assertEqual(packet.sequence, 3)

    def test_invalid_control_packets(self):
        # Angle out of bounds
        self.assertIsNone(parse_steer_packet("CONTROL|120.000|0.500|4"))
        
        # Throttle out of bounds
        self.assertIsNone(parse_steer_packet("CONTROL|45.000|-0.100|5"))
        self.assertIsNone(parse_steer_packet("CONTROL|45.000|1.100|6"))
        
        # Malformed strings
        self.assertIsNone(parse_steer_packet("CONTROL|abc|0.500|7"))
        self.assertIsNone(parse_steer_packet("CONTROL|45.000|abc|8"))
        self.assertIsNone(parse_steer_packet("CONTROL|45.000|0.500|abc"))
        
        # Missing or extra fields
        self.assertIsNone(parse_steer_packet("CONTROL"))
        self.assertIsNone(parse_steer_packet("CONTROL|45.000"))
        self.assertIsNone(parse_steer_packet("CONTROL|45.000|0.500"))
        self.assertIsNone(parse_steer_packet("CONTROL|45.000|0.500|100|EXTRA"))

    def test_backward_compatibility(self):
        packet = parse_steer_packet("STEER|45.000|100")
        self.assertIsNotNone(packet)
        self.assertEqual(packet.angle, 45.0)
        self.assertEqual(packet.throttle, 0.0)
        self.assertEqual(packet.sequence, 100)

if __name__ == '__main__':
    unittest.main()
