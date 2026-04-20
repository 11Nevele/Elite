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
    private static final double AIM_ASSIST_BLEND = 1.0;
    private static final double AIM_ASSIST_DISTANCE_BONUS = 0.15;

    private double shootTimer = 0;
    private final AimAssistDebugState debugState = new AimAssistDebugState();

    public static class AimAssistDebugState
    {
        private boolean hasTarget;
        private final Vector3 targetPosition = new Vector3();
        private double targetDistance;
        private double targetAlignment;
        private double targetScore;

        public boolean hasTarget()
        {
            return hasTarget;
        }

        public Vector3 getTargetPosition()
        {
            return targetPosition;
        }

        public double getTargetDistance()
        {
            return targetDistance;
        }

        public double getTargetAlignment()
        {
            return targetAlignment;
        }

        public double getTargetScore()
        {
            return targetScore;
        }

        private void clear()
        {
            hasTarget = false;
            targetPosition.set(0, 0, 0);
            targetDistance = 0;
            targetAlignment = 0;
            targetScore = 0;
        }

        private void set(Vector3 targetPosition, double targetDistance, double targetAlignment, double targetScore)
        {
            hasTarget = true;
            this.targetPosition.set(targetPosition);
            this.targetDistance = targetDistance;
            this.targetAlignment = targetAlignment;
            this.targetScore = targetScore;
        }
    }

    private static class AimAssistCandidate
    {
        private final Vector3 targetPosition;
        private final Vector3 targetDirection;
        private final double targetDistance;
        private final double targetAlignment;
        private final double targetScore;

        private AimAssistCandidate(Vector3 targetPosition, Vector3 targetDirection,
                                   double targetDistance, double targetAlignment, double targetScore)
        {
            this.targetPosition = targetPosition;
            this.targetDirection = targetDirection;
            this.targetDistance = targetDistance;
            this.targetAlignment = targetAlignment;
            this.targetScore = targetScore;
        }
    }

    public void reset()
    {
        shootTimer = 0;
        debugState.clear();
    }

    public AimAssistDebugState getAimAssistDebugState()
    {
        return debugState;
    }

    public void update(double delta, Vector3 position, Quaternion rotation)
    {
        if (GameState.gameState.isDead()) return;

        shootTimer -= delta;

        Vector3 defaultDirection = EngineUtil.quaternionToDirection(rotation).normalize();
        AimAssistCandidate candidate = getAimAssistCandidate(position, defaultDirection);
        updateDebugState(candidate);

        if (Input.input.keys[KeyEvent.VK_SPACE] && shootTimer <= 0)
        {
            shoot(position, defaultDirection, candidate);
            shootTimer = SHOOT_COOLDOWN;
        }
    }

    private void shoot(Vector3 position, Vector3 defaultDirection, AimAssistCandidate candidate)
    {
        Vector3 shotDirection = candidate == null
            ? defaultDirection
            : blendDirections(defaultDirection, candidate.targetDirection, AIM_ASSIST_BLEND);

        new Bullet(position, shotDirection.multiply(BULLET_SPEED), BULLET_LIFETIME);
        Audio.playLaser();
    }

    private AimAssistCandidate getAimAssistCandidate(Vector3 origin, Vector3 defaultDirection)
    {
        if (CollisionManager.instance == null)
        {
            return null;
        }

        AimAssistCandidate bestCandidate = null;
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
                bestCandidate = new AimAssistCandidate(
                    new Vector3(collidable.getPosition()),
                    targetDirection,
                    distance,
                    alignment,
                    score
                );
            }
        }

        return bestCandidate;
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

    private void updateDebugState(AimAssistCandidate candidate)
    {
        if (candidate == null)
        {
            debugState.clear();
            return;
        }

        debugState.set(
            candidate.targetPosition,
            candidate.targetDistance,
            candidate.targetAlignment,
            candidate.targetScore
        );
    }

    public static double getAimAssistRange()
    {
        return AIM_ASSIST_RANGE;
    }

    public static double getAimAssistConeDot()
    {
        return AIM_ASSIST_CONE_DOT;
    }

    public static double getAimAssistBlend()
    {
        return AIM_ASSIST_BLEND;
    }
}
