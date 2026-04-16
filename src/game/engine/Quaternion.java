package game.engine;

/**
 * Represents a quaternion for 3D rotations.
 * Quaternions provide a more robust way to handle rotations compared to Euler angles.
 */
public class Quaternion 
{
    private double x, y, z, w;

    public Quaternion()
    {
        x = 0; y = 0; z = 0; w = 1;
    }

    public Quaternion(double x, double y, double z, double w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Quaternion(Quaternion q)
    {
        x = q.x;
        y = q.y;
        z = q.z;
        w = q.w;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public double getW() { return w; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setZ(double z) { this.z = z; }
    public void setW(double w) { this.w = w; }

    /**
     * Creates a quaternion for pitch rotation (X axis)
     * @param theta Rotation angle in degrees
     */
    public static Quaternion pitch(double theta)
    {
        return fromAxisAngle(new Vector3(1, 0, 0), theta);
    }

    /**
     * Creates a quaternion for yaw rotation (Y axis)
     * @param theta Rotation angle in degrees
     */
    public static Quaternion yaw(double theta)
    {
        return fromAxisAngle(new Vector3(0, 1, 0), theta);
    }

    /**
     * Creates a quaternion for roll rotation (Z axis)
     * @param theta Rotation angle in degrees
     */
    public static Quaternion roll(double theta)
    {
        return fromAxisAngle(new Vector3(0, 0, 1), theta);
    }

    /**
     * Creates a quaternion from axis-angle representation.
     * @param axis Rotation axis (should be normalized)
     * @param angleDegrees Rotation angle in degrees
     */
    public static Quaternion fromAxisAngle(Vector3 axis, double angleDegrees)
    {
        double angleRad = Math.toRadians(angleDegrees);
        double halfAngle = angleRad / 2;
        double sinHalf = Math.sin(halfAngle);
        return new Quaternion(
            axis.getX() * sinHalf,
            axis.getY() * sinHalf,
            axis.getZ() * sinHalf,
            Math.cos(halfAngle)
        );
    }

    /**
     * Creates a quaternion from Euler angles.
     * @param pitch Pitch angle in radians
     * @param yaw Yaw angle in radians
     * @param roll Roll angle in radians
     */
    public static Quaternion fromEuler(double pitch, double yaw, double roll)
    {
        double cy = Math.cos(yaw * 0.5);
        double sy = Math.sin(yaw * 0.5);
        double cp = Math.cos(pitch * 0.5);
        double sp = Math.sin(pitch * 0.5);
        double cr = Math.cos(roll * 0.5);
        double sr = Math.sin(roll * 0.5);

        return new Quaternion(
            sr * cp * cy - cr * sp * sy,
            cr * sp * cy + sr * cp * sy,
            cr * cp * sy - sr * sp * cy,
            cr * cp * cy + sr * sp * sy
        );
    }

    /**
     * Multiplies this quaternion by another quaternion.
     */
    public Quaternion multiply(Quaternion other)
    {
        return new Quaternion(
            w * other.x + x * other.w + y * other.z - z * other.y,
            w * other.y - x * other.z + y * other.w + z * other.x,
            w * other.z + x * other.y - y * other.x + z * other.w,
            w * other.w - x * other.x - y * other.y - z * other.z
        );
    }

    /**
     * Gets the vector part (x,y,z) of this quaternion.
     */
    public Vector3 asVector3()
    {
        return new Vector3(x, y, z);
    }

    /**
     * Gets the conjugate of this quaternion.
     */
    public Quaternion conjugate()
    {
        return new Quaternion(-x, -y, -z, w);
    }

    /**
     * Converts this quaternion to Euler angles.
     * @return Vector3 containing pitch, yaw, and roll in radians
     */
    public Vector3 asEuler()
    {
        double pitch = Math.asin(2 * (w * y - z * x));
        double yaw = Math.atan2(2 * (w * z + x * y), 1 - 2 * (y * y + z * z));
        double roll = Math.atan2(2 * (w * x + y * z), 1 - 2 * (x * x + y * y));
        return new Vector3(pitch, yaw, roll);
    }

    /**
     * Rotates a vector by this quaternion.
     */
    public Vector3 rotate(Vector3 v)
    {
        Quaternion vQuat = new Quaternion(v.getX(), v.getY(), v.getZ(), 0);
        Quaternion qConj = new Quaternion(-x, -y, -z, w);
        Quaternion result = this.multiply(vQuat).multiply(qConj);
        return new Vector3(result.x, result.y, result.z);
    }

    /**
     * Scales the rotation represented by this quaternion by the given factor.
     * Extracts axis-angle, multiplies angle by factor, and converts back.
     * Used to apply a fraction of a rotation (e.g. for delta-time scaling).
     */
    public Quaternion scaleRotation(double factor)
    {
        double theta = Math.acos(Math.max(-1, Math.min(1, w))) * 2;
        double sinHalf = Math.sin(theta / 2);
        Vector3 axis;
        if (sinHalf == 0)
        {
            axis = new Vector3(0, 0, 1);
        }
        else
        {
            axis = asVector3().multiply(1.0 / sinHalf);
        }
        // Note: theta is in radians, fromAxisAngle expects degrees
        return fromAxisAngle(axis, Math.toDegrees(theta) * factor);
    }

    /**
     * Normalizes this quaternion.
     */
    public Quaternion normalize()
    {
        double mag = Math.sqrt(x * x + y * y + z * z + w * w);
        if (mag == 0) return new Quaternion();
        return new Quaternion(x / mag, y / mag, z / mag, w / mag);
    }
}
