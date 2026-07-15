import time

class VirtualController:
    """
    Manages the virtual Xbox 360 controller using the vgamepad library.
    Normalizes the -90 to +90 degree steering angle to the -1.0 to +1.0
    joystick axis range, and provides fail-safe centering.
    """
    def __init__(self):
        self.available = False
        self.gamepad = None
        self.current_normalized_x = 0.0
        self.is_centered = True
        
        try:
            import vgamepad as vg
            self.gamepad = vg.VX360Gamepad()
            self.available = True
            self.center()
        except ImportError:
            print("ERROR: Could not initialize virtual Xbox controller.")
            print("Check that the required Python package (vgamepad) is installed.")
        except Exception as e:
            print("ERROR: Could not initialize virtual Xbox controller.")
            print("Check that the Windows virtual gamepad driver (ViGEmBus) is installed.")
            
    def set_steering_angle(self, angle: float):
        """
        Takes a raw steering angle in degrees (-90.0 to 90.0)
        and applies it to the virtual left-stick X-axis.
        """
        if not self.available:
            return
            
        # Proportional mapping: -90 -> -1.0, 90 -> 1.0
        normalized = angle / 90.0
        
        # Defensive clamp
        normalized = max(-1.0, min(1.0, normalized))
        
        self.current_normalized_x = normalized
        self.is_centered = False
        
        self.gamepad.left_joystick_float(x_value_float=normalized, y_value_float=0.0)
        self.gamepad.update()
        
    def center(self):
        """
        Immediately returns the virtual left stick to the center (0.0, 0.0).
        Used for startup, shutdown, and the 250ms fail-safe timeout.
        """
        if not self.available:
            return
            
        self.current_normalized_x = 0.0
        self.is_centered = True
        
        self.gamepad.left_joystick_float(x_value_float=0.0, y_value_float=0.0)
        self.gamepad.update()
        
    def stop(self):
        """
        Cleans up the controller on shutdown, ensuring it is centered.
        """
        if self.available:
            self.center()
            # Explicit cleanup if needed by library, though usually garbage collection handles it.
            try:
                del self.gamepad
            except Exception:
                pass
            self.gamepad = None
            self.available = False
