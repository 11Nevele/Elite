package game;

import game.engine.Face;

/**
 * Central model registry. Loads and provides access to all game models.
 * Delegates to AssetLoader for OBJ models and ProceduralMeshes for generated geometry.
 */
public class Models
{
    public static Face[] playerShip;
    public static Face[] player2Ship;
    public static Face[] tieFighter;
    public static Face[] deathStar;
    public static Face[][] asteroids;
    public static Face[] bulletModel;
    public static Face[] blueBulletModel;
    public static Face[] enemyBulletModel;

    public static void init()
    {
        playerShip = AssetLoader.loadPlayerShip();
        player2Ship = AssetLoader.loadPlayer2Ship();
        tieFighter = AssetLoader.loadTieFighter();
        deathStar = AssetLoader.loadDeathStar();
        bulletModel = AssetLoader.loadBullet();
        blueBulletModel = AssetLoader.loadBlueBullet();
        enemyBulletModel = AssetLoader.loadGreenBullet();

        asteroids = new Face[3][];
        for (int i = 0; i < 3; i++)
        {
            asteroids[i] = AssetLoader.loadAsteroid(i);
        }

    }
}
