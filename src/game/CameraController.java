package game;

import game.engine.*;

/**
 * Keeps the camera in a fixed chase position behind the player ship.
 */
public class CameraController
{
    private static final double CAMERA_BEHIND_OFFSET = 50;
    private static final double CAMERA_UP_OFFSET = -16;

    public void reset()
    {
    }

    public void updateRendererCamera(Vector3 position, Quaternion rotation)
    {
        Vector3 forward = EngineUtil.quaternionToDirection(rotation);
        Vector3 up = rotation.rotate(new Vector3(0, 1, 0));
        Vector3 camPos = position.plus(forward.multiply(-CAMERA_BEHIND_OFFSET)).plus(up.multiply(CAMERA_UP_OFFSET));
        Renderer.renderer.updateCamera(camPos, rotation);
        Renderer.renderer.spdPercentage = 0;
    }
}
