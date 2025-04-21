package game.engine;

/**
 * Represents a quaternion in axis-angle format (theta and axis).
 * Provides conversion to/from standard quaternion representation.
 */
public class QuaternionT 
{
    /** Rotation angle in degrees */
    public double theta;
    
    /** Rotation axis as a 3D vector */
    public Vector3 axis;
    
    /**
     * Default constructor - creates zero rotation quaternion
     */
    public QuaternionT()
    {
        theta = 0;
        axis = new Vector3();
    }
    
    /**
     * Constructor with angle and axis
     * @param nTheta Rotation angle in degrees
     * @param nAxis Rotation axis
     */
    public QuaternionT(double nTheta, Vector3 nAxis)
    {
        theta = nTheta;
        axis = nAxis;
    }
    
    /**
     * Copy constructor
     * @param q QuaternionT to copy
     */
    public QuaternionT(QuaternionT q)
    {
        theta = q.theta;
        axis = new Vector3(q.axis);
    }
    
    /**
     * Constructs a QuaternionT from a standard quaternion
     * @param q Quaternion to convert
     */
    public QuaternionT(Quaternion q)
    {
        Vector3 v = q.asVector3();
        theta = (double)Math.acos(q.w) * 2;
        axis = v.multi(1/(double)Math.sin(theta/2));
    }
    
    /**
     * Converts this representation to standard quaternion
     * @return Standard quaternion representation
     */
    public Quaternion asQuaternion()
    {
        return Quaternion.fromAxisAngle(axis, theta);
    }
    
    /**
     * Multiplies this quaternion by another quaternion
     * @param other The right-hand quaternion in the multiplication
     * @return Result of quaternion multiplication
     */
    public QuaternionT x(QuaternionT other)
    {
        Quaternion q1 = this.asQuaternion();
        Quaternion q2 = other.asQuaternion();
        Quaternion q = q1.x(q2);
        return new QuaternionT(q);
    }
}
