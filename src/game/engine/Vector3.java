package game.engine;

/**
 * Represents a 3D vector with x, y, and z components.
 * Provides common vector operations.
 */
public class Vector3 
{
    /** X coordinate */
    public double x;
    
    /** Y coordinate */
    public double y;
    
    /** Z coordinate */
    public double z;
    
    /**
     * Default constructor - creates a zero vector
     */
    public Vector3()
    {
        x = 0 ; y = 0; z = 0;
    }
    
    /**
     * Constructor with coordinates
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Copy constructor
     * @param v Vector to copy
     */
    public Vector3(Vector3 v)
    {
        x = v.x;
        y = v.y;
        z = v.z;
    }
    
    /**
     * Subtracts another vector from this one
     * @param v Vector to subtract
     * @return Result of subtraction
     */
    public Vector3 minus(Vector3 v)
    {
        return new Vector3(x - v.x, y - v.y, z - v.z);
    }
    
    /**
     * Adds another vector to this one
     * @param v Vector to add
     * @return Result of addition
     */
    public Vector3 plus(Vector3 v)
    {
        return new Vector3(x + v.x, y + v.y, z + v.z);
    }
    
    /**
     * Computes the cross product of this vector with another
     * @param v Other vector
     * @return Cross product
     */
    public Vector3 cross(Vector3 v)
    {
        return new Vector3(y*v.z - z*v.y, z*v.x - x*v.z, x*v.y - y*v.x);
    }
    
    /**
     * Computes the dot product of this vector with another
     * @param v Other vector
     * @return Dot product
     */
    public double dot(Vector3 v)
    {
        return x*v.x + y*v.y + z*v.z;
    }
    
    /**
     * Multiplies this vector by a scalar
     * @param scale Scalar factor
     * @return Scaled vector
     */
    public Vector3 multi(double scale)
    {
        return new Vector3(scale * x, scale * y, scale * z);
    }

    /**
     * Returns a normalized version of this vector
     * @return Normalized vector with same direction but unit length
     */
    public Vector3 normalize()
    {
        double mag = (double)Math.sqrt(x*x + y*y + z*z);
        return new Vector3(x/mag, y/mag, z/mag);
    }
    
    /**
     * Calculates the distance to another vector
     * @param v Other vector
     * @return Distance between the points
     */
    public double distance(Vector3 v)
    {
        return (double)Math.sqrt((x-v.x)*(x-v.x) + (y-v.y)*(y-v.y) + (z-v.z)*(z-v.z));
    }
    
    /**
     * Calculates the magnitude (length) of this vector
     * @return Magnitude of the vector
     */
    public double magnitude()
    {
        return (double)Math.sqrt(x*x + y*y + z*z);
    }
}
