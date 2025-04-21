package game;

import java.util.ArrayList;

/**
 * Manages the creation and tracking of asteroids and enemies in the game.
 * Ensures that there are always a minimum number of objects in the game world.
 */
public class AstroidManager
{
    /** Singleton instance */
    public static AstroidManager instance = new AstroidManager();

    /** List of all active asteroids */
    private ArrayList<Astroid> astroids = new ArrayList<>();
    
    /** List of all active enemies */
    private ArrayList<Enemy> enemies = new ArrayList<>();
    
    /**
     * Default constructor
     */
    public AstroidManager()
    {
    }
    
    /**
     * Clears all tracked objects
     */
    public void Clear()
    {
        astroids.clear();
        enemies.clear();
    }

    /**
     * Updates the asteroid and enemy population
     * @param delta Time elapsed since last update in seconds
     */
    public void Update(double delta)
    {
        while(astroids.size() < 50)
        {
            astroids.add(new Astroid());
        }
        while(enemies.size() < 10)
        {
            enemies.add(new Enemy(Models.Plane));
        }
    }
    
    /**
     * Removes an enemy from tracking when destroyed
     * @param enemy Enemy to unregister
     */
    public void Unrigister(Enemy enemy)
    {
        enemies.remove(enemy);
    }
    
    /**
     * Removes an asteroid from tracking when destroyed
     * @param astroid Asteroid to unregister
     */
    public void Unrigister(Astroid astroid)
    {
        astroids.remove(astroid);
    }
}
