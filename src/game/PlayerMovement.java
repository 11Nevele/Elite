package game;

import game.engine.*;
import java.awt.event.KeyEvent;

/**
 * Manages bounded dodge movement for the rail-shooter prototype.
 */
public class PlayerMovement
{
    private static final double HORIZONTAL_SPEED = 85;
    private static final double VERTICAL_SPEED = 60;
    private static final double MAX_X = 48;
    private static final double MIN_Y = -22;
    private static final double MAX_Y = 26;
    private static final double MAX_BANK_DEGREES = -18;
    private static final double MAX_PITCH_DEGREES = 10;

    private double horizontalInput = 0;
    private double verticalInput = 0;

    public void reset()
    {
        horizontalInput = 0;
        verticalInput = 0;
    }

    /**
     * Returns the bounded dodge movement to apply this frame.
     */
    public Vector3 update(double delta, Vector3 position)
    {
        if (GameState.gameState.isDead())
        {
            horizontalInput = 0;
            verticalInput = 0;
            return new Vector3();
        }

        horizontalInput = getHorizontalAxis();
        verticalInput = getVerticalAxis();

        double nextX = clamp(position.getX() + horizontalInput * HORIZONTAL_SPEED * delta, -MAX_X, MAX_X);
        double nextY = clamp(position.getY() + verticalInput * VERTICAL_SPEED * delta, MIN_Y, MAX_Y);

        return new Vector3(nextX - position.getX(), nextY - position.getY(), 0);
    }

    public Quaternion getShipRotation()
    {
        return Quaternion.pitch(-verticalInput * MAX_PITCH_DEGREES)
            .multiply(Quaternion.roll(-horizontalInput * MAX_BANK_DEGREES));
    }

    private double getHorizontalAxis()
    {
        double axis = 0;
        if (Input.input.keys[KeyEvent.VK_A] || Input.input.keys[KeyEvent.VK_LEFT])
        {
            axis -= 1;
        }
        if (Input.input.keys[KeyEvent.VK_D] || Input.input.keys[KeyEvent.VK_RIGHT])
        {
            axis += 1;
        }
        return axis;
    }

    private double getVerticalAxis()
    {
        double axis = 0;
        if (Input.input.keys[KeyEvent.VK_W] || Input.input.keys[KeyEvent.VK_UP])
        {
            axis -= 1;
        }
        if (Input.input.keys[KeyEvent.VK_S] || Input.input.keys[KeyEvent.VK_DOWN])
        {
            axis += 1;
        }
        return axis;
    }

    private double clamp(double value, double min, double max)
    {
        return Math.max(min, Math.min(max, value));
    }
}
