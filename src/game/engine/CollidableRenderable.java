package game.engine;

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
    public void Update(double delta)
    {
        super.Update(delta);
    }
    @Override
    public boolean Collides(Collidable other)
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
    public void destroy()
    {
        CollisionManager.instance.deregister(this);
        super.destroy();
    }
    @Override
    public String getTag()
    {
        return tag;
    }
    @Override
    public CollidableRenderable getGameObject()
    {
        return this;
    }
    
}
