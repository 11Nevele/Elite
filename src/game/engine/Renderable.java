package game.engine;

/**
 * A game object that can be rendered to the screen.
 * Contains a 3D model composed of faces.
 */
public class Renderable extends GameObject
{
    /** The 3D model data as an array of Face objects */
    public Face []model;
    
    /** Scale factor for the model */
    public double scale = 1;
    
    /**
     * Default constructor - creates an empty renderable object
     */
    public Renderable()
    {
        super();
        model = new Face[0];
    }
    
    /**
     * Constructor with model data
     * @param newModel Array of Face objects comprising the 3D model
     */
    public Renderable(Face[] newModel) 
    {
        super();
        model = newModel;
    }

    /**
     * Updates this renderable object and submits it for rendering
     * @param delta Time elapsed since last update in seconds
     */
    @Override
    public void Update(double delta)
    {
        super.Update(delta);
        Renderer.renderer.Render(this);
    }
}
