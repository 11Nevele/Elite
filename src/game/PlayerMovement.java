package game;

import game.engine.*;
import java.awt.event.KeyEvent;

/**
 * Manages player movement: acceleration, speed, fuel consumption.
 */
public class PlayerMovement
{
    private static final double MAX_SPEED = 1.0;
    private static final double ACCELERATION = 0.3;
    private static final double DECELERATION = 0.1;
    private static final double FUEL_CONSUMPTION = 0.05;
    private static final double INITIAL_FUEL = 100;
    private static final double ROTATION_SPEED = 100;

    private double speed = 0;
    private double fuel = INITIAL_FUEL;

    public double getSpeed() { return speed; }
    public double getFuel() { return fuel; }
    public double getMaxSpeed() { return MAX_SPEED; }
    public double getInitialFuel() { return INITIAL_FUEL; }

    public void reset()
    {
        speed = 0;
        fuel = INITIAL_FUEL;
    }

    /**
     * Updates movement based on input.
     * @return The velocity vector to apply to the camera position.
     */
    public Vector3 update(double delta, Quaternion rotation)
    {
        if (GameState.gameState.isDead())
        {
            speed = Math.max(0, speed - DECELERATION * delta);
        }
        else
        {
            // Acceleration
            if (Input.input.keys[KeyEvent.VK_W] || Input.input.keys[KeyEvent.VK_UP])
            {
                speed = Math.min(MAX_SPEED, speed + ACCELERATION * delta);
            }
            else
            {
                speed = Math.max(0, speed - DECELERATION * delta);
            }

            // Fuel consumption
            if (speed > 0)
            {
                fuel -= FUEL_CONSUMPTION * delta * speed;
                if (fuel <= 0)
                {
                    fuel = 0;
                    GameState.gameState.setNoFuel(true);
                }
            }
        }

        Vector3 forward = EngineUtil.quaternionToDirection(rotation);
        return forward.multiply(speed * 200 * delta);
    }

    /**
     * Updates rotation based on input.
     * @return Quaternion delta to apply to camera rotation.
     */
    public Quaternion getRotationDelta(double delta)
    {
        if (GameState.gameState.isDead()) return new Quaternion();

        Quaternion rotDelta = new Quaternion();

        if (Input.input.keys[KeyEvent.VK_A] || Input.input.keys[KeyEvent.VK_LEFT])
        {
            rotDelta = rotDelta.multiply(Quaternion.yaw(-ROTATION_SPEED * delta));
        }
        if (Input.input.keys[KeyEvent.VK_D] || Input.input.keys[KeyEvent.VK_RIGHT])
        {
            rotDelta = rotDelta.multiply(Quaternion.yaw(ROTATION_SPEED * delta));
        }
        if (Input.input.keys[KeyEvent.VK_S] || Input.input.keys[KeyEvent.VK_DOWN])
        {
            rotDelta = rotDelta.multiply(Quaternion.pitch(-ROTATION_SPEED * delta));
        }
        if (Input.input.keys[KeyEvent.VK_SHIFT])
        {
            rotDelta = rotDelta.multiply(Quaternion.pitch(ROTATION_SPEED * delta));
        }
        if (Input.input.keys[KeyEvent.VK_Q])
        {
            rotDelta = rotDelta.multiply(Quaternion.roll(-ROTATION_SPEED * delta));
        }
        if (Input.input.keys[KeyEvent.VK_E])
        {
            rotDelta = rotDelta.multiply(Quaternion.roll(ROTATION_SPEED * delta));
        }

        return rotDelta;
    }
}
