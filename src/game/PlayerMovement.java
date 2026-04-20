package game;

import game.engine.*;
import java.awt.event.KeyEvent;

/**
 * Manages bounded dodge movement for the rail-shooter prototype.
 */
public class PlayerMovement
{
    private static final double HORIZONTAL_ACCELERATION = 260;
    private static final double VERTICAL_ACCELERATION = 220;
    private static final double HORIZONTAL_DRAG = 120;
    private static final double VERTICAL_DRAG = 100;
    private static final double MAX_HORIZONTAL_SPEED = 85;
    private static final double MAX_VERTICAL_SPEED = 60;
    private static final double MAX_X = 80;
    private static final double MIN_Y = -40;
    private static final double MAX_Y = 40;
    private static final double MAX_YAW_DEGREES = 18;
    private static final double MAX_BANK_DEGREES = 30;
    private static final double MAX_PITCH_DEGREES = 30;
    private static final double TILT_RESPONSE = 15;

    private double horizontalInput = 0;
    private double verticalInput = 0;
    private double horizontalVelocity = 0;
    private double verticalVelocity = 0;
    private double currentYawDegrees = 0;
    private double currentBankDegrees = 0;
    private double currentPitchDegrees = 0;

    public void reset()
    {
        horizontalInput = 0;
        verticalInput = 0;
        horizontalVelocity = 0;
        verticalVelocity = 0;
        currentYawDegrees = 0;
        currentBankDegrees = 0;
        currentPitchDegrees = 0;
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
            horizontalVelocity = 0;
            verticalVelocity = 0;
            updateTilt(delta);
            return new Vector3();
        }

        horizontalInput = getHorizontalAxis();
        verticalInput = getVerticalAxis();

        horizontalVelocity = updateAxisVelocity(
            horizontalVelocity,
            horizontalInput,
            HORIZONTAL_ACCELERATION,
            HORIZONTAL_DRAG,
            MAX_HORIZONTAL_SPEED,
            delta
        );
        verticalVelocity = updateAxisVelocity(
            verticalVelocity,
            verticalInput,
            VERTICAL_ACCELERATION,
            VERTICAL_DRAG,
            MAX_VERTICAL_SPEED,
            delta
        );

        double nextX = clamp(position.getX() + horizontalVelocity * delta, -MAX_X, MAX_X);
        double nextY = clamp(position.getY() + verticalVelocity * delta, MIN_Y, MAX_Y);

        if ((nextX <= -MAX_X && horizontalVelocity < 0) || (nextX >= MAX_X && horizontalVelocity > 0))
        {
            horizontalVelocity = 0;
        }
        if ((nextY <= MIN_Y && verticalVelocity < 0) || (nextY >= MAX_Y && verticalVelocity > 0))
        {
            verticalVelocity = 0;
        }

        updateTilt(delta);

        return new Vector3(nextX - position.getX(), nextY - position.getY(), 0);
    }

    public Quaternion getShipRotation()
    {
        return Quaternion.yaw(currentYawDegrees)
            .multiply(Quaternion.pitch(currentPitchDegrees))
            .multiply(Quaternion.roll(currentBankDegrees));
    }

    public double getHorizontalInput()
    {
        return horizontalInput;
    }

    public double getVerticalInput()
    {
        return verticalInput;
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

    private double updateAxisVelocity(double currentVelocity, double input, double acceleration, double drag, double maxSpeed, double delta)
    {
        if (input != 0)
        {
            currentVelocity += input * acceleration * delta;
            return clamp(currentVelocity, -maxSpeed, maxSpeed);
        }

        return moveToward(currentVelocity, 0, drag * delta);
    }

    private void updateTilt(double delta)
    {
        double blend = smoothingFactor(TILT_RESPONSE, delta);
        double targetYawDegrees = horizontalInput * MAX_YAW_DEGREES;
        double targetBankDegrees = horizontalInput * MAX_BANK_DEGREES;
        double targetPitchDegrees = -verticalInput * MAX_PITCH_DEGREES;

        currentYawDegrees = lerp(currentYawDegrees, targetYawDegrees, blend);
        currentBankDegrees = lerp(currentBankDegrees, targetBankDegrees, blend);
        currentPitchDegrees = lerp(currentPitchDegrees, targetPitchDegrees, blend);
    }

    private double moveToward(double current, double target, double maxDelta)
    {
        if (current < target)
        {
            return Math.min(current + maxDelta, target);
        }
        if (current > target)
        {
            return Math.max(current - maxDelta, target);
        }

        return target;
    }

    private double lerp(double current, double target, double factor)
    {
        return current + (target - current) * factor;
    }

    private double smoothingFactor(double response, double delta)
    {
        return 1 - Math.exp(-response * delta);
    }
}
