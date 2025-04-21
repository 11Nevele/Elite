package game.engine;

/**
 * Represents a 2D vector with x and y components.
 * Used primarily for screen coordinates after projection.
 */
public class Vector2 
{
    /** X coordinate */
    public double x;
    
    /** Y coordinate */
    public double y;
    
    /**
     * Default constructor - creates a zero vector
     */
    public Vector2()
    {
        x = 0;
        y = 0;
    }
    
    /**
     * Constructor with coordinates
     * @param x X coordinate
     * @param y Y coordinate
     */
    public Vector2(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Calculates the magnitude (length) of this vector
     * @return Magnitude of the vector
     */
    public double magnitude()
    {
        return (double)Math.sqrt(x*x + y*y);
    }
    
    /**
     * Returns a normalized version of this vector
     * @return Normalized vector with same direction but unit length
     */
    public Vector2 normalize()
    {
        double mag = magnitude();
        return new Vector2(x/mag, y/mag);
    }  
}
