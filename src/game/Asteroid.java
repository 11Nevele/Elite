package game;

import game.engine.*;

/**
 * An asteroid that drifts and rotates in space.
 * Detected by collision system and can be destroyed for score.
 */
public class Asteroid extends CollidableRenderable
{
    private static final double SPEED = 5;
    private static final double MAX_DISTANCE = 10000;
    private static final String TAG_NAME = "asteroid";

    private double rotationVelocity;
    private Vector3 rotationAxis;
    private Vector3 velocity;

    public Asteroid(Face[] model, Vector3 spawnPos)
    {
        super(model);
        tag = TAG_NAME;
        position = new Vector3(spawnPos);
        boundingRadius = 5;
        scale = 3;

        rotationVelocity = Math.random() * 100 + 20;
        rotationAxis = EngineUtil.randomOnSphere(1).normalize();
        velocity = EngineUtil.randomOnSphere(SPEED);
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);
        rotation = rotation.multiply(Quaternion.fromAxisAngle(rotationAxis, rotationVelocity * delta));
        position = position.plus(velocity.multiply(delta));

        // Remove if too far away
        if (position.magnitude() > MAX_DISTANCE)
        {
            System.out.println("Removing distant asteroid" + position);
            AsteroidManager.instance.unregister(this);
            GameObject.destroyObject(this);
        }

        // Check collision with player bullets
        if (CollisionManager.instance.getCollision(getID(), "bullet"))
        {
            Explosion.generateExplosion(position, 10);
            GameState.gameState.addScore(100);
            AsteroidManager.instance.unregister(this);
            GameObject.destroyObject(this);
        }
    }
}
