package game;

import game.engine.*;

/**
 * Manages camera view modes: first-person, third-person, etc.
 * Updates the renderer camera position and rotation each frame.
 */
public class CameraController
{
    private static final double THIRD_PERSON_DISTANCE = 20;
    private static final double CAMERA_BEHIND_OFFSET = 12;
    private static final double CAMERA_UP_OFFSET = -4;

    private boolean thirdPerson = false;

    public void reset()
    {
        thirdPerson = false;
    }

    public void updateRendererCamera(Vector3 position, Quaternion rotation)
    {
        if (thirdPerson)
        {
            Vector3 back = EngineUtil.quaternionToDirection(rotation).multiply(-THIRD_PERSON_DISTANCE);
            Vector3 camPos = position.plus(back);
            Renderer.renderer.updateCamera(camPos, rotation);
        }
        else
        {
            // Offset camera behind and above player so the player model is visible
            Vector3 forward = EngineUtil.quaternionToDirection(rotation);
            Vector3 up = rotation.rotate(new Vector3(0, 1, 0));
            Vector3 camPos = position.plus(forward.multiply(-CAMERA_BEHIND_OFFSET)).plus(up.multiply(CAMERA_UP_OFFSET));
            Renderer.renderer.updateCamera(camPos, rotation);
        }

        Renderer.renderer.spdPercentage = 0;
    }

    public void toggleView()
    {
        thirdPerson = !thirdPerson;
    }

    public boolean isThirdPerson()
    {
        return thirdPerson;
    }
}
