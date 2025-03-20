package game.engine;
//quaternion in theta x y z
public class QuaternionT 
{
    
    public double theta;
    public Vector3 axis;
    public QuaternionT()
    {
        theta = 0;
        axis = new Vector3();
    }
    public QuaternionT(double nTheta, Vector3 nAxis)
    {
        theta = nTheta;
        axis = nAxis;
    }
    public QuaternionT(QuaternionT q)
    {
        theta = q.theta;
        axis = new Vector3(q.axis);
    }
    public QuaternionT(Quaternion q)
    {
        Vector3 v = q.asVector3();
        theta = (double)Math.acos(q.w) * 2;
        axis = v.multi(1/(double)Math.sin(theta/2));
    }
    public Quaternion asQuaternion()
    {
        return Quaternion.fromAxisAngle(axis, theta);
    }
    public QuaternionT x(QuaternionT other)
    {
        Quaternion q1 = this.asQuaternion();
        Quaternion q2 = other.asQuaternion();
        Quaternion q = q1.x(q2);
        return new QuaternionT(q);
    }

    
}
