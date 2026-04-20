package game.engine;

import java.util.HashMap;

/**
 * Defines collision layers as bit flags and manages the collision matrix.
 * Use setCollision() to enable/disable collision between specific layers.
 */
public class CollisionLayer
{
    public static final int NONE     = 0;
    public static final int PLAYER   = 1;
    public static final int ENEMY    = 1 << 1;
    public static final int ASTEROID = 1 << 2;
    public static final int BULLET   = 1 << 3;
    public static final int ENEMY_BACK_ENTRY = 1 << 4;

    // Maps each layer bit to a bitmask of layers it collides with
    private static HashMap<Integer, Integer> collisionMatrix = new HashMap<>();

    static
    {
        // Default collision rules
        setCollision(PLAYER, ENEMY, true);
        setCollision(PLAYER, ASTEROID, true);
        setCollision(BULLET, ENEMY, true);
        setCollision(BULLET, ASTEROID, true);
        setCollision(BULLET, ENEMY_BACK_ENTRY, true);
    }

    /**
     * Enable or disable collision between two layers.
     * This is symmetric: setCollision(A, B, true) also sets B collides with A.
     */
    public static void setCollision(int layerA, int layerB, boolean enabled)
    {
        if (enabled)
        {
            collisionMatrix.put(layerA, collisionMatrix.getOrDefault(layerA, 0) | layerB);
            collisionMatrix.put(layerB, collisionMatrix.getOrDefault(layerB, 0) | layerA);
        }
        else
        {
            collisionMatrix.put(layerA, collisionMatrix.getOrDefault(layerA, 0) & ~layerB);
            collisionMatrix.put(layerB, collisionMatrix.getOrDefault(layerB, 0) & ~layerA);
        }
    }

    /**
     * Returns true if the two layers can collide according to the collision matrix.
     */
    public static boolean canCollide(int layerA, int layerB)
    {
        int maskA = collisionMatrix.getOrDefault(layerA, 0);
        return (maskA & layerB) != 0;
    }
}
