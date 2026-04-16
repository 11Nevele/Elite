package game.engine;

/**
 * Represents a 2D vector with x and y components.
 * Used primarily for screen coordinates after projection.
 */
public class Vector2 
{
    private double x;
    private double y;

    public Vector2()
    {
        x = 0;
        y = 0;
    }

    public Vector2(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    public Vector2 minus(Vector2 v)
    {
        return new Vector2(x - v.x, y - v.y);
    }

    public Vector2 plus(Vector2 v)
    {
        return new Vector2(x + v.x, y + v.y);
    }

    public double dot(Vector2 v)
    {
        return x * v.x + y * v.y;
    }

    public Vector2 multiply(double scale)
    {
        return new Vector2(scale * x, scale * y);
    }

    public double magnitude()
    {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2 normalize()
    {
        double mag = magnitude();
        if (mag == 0) return new Vector2();
        return new Vector2(x / mag, y / mag);
    }
}
