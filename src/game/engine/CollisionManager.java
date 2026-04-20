package game.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Manages collision detection between game objects using a layer-based system.
 * Calls onCollisionEnter/onCollisionExit callbacks on collidables.
 * Provides ray casting functionality for object detection.
 */
public class CollisionManager extends GameObject
{
    public static CollisionManager instance = new CollisionManager();

    private ArrayList<Collidable> collidables = new ArrayList<>();
    private HashMap<Integer, HashSet<Integer>> activeCollisions = new HashMap<>();
    private HashMap<Integer, Collidable> idMap = new HashMap<>();
    private int idCnt = 0;

    public void clear()
    {
        collidables.clear();
        activeCollisions.clear();
        idMap.clear();
        idCnt = 0;
    }

    public List<Collidable> getCollidables()
    {
        return Collections.unmodifiableList(collidables);
    }

    public int register(Collidable c)
    {
        int id = idCnt++;
        c.setID(id);
        collidables.add(c);
        activeCollisions.put(id, new HashSet<>());
        idMap.put(id, c);
        return id;
    }

    public void deregister(Collidable c)
    {
        int id = c.getID();
        collidables.remove(c);
        activeCollisions.remove(id);
        idMap.remove(id);

        // Remove this id from all other active collision sets
        for (HashSet<Integer> set : activeCollisions.values())
        {
            set.remove(id);
        }
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);
        long t0 = System.nanoTime();

        // Build new collision set for this frame
        HashMap<Integer, HashSet<Integer>> newCollisions = new HashMap<>();
        for (Collidable c : collidables)
        {
            newCollisions.put(c.getID(), new HashSet<>());
        }

        for (int i = 0; i < collidables.size(); i++)
        {
            Collidable a = collidables.get(i);
            for (int j = i + 1; j < collidables.size(); j++)
            {
                Collidable b = collidables.get(j);

                // Skip pairs that can't collide based on layers
                if (!CollisionLayer.canCollide(a.getCollisionLayer(), b.getCollisionLayer()))
                    continue;

                if (a.collidesWith(b))
                {
                    HashSet<Integer> aSet = newCollisions.get(a.getID());
                    HashSet<Integer> bSet = newCollisions.get(b.getID());
                    if (aSet != null) aSet.add(b.getID());
                    if (bSet != null) bSet.add(a.getID());
                }
            }
        }

        // Fire onCollisionEnter for new collisions
        for (Collidable c : collidables)
        {
            int id = c.getID();
            HashSet<Integer> prev = activeCollisions.getOrDefault(id, new HashSet<>());
            HashSet<Integer> curr = newCollisions.getOrDefault(id, new HashSet<>());

            for (int otherId : curr)
            {
                if (!prev.contains(otherId))
                {
                    Collidable other = idMap.get(otherId);
                    if (other != null)
                    {
                        c.onCollisionEnter(other);
                    }
                }
            }
        }

        // Fire onCollisionExit for ended collisions
        for (Collidable c : collidables)
        {
            int id = c.getID();
            HashSet<Integer> prev = activeCollisions.getOrDefault(id, new HashSet<>());
            HashSet<Integer> curr = newCollisions.getOrDefault(id, new HashSet<>());

            for (int otherId : prev)
            {
                if (!curr.contains(otherId))
                {
                    Collidable other = idMap.get(otherId);
                    if (other != null)
                    {
                        c.onCollisionExit(other);
                    }
                }
            }
        }

        activeCollisions = newCollisions;

        long t1 = System.nanoTime();
        game.Profiler.instance.setCollisionTime(t1 - t0);
        game.Profiler.instance.setCollidableCount(collidables.size());
    }

    public ArrayList<Collidable> raycast(Vector3 origin, Vector3 direction)
    {
        return raycast(origin, direction, ~0);
    }

    public ArrayList<Collidable> raycast(Vector3 origin, Vector3 direction, int layerMask)
    {
        ArrayList<Collidable> hits = new ArrayList<>();
        for (Collidable colli : collidables)
        {
            if ((colli.getCollisionLayer() & layerMask) == 0)
                continue;

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
