package game;

import game.engine.CollidableRenderable;
import game.engine.Face;
import game.engine.QuaternionT;
import game.engine.Vector3;

/**
 * Represents a star or planet in the game world.
 * Stars have both self-rotation and orbital movement.
 */
public class Star extends CollidableRenderable
{
    /** Time in seconds for a complete self-rotation */
    public double selfRotationPeriod = 60;
    
    /** Time in seconds for a complete orbit around (0,0,0) */
    public double rotationPeriod = 300;
    
    /** Axis of self-rotation */
    public Vector3 rotationAxis = new Vector3(0,1,0);
    
    /** Orbital radius from the center (0,0,0) */
    public final double orbitRadius;
    
    /** Self-rotation velocity in degrees per second */
    public final double rotationVelocity;
    
    /** Orbital velocity in degrees per second */
    public final double orbitSpeed;
    
    /**
     * Creates a new star with specified parameters
     * @param position Initial position
     * @param selfRotationPeriod Time for one complete self-rotation in seconds
     * @param rotationPeriod Time for one complete orbit in seconds
     * @param rotationAxis Axis of self-rotation
     * @param model 3D model for the star
     */
    public Star(Vector3 position, double selfRotationPeriod, double rotationPeriod, Vector3 rotationAxis, Face[] model)
    {
        super(model);
        this.position = position;
        this.selfRotationPeriod = selfRotationPeriod;
        this.rotationPeriod = rotationPeriod;
        this.rotationAxis = rotationAxis;
        orbitRadius = position.distance(new Vector3());
        rotationVelocity = 360.0 / selfRotationPeriod;
        orbitSpeed = 360.0 / rotationPeriod;
    }

    /**
     * Updates the star's rotation and orbital position
     * @param delta Time elapsed since last update in seconds
     */
    @Override
    public void Update(double delta)
    {
        super.Update(delta);
        //self rotation
        QuaternionT selfRotation = new QuaternionT(rotationVelocity * delta, rotationAxis);
        rotation = rotation.multiply(selfRotation.asQuaternion());
        //orbit rotation around(0,0,0)
        QuaternionT orbitRotation = new QuaternionT(orbitSpeed * delta, new Vector3(0,1,0));
        position = orbitRotation.asQuaternion().rotate(position);
    }
}
