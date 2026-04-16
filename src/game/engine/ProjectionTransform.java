package game.engine;

/**
 * Handles 3D-to-2D projection math.
 */
public class ProjectionTransform
{
    private final double screenWidth;
    private final double screenHeight;

    public ProjectionTransform(double screenWidth, double screenHeight)
    {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    /**
     * Projects a 3D point onto the 2D screen.
     */
    public Vector2 project(Vector3 point, double scale)
    {
        if (point.getZ() <= 0)
        {
            return new Vector2(point.getX() * 100000, point.getY() * 100000);
        }
        return new Vector2(
            (point.getX() * scale) / point.getZ() + screenWidth / 2,
            (point.getY() * scale) / point.getZ() + screenHeight / 2
        );
    }
}
