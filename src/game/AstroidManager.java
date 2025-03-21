package game;

import java.util.ArrayList;

public class AstroidManager
{
    public static AstroidManager instance = new AstroidManager();

    private ArrayList<Astroid> astroids = new ArrayList<>();
    private ArrayList<Enemy> enemies = new ArrayList<>();
    public AstroidManager()
    {

    }
    
    public void Clear()
    {
        astroids.clear();
        enemies.clear();
    }

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
    
    public void Unrigister(Enemy enemy)
    {
        enemies.remove(enemy);
    }
    public void Unrigister(Astroid astroid)
    {
        astroids.remove(astroid);
    }
    
}
