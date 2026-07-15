import unittest
from unittest.mock import MagicMock
import sys

# Mock vgamepad so the tests run even without the driver/library installed
sys.modules['vgamepad'] = MagicMock()

from virtual_controller import VirtualController

class TestVirtualController(unittest.TestCase):
    def setUp(self):
        self.controller = VirtualController()
        # Force it to available so we can test the normalization logic
        self.controller.available = True
        self.controller.gamepad = MagicMock()

    def test_startup_centered(self):
        # We manually centered in __init__, but let's test the center() method
        self.controller.center()
        self.assertEqual(self.controller.current_normalized_x, 0.0)
        self.assertTrue(self.controller.is_centered)
        self.controller.gamepad.left_joystick_float.assert_called_with(x_value_float=0.0, y_value_float=0.0)

    def test_proportional_mapping(self):
        test_cases = [
            (-90.0, -1.0),
            (-60.0, -0.6666666666666666),
            (-45.0, -0.5),
            (-30.0, -0.3333333333333333),
            (0.0, 0.0),
            (30.0, 0.3333333333333333),
            (45.0, 0.5),
            (60.0, 0.6666666666666666),
            (90.0, 1.0)
        ]

        for angle, expected_normalized in test_cases:
            with self.subTest(angle=angle):
                self.controller.set_steering_angle(angle)
                self.assertAlmostEqual(self.controller.current_normalized_x, expected_normalized, places=5)
                self.controller.gamepad.left_joystick_float.assert_called_with(x_value_float=self.controller.current_normalized_x, y_value_float=0.0)

    def test_clamping(self):
        self.controller.set_steering_angle(-120.0)
        self.assertEqual(self.controller.current_normalized_x, -1.0)
        
        self.controller.set_steering_angle(130.0)
        self.assertEqual(self.controller.current_normalized_x, 1.0)

if __name__ == '__main__':
    unittest.main()
