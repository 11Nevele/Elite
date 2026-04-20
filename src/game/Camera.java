package game;

import game.engine.*;

/**
 * The player camera/ship. Coordinates movement, weapons, and camera view.
 * Registered as a collidable with the "player" tag for enemy collision detection.
 */
public class Camera extends CollidableRenderable
{
    public static Camera instance;
    public static final double SCROLL_SPEED = 200;

    private static final double MUZZLE_OFFSET = 8;
    private static final Quaternion MODEL_UPRIGHT_ROTATION = Quaternion.roll(180);

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
        rotation = rot.multiply(MODEL_UPRIGHT_ROTATION).normalize();
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
        if (!GameState.gameState.isDead())
        {
            GameState.gameState.addDistance(SCROLL_SPEED * delta);
        }

        position = position.plus(dodgeOffset);
        Quaternion shipRotation = movement.getShipRotation();
        rotation = shipRotation.multiply(MODEL_UPRIGHT_ROTATION).normalize();

        Vector3 shipForward = EngineUtil.quaternionToDirection(shipRotation).normalize();
        Vector3 muzzlePos = position.plus(shipForward.multiply(MUZZLE_OFFSET));
        weapons.update(delta, muzzlePos, shipRotation);

        cameraController.updateRendererCamera(
            delta,
            position,
            railRotation,
            movement.getHorizontalInput(),
            movement.getVerticalInput()
        );
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
        rotation = rot.multiply(MODEL_UPRIGHT_ROTATION).normalize();
        railRotation = new Quaternion(rot);
        movement.reset();
        weapons.reset();
        cameraController.reset();
    }

    public static Vector3 getWorldScrollVelocity()
    {
        if (instance == null || GameState.gameState == null || GameState.gameState.isDead())
        {
            return new Vector3();
        }

        return instance.getRailForward().multiply(-SCROLL_SPEED);
    }

    public static Vector3 getWorldScrollDelta(double delta)
    {
        return getWorldScrollVelocity().multiply(delta);
    }

    public static Vector3 getWorldScrollOffset()
    {
        if (instance == null || GameState.gameState == null)
        {
            return new Vector3();
        }

        return instance.getRailForward().multiply(-GameState.gameState.getDistanceTravelled());
    }

    public WeaponSystem getWeapons()
    {
        return weapons;
    }

    private Vector3 getRailForward()
    {
        return EngineUtil.quaternionToDirection(railRotation);
    }
}
