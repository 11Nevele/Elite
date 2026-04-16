package game;

import game.engine.*;
import java.awt.Color;

/**
 * A star object used for background scenery. Rendered as a 2D point for performance.
 */
public class Star extends GameObject
{
    private double orbitRadius;
    private Color color;
    private int size;

    public Star(double orbitRadius)
    {
        super();
        this.orbitRadius = orbitRadius;
        position = EngineUtil.randomOnSphere(orbitRadius);
        // Random tint between white and yellow
        if (Math.random() > 0.5)
            color = GameColors.STAR_WHITE;
        else
            color = GameColors.STAR_YELLOW;
        size = (Math.random() > 0.7) ? 3 : 2;
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);
        Renderer.renderer.renderPoint(this.position, color, size);
    }
}
