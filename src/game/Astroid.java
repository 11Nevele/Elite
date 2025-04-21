package game;

import game.engine.CollidableRenderable;
import game.engine.EngineUtil;
import game.engine.GameObject;
import game.engine.QuaternionT;
import game.engine.Vector3;
import java.util.Random;

/**
 * Represents an asteroid in the game world.
 * Asteroids move in a straight line with constant velocity and rotate around a random axis.
 * They are destroyed when they move too far from the origin or when hit by a player's weapon.
 */
public class Astroid extends CollidableRenderable
{
    /** Velocity vector (direction and speed) */
    final Vector3 velocity;
    
    /** Axis around which the asteroid rotates */
    final Vector3 rotationAxis;
    
    /** Angular velocity (degrees per second) */
    final double rotationVelocity;
    
    /** Maximum possible speed for a new asteroid */
    final double mxVelocity = 50;
    
    /** Maximum possible rotation speed for a new asteroid */
    final double mxRotationVelocity = 50;
    
    /**
     * Constructor - creates a random asteroid with random position, velocity, and rotation
     */
    public Astroid() 
    {
        super(Models.Astroids[new Random().nextInt(Models.Astroids.length)]);
        velocity = EngineUtil.RandomOnSphere(Math.random() * mxVelocity);
        rotationAxis = EngineUtil.RandomOnSphere(1);
        rotationVelocity = Math.random() * mxRotationVelocity;
        this.position = EngineUtil.RandomOnSphere(1).multi(Math.random() * 5000);
        this.boundingRadius = 3;
        this.scale = 3;
        this.tag = "asteroid";
    }
    
    /**
     * Updates the asteroid's position and rotation
     * @param delta Time elapsed since last update in seconds
     */
    @Override
    public void Update(double delta)
    {
        super.Update(delta);
        rotation = rotation.multiply(new QuaternionT(rotationVelocity * delta, rotationAxis).asQuaternion());
        position = position.plus(velocity.multi(delta));
        if(position.distance(new Vector3()) > 10000)
        {
            GameObject.DestroyObject(this);
        }
    }
    
    /**
     * Cleanup when asteroid is destroyed
     */
    @Override
    public void destroy()
    {
        super.destroy();
    }
}
