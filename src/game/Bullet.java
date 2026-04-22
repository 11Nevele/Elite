package game;

import game.engine.*;

/**
 * A bullet projectile that flies forward and participates in collision detection.
 */
public class Bullet extends CollidableRenderable
{
    private Vector3 velocity;
    private double lifetime;
    private final boolean applyWorldScroll;

    public Bullet(Vector3 pos, Vector3 velocity, double lifetime)
    {
        this(pos, velocity, lifetime, Models.bulletModel, CollisionLayer.BULLET, true);
    }

    public Bullet(Vector3 pos, Vector3 velocity, double lifetime, Face[] model, int layer)
    {
        this(pos, velocity, lifetime, model, layer, true);
    }

    public Bullet(Vector3 pos, Vector3 velocity, double lifetime, Face[] model, int layer, boolean applyWorldScroll)
    {
        super(model);
        collisionLayer = layer;
        position = new Vector3(pos);
        this.velocity = velocity;
        this.lifetime = lifetime;
        this.applyWorldScroll = applyWorldScroll;
        boundingRadius = 5;
        scale = 1;
        rotation = rotationForMotion(getWorldVelocity());
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);
        lifetime -= delta;
        if (lifetime <= 0)
        {
            GameObject.destroyObject(this);
            return;
        }
        Vector3 worldVelocity = getWorldVelocity();
        rotation = rotationForMotion(worldVelocity);
        position = position.plus(worldVelocity.multiply(delta));
    }

    private Vector3 getWorldVelocity()
    {
        return applyWorldScroll
            ? velocity.plus(Camera.getWorldScrollVelocity())
            : velocity;
    }

    private Quaternion rotationForMotion(Vector3 motion)
    {
        if (motion.magnitude() == 0)
        {
            return new Quaternion();
        }

        Vector3 direction = motion.normalize();
        double horizontalLength = Math.sqrt(direction.getX() * direction.getX() + direction.getZ() * direction.getZ());
        double yawDegrees = Math.toDegrees(Math.atan2(direction.getX(), direction.getZ()));
        double pitchDegrees = -Math.toDegrees(Math.atan2(direction.getY(), Math.max(0.001, horizontalLength)));

        return Quaternion.yaw(yawDegrees).multiply(Quaternion.pitch(pitchDegrees));
    }

    @Override
    public void onCollisionEnter(Collidable other)
    {
        GameObject.destroyObject(this);
    }
}
