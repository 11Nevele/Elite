package game.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Manages collision detection and tracking between game objects.
 * This singleton class handles registration of collidable objects,
 * performs collision checks, and keeps track of collision states.
 * It also provides ray casting functionality for object detection.
 */
public class CollisionManager extends GameObject
{
    /** Debug flag to enable/disable collision logging. */
    private final boolean DEBUG = false;
    
    /** Singleton instance of the CollisionManager. */
    public static CollisionManager instance = new CollisionManager();
    
    /** List of all registered collidable objects. */
    private ArrayList<Collidable> collidables = new ArrayList<>();
    
    /** Collision tag tracking for each registered object, indexed by ID. */
    private ArrayList<HashSet<String>> tags = new ArrayList<>();
    
    /** Counter for generating unique IDs for collidable objects. */
    private int idCnt = 0;
    
    /**
     * Clears all collidable registrations and resets the ID counter.
     * Useful when switching game levels or resetting the game state.
     */
    public void Clear()
    {
        collidables.clear();
        tags.clear();
        idCnt = 0;
    }
    
    /**
     * Registers a new collidable object with the collision system.
     * Assigns a unique ID to the object and initializes its collision tracking.
     *
     * @param c The collidable object to register
     * @return The assigned ID for the registered collidable
     */
    public int register(Collidable c)
    {
        collidables.add(c);
        tags.add(new HashSet<String>());
        c.setID(idCnt++);
        return idCnt - 1;
    }
    
    /**
     * Deregisters a collidable object from the collision system.
     * Should be called when an object is destroyed or no longer needs collision detection.
     *
     * @param c The collidable object to deregister
     */
    public void deregister(Collidable c)
    {
        collidables.remove(c);
    }
    
    /**
     * Checks if an object with the specified ID has collided with an object of the given tag.
     *
     * @param id The ID of the object to check
     * @param tag The tag to check for collision with
     * @return true if the object has collided with an object of the specified tag, false otherwise
     */
    public boolean getCollision(int id, String tag)
    {
        return tags.get(id).contains(tag);
    }

    /**
     * Updates the collision system by checking for collisions between all registered objects.
     * Clears previous collision data and performs new collision checks each frame.
     *
     * @param delta Time elapsed since the last update in seconds
     */
    @Override
    public void Update(double delta)
    {
        super.Update(delta);
        for (Collidable c : collidables)
        {
            tags.get(c.getID()).clear();
            for (Collidable other : collidables)
            {
                if (c.getID() == other.getID())
                {
                    continue;
                }
                if (c.Collides(other))
                {
                    if(DEBUG)
                    {
                        System.out.println("Collision between " + c.getID() + " and " + other.getID());
                    }
                    tags.get(c.getID()).add(other.getTag());
                    tags.get(other.getID()).add(c.getTag());
                }
            }
        }
    }
    
    /**
     * Performs a ray cast from a specified origin in a given direction.
     * Returns all collidable objects whose bounding spheres intersect with the ray.
     *
     * @param origin The starting point of the ray
     * @param direction The direction of the ray
     * @return List of collidable objects hit by the ray
     */
    public ArrayList<Collidable> Raycast(Vector3 origin, Vector3 direction)
    {
        ArrayList<Collidable> hits = new ArrayList<>();
        for(Collidable colli : collidables)
        {
            Vector3 oc = origin.minus(colli.getPosition());
            double a = direction.dot(direction);
            double b = 2.0f * oc.dot(direction);
            double c = oc.dot(oc) - colli.getBoundingRadius() * colli.getBoundingRadius();
            double discriminant = b * b - 4 * a * c;

            if (discriminant >= 0) 
            {
                hits.add(colli);
            }
        }
        return hits;
    }
}
