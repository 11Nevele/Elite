package game;

import game.engine.Face;
import game.engine.ObjReader;
import java.io.File;

/**
 * Loads 3D models from OBJ files.
 * Uses platform-independent file paths.
 */
public class AssetLoader
{
    private static final String MODEL_DIR = "Model" + File.separator;

    public static Face[] loadPlayerShip()
    {
        return ObjReader.readObj(MODEL_DIR + "Plane.obj");
    }

    public static Face[] loadPlayer2Ship()
    {
        return ObjReader.readObj(MODEL_DIR + "BluePlane.obj");
    }

    public static Face[] loadTieFighter()
    {
        return ObjReader.readObj(MODEL_DIR + "TIE.obj");
    }

    public static Face[] loadDeathStar()
    {
        return ObjReader.readObj(MODEL_DIR + "untitled.obj");
    }

    public static Face[] loadBullet()
    {
        return ObjReader.readObj(MODEL_DIR + "bullet.obj");
    }

    public static Face[] loadBlueBullet()
    {
        return ObjReader.readObj(MODEL_DIR + "BlueBullet.obj");
    }

    public static Face[] loadGreenBullet()
    {
        return ObjReader.readObj(MODEL_DIR + "GreenBullet.obj");
    }

    public static Face[] loadAsteroid(int variant)
    {
        return ObjReader.readObj(MODEL_DIR + "asteroids" + variant + ".obj");
    }
}
