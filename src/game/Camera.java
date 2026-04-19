package game;

import game.engine.*;

/**
 * The player camera/ship. Coordinates movement, weapons, and camera view.
 * Registered as a collidable with the "player" tag for enemy collision detection.
 */
public class Camera extends CollidableRenderable
{
    public static Camera instance;
    public static final double SCROLL_SPEED = 300;

    private static final double MUZZLE_OFFSET = 8;

    private final PlayerMovement movement;
    private final WeaponSystem weapons;
    private final CameraController cameraController;
    private Quaternion railRotation;

    public Camera(Vector3 pos, Quaternion rot)
    {
        super(Models.playerShip);
        instance = this;
        collisionLayer = CollisionLayer.PLAYER;
        boundingRadius = 5;
        position = new Vector3(pos);
        rotation = new Quaternion(rot);
        railRotation = new Quaternion(rot);

        movement = new PlayerMovement();
        weapons = new WeaponSystem();
        cameraController = new CameraController();
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);

        Vector3 dodgeOffset = movement.update(delta, position);
        Vector3 forwardOffset = new Vector3();
        if (!GameState.gameState.isDead())
        {
            forwardOffset = EngineUtil.quaternionToDirection(railRotation).multiply(SCROLL_SPEED * delta);
            GameState.gameState.addDistance(SCROLL_SPEED * delta);
        }

        position = position.plus(dodgeOffset).plus(forwardOffset);
        rotation = movement.getShipRotation();

        Vector3 muzzlePos = position.plus(EngineUtil.quaternionToDirection(railRotation).multiply(MUZZLE_OFFSET));
        weapons.update(delta, muzzlePos, railRotation);

        cameraController.updateRendererCamera(position, railRotation);
        Renderer.renderer.spdPercentage = 0;
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
        railRotation = new Quaternion(rot);
        movement.reset();
        weapons.reset();
        cameraController.reset();
    }
}
