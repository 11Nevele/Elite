package game.engine;

import java.util.ArrayList;

/**
 * Base class for all game objects in the game world.
 * Provides position, rotation, and lifecycle management.
 */
public class GameObject implements AutoCloseable
{
    public static ArrayList<GameObject> gameObjects = new ArrayList<>();
    public static ArrayList<GameObject> removedObjects = new ArrayList<>();
    public static ArrayList<GameObject> newObjects = new ArrayList<>();

    public static void updateAll(double delta)
    {
        for (GameObject go : gameObjects)
        {
            go.update(delta);
        }
        for (GameObject go : removedObjects)
        {
            go.close();
        }
        for (GameObject go : newObjects)
        {
            gameObjects.add(go);
        }
        newObjects.clear();
        removedObjects.clear();
    }

    public static void destroyObject(GameObject go)
    {
        removedObjects.add(go);
    }

    public static void addObject(GameObject go)
    {
        newObjects.add(go);
    }

    public Vector3 position;
    public Quaternion rotation;

    public GameObject()
    {
        position = new Vector3();
        rotation = new Quaternion(0, 0, 0, 1);
        newObjects.add(this);
    }

    public Vector3 rotation()
    {
        return rotation.asEuler();
    }

    @Override
    public void close()
    {
        destroy();
    }

    public void destroy()
    {
        gameObjects.remove(this);
    }

    public void update(double delta)
    {
    }
}
