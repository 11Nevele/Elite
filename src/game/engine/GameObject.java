package game.engine;
import java.util.ArrayList;
public class GameObject implements AutoCloseable
{
    public static ArrayList<GameObject> gameObjects = new ArrayList<>();
    public static ArrayList<GameObject> removedObjects = new ArrayList<>();
    
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
        removedObjects.clear();
    }
    public static void DestroyObject(GameObject go)
    {
        removedObjects.add(go);
    }
    public Vector3 position;
    public Quaternion rotation;
    public GameObject()
    {
        position = new Vector3();
        rotation = new Quaternion(0,0,0,1);
        gameObjects.add(this);
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
    public void Update(double delta)
    {

    }
}
