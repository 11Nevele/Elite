package game.engine;
public class Vector3 
{
    public double x,y,z;
    public Vector3()
    {
        x = 0 ; y = 0; z = 0;
    }
    public Vector3(double x, double y, double z) {
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
        return new Vector3(y*v.z - z*v.y, z*v.x - x*v.z, x*v.y - y*v.x);
    }
    public double dot(Vector3 v)
    {
        return x*v.x + y*v.y + z*v.z;
    }
    public Vector3 multi(double scale)
    {
        return new Vector3(scale * x, scale * y, scale * z);
    }

    public Vector3 normalize()
    {
        double mag = (double)Math.sqrt(x*x + y*y + z*z);
        return new Vector3(x/mag, y/mag, z/mag);
    }
    public double distance(Vector3 v)
    {
        return (double)Math.sqrt((x-v.x)*(x-v.x) + (y-v.y)*(y-v.y) + (z-v.z)*(z-v.z));
    }
    public double magnitude()
    {
        return (double)Math.sqrt(x*x + y*y + z*z);
    }

    
}
