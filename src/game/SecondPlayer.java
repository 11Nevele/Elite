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
    private static final Quaternion MODEL_UPRIGHT_ROTATION = Quaternion.roll(180);

    private final PlayerMovement movement;
    private final WeaponSystem weapons;

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
        weapons = new WeaponSystem(KeyEvent.VK_CONTROL, Models.blueBulletModel);
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);

        Vector3 dodgeOffset = movement.update(delta, position);
        position = position.plus(dodgeOffset);

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
        if (layer == CollisionLayer.ASTEROID
            || layer == CollisionLayer.ENEMY
            || layer == CollisionLayer.ENEMY_BULLET)
        {
            if (!GameState.gameState.isDead())
            {
                Explosion.generateExplosion(position, 20);
                GameState.gameState.setCrashed(true);
            }
        }
    }

    public WeaponSystem getWeapons()
    {
        return weapons;
    }
}
