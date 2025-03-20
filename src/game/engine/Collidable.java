package game.engine;

public interface Collidable 
{
    boolean Collides(Collidable other);
    double getBoundingRadius();
    Vector3 getPosition();    
    int getID();
    void setID(int id);
    String getTag();
    CollidableRenderable getGameObject();
}
