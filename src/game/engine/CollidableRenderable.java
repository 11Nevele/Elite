package game.engine;

/**
 * A game object that can both be rendered and participate in collision detection.
 * Automatically registered with CollisionManager upon creation.
 */
public class CollidableRenderable extends Renderable implements Collidable
{
    public double boundingRadius = 1;
    private int id = 0;
    public String tag = "default";

    public CollidableRenderable()
    {
        super();
        CollisionManager.instance.register(this);
    }

    public CollidableRenderable(Face[] newModel)
    {
        super(newModel);
        CollisionManager.instance.register(this);
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);
    }

    @Override
    public boolean collidesWith(Collidable other)
    {
        return getPosition().distance(other.getPosition()) < getBoundingRadius() + other.getBoundingRadius();
    }

    @Override
    public double getBoundingRadius()
    {
        return boundingRadius;
    }

    @Override
    public Vector3 getPosition()
    {
        return position;
    }

    @Override
    public int getID()
    {
        return id;
    }

    @Override
    public void setID(int id)
    {
        this.id = id;
    }

    @Override
    public String getTag()
    {
        return tag;
    }

    @Override
    public GameObject getGameObject()
    {
        return this;
    }

    @Override
    public void destroy()
    {
        CollisionManager.instance.deregister(this);
        super.destroy();
    }
}
