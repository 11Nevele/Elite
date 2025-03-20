package game;

import java.util.ArrayList;

public class AstroidManager
{
    public static AstroidManager instance = new AstroidManager();

    private ArrayList<Astroid> astroids = new ArrayList<>();
    public AstroidManager()
    {
    }
    
    public void Clear()
    {
        astroids.clear();
    }

    public void Update(double delta)
    {
        while(astroids.size() < 30)
        {
            astroids.add(new Astroid());
        }
    }
    
    
    public void Unrigister(Astroid astroid)
    {
        astroids.remove(astroid);
    }
    
}
