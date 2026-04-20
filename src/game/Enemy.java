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

    private final EntryType entryType;
    private final Vector3 holdAnchor;
    private final double holdDistanceFromPlayer;
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

    public Enemy(Face[] model, Vector3 spawnPos, Vector3 holdPos, EntryType entryType,
                 double entrySpeed, Vector3 holdDriftVelocity, Vector3 oscillationAxis,
                 double oscillationAmplitude, double oscillationFrequency, double oscillationPhase,
                 double holdDuration, Vector3 exitVelocity)
    {
        super(model);
        collisionLayer = entryType == EntryType.BACK ? CollisionLayer.ENEMY_BACK_ENTRY : CollisionLayer.ENEMY;
        position = new Vector3(spawnPos);
        holdAnchor = new Vector3(holdPos);
        holdDistanceFromPlayer = holdPos.getZ() - getPlayerPosition().getZ();
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

        if (phaseAge >= holdDuration)
        {
            beginExit();
        }
    }

    private void updateExiting(double delta)
    {
        refreshCollisionState();
        position.addXYZ(exitVelocity.getX() * delta, exitVelocity.getY() * delta, 0);
        position.setZ(getPlayerPosition().getZ() + holdDistanceFromPlayer);
        currentVelocity = new Vector3(exitVelocity);
    }

    private void beginHolding()
    {
        phase = Phase.HOLDING;
        phaseAge = 0;
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
        Vector3 holdPosition = new Vector3(holdAnchor.getX(), holdAnchor.getY(),
            getPlayerPosition().getZ() + holdDistanceFromPlayer);

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
        double relativeX = Math.abs(position.getX() - playerPos.getX());
        double relativeY = Math.abs(position.getY() - playerPos.getY());
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
