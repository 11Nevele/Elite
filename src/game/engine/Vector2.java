package game.engine;

public class Vector2 
{
    public double x;
    public double y;
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
    public double magnitude ()
    {
        return (double)Math.sqrt(x*x + y*y);
    }
    public Vector2 normalize()
    {
        double mag = magnitude();
        return new Vector2(x/mag, y/mag);
    }  
}
