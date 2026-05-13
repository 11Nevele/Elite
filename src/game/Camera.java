package game;

import game.engine.*;
import java.awt.event.KeyEvent;

/**
 * The player camera/ship. Coordinates movement, weapons, and camera view.
 * Registered as a collidable with the "player" tag for enemy collision detection.
 */
public class Camera extends CollidableRenderable
{
    public static Camera instance;
    public static final double SCROLL_SPEED = 200;

    private static final double MUZZLE_OFFSET = 8;
    private static final double PLAYER_BOUNCE_SPEED = 40;
    private static final double PLAYER_BOUNCE_COOLDOWN_SEC = 0.3;
    private static final double BOUNCE_STUN_DURATION = 0.3;
    private static final double BOUNCE_VELOCITY_DRAG = 8;
    private static final Quaternion MODEL_UPRIGHT_ROTATION = Quaternion.roll(180);

    private final PlayerMovement movement;
    private final WeaponSystem weapons;
    private final CameraController cameraController;
    private Quaternion railRotation;
    private Vector3 bounceVelocity = new Vector3();
    private double bounceCooldownRemainingSec;
    private double bounceStunRemaining;

    public Camera(Vector3 pos, Quaternion rot)
    {
        this(pos, rot, false, KeyEvent.VK_E);
    }

    public Camera(Vector3 pos, Quaternion rot, boolean wasdOnlyMovement, int shootKey)
    {
        super(Models.playerShip);
        instance = this;
        collisionLayer = CollisionLayer.PLAYER;
        boundingRadius = 2;
        scale = 0.2;
        position = new Vector3(pos);
        rotation = rot.multiply(MODEL_UPRIGHT_ROTATION).normalize();
        railRotation = new Quaternion(rot);

        movement = wasdOnlyMovement ? PlayerMovement.createWasdOnly() : new PlayerMovement();
        weapons = new WeaponSystem(shootKey, Models.bulletModel, CollisionLayer.PLAYER);
        cameraController = new CameraController();
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);

        bounceCooldownRemainingSec = Math.max(0, bounceCooldownRemainingSec - delta);
        bounceStunRemaining = Math.max(0, bounceStunRemaining - delta);

        Vector3 dodgeOffset = bounceStunRemaining > 0 ? new Vector3() : movement.update(delta, position);
        if (!GameState.gameState.isDead())
        {
            GameState.gameState.addDistance(SCROLL_SPEED * delta);
        }

        if (bounceVelocity.magnitude() > 0.01)
        {
            double decayFactor = Math.max(0, 1.0 - BOUNCE_VELOCITY_DRAG * delta);
            bounceVelocity = bounceVelocity.multiply(decayFactor);
        }
        position = position.plus(dodgeOffset.plus(bounceVelocity.multiply(delta)));
        Quaternion shipRotation = movement.getShipRotation();
        rotation = shipRotation.multiply(MODEL_UPRIGHT_ROTATION).normalize();

        Vector3 shipForward = EngineUtil.quaternionToDirection(shipRotation).normalize();
        Vector3 muzzlePos = position.plus(shipForward.multiply(MUZZLE_OFFSET));
        weapons.update(delta, muzzlePos, shipRotation);

        cameraController.updateRendererCamera(
            delta,
            position,
            railRotation,
            movement.getHorizontalInput(),
            movement.getVerticalInput()
        );
    }

    @Override
    public void onCollisionEnter(Collidable other)
    {
        int layer = other.getCollisionLayer();
        if (GameState.gameState.isCompetitiveMode() && layer == CollisionLayer.PLAYER2 && other instanceof SecondPlayer secondPlayer)
        {
            tryBounce(secondPlayer);
            return;
        }

        if (layer == CollisionLayer.ASTEROID
            || layer == CollisionLayer.ENEMY
            || layer == CollisionLayer.ENEMY_BULLET)
        {
            if (!GameState.gameState.isDead())
            {
                Explosion.generateExplosion(position, 20);
                Audio.playExplosion();
                if (GameState.gameState.isCompetitiveMode())
                {
                    GameState.gameState.markPlayerDead(CollisionLayer.PLAYER);
                }
                else
                {
                    GameState.gameState.setCrashed(true);
                }
            }
        }
    }

    public void reset(Vector3 pos, Quaternion rot)
    {
        position = new Vector3(pos);
        rotation = rot.multiply(MODEL_UPRIGHT_ROTATION).normalize();
        railRotation = new Quaternion(rot);
        movement.reset();
        weapons.reset();
        cameraController.reset();
    }

    public static Vector3 getWorldScrollVelocity()
    {
        if (instance == null || GameState.gameState == null || GameState.gameState.isDead())
        {
            return new Vector3();
        }

        return instance.getRailForward().multiply(-SCROLL_SPEED);
    }

    public static Vector3 getWorldScrollDelta(double delta)
    {
        return getWorldScrollVelocity().multiply(delta);
    }

    public static Vector3 getWorldScrollOffset()
    {
        if (instance == null || GameState.gameState == null)
        {
            return new Vector3();
        }

        return instance.getRailForward().multiply(-GameState.gameState.getDistanceTravelled());
    }

    public WeaponSystem getWeapons()
    {
        return weapons;
    }

    void applyBounceVelocity(Vector3 velocity)
    {
        movement.resetVelocity();
        bounceVelocity = bounceVelocity.plus(velocity);
        bounceStunRemaining = BOUNCE_STUN_DURATION;
        bounceCooldownRemainingSec = PLAYER_BOUNCE_COOLDOWN_SEC;
    }

    private void tryBounce(SecondPlayer otherPlayer)
    {
        if (bounceCooldownRemainingSec > 0 || otherPlayer.isBounceCooldownActive())
        {
            return;
        }

        Vector3 diff = otherPlayer.position.minus(position);
        diff.setZ(0);
        if (diff.magnitude() == 0)
        {
            diff = new Vector3(1, 0, 0);
        }

        Vector3 separationDir = diff.normalize();
        Vector3 impulse = separationDir.multiply(PLAYER_BOUNCE_SPEED);
        applyBounceVelocity(impulse.multiply(-1));
        otherPlayer.applyBounceVelocity(impulse);
    }

    boolean isBounceCooldownActive()
    {
        return bounceCooldownRemainingSec > 0;
    }

    private Vector3 getRailForward()
    {
        return EngineUtil.quaternionToDirection(railRotation);
    }
}
