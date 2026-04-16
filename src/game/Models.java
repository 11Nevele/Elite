package game;

import game.engine.Face;

/**
 * Central model registry. Loads and provides access to all game models.
 * Delegates to AssetLoader for OBJ models and ProceduralMeshes for generated geometry.
 */
public class Models
{
    public static Face[] playerShip;
    public static Face[] tieFighter;
    public static Face[] deathStar;
    public static Face[][] asteroids;
    public static Face[] bulletModel;

    public static void init()
    {
        playerShip = AssetLoader.loadPlayerShip();
        tieFighter = AssetLoader.loadTieFighter();
        deathStar = AssetLoader.loadDeathStar();

        asteroids = new Face[3][];
        for (int i = 0; i < 3; i++)
        {
            asteroids[i] = AssetLoader.loadAsteroid(i);
        }

        bulletModel = ProceduralMeshes.getSphere(
            0.3, 4, 4, GameColors.LASER_RED, GameColors.LASER_RED);
    }
}
