package game;

import game.engine.CollidableRenderable;
import game.engine.Face;
import game.engine.QuaternionT;
import game.engine.Vector3;

public class Star extends CollidableRenderable
{
    public double selfRotationPeriod = 60;
    public double rotationPeriod = 300;
    public Vector3 rotationAxis = new Vector3(0,1,0);
    public final double orbitRadius;
    public final double rotationVelocity;
    public final double orbitSpeed;
    
    public Star(Vector3 position, double selfRotationPeriod, double rotationPeriod, Vector3 rotationAxis, Face[] model)
    {
        super(model);
        this.position = position;
        this.selfRotationPeriod = selfRotationPeriod;
        this.rotationPeriod = rotationPeriod;
        this.rotationAxis = rotationAxis;
        orbitRadius = position.distance(new Vector3());
        rotationVelocity = 360.0 / rotationPeriod;
        orbitSpeed = 360.0 / rotationPeriod;
    }

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
