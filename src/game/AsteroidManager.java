package game;

import game.engine.*;

/**
 * Coordinates asteroid and enemy spawning.
 * Provides a unified interface for the game loop to manage both populations.
 */
public class AsteroidManager extends GameObject
{
    public static AsteroidManager instance = new AsteroidManager();

    private final AsteroidSpawner asteroidSpawner = new AsteroidSpawner();
    private final EnemySpawner enemySpawner = new EnemySpawner();

    @Override
    public void update(double delta)
    {
        super.update(delta);
        if (Camera.instance != null)
        {
            Vector3 playerPos = Camera.instance.position;
            enemySpawner.update(delta, playerPos);
        }
    }

    /**
     * Called when an asteroid or enemy is destroyed.
     */
    public void unregister(Object obj)
    {
        if (obj instanceof Asteroid)
        {
            asteroidSpawner.unregister();
        }
        else if (obj instanceof Enemy)
        {
            enemySpawner.unregister();
        }
    }

    public void clear()
    {
        asteroidSpawner.clear();
        enemySpawner.clear();
    }
}
