package game;

import game.engine.*;

/**
 * An enemy ship (TIE fighter) that chases the player.
 * Can be destroyed by player bullets and explodes on contact.
 */
public class Enemy extends CollidableRenderable
{
    private static final double TURN_SPEED = 30;
    private static final double MOVE_SPEED = 40;
    private static final double MAX_DISTANCE = 10000;
    private static final String TAG_NAME = "enemy";

    private Quaternion rotationVelocity;

    public Enemy(Face[] model, Vector3 spawnPos)
    {
        super(model);
        tag = TAG_NAME;
        position = new Vector3(spawnPos);
        boundingRadius = 5;
        scale = 3;
        rotationVelocity = new Quaternion();
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);

        double turnSpd = TURN_SPEED;

        // Randomize turn direction slightly
        Vector3 randomAxis = EngineUtil.randomOnSphere(1).normalize();
        Quaternion randomTurn = Quaternion.fromAxisAngle(randomAxis, Math.random() * turnSpd);
        rotationVelocity = randomTurn;

        // Apply rotation over delta time
        rotation = rotation.multiply(rotationVelocity.scaleRotation(delta));

        // Move forward
        Vector3 forward = EngineUtil.quaternionToDirection(rotation);
        Vector3 vel = forward.multiply(MOVE_SPEED * delta);
        position = position.plus(vel);

        // Remove if too far
        if (position.magnitude() > MAX_DISTANCE)
        {
            System.out.println("Removing distant enemy" + position);
            AsteroidManager.instance.unregister(this);
            GameObject.destroyObject(this);
        }

        // Check collision with bullet
        if (CollisionManager.instance.getCollision(getID(), "bullet"))
        {
            Explosion.generateExplosion(position, 15);
            GameState.gameState.addScore(250);
            AsteroidManager.instance.unregister(this);
            GameObject.destroyObject(this);
        }

        // Check collision with player
        if (CollisionManager.instance.getCollision(getID(), "player"))
        {
            Explosion.generateExplosion(position, 20);
            GameState.gameState.setCrashed(true);
            AsteroidManager.instance.unregister(this);
            GameObject.destroyObject(this);
        }
    }
}
