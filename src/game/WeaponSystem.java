package game;

import game.engine.*;
import java.awt.event.KeyEvent;

/**
 * Manages the weapon system: shooting, bullet creation, cooldowns.
 */
public class WeaponSystem
{
    private static final double SHOOT_COOLDOWN = 0.15;
    private static final double BULLET_SPEED = 500;
    private static final double BULLET_LIFETIME = 2.0;

    private double shootTimer = 0;

    public void reset()
    {
        shootTimer = 0;
    }

    public void update(double delta, Vector3 position, Quaternion rotation)
    {
        if (GameState.gameState.isDead()) return;

        shootTimer -= delta;

        if (Input.input.keys[KeyEvent.VK_SPACE] && shootTimer <= 0)
        {
            shoot(position, rotation);
            shootTimer = SHOOT_COOLDOWN;
        }
    }

    private void shoot(Vector3 position, Quaternion rotation)
    {
        Vector3 direction = EngineUtil.quaternionToDirection(rotation);
        new Bullet(position, direction.multiply(BULLET_SPEED), BULLET_LIFETIME);
        Audio.playLaser();
    }
}
