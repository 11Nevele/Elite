package game;

import game.engine.CollidableRenderable;
import game.engine.EngineUtil;
import game.engine.GameObject;
import game.engine.QuaternionT;
import game.engine.Vector3;
import java.util.Random;

public class Astroid extends CollidableRenderable
{
    final Vector3 velocity;
    final Vector3 rotationAxis;
    final double rotationVelocity;
    final double mxVelocity = 50;
    final double mxRotationVelocity = 50;
    
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
    @Override
    public void destroy()
    {
        super.destroy();
    }
    
}
