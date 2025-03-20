package game.engine;

public class Renderable extends GameObject
{
    public Face []model;
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
    public void Update(double delta)
    {
        super.Update(delta);
        Renderer.renderer.Render(this);
    }
    
}
