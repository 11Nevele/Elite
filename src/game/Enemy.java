package game;

import game.engine.CollidableRenderable;
import game.engine.EngineUtil;
import game.engine.Face;
import game.engine.GameObject;
import game.engine.Quaternion;
import game.engine.QuaternionT;
import game.engine.Renderer;
import game.engine.Vector3;
import java.util.Random;

/**
 * Represents an enemy ship in the game.
 * Enemies move with random direction changes and can be destroyed by the player.
 */
public class Enemy extends CollidableRenderable
{
    /**
     * Creates a new enemy with the given model
     * @param newModel 3D model for the enemy
     */
    public Enemy(Face[] newModel) 
    {
        super(newModel);
        position = EngineUtil.RandomOnSphere(1).multi(1000 + (Math.random() * 4000));
        this.scale = 0.1;
        boundingRadius = 3;
        this.tag = "enemy";
    }
    
    /** Movement speed */
    final double spd = 80;
    
    /** Turning speed in degrees per second */
    final double turnSpd = 70;
    
    /** Current rotation velocity */
    Quaternion rotationVelocity = new Quaternion();
    
    /** Time for next direction change */
    long nextTime = 0;
    
    /**
     * Generates a random rotation quaternion
     * @param radius Maximum rotation angle
     * @return Random rotation as a quaternion
     */
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
    
    /**
     * Updates the enemy's position and rotation
     * @param delta Time elapsed since last update in seconds
     */
    @Override
    public void Update(double delta)
    {
        super.Update(delta);
        if(this.position.distance(new Vector3()) > 10000)
        {
            GameObject.DestroyObject(this);
        }
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
        v =v.normalize();
        v.x *= spd * delta;
        v.y *= spd * delta;
        v.z *= spd * delta;
        
        position = position.plus(v);
        Renderer.renderer.Render(this);
    }
    
    /**
     * Cleanup when enemy is destroyed
     */
    @Override
    public void destroy()
    {
        super.destroy();
        AstroidManager.instance.Unrigister(this);
    }
}
