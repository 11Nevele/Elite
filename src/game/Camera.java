package game;

import game.engine.Collidable;
import game.engine.CollidableRenderable;
import game.engine.CollisionManager;
import game.engine.EngineUtil;
import game.engine.GameObject;
import game.engine.Quaternion;
import game.engine.QuaternionT;
import game.engine.Renderable;
import game.engine.Renderer;
import game.engine.Vector2;
import game.engine.Vector3;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Represents the player's ship/camera.
 * Handles player movement, view control, shooting, and fuel management.
 */
public class Camera extends CollidableRenderable
{
    /** Visual model of the player's ship */
    Renderable plane;
    
    /**
     * Creates a new camera at the specified position and orientation
     * @param pos Initial position
     * @param rot Initial rotation in Euler angles (degrees)
     */
    public Camera(Vector3 pos, Vector3 rot)
    {
        super();
        position = pos;
        rotation = EngineUtil.eulerToQuaternion(rot);
        plane = new Renderable(Models.TIE);
        plane.scale = 1;
    }
    
    /** Current speed */
    double curSpd = 0;
    
    /** Acceleration rate (units/s²) */
    final double accel = 20;
    
    /** Maximum speed */
    final double mxSpd = 100;

    /** Turn rate in degrees per second */
    final double turnSpd = 90;
    
    /** Time until next shot can be fired */
    private long nextShootTime = 0;
    
    /** Cooldown between shots in seconds */
    private final double shootDelay = 1f; 
    
    /** Maximum fuel capacity */
    private final double mxFuel = 100;
    
    /** Fuel consumption rate per second */
    private final double fuelBurn = 1;
    
    /** Current fuel level */
    private double fuel = mxFuel;

    /**
     * Updates the player's state including movement, shooting, and view control
     * @param delta Time elapsed since last update in seconds
     */
    @Override
    public void Update(double delta)
    {
        super.Update(delta);
        
        // Constant fuel consumption
        fuel -= 1 * delta;
        
        // Handle acceleration and deceleration
        if(Input.input.keys[KeyEvent.VK_CONTROL])
        {
            fuel = Math.max(0, fuel - fuelBurn * delta);
            curSpd = Math.max(0, curSpd - accel * delta);
        }
        if(Input.input.keys[KeyEvent.VK_SHIFT])
        {
            fuel = Math.max(0, fuel - fuelBurn * delta);
            curSpd = Math.min(mxSpd, curSpd + accel * delta);
        }

        // Aiming mode slow down factor
        double slowdownScale = 1;
        if(Input.input.keys[KeyEvent.VK_F])
        {
            slowdownScale = 0.3;
        }

        // Mouse control for rotation
        if(Input.input.mouseDown)
        {
            Vector2 mouse = Input.input.mousePos;
            mouse = new Vector2(mouse.x - UI.ui.center.x, mouse.y - UI.ui.center.y);
            if(Math.sqrt(mouse.x * mouse.x + mouse.y * mouse.y) > 5)
            {
                if(mouse.x > UI.ui.HEIGHT / 2)
                {
                    mouse.x = UI.ui.HEIGHT / 2;
                }
                if(mouse.x < -UI.ui.HEIGHT / 2)
                {
                    mouse.x = -UI.ui.HEIGHT / 2;
                }
                mouse.x = mouse.x / UI.ui.HEIGHT * 2*1.2f;
                mouse.y = mouse.y / UI.ui.HEIGHT * 2*1.2f;
                if(mouse.magnitude() > 1)
                {
                    mouse = mouse.normalize();
                }

                fuel = Math.max(0, fuel - fuelBurn * delta * mouse.magnitude());
                rotation = rotation.multiply(Quaternion.PITCH(-mouse.y * turnSpd * delta * slowdownScale))
                .multiply(Quaternion.YAW(mouse.x * turnSpd * delta * slowdownScale));
            }
        }
        
        // Roll control
        if(Input.input.keys[KeyEvent.VK_E])
        {
            fuel = Math.max(0, fuel - fuelBurn * delta * slowdownScale);
            rotation = rotation.multiply(Quaternion.ROLL(turnSpd * delta * slowdownScale));
        }
        if(Input.input.keys[KeyEvent.VK_Q])
        {
            fuel = Math.max(0, fuel - fuelBurn * delta * slowdownScale);
            rotation = rotation.multiply(Quaternion.ROLL(-turnSpd * delta * slowdownScale));
        }
        
        // Weapon firing
        if(Input.input.keyPressed[KeyEvent.VK_SPACE] && System.currentTimeMillis() >= nextShootTime)
        {
            UI.ui.DrawBullet();
            System.out.println("Shoot");
            nextShootTime = System.currentTimeMillis() + (long)(shootDelay * 1000);
            Audio.lazer.play();
            fuel = Math.max(0, fuel - 5);
            ArrayList<Collidable> collis = CollisionManager.instance.Raycast(position, EngineUtil.quaternionToDirection(rotation));
            for(Collidable c : collis)
            {
                if(c.getTag().equals("enemy"))
                {
                    GameState.gameState.score+=100;
                    fuel = Math.min(mxFuel, fuel + 50);
                    Explosion.GenerateExplosion(c.getGameObject().position);
                    GameObject.DestroyObject(c.getGameObject());  
                    Audio.explosionShip.play();
                }
                if(c.getTag().equals("asteroid"))
                {
                    GameState.gameState.score+=30;
                    fuel = Math.min(mxFuel, fuel + 30);
                    Explosion.GenerateExplosion(c.getGameObject().position);
                    GameObject.DestroyObject(c.getGameObject());
                    Audio.explosionAsteroid.play();
                }
            }
        }

        // Move the player forward
        Vector3 v = EngineUtil.quaternionToDirection(rotation);
        v = v.normalize().multi(curSpd * delta);
        
        position = position.plus(v);
        
        // Update visual ship model position and orientation
        plane.position = position;
        plane.rotation = rotation;
        Vector3 planeDv = new QuaternionT(-20, rotation.rotate(new Vector3(1,0,0)))
        .asQuaternion()
        .rotate(EngineUtil.quaternionToDirection(rotation)).multi(5);
        plane.position = plane.position.plus(planeDv);
        
        // Update UI elements
        UI.ui.fuelPercentage = fuel / mxFuel;
        UI.ui.spdPercentage = curSpd / mxSpd;
        Renderer.renderer.spdPercentage = curSpd / mxSpd;
        if(fuel <= 0)
            GameState.gameState.noFuel = true;

        // Handle view switching
        if(Input.input.keys[KeyEvent.VK_A])
        {
            Renderer.renderer.UpdateCamera(position, rotation.multiply(Quaternion.YAW(-90)));
            UI.ui.currentView = "Left View";
        }
        else if(Input.input.keys[KeyEvent.VK_D])
        {
            Renderer.renderer.UpdateCamera(position, rotation.multiply(Quaternion.YAW(90)));
            UI.ui.currentView = "Right View";
        }
        else if(Input.input.keys[KeyEvent.VK_S])
        {
            Renderer.renderer.UpdateCamera(position, rotation.multiply(Quaternion.YAW(180)));
            UI.ui.currentView = "Back View";
        }
        else
        {
            Renderer.renderer.UpdateCamera(position, rotation);
            UI.ui.currentView = "Front View";
        }
        
        // Update UI position display
        UI.ui.curPos = position;
        UI.ui.curRotation = EngineUtil.quaternionToEuler(rotation);
    }
}
