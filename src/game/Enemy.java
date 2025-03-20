package game;

import game.engine.CollidableRenderable;
import game.engine.EngineUtil;
import game.engine.Face;
import game.engine.Quaternion;
import game.engine.QuaternionT;
import game.engine.Renderer;
import game.engine.Vector3;
import java.util.Random;

public class Enemy extends CollidableRenderable
{
    public Enemy()
    {
        super();
    }
    public Enemy(Face[] newModel) 
    {
        super(newModel);
    }
    final double spd = 300;
    final double turnSpd = 90f;
    Quaternion rotationVelocity = new Quaternion();
    long nextTime = 0;
    public Quaternion Rand(double radius)
    {
        Vector3 v = new Vector3();
        Random random = new Random();
        double theta = 2 * (double)Math.PI * random.nextDouble(); // Azimuthal angle
        double cosPhi = 2 * random.nextDouble() - 1; // Uniformly sample cos(phi)
        double phi = (double)Math.acos(cosPhi); // Compute phi
        
        v.x = radius * (double)Math.cos(theta) * (double)Math.sin(phi);
        v.y = radius * (double)Math.sin(theta) * (double)Math.sin(phi);
        v.z = radius * (double)Math.cos(phi);
        return new QuaternionT((double)Math.random() * turnSpd, v).asQuaternion();
    }
    @Override
    public void Update(double delta)
    {
        super.Update(delta);
        //gettime
        if(System.currentTimeMillis() >= nextTime)
        {
            long wait = new Random().nextInt(3000) + 2000;
            nextTime = System.currentTimeMillis() + wait;
            rotationVelocity = Rand(turnSpd);
        }
        else
        {
            //scale velocity down by delta
            QuaternionT deltaQ = new QuaternionT(rotationVelocity);
            deltaQ.theta *= delta;
            rotation = rotation.multiply(deltaQ.asQuaternion());
        }
        //change the velocity vector to the rotation of the enemy
        Vector3 v = EngineUtil.quaternionToDirection(rotation);
        v.x *= spd * delta;
        v.y *= spd * delta;
        v.z *= spd * delta;
        
        position = position.plus(v);
        Renderer.renderer.Render(this);
    }
    
}
