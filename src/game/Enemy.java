package game;

import game.engine.*;

/**
 * A scripted enemy ship that flies a deterministic attack path.
 */
public class Enemy extends CollidableRenderable
{
    private static final double DESPAWN_BEHIND_DISTANCE = 120;
    private static final double DESPAWN_AHEAD_DISTANCE = 1400;
    private static final double DESPAWN_SIDE_DISTANCE = 220;
    private static final double MAX_LIFETIME = 12;

    private Vector3 spawnOrigin;
    private Vector3 baseVelocity;
    private Vector3 oscillationAxis;
    private double oscillationAmplitude;
    private double oscillationFrequency;
    private double oscillationPhase;
    private double age = 0;

    public Enemy(Face[] model, Vector3 spawnPos)
    {
        this(model, spawnPos, new Vector3(0, 0, Camera.SCROLL_SPEED * 0.35), new Vector3(), 0, 0, 0);
    }

    public Enemy(Face[] model, Vector3 spawnPos, Vector3 velocity, Vector3 oscillationAxis,
                 double oscillationAmplitude, double oscillationFrequency, double oscillationPhase)
    {
        super(model);
        collisionLayer = CollisionLayer.ENEMY;
        position = new Vector3(spawnPos);
        spawnOrigin = new Vector3(spawnPos);
        baseVelocity = new Vector3(velocity);
        this.oscillationAxis = oscillationAxis.magnitude() == 0 ? new Vector3() : oscillationAxis.normalize();
        this.oscillationAmplitude = oscillationAmplitude;
        this.oscillationFrequency = oscillationFrequency;
        this.oscillationPhase = oscillationPhase;
        boundingRadius = 5;
        scale = 3;
        rotation = rotationForMotion(baseVelocity);
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);
        age += delta;
        position = spawnOrigin.plus(baseVelocity.multiply(age));

        if (oscillationAmplitude > 0 && oscillationFrequency != 0 && oscillationAxis.magnitude() > 0)
        {
            double offset = Math.sin(age * oscillationFrequency + oscillationPhase) * oscillationAmplitude;
            position = position.plus(oscillationAxis.multiply(offset));
        }

        rotation = rotationForMotion(getCurrentVelocity());

        if (shouldDespawn())
        {
            AsteroidManager.instance.unregister(this);
            GameObject.destroyObject(this);
        }
    }

    @Override
    public void onCollisionEnter(Collidable other)
    {
        int layer = other.getCollisionLayer();
        if (layer == CollisionLayer.BULLET)
        {
            Explosion.generateExplosion(position, 15);
            GameState.gameState.addScore(250);
            GameState.gameState.recordEnemyDestroyed();
            AsteroidManager.instance.unregister(this);
            GameObject.destroyObject(this);
        }
        else if (layer == CollisionLayer.PLAYER)
        {
            Explosion.generateExplosion(position, 20);
            GameState.gameState.setCrashed(true);
            AsteroidManager.instance.unregister(this);
            GameObject.destroyObject(this);
        }
    }

    private Vector3 getCurrentVelocity()
    {
        Vector3 currentVelocity = new Vector3(baseVelocity);
        if (oscillationAmplitude > 0 && oscillationFrequency != 0 && oscillationAxis.magnitude() > 0)
        {
            double oscillationVelocity = Math.cos(age * oscillationFrequency + oscillationPhase)
                * oscillationAmplitude * oscillationFrequency;
            currentVelocity = currentVelocity.plus(oscillationAxis.multiply(oscillationVelocity));
        }
        return currentVelocity;
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
        double bankDegrees = -direction.getX() * 20;

        return Quaternion.yaw(yawDegrees)
            .multiply(Quaternion.pitch(pitchDegrees))
            .multiply(Quaternion.roll(bankDegrees));
    }

    private boolean shouldDespawn()
    {
        if (age > MAX_LIFETIME || Camera.instance == null)
        {
            return age > MAX_LIFETIME;
        }

        Vector3 playerPos = Camera.instance.position;
        return position.getZ() < playerPos.getZ() - DESPAWN_BEHIND_DISTANCE
            || position.getZ() > playerPos.getZ() + DESPAWN_AHEAD_DISTANCE
            || Math.abs(position.getX() - playerPos.getX()) > DESPAWN_SIDE_DISTANCE
            || Math.abs(position.getY() - playerPos.getY()) > DESPAWN_SIDE_DISTANCE;
    }
}
