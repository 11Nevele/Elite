package game;

import game.engine.*;

/**
 * Manages spawning of enemy ships.
 */
public class EnemySpawner
{
    private static final int MAX_ENEMIES = 10;
    private static final double SPAWN_DISTANCE = 300;

    private int enemyCount = 0;

    public void spawnIfNeeded(Vector3 playerPos)
    {
        while (enemyCount < MAX_ENEMIES)
        {
            Vector3 spawnPos = playerPos.plus(EngineUtil.randomOnSphere(SPAWN_DISTANCE));
            new Enemy(Models.playerShip, spawnPos);
            enemyCount++;
        }
    }

    public void unregister()
    {
        enemyCount = Math.max(0, enemyCount - 1);
    }

    public void clear()
    {
        enemyCount = 0;
    }
}
