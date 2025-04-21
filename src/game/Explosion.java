package game;

import game.engine.EngineUtil;
import game.engine.Face;
import game.engine.GameObject;
import game.engine.Quaternion;
import game.engine.Renderable;
import game.engine.Vector3;
import java.awt.Color;
import java.util.ArrayList;

/**
 * Creates visual explosion effects made of multiple particles.
 * Each explosion consists of multiple small triangles moving outward.
 */
public class Explosion extends Renderable
{
    /** List of all active explosions */
    static ArrayList<Explosion> explosions = new ArrayList<>();
    
    /**
     * Creates a new explosion at the specified position
     * @param position Center point for the explosion
     */
    static void GenerateExplosion(Vector3 position)
    {
        for(int i = 0; i < 50; ++i)
            explosions.add(new Explosion(position));
    }
    
    /** Max radius for explosion particles */
    final double radius = 50;
    
    /** Minimum speed for explosion particles */
    final double minSpeed = 80;
    
    /** Maximum speed for explosion particles */
    final double maxSpeed = 150;
    
    /** Velocity vector for this particle */
    final Vector3 velocity;
    
    /** Time when this explosion was created */
    long startTime;
    
    /** How long this explosion lasts in milliseconds */
    final long duration = 5000;
    
    /**
     * Creates an individual explosion particle
     * @param newPosition Center position for the explosion
     */
    public Explosion(Vector3 newPosition) 
    {
        super();
        scale = 0.5;
        //random from red to orange
        Color color = new Color(255, (int)(Math.random() * 128), 0);
        velocity = EngineUtil.RandomOnSphere(minSpeed + Math.random() * (maxSpeed - minSpeed));
        position = new Vector3(newPosition);
        model = new Face[2];
        
        model[0] = new Face(Models.explosion[0]);
        model[1] = new Face(Models.explosion[1]);
        model[0].fill = color;
        model[1].fill = color;
        Vector3 pos = EngineUtil.RandomOnSphere(1).multi(Math.random() * radius);
        //random rotation
        Quaternion rot = EngineUtil.eulerToQuaternion(new Vector3(Math.random() * 360, Math.random() * 360, Math.random() * 360));
        for(int j = 0; j < model[0].vertex.length; ++j)
        {
            model[0].vertex[j] = rot.rotate(model[0].vertex[j]);
            model[1].vertex[j] = rot.rotate(model[1].vertex[j]);
            model[0].vertex[j] = model[0].vertex[j].plus(pos);
            model[1].vertex[j] = model[1].vertex[j].plus(pos);
        }
        startTime = System.currentTimeMillis();
    }
    
    /**
     * Updates this explosion particle's position
     * @param delta Time elapsed since last update in seconds
     */
    @Override
    public void Update(double delta)
    {
        super.Update(delta);
        position = position.plus(velocity.multi(delta));
        if(System.currentTimeMillis() - startTime > duration)
        {
            GameObject.DestroyObject(this);
        }
    }

    /**
     * Cleanup when explosion particle is destroyed
     */
    @Override
    public void destroy()
    {
        super.destroy();
        explosions.remove(this);
    }
}
