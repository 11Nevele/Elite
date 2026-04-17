package game;

import game.engine.*;

/**
 * The player camera/ship. Coordinates movement, weapons, and camera view.
 * Registered as a collidable with the "player" tag for enemy collision detection.
 */
public class Camera extends CollidableRenderable
{
    public static Camera instance;

    private PlayerMovement movement;
    private WeaponSystem weapons;
    private CameraController cameraController;

    public Camera(Vector3 pos, Quaternion rot)
    {
        super(Models.tieFighter);
        instance = this;
        collisionLayer = CollisionLayer.PLAYER;
        boundingRadius = 5;
        position = new Vector3(pos);
        rotation = new Quaternion(rot);

        movement = new PlayerMovement();
        weapons = new WeaponSystem();
        cameraController = new CameraController();
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);

        // Rotation
        Quaternion rotDelta = movement.getRotationDelta(delta);
        rotation = rotation.multiply(rotDelta);

        // Movement
        Vector3 velocity = movement.update(delta, rotation);
        position = position.plus(velocity);

        // Weapon system
        weapons.update(delta, position, rotation);

        // Update renderer camera
        cameraController.updateRendererCamera(position, rotation);

        // Speed percentage for zoom effect
        Renderer.renderer.spdPercentage = movement.getSpeed() / movement.getMaxSpeed();
    }

    @Override
    public void onCollisionEnter(Collidable other)
    {
        int layer = other.getCollisionLayer();
        if (layer == CollisionLayer.ASTEROID || layer == CollisionLayer.ENEMY)
        {
            if (!GameState.gameState.isDead())
            {
                Explosion.generateExplosion(position, 20);
                GameState.gameState.setCrashed(true);
            }
        }
    }

    public void reset(Vector3 pos, Quaternion rot)
    {
        position = new Vector3(pos);
        rotation = new Quaternion(rot);
        movement.reset();
        weapons.reset();
        cameraController.reset();
    }

    public double getSpeed() { return movement.getSpeed(); }
    public double getFuel() { return movement.getFuel(); }
    public double getMaxSpeed() { return movement.getMaxSpeed(); }
    public double getInitialFuel() { return movement.getInitialFuel(); }
}
