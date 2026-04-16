package game.engine;

/**
 * Represents a 3D vector with x, y, and z components.
 * Provides common vector operations.
 */
public class Vector3 
{
    private double x;
    private double y;
    private double z;

    public Vector3()
    {
        x = 0; y = 0; z = 0;
    }

    public Vector3(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(Vector3 v)
    {
        x = v.x;
        y = v.y;
        z = v.z;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setZ(double z) { this.z = z; }

    public Vector3 minus(Vector3 v)
    {
        return new Vector3(x - v.x, y - v.y, z - v.z);
    }

    public Vector3 plus(Vector3 v)
    {
        return new Vector3(x + v.x, y + v.y, z + v.z);
    }

    public Vector3 cross(Vector3 v)
    {
        return new Vector3(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
    }

    public double dot(Vector3 v)
    {
        return x * v.x + y * v.y + z * v.z;
    }

    public Vector3 multiply(double scale)
    {
        return new Vector3(scale * x, scale * y, scale * z);
    }

    public Vector3 normalize()
    {
        double mag = Math.sqrt(x * x + y * y + z * z);
        if (mag == 0) return new Vector3();
        return new Vector3(x / mag, y / mag, z / mag);
    }

    public double distance(Vector3 v)
    {
        return Math.sqrt((x - v.x) * (x - v.x) + (y - v.y) * (y - v.y) + (z - v.z) * (z - v.z));
    }

    public double magnitude()
    {
        return Math.sqrt(x * x + y * y + z * z);
    }

    // === In-place (mutable) operations for hot paths ===

    public void set(Vector3 v)
    {
        x = v.x;
        y = v.y;
        z = v.z;
    }

    public void set(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void addInPlace(Vector3 v)
    {
        x += v.x;
        y += v.y;
        z += v.z;
    }

    public void addXYZ(double dx, double dy, double dz)
    {
        x += dx;
        y += dy;
        z += dz;
    }

    public void subtractInPlace(Vector3 v)
    {
        x -= v.x;
        y -= v.y;
        z -= v.z;
    }

    public void multiplyInPlace(double scale)
    {
        x *= scale;
        y *= scale;
        z *= scale;
    }
}
