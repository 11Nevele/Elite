package game.engine;

/**
 * Interface for objects that can participate in collision detection.
 */
public interface Collidable 
{
    boolean collidesWith(Collidable other);
    double getBoundingRadius();
    Vector3 getPosition();
    int getID();
    void setID(int id);
    String getTag();
    GameObject getGameObject();
}
