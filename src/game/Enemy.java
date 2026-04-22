package game;

import game.engine.*;

/**
 * A scripted enemy ship that flies a deterministic attack path.
 */
public class Enemy extends CollidableRenderable
{
    public enum EntryType
    {
        FRONT,
        BACK
    }

    private enum Phase
    {
        ENTERING,
        HOLDING,
        EXITING
    }

    private static final double HOLD_ARRIVAL_DISTANCE = 8;
    private static final double HOLDING_DESPAWN_AHEAD_DISTANCE = 1100;
    private static final double HOLDING_DESPAWN_SIDE_DISTANCE = 340;
    private static final double HOLDING_DESPAWN_VERTICAL_DISTANCE = 260;
    private static final double ENTRY_DESPAWN_AHEAD_DISTANCE = 1500;
    private static final double ENTRY_DESPAWN_BEHIND_DISTANCE = 420;
    private static final double ENTRY_DESPAWN_SIDE_DISTANCE = 520;
    private static final double ENTRY_DESPAWN_VERTICAL_DISTANCE = 320;
    private static final double EXIT_DESPAWN_SIDE_DISTANCE = 460;
    private static final double EXIT_DESPAWN_VERTICAL_DISTANCE = 320;
    private static final double MAX_LIFETIME = 18;
    private static final double ENEMY_BULLET_SPEED = 20;
    private static final double ENEMY_BULLET_LIFETIME = 10;
    private static final double SHOT_MUZZLE_OFFSET = 0;
    private static final double MIN_SHOT_INTERVAL = 1.6;
    private static final double MAX_SHOT_INTERVAL = 2;
    private static final double HORIZONTAL_AIM_OFFSET = 5;
    private static final double VERTICAL_AIM_OFFSET = 5;

    private final EntryType entryType;
    private final Vector3 holdAnchor;
    private final double holdDepth;
    private final double entrySpeed;
    private final Vector3 holdDriftVelocity;
    private final Vector3 oscillationAxis;
    private final double oscillationAmplitude;
    private final double oscillationFrequency;
    private final double oscillationPhase;
    private final double holdDuration;
    private final Vector3 exitVelocity;

    private Phase phase = Phase.ENTERING;
    private Vector3 currentVelocity;
    private double age = 0;
    private double phaseAge = 0;
    private double shotTimer = 0;

    public Enemy(Face[] model, Vector3 spawnPos, Vector3 holdPos, EntryType entryType,
                 double entrySpeed, Vector3 holdDriftVelocity, Vector3 oscillationAxis,
                 double oscillationAmplitude, double oscillationFrequency, double oscillationPhase,
                 double holdDuration, Vector3 exitVelocity)
    {
        super(model);
        collisionLayer = entryType == EntryType.BACK ? CollisionLayer.ENEMY_BACK_ENTRY : CollisionLayer.ENEMY;
        position = new Vector3(spawnPos);
        holdAnchor = new Vector3(holdPos);
        holdDepth = holdPos.getZ();
        this.entryType = entryType;
        this.entrySpeed = entrySpeed;
        this.holdDriftVelocity = new Vector3(holdDriftVelocity.getX(), holdDriftVelocity.getY(), 0);
        this.oscillationAxis = sanitizedOscillationAxis(oscillationAxis);
        this.oscillationAmplitude = oscillationAmplitude;
        this.oscillationFrequency = oscillationFrequency;
        this.oscillationPhase = oscillationPhase;
        this.holdDuration = holdDuration;
        this.exitVelocity = new Vector3(exitVelocity.getX(), exitVelocity.getY(), 0);
        currentVelocity = initialEntryVelocity();
        boundingRadius = 5;
        scale = 3;
        rotation = rotationForMotion(currentVelocity);
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);
        age += delta;
        phaseAge += delta;

        switch (phase)
        {
            case ENTERING -> updateEntering(delta);
            case HOLDING -> updateHolding(delta);
            case EXITING -> updateExiting(delta);
        }

        rotation = rotationForMotion(currentVelocity);

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

    private void updateEntering(double delta)
    {
        refreshCollisionState();

        Vector3 targetPosition = getHoldPosition();
        Vector3 toTarget = targetPosition.minus(position);
        double distance = toTarget.magnitude();
        double maxStep = entrySpeed * delta;

        if (distance <= Math.max(HOLD_ARRIVAL_DISTANCE, maxStep))
        {
            beginHolding();
            return;
        }

        currentVelocity = toTarget.normalize().multiply(entrySpeed);
        position = position.plus(currentVelocity.multiply(delta));
    }

    private void updateHolding(double delta)
    {
        refreshCollisionState();
        holdAnchor.addXYZ(holdDriftVelocity.getX() * delta, holdDriftVelocity.getY() * delta, 0);
        position = getHoldPosition();
        currentVelocity = getHoldVelocity();
        updateShooting(delta);

        if (phaseAge >= holdDuration)
        {
            beginExit();
        }
    }

    private void updateExiting(double delta)
    {
        refreshCollisionState();
        position.addXYZ(exitVelocity.getX() * delta, exitVelocity.getY() * delta, 0);
        position.setZ(holdDepth);
        currentVelocity = new Vector3(exitVelocity);
    }

    private void beginHolding()
    {
        phase = Phase.HOLDING;
        phaseAge = 0;
        shotTimer = randomShotInterval();
        position = getHoldPosition();
        currentVelocity = getHoldVelocity();
    }

    private void beginExit()
    {
        phase = Phase.EXITING;
        phaseAge = 0;
        position = getHoldPosition();
        currentVelocity = new Vector3(exitVelocity);
    }

    private Vector3 getHoldPosition()
    {
        Vector3 holdPosition = new Vector3(holdAnchor.getX(), holdAnchor.getY(), holdDepth);

        if (oscillationAmplitude > 0 && oscillationFrequency != 0 && oscillationAxis.magnitude() > 0)
        {
            double offset = Math.sin(age * oscillationFrequency + oscillationPhase) * oscillationAmplitude;
            Vector3 oscillationOffset = oscillationAxis.multiply(offset);
            oscillationOffset.setZ(0);
            holdPosition = holdPosition.plus(oscillationOffset);
        }

        return holdPosition;
    }

    private Vector3 getHoldVelocity()
    {
        Vector3 holdVelocity = new Vector3(holdDriftVelocity);
        if (oscillationAmplitude > 0 && oscillationFrequency != 0 && oscillationAxis.magnitude() > 0)
        {
            double oscillationVelocity = Math.cos(age * oscillationFrequency + oscillationPhase)
                * oscillationAmplitude * oscillationFrequency;
            Vector3 oscillationVelocityVector = oscillationAxis.multiply(oscillationVelocity);
            oscillationVelocityVector.setZ(0);
            holdVelocity = holdVelocity.plus(oscillationVelocityVector);
        }
        return holdVelocity;
    }

    private Vector3 initialEntryVelocity()
    {
        Vector3 toHold = getHoldPosition().minus(position);
        if (toHold.magnitude() == 0)
        {
            return new Vector3();
        }
        return toHold.normalize().multiply(entrySpeed);
    }

    private Vector3 getPlayerPosition()
    {
        if (Camera.instance == null)
        {
            return new Vector3();
        }
        return Camera.instance.position;
    }

    private void updateShooting(double delta)
    {
        if (GameState.gameState.isDead() || Camera.instance == null)
        {
            return;
        }

        shotTimer -= delta;
        if (shotTimer > 0)
        {
            return;
        }

        fireAtPlayer();
        shotTimer = randomShotInterval();
    }

    private void fireAtPlayer()
    {
        Vector3 playerPosition = getPlayerPosition();
        /*System.out.println("Enemy shooting at player at position: " + playerPosition.getX()
            + ", " + playerPosition.getY()
            + ", " + playerPosition.getZ());*/
        Vector3 toPlayer = playerPosition.minus(position);
        if (toPlayer.magnitude() == 0)
        {
            return;
        }

        Vector3 aimPoint = getRandomizedAimPoint(playerPosition, toPlayer.normalize());
        Vector3 shotDirection = aimPoint.minus(position).normalize();
        Vector3 muzzlePosition = position.plus(shotDirection.multiply(SHOT_MUZZLE_OFFSET));
        Bullet enemyBullet = new Bullet(
            muzzlePosition,
            shotDirection.multiply(ENEMY_BULLET_SPEED),
            ENEMY_BULLET_LIFETIME,
            Models.enemyBulletModel,
            CollisionLayer.ENEMY_BULLET,
            false
        );
        enemyBullet.scale = 0.55;
    }

    private double randomShotInterval()
    {
        return MIN_SHOT_INTERVAL + Math.random() * (MAX_SHOT_INTERVAL - MIN_SHOT_INTERVAL);
    }

    private Vector3 getRandomizedAimPoint(Vector3 playerPosition, Vector3 directionToPlayer)
    {
        Vector3 referenceUp = Math.abs(directionToPlayer.getY()) > 0.98
            ? new Vector3(1, 0, 0)
            : new Vector3(0, 1, 0);
        Vector3 right = referenceUp.cross(directionToPlayer).normalize();
        Vector3 up = directionToPlayer.cross(right).normalize();

        return playerPosition
            .plus(right.multiply(randomBetween(-HORIZONTAL_AIM_OFFSET, HORIZONTAL_AIM_OFFSET)))
            .plus(up.multiply(randomBetween(-VERTICAL_AIM_OFFSET, VERTICAL_AIM_OFFSET)));
    }

    private double randomBetween(double min, double max)
    {
        return min + Math.random() * (max - min);
    }

    private void refreshCollisionState()
    {
        if (entryType == EntryType.BACK && position.getZ() <= getPlayerPosition().getZ())
        {
            collisionLayer = CollisionLayer.ENEMY_BACK_ENTRY;
            return;
        }

        collisionLayer = CollisionLayer.ENEMY;
    }

    private Vector3 sanitizedOscillationAxis(Vector3 axis)
    {
        if (axis.magnitude() == 0)
        {
            return new Vector3();
        }

        Vector3 sanitizedAxis = axis.normalize();
        sanitizedAxis.setZ(0);
        if (sanitizedAxis.magnitude() == 0)
        {
            return new Vector3();
        }
        return sanitizedAxis.normalize();
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

        Vector3 playerPos = getPlayerPosition();
        double relativeX = Math.abs(position.getX());
        double relativeY = Math.abs(position.getY());
        double relativeZ = position.getZ() - playerPos.getZ();
        double entryBehindDistance = entryType == EntryType.BACK
            ? ENTRY_DESPAWN_BEHIND_DISTANCE + 120
            : ENTRY_DESPAWN_BEHIND_DISTANCE;
        double entryAheadDistance = entryType == EntryType.FRONT
            ? ENTRY_DESPAWN_AHEAD_DISTANCE + 180
            : ENTRY_DESPAWN_AHEAD_DISTANCE;

        return switch (phase)
        {
            case ENTERING -> relativeZ < -entryBehindDistance
                || relativeZ > entryAheadDistance
                || relativeX > ENTRY_DESPAWN_SIDE_DISTANCE
                || relativeY > ENTRY_DESPAWN_VERTICAL_DISTANCE;
            case HOLDING -> relativeZ < 0
                || relativeZ > HOLDING_DESPAWN_AHEAD_DISTANCE
                || relativeX > HOLDING_DESPAWN_SIDE_DISTANCE
                || relativeY > HOLDING_DESPAWN_VERTICAL_DISTANCE;
            case EXITING -> relativeX > EXIT_DESPAWN_SIDE_DISTANCE
                || relativeY > EXIT_DESPAWN_VERTICAL_DISTANCE
                || relativeZ < -ENTRY_DESPAWN_BEHIND_DISTANCE
                || relativeZ > HOLDING_DESPAWN_AHEAD_DISTANCE;
        };
    }
}
