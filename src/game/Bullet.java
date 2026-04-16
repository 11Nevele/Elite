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
        tag = "bullet";
        position = new Vector3(pos);
        this.velocity = velocity;
        this.lifetime = lifetime;
        boundingRadius = 1;
        scale = 0.5;
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
        position = position.plus(velocity.multiply(delta));
    }
}
