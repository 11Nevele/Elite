package game;

import game.engine.*;
import java.awt.event.KeyEvent;

/**
 * Manages the weapon system: shooting, bullet creation, cooldowns.
 */
public class WeaponSystem
{
    private static final double SHOOT_COOLDOWN = 0.15;
    private static final double BULLET_SPEED = 1000;
    private static final double BULLET_LIFETIME = 2.0;
    private static final double AIM_ASSIST_RANGE = 220;
    private static final double AIM_ASSIST_CONE_DOT = 0.94;
    private static final double AIM_ASSIST_BLEND = 0.35;
    private static final double AIM_ASSIST_DISTANCE_BONUS = 0.15;
    private static final double AIM_PLANE_EPSILON = 0.0001;

    private double shootTimer = 0;
    private final Vector3 currentAimPointAtHoldDistance = new Vector3();
    private boolean hasCurrentAimPointAtHoldDistance = false;

    public void reset()
    {
        shootTimer = 0;
        hasCurrentAimPointAtHoldDistance = false;
        currentAimPointAtHoldDistance.set(0, 0, 0);
    }

    public boolean hasCurrentAimPointAtHoldDistance()
    {
        return hasCurrentAimPointAtHoldDistance;
    }

    public Vector3 getCurrentAimPointAtHoldDistance()
    {
        return currentAimPointAtHoldDistance;
    }

    public void update(double delta, Vector3 position, Quaternion rotation)
    {
        if (GameState.gameState.isDead()) return;

        shootTimer -= delta;

        Vector3 defaultDirection = EngineUtil.quaternionToDirection(rotation).normalize();
        updateAimPointAtHoldDistance(position, defaultDirection);

        if (Input.input.keys[KeyEvent.VK_SPACE] && shootTimer <= 0)
        {
            shoot(position, defaultDirection);
            shootTimer = SHOOT_COOLDOWN;
        }
    }

    private void shoot(Vector3 position, Vector3 defaultDirection)
    {
        Vector3 assistedDirection = getAimAssistDirection(position, defaultDirection);
        Vector3 shotDirection = assistedDirection == null
            ? defaultDirection
            : blendDirections(defaultDirection, assistedDirection, AIM_ASSIST_BLEND);

        new Bullet(position, shotDirection.multiply(BULLET_SPEED), BULLET_LIFETIME);
        Audio.playLaser();
    }

    private Vector3 getAimAssistDirection(Vector3 origin, Vector3 defaultDirection)
    {
        if (CollisionManager.instance == null)
        {
            return null;
        }

        Vector3 bestDirection = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Collidable collidable : CollisionManager.instance.getCollidables())
        {
            if (!isAimAssistTarget(collidable))
            {
                continue;
            }

            Vector3 toTarget = collidable.getPosition().minus(origin);
            double distance = toTarget.magnitude();
            if (distance <= 0 || distance > AIM_ASSIST_RANGE)
            {
                continue;
            }

            Vector3 targetDirection = toTarget.normalize();
            double alignment = targetDirection.dot(defaultDirection);
            if (alignment < AIM_ASSIST_CONE_DOT)
            {
                continue;
            }

            double distanceBonus = (1 - distance / AIM_ASSIST_RANGE) * AIM_ASSIST_DISTANCE_BONUS;
            double score = alignment + distanceBonus;
            if (score > bestScore)
            {
                bestScore = score;
                bestDirection = targetDirection;
            }
        }

        return bestDirection;
    }

    private boolean isAimAssistTarget(Collidable collidable)
    {
        int layer = collidable.getCollisionLayer();
        return layer == CollisionLayer.ENEMY || layer == CollisionLayer.ENEMY_BACK_ENTRY;
    }

    private Vector3 blendDirections(Vector3 defaultDirection, Vector3 targetDirection, double blend)
    {
        double clampedBlend = Math.max(0, Math.min(1, blend));
        return defaultDirection.multiply(1 - clampedBlend)
            .plus(targetDirection.multiply(clampedBlend))
            .normalize();
    }

    private void updateAimPointAtHoldDistance(Vector3 origin, Vector3 direction)
    {
        double directionZ = direction.getZ();
        if (Math.abs(directionZ) < AIM_PLANE_EPSILON)
        {
            hasCurrentAimPointAtHoldDistance = false;
            currentAimPointAtHoldDistance.set(0, 0, 0);
            return;
        }

        double travel = (EnemySpawner.getHoldDistance() - origin.getZ()) / directionZ;
        if (travel < 0)
        {
            hasCurrentAimPointAtHoldDistance = false;
            currentAimPointAtHoldDistance.set(0, 0, 0);
            return;
        }

        currentAimPointAtHoldDistance.set(origin.plus(direction.multiply(travel)));
        hasCurrentAimPointAtHoldDistance = true;
    }
}
