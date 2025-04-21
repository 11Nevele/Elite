package game.engine;

/**
 * A class that represents objects that can both be rendered and participate in collision detection.
 * This class extends Renderable for visual representation and implements Collidable for physics interactions.
 * Objects of this class are automatically registered with the CollisionManager upon creation.
 */
public class CollidableRenderable extends Renderable implements Collidable
{
    /** The radius of the bounding sphere used for collision detection. */
    public double boundingRadius = 1;
    /** Unique identifier for this object. */
    private int id = 0;
    /** Tag used to identify the type or category of this object. */
    public String tag = "default";
    
    /**
     * Default constructor that creates a CollidableRenderable with default model.
     * Automatically registers this object with the CollisionManager.
     */
    public CollidableRenderable()
    {
        super();
        CollisionManager.instance.register(this);
    }
    
    /**
     * Constructor that creates a CollidableRenderable with the specified model.
     * Automatically registers this object with the CollisionManager.
     * 
     * @param newModel Array of Face objects representing the 3D model
     */
    public CollidableRenderable(Face[] newModel) 
    {
        super(newModel);
        CollisionManager.instance.register(this);
    }
    
    /**
     * Updates the object's state for the current frame.
     * 
     * @param delta Time elapsed since the last update in seconds
     */
    @Override
    public void Update(double delta)
    {
        super.Update(delta);
    }
    
    /**
     * Determines if this object collides with another collidable object.
     * Uses a simple distance-based sphere collision detection algorithm.
     * 
     * @param other The other collidable object to check collision against
     * @return true if the objects' bounding spheres overlap, false otherwise
     */
    @Override
    public boolean Collides(Collidable other)
    {
        return getPosition().distance(other.getPosition()) < getBoundingRadius() + other.getBoundingRadius();
    }
    
    /**
     * Gets the radius of the bounding sphere used for collision detection.
     * 
     * @return The radius of the bounding sphere
     */
    @Override
    public double getBoundingRadius()
    {
        return boundingRadius;
    }
    
    /**
     * Gets the current position of the object in 3D space.
     * 
     * @return The Vector3 representing the object's position
     */
    @Override
    public Vector3 getPosition()
    {
        return position;
    }
    
    /**
     * Gets the unique identifier for this collidable object.
     * 
     * @return The integer ID of this object
     */
    @Override
    public int getID()
    {
        return id;
    }
    
    /**
     * Sets the unique identifier for this collidable object.
     * 
     * @param id The integer ID to set
     */
    @Override
    public void setID(int id)
    {
        this.id = id;
    }
    
    /**
     * Destroys this object and deregisters it from the CollisionManager.
     */
    @Override
    public void destroy()
    {
        CollisionManager.instance.deregister(this);
        super.destroy();
    }
    
    /**
     * Gets the tag used to identify the type or category of this object.
     * 
     * @return The tag of this object
     */
    @Override
    public String getTag()
    {
        return tag;
    }
    
    /**
     * Gets this object as a CollidableRenderable.
     * 
     * @return This object
     */
    @Override
    public CollidableRenderable getGameObject()
    {
        return this;
    }
}
