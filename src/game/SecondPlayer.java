package game;

import game.engine.*;
import java.awt.event.KeyEvent;

/**
 * A second local player ship used in two-player mode.
 */
public class SecondPlayer extends CollidableRenderable
{
    public static SecondPlayer instance;

    private static final double MUZZLE_OFFSET = 8;
    private static final double PLAYER_BOUNCE_SPEED = 40;
    private static final double PLAYER_BOUNCE_COOLDOWN_SEC = 0.3;
    private static final double BOUNCE_STUN_DURATION = 0.3;
    private static final double BOUNCE_VELOCITY_DRAG = 8;
    private static final Quaternion MODEL_UPRIGHT_ROTATION = Quaternion.roll(180);

    private final PlayerMovement movement;
    private final WeaponSystem weapons;
    private Vector3 bounceVelocity = new Vector3();
    private double bounceCooldownRemainingSec;
    private double bounceStunRemaining;

    public SecondPlayer(Vector3 pos, Quaternion rot)
    {
        super(Models.player2Ship);
        instance = this;
        collisionLayer = CollisionLayer.PLAYER2;
        boundingRadius = 1;
        scale = 0.2;
        position = new Vector3(pos);
        rotation = rot.multiply(MODEL_UPRIGHT_ROTATION).normalize();

        movement = PlayerMovement.createArrowOnly();
        weapons = new WeaponSystem(KeyEvent.VK_I, Models.blueBulletModel, CollisionLayer.PLAYER2);
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);

        bounceCooldownRemainingSec = Math.max(0, bounceCooldownRemainingSec - delta);
        bounceStunRemaining = Math.max(0, bounceStunRemaining - delta);

        Vector3 dodgeOffset = bounceStunRemaining > 0 ? new Vector3() : movement.update(delta, position);

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
    }

    @Override
    public void onCollisionEnter(Collidable other)
    {
        int layer = other.getCollisionLayer();
        if (GameState.gameState.isCompetitiveMode() && layer == CollisionLayer.PLAYER && other instanceof Camera camera)
        {
            tryBounce(camera);
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
                    GameState.gameState.markPlayerDead(CollisionLayer.PLAYER2);
                }
                else
                {
                    GameState.gameState.setCrashed(true);
                }
            }
        }
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

    private void tryBounce(Camera otherPlayer)
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
}
