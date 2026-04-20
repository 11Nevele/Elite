package game;

import game.engine.*;
import java.awt.Color;

/**
 * A star object used for background scenery. Rendered as a 2D point for performance.
 */
public class Star extends GameObject
{
    private static final double FIELD_WIDTH = 2000;
    private static final double FIELD_HEIGHT = 2000;
    private static final double BEHIND_BUFFER = 40;
    private static final double FORWARD_BUFFER = 160;
    private static final double WRAP_JITTER = 140;
    private static final double STAR_SCROLL_MULTIPLIER = 10;

    private final double wrapDistance;
    private final Color color;
    private final int size;

    public Star(double orbitRadius)
    {
        super();
        wrapDistance = orbitRadius + 900;
        // Random tint between white and yellow
        if (Math.random() > 0.5)
            color = GameColors.STAR_WHITE;
        else
            color = GameColors.STAR_YELLOW;
        size = (Math.random() > 0.7) ? 3 : 2;

        respawnInitial();
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);

        position = position.plus(Camera.getWorldScrollDelta(delta).multiply(STAR_SCROLL_MULTIPLIER));

        if (Camera.instance != null && position.getZ() < Camera.instance.position.getZ() + BEHIND_BUFFER)
        {
            wrapAhead(Camera.instance.position);
        }

        Renderer.renderer.renderPoint(this.position, color, size);
    }

    private void respawnInitial()
    {
        position = new Vector3(
            randomX(0),
            randomY(0),
            FORWARD_BUFFER + Math.random() * wrapDistance
        );
    }

    private void wrapAhead(Vector3 anchor)
    {
        position = new Vector3(
            randomX(anchor.getX()),
            randomY(anchor.getY()),
            position.getZ() + wrapDistance + Math.random() * WRAP_JITTER
        );

        double minDepth = anchor.getZ() + FORWARD_BUFFER;
        while (position.getZ() < minDepth)
        {
            position.setZ(position.getZ() + wrapDistance);
        }
    }

    private double randomX(double anchorX)
    {
        return anchorX + (Math.random() - 0.5) * FIELD_WIDTH;
    }

    private double randomY(double anchorY)
    {
        return anchorY + (Math.random() - 0.5) * FIELD_HEIGHT;
    }
}
