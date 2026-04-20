package game;

import game.engine.*;

/**
 * Keeps the camera in a fixed chase position behind the player ship.
 */
public class CameraController
{
    private static final double CAMERA_BEHIND_OFFSET = 80;
    private static final double CAMERA_UP_OFFSET = 0;
    private static final double MAX_CAMERA_X = 0;
    private static final double MIN_CAMERA_Y = -0;
    private static final double MAX_CAMERA_Y = 0;
    private static final double DEAD_ZONE_HALF_WIDTH = 12;
    private static final double DEAD_ZONE_HALF_HEIGHT = 7;
    private static final double FOLLOW_RESPONSE = 6;
    private static final double TILT_RESPONSE = 8;
    private static final double MAX_CAMERA_BANK_DEGREES = 4.5;
    private static final double MAX_CAMERA_PITCH_DEGREES = 2.5;

    private Vector3 followTarget = new Vector3();
    private Vector3 cameraPosition = new Vector3();
    private double currentBankDegrees = 0;
    private double currentPitchDegrees = 0;
    private boolean initialized = false;

    public void reset()
    {
        followTarget = new Vector3();
        cameraPosition = new Vector3();
        currentBankDegrees = 0;
        currentPitchDegrees = 0;
        initialized = false;
    }

    public void updateRendererCamera(double delta, Vector3 position, Quaternion railRotation, double horizontalInput, double verticalInput)
    {
        Vector3 forward = EngineUtil.quaternionToDirection(railRotation).normalize();
        Vector3 right = railRotation.rotate(new Vector3(1, 0, 0)).normalize();
        Vector3 up = railRotation.rotate(new Vector3(0, 1, 0)).normalize();

        if (!initialized)
        {
            followTarget = new Vector3(position);
            cameraPosition = clampCameraPosition(getCameraTargetPosition(followTarget, forward, up));
            initialized = true;
        }

        followTarget = updateFollowTarget(position, followTarget, forward, right, up);

        Vector3 targetCameraPosition = getCameraTargetPosition(followTarget, forward, up);
        double followBlend = smoothingFactor(FOLLOW_RESPONSE, delta);
        cameraPosition = clampCameraPosition(lerp(cameraPosition, targetCameraPosition, followBlend));

        double tiltBlend = smoothingFactor(TILT_RESPONSE, delta);
        double targetBankDegrees = horizontalInput * MAX_CAMERA_BANK_DEGREES;
        double targetPitchDegrees = -verticalInput * MAX_CAMERA_PITCH_DEGREES;

        currentBankDegrees = lerp(currentBankDegrees, targetBankDegrees, tiltBlend);
        currentPitchDegrees = lerp(currentPitchDegrees, targetPitchDegrees, tiltBlend);

        Quaternion cameraTilt = Quaternion.pitch(currentPitchDegrees)
            .multiply(Quaternion.roll(currentBankDegrees));
        Quaternion cameraRotation = railRotation.multiply(cameraTilt).normalize();

        Renderer.renderer.updateCamera(cameraPosition, cameraRotation);
        Renderer.renderer.spdPercentage = 0;
    }

    private Vector3 updateFollowTarget(Vector3 playerPosition, Vector3 currentTarget, Vector3 forward, Vector3 right, Vector3 up)
    {
        Vector3 offset = playerPosition.minus(currentTarget);
        double forwardOffset = offset.dot(forward);
        double horizontalOffset = offset.dot(right);
        double verticalOffset = offset.dot(up);

        double horizontalCorrection = deadZoneCorrection(horizontalOffset, DEAD_ZONE_HALF_WIDTH);
        double verticalCorrection = deadZoneCorrection(verticalOffset, DEAD_ZONE_HALF_HEIGHT);

        return currentTarget
            .plus(forward.multiply(forwardOffset))
            .plus(right.multiply(horizontalCorrection))
            .plus(up.multiply(verticalCorrection));
    }

    private double deadZoneCorrection(double offset, double halfSize)
    {
        if (offset > halfSize)
        {
            return offset - halfSize;
        }
        if (offset < -halfSize)
        {
            return offset + halfSize;
        }

        return 0;
    }

    private Vector3 getCameraTargetPosition(Vector3 focusPosition, Vector3 forward, Vector3 up)
    {
        return focusPosition.plus(forward.multiply(-CAMERA_BEHIND_OFFSET)).plus(up.multiply(CAMERA_UP_OFFSET));
    }

    private Vector3 clampCameraPosition(Vector3 position)
    {
        return new Vector3(
            clamp(position.getX(), -MAX_CAMERA_X, MAX_CAMERA_X),
            clamp(position.getY(), MIN_CAMERA_Y, MAX_CAMERA_Y),
            position.getZ()
        );
    }

    private Vector3 lerp(Vector3 current, Vector3 target, double factor)
    {
        return new Vector3(
            lerp(current.getX(), target.getX(), factor),
            lerp(current.getY(), target.getY(), factor),
            lerp(current.getZ(), target.getZ(), factor)
        );
    }

    private double lerp(double current, double target, double factor)
    {
        return current + (target - current) * factor;
    }

    private double clamp(double value, double min, double max)
    {
        return Math.max(min, Math.min(max, value));
    }

    private double smoothingFactor(double response, double delta)
    {
        return 1 - Math.exp(-response * delta);
    }
}
