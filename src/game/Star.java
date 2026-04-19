package game;

import game.engine.*;
import java.awt.Color;

/**
 * A star object used for background scenery. Rendered as a 2D point for performance.
 */
public class Star extends GameObject
{
    private static final double FIELD_WIDTH = 900;
    private static final double FIELD_HEIGHT = 540;
    private static final double BEHIND_BUFFER = 40;

    private final double orbitRadius;
    private final Color color;
    private final int size;

    public Star(double orbitRadius)
    {
        super();
        this.orbitRadius = orbitRadius;
        // Random tint between white and yellow
        if (Math.random() > 0.5)
            color = GameColors.STAR_WHITE;
        else
            color = GameColors.STAR_YELLOW;
        size = (Math.random() > 0.7) ? 3 : 2;

        respawn(new Vector3());
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);

        if (Camera.instance != null && position.getZ() < Camera.instance.position.getZ() + BEHIND_BUFFER)
        {
            respawn(Camera.instance.position);
        }

        Renderer.renderer.renderPoint(this.position, color, size);
    }

    private void respawn(Vector3 anchor)
    {
        position = new Vector3(
            anchor.getX() + (Math.random() - 0.5) * FIELD_WIDTH,
            anchor.getY() + (Math.random() - 0.5) * FIELD_HEIGHT,
            anchor.getZ() + orbitRadius
        );
    }
}
