package game.engine;

/**
 * A game object that can be rendered to the screen.
 * Contains a 3D model composed of faces.
 */
public class Renderable extends GameObject
{
    public Face[] model;
    public double scale = 1;

    public Renderable()
    {
        super();
        model = new Face[0];
    }

    public Renderable(Face[] newModel)
    {
        super();
        model = newModel;
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);
        Renderer.renderer.render(this);
    }
}
