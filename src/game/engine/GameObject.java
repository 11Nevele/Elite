package game.engine;
import java.util.ArrayList;

/**
 * Base class for all game objects in the game world.
 * Provides position, rotation, and lifecycle management.
 * Implements AutoCloseable for resource management.
 */
public class GameObject implements AutoCloseable
{
    /** List of all active game objects */
    public static ArrayList<GameObject> gameObjects = new ArrayList<>();
    
    /** List of objects to be removed at end of frame */
    public static ArrayList<GameObject> removedObjects = new ArrayList<>();
    
    /** List of objects to be added at end of frame */
    public static ArrayList<GameObject> newObjects = new ArrayList<>();
    
    /**
     * Updates all game objects and handles object creation/destruction
     * @param delta Time elapsed since last update in seconds
     */
    public static void UpdateAll(double delta)
    {
        for (GameObject go : gameObjects)
        {
            go.Update(delta);
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
    
    /**
     * Marks a game object for destruction
     * @param go GameObject to destroy
     */
    public static void DestroyObject(GameObject go)
    {
        removedObjects.add(go);
    }
    
    /**
     * Adds a new game object to the world
     * @param go GameObject to add
     */
    public static void AddObject(GameObject go)
    {
        newObjects.add(go);
    }
    
    /** Position of this object in 3D space */
    public Vector3 position;
    
    /** Rotation of this object as a quaternion */
    public Quaternion rotation;
    
    /**
     * Default constructor - creates a game object at origin with identity rotation
     */
    public GameObject()
    {
        position = new Vector3();
        rotation = new Quaternion(0,0,0,1);
        newObjects.add(this);
    }
    
    /**
     * Gets the rotation of this object as Euler angles
     * @return Vector3 containing roll, pitch, and yaw in degrees
     */
    public Vector3 rotation()
    {
        return rotation.asEuler();
    }
    
    /**
     * AutoCloseable implementation to clean up resources
     */
    @Override
    public void close()
    {
        destroy();
    }
    
    /**
     * Clean up resources and remove from the game world
     */
    public void destroy()
    {
        gameObjects.remove(this);
    }
    
    /**
     * Update this object's state
     * @param delta Time elapsed since last update in seconds
     */
    public void Update(double delta)
    {
        // Base implementation does nothing - subclasses should override
    }
}
