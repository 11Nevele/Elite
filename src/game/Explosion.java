package game;

import game.engine.*;

/**
 * Represents an explosion particle that expands outward and fades.
 */
public class Explosion extends Renderable
{
    private static final double EXPANSION_SPEED = 30;
    private static final double LIFETIME = 1.5;

    private Vector3 velocity;
    private double age = 0;

    public Explosion(Vector3 pos, Vector3 dir, Face[] particleModel)
    {
        super(particleModel);
        position = new Vector3(pos);
        velocity = dir;
        scale = 0.5;
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);
        age += delta;
        if (age > LIFETIME)
        {
            GameObject.destroyObject(this);
            return;
        }
        position = position.plus(velocity.multiply(delta));
        scale = 0.5 + age * 2;
    }

    /**
     * Generates an explosion at the given position with random particle directions.
     * @param pos World position for the explosion center
     * @param count Number of particles to spawn
     */
    public static void generateExplosion(Vector3 pos, int count)
    {
        Face[] particleModel = ProceduralMeshes.getExplosionParticle(
            0.3, GameColors.EXPLOSION_ORANGE);

        for (int i = 0; i < count; i++)
        {
            Vector3 dir = EngineUtil.randomOnSphere(EXPANSION_SPEED);
            new Explosion(pos, dir, particleModel);
        }
        Audio.playExplosion();
    }
}
