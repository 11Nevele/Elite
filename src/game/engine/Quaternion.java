package game.engine;

public class Quaternion 
{
    public static Quaternion PITCH(double theta)
    {
        return new QuaternionT(theta, new Vector3(1,0,0)).asQuaternion();
    }

    public static Quaternion YAW(double theta)
    {
        return new QuaternionT(theta, new Vector3(0,1,0)).asQuaternion();
    }
    public static Quaternion ROLL(double theta)
    {
        return new QuaternionT(theta, new Vector3(0,0,1)).asQuaternion();
    }


    public double x,y,z,w;

    public Quaternion() 
    {
        x = 0f; y = 0f; z = 0f; w = 1f;
    }

    public Quaternion(double nx,double ny, double nz,double na)
    {
        x = nx;
        y = ny;
        z = nz;
        w = na;
    }
    public Quaternion(Quaternion q)
    {
        x = q.x;
        y = q.y;
        z = q.z;
        w = q.w;
    }

    public Quaternion x(Quaternion other)
    {
        double nx = w * other.x + x * other.w + y * other.z - z * other.y;
        double ny = w * other.y - x * other.z + y * other.w + z * other.x;
        double nz = w * other.z + x * other.y - y * other.x + z * other.w;
        double na = w * other.w - x * other.x - y * other.y - z * other.z;
        return new Quaternion(nx,ny,nz,na);
    }
    
    public Vector3 asVector3()
    {
        return new Vector3(x,y,z);
    }
    public Quaternion Conjugate()
    {
        return new Quaternion(-x,-y,-z,w);
    }
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
    public Vector3 asEuler()
    {
        double pitch = (double)Math.asin(2 * (w * y - z * x));
        double yaw = (double)Math.atan2(2 * (w * z + x * y), 1 - 2 * (y * y + z * z));
        double roll = (double)Math.atan2(2 * (w * x + y * z), 1 - 2 * (x * x + y * y));
        return new Vector3(pitch, yaw, roll);
    }

    public Vector3 rotate(Vector3 v) 
    {
        Quaternion q = this;
        Quaternion vQuat = new Quaternion(v.x, v.y, v.z, 0);
        Quaternion qConjugate = new Quaternion(-q.x, -q.y, -q.z, q.w);

        Quaternion result = q.multiply(vQuat).multiply(qConjugate);
        return new Vector3(result.x, result.y, result.z);
    }

    public Quaternion multiply(Quaternion other) {
        double newW = w * other.w - x * other.x - y * other.y - z * other.z;
        double newX = w * other.x + x * other.w + y * other.z - z * other.y;
        double newY = w * other.y - x * other.z + y * other.w + z * other.x;
        double newZ = w * other.z + x * other.y - y * other.x + z * other.w;

        return new Quaternion(newX, newY, newZ, newW);
    }
    public Quaternion normalize() {
        double magnitude = (double) Math.sqrt(x * x + y * y + z * z + w * w);
        return new Quaternion(x / magnitude, y / magnitude, z / magnitude, w / magnitude);
    }

    
}
