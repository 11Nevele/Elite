package game;

import game.engine.*;

/**
 * Manages spawning of asteroids.
 */
public class AsteroidSpawner
{
    private static final int MAX_ASTEROIDS = 10;
    private static final double SPAWN_DISTANCE = 200;

    private int asteroidCount = 0;

    public void spawnIfNeeded(Vector3 playerPos)
    {
        while (asteroidCount < MAX_ASTEROIDS)
        {
            Vector3 spawnPos = playerPos.plus(EngineUtil.randomOnSphere(SPAWN_DISTANCE));
            int variant = (int)(Math.random() * 3);
            new Asteroid(Models.asteroids[variant], spawnPos);
            asteroidCount++;
        }
    }

    public void unregister()
    {
        asteroidCount = Math.max(0, asteroidCount - 1);
    }

    public void clear()
    {
        asteroidCount = 0;
    }
}
