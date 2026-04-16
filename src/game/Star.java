package game;

import game.engine.*;

/**
 * A star object that orbits and rotates, used for background scenery.
 */
public class Star extends Renderable
{
    private static final double ORBIT_SPEED = 0;

    private double rotationVelocity;
    private Vector3 rotationAxis;
    private double orbitRadius;

    public Star(Face[] model, double orbitRadius)
    {
        super(model);
        this.orbitRadius = orbitRadius;
        rotationVelocity = Math.random() * 50 + 10;
        rotationAxis = EngineUtil.randomOnSphere(1).normalize();
        position = EngineUtil.randomOnSphere(orbitRadius);
        scale = 0.02;
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);
        rotation = rotation.multiply(Quaternion.fromAxisAngle(rotationAxis, rotationVelocity * delta));

        // Orbit around origin
        Quaternion orbitQ = Quaternion.fromAxisAngle(new Vector3(0, 1, 0), ORBIT_SPEED * delta);
        position = orbitQ.rotate(position);
    }
}
