package game.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CollisionManager extends GameObject
{
    private final boolean DEBUG = false;
    public static CollisionManager instance = new CollisionManager();
    private ArrayList<Collidable> collidables = new ArrayList<>();
    private ArrayList<HashSet<String>> tags = new ArrayList<>();
    private int idCnt = 0;
    public void Clear()
    {
        collidables.clear();
        tags.clear();
        idCnt = 0;
    }
    public int register(Collidable c)
    {
        collidables.add(c);
        tags.add(new HashSet<String>());
        c.setID(idCnt++);
        return idCnt - 1;
    }
    public void deregister(Collidable c)
    {
        collidables.remove(c);
    }
    public boolean getCollision(int id, String tag)
    {
        return tags.get(id).contains(tag);
    }

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
