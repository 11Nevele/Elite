package game.engine;

/**
 * Represents a quaternion for 3D rotations.
 * Quaternions provide a more robust way to handle rotations compared to Euler angles.
 */
public class Quaternion 
{
    /**
     * Creates a quaternion for pitch rotation (X axis)
     * @param theta Rotation angle in degrees
     * @return Quaternion representing the pitch rotation
     */
    public static Quaternion PITCH(double theta)
    {
        return new QuaternionT(theta, new Vector3(1,0,0)).asQuaternion();
    }

    /**
     * Creates a quaternion for yaw rotation (Y axis)
     * @param theta Rotation angle in degrees
     * @return Quaternion representing the yaw rotation
     */
    public static Quaternion YAW(double theta)
    {
        return new QuaternionT(theta, new Vector3(0,1,0)).asQuaternion();
    }
    
    /**
     * Creates a quaternion for roll rotation (Z axis)
     * @param theta Rotation angle in degrees
     * @return Quaternion representing the roll rotation
     */
    public static Quaternion ROLL(double theta)
    {
        return new QuaternionT(theta, new Vector3(0,0,1)).asQuaternion();
    }

    /** Quaternion components */
    public double x,y,z,w;

    /**
     * Default constructor - creates identity quaternion
     */
    public Quaternion() 
    {
        x = 0f; y = 0f; z = 0f; w = 1f;
    }

    /**
     * Constructor with explicit components
     * @param nx X component
     * @param ny Y component
     * @param nz Z component
     * @param na W component
     */
    public Quaternion(double nx,double ny, double nz,double na)
    {
        x = nx;
        y = ny;
        z = nz;
        w = na;
    }
    
    /**
     * Copy constructor
     * @param q Quaternion to copy
     */
    public Quaternion(Quaternion q)
    {
        x = q.x;
        y = q.y;
        z = q.z;
        w = q.w;
    }

    /**
     * Multiplies this quaternion by another quaternion
     * @param other The right-hand quaternion in the multiplication
     * @return Result of quaternion multiplication
     */
    public Quaternion x(Quaternion other)
    {
        double nx = w * other.x + x * other.w + y * other.z - z * other.y;
        double ny = w * other.y - x * other.z + y * other.w + z * other.x;
        double nz = w * other.z + x * other.y - y * other.x + z * other.w;
        double na = w * other.w - x * other.x - y * other.y - z * other.z;
        return new Quaternion(nx,ny,nz,na);
    }
    
    /**
     * Gets the vector part (x,y,z) of this quaternion
     * @return Vector3 containing the vector part
     */
    public Vector3 asVector3()
    {
        return new Vector3(x,y,z);
    }
    
    /**
     * Gets the conjugate of this quaternion
     * @return Conjugate quaternion
     */
    public Quaternion Conjugate()
    {
        return new Quaternion(-x,-y,-z,w);
    }
    
    /**
     * Creates a quaternion from axis-angle representation
     * @param axis Rotation axis (should be normalized)
     * @param angle Rotation angle in degrees
     * @return Quaternion representing the rotation
     */
    public static Quaternion fromAxisAngle(Vector3 axis, double angle) {
        angle = (double) Math.toRadians(angle); // Convert angle to radians
        double halfAngle = angle / 2;
        double sinHalfAngle = (double) Math.sin(halfAngle);

        return new Quaternion(
            axis.x * sinHalfAngle,
            axis.y * sinHalfAngle,
            axis.z * sinHalfAngle,
            (double) Math.cos(halfAngle)
        );
    }
    
    /**
     * Creates a quaternion from Euler angles
     * @param pitch Pitch angle in radians
     * @param yaw Yaw angle in radians
     * @param roll Roll angle in radians
     * @return Quaternion representing the rotation
     */
    public static Quaternion fromEuler(double pitch, double yaw, double roll) {
        double cy = (double) Math.cos(yaw * 0.5);
        double sy = (double) Math.sin(yaw * 0.5);
        double cp = (double) Math.cos(pitch * 0.5);
        double sp = (double) Math.sin(pitch * 0.5);
        double cr = (double) Math.cos(roll * 0.5);
        double sr = (double) Math.sin(roll * 0.5);

        double w = cr * cp * cy + sr * sp * sy;
        double x = sr * cp * cy - cr * sp * sy;
        double y = cr * sp * cy + sr * cp * sy;
        double z = cr * cp * sy - sr * sp * cy;

        return new Quaternion(x, y, z, w);
    }
    
    /**
     * Converts this quaternion to Euler angles
     * @return Vector3 containing pitch, yaw, and roll in radians
     */
    public Vector3 asEuler()
    {
        double pitch = (double)Math.asin(2 * (w * y - z * x));
        double yaw = (double)Math.atan2(2 * (w * z + x * y), 1 - 2 * (y * y + z * z));
        double roll = (double)Math.atan2(2 * (w * x + y * z), 1 - 2 * (x * x + y * y));
        return new Vector3(pitch, yaw, roll);
    }

    /**
     * Rotates a vector by this quaternion
     * @param v Vector to rotate
     * @return Rotated vector
     */
    public Vector3 rotate(Vector3 v) 
    {
        Quaternion q = this;
        Quaternion vQuat = new Quaternion(v.x, v.y, v.z, 0);
        Quaternion qConjugate = new Quaternion(-q.x, -q.y, -q.z, q.w);

        Quaternion result = q.multiply(vQuat).multiply(qConjugate);
        return new Vector3(result.x, result.y, result.z);
    }

    /**
     * Multiplies this quaternion by another quaternion
     * @param other The right-hand quaternion in the multiplication
     * @return Result of quaternion multiplication
     */
    public Quaternion multiply(Quaternion other) {
        double newW = w * other.w - x * other.x - y * other.y - z * other.z;
        double newX = w * other.x + x * other.w + y * other.z - z * other.y;
        double newY = w * other.y - x * other.z + y * other.w + z * other.x;
        double newZ = w * other.z + x * other.y - y * other.x + z * other.w;

        return new Quaternion(newX, newY, newZ, newW);
    }
    
    /**
     * Normalizes this quaternion
     * @return Normalized quaternion
     */
    public Quaternion normalize() {
        double magnitude = (double) Math.sqrt(x * x + y * y + z * z + w * w);
        return new Quaternion(x / magnitude, y / magnitude, z / magnitude, w / magnitude);
    }
}
