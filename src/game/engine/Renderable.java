package game.engine;

/**
 * A game object that can be rendered to the screen.
 * Contains a 3D model composed of faces.
 */
public class Renderable extends GameObject
{
    public Face[] model;
    public double scale = 1;
    public double modelRadius = 0;

    public Renderable()
    {
        super();
        model = new Face[0];
    }

    public Renderable(Face[] newModel)
    {
        super();
        model = newModel;
        computeModelRadius();
    }

    private void computeModelRadius()
    {
        double maxDistSq = 0;
        for (Face f : model)
        {
            for (Vector3 v : f.vertex)
            {
                double distSq = v.getX() * v.getX() + v.getY() * v.getY() + v.getZ() * v.getZ();
                if (distSq > maxDistSq) maxDistSq = distSq;
            }
        }
        modelRadius = Math.sqrt(maxDistSq);
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);
        Renderer.renderer.render(this);
    }
}
