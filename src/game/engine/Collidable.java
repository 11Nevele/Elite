package game.engine;

/**
 * Interface for objects that can participate in collision detection.
 * Any GameObject can implement this to participate in the collision system.
 */
public interface Collidable 
{
    boolean collidesWith(Collidable other);
    double getBoundingRadius();
    Vector3 getPosition();
    int getID();
    void setID(int id);
    int getCollisionLayer();
    GameObject getGameObject();
    void onCollisionEnter(Collidable other);
    void onCollisionExit(Collidable other);
}
