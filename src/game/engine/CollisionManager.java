package game.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Manages collision detection and tracking between game objects.
 * Provides ray casting functionality for object detection.
 */
public class CollisionManager extends GameObject
{
    public static CollisionManager instance = new CollisionManager();

    private ArrayList<Collidable> collidables = new ArrayList<>();
    private HashMap<Integer, HashSet<String>> tags = new HashMap<>();
    private int idCnt = 0;

    public void clear()
    {
        collidables.clear();
        tags.clear();
        idCnt = 0;
    }

    public int register(Collidable c)
    {
        int id = idCnt++;
        c.setID(id);
        collidables.add(c);
        tags.put(id, new HashSet<>());
        return id;
    }

    public void deregister(Collidable c)
    {
        collidables.remove(c);
        tags.remove(c.getID());
    }

    public boolean getCollision(int id, String tag)
    {
        HashSet<String> set = tags.get(id);
        return set != null && set.contains(tag);
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);
        long t0 = System.nanoTime();
        for (Collidable c : collidables)
        {
            HashSet<String> set = tags.get(c.getID());
            if (set != null) set.clear();
        }
        for (int i = 0; i < collidables.size(); i++)
        {
            Collidable c = collidables.get(i);
            for (int j = i + 1; j < collidables.size(); j++)
            {
                Collidable other = collidables.get(j);
                if (c.collidesWith(other))
                {
                    HashSet<String> cTags = tags.get(c.getID());
                    HashSet<String> otherTags = tags.get(other.getID());
                    if (cTags != null) cTags.add(other.getTag());
                    if (otherTags != null) otherTags.add(c.getTag());
                }
            }
        }
        long t1 = System.nanoTime();
        game.Profiler.instance.setCollisionTime(t1 - t0);
        game.Profiler.instance.setCollidableCount(collidables.size());
    }

    public ArrayList<Collidable> raycast(Vector3 origin, Vector3 direction)
    {
        ArrayList<Collidable> hits = new ArrayList<>();
        for (Collidable colli : collidables)
        {
            Vector3 oc = origin.minus(colli.getPosition());
            double a = direction.dot(direction);
            double b = 2.0 * oc.dot(direction);
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
