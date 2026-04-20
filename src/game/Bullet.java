package game;

import game.engine.*;

/**
 * A bullet projectile that flies forward and participates in collision detection.
 */
public class Bullet extends CollidableRenderable
{
    private Vector3 velocity;
    private double lifetime;

    public Bullet(Vector3 pos, Vector3 velocity, double lifetime)
    {
        super(Models.bulletModel);
        collisionLayer = CollisionLayer.BULLET;
        position = new Vector3(pos);
        this.velocity = velocity;
        this.lifetime = lifetime;
        boundingRadius = 2;
        scale = 1;
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
        Vector3 worldVelocity = velocity.plus(Camera.getWorldScrollVelocity());
        position = position.plus(worldVelocity.multiply(delta));
    }

    @Override
    public void onCollisionEnter(Collidable other)
    {
        GameObject.destroyObject(this);
    }
}
