package game.engine;

/**
 * The Collidable interface represents objects in the game that can collide with each other.
 * Classes that implement this interface must provide collision detection, positioning,
 * and identification functionality.
 */
public interface Collidable 
{
    /**
     * Determines if this object collides with another collidable object.
     * @param other The other collidable object to check collision against
     * @return true if the objects collide, false otherwise
     */
    boolean Collides(Collidable other);
    
    /**
     * Gets the radius of the bounding sphere used for collision detection.
     * @return The radius of the bounding sphere
     */
    double getBoundingRadius();
    
    /**
     * Gets the current position of the object in 3D space.
     * @return The Vector3 representing the object's position
     */
    Vector3 getPosition();    
    
    /**
     * Gets the unique identifier for this collidable object.
     * @return The integer ID of this object
     */
    int getID();
    
    /**
     * Sets the unique identifier for this collidable object.
     * @param id The integer ID to assign to this object
     */
    void setID(int id);
    
    /**
     * Gets the tag that identifies the type or category of this object.
     * @return A string representing the object's tag
     */
    String getTag();
    
    /**
     * Gets the game object associated with this collidable.
     * @return The CollidableRenderable object representing this collidable in the game
     */
    CollidableRenderable getGameObject();
}
