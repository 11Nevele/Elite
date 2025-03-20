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
public class Camera extends CollidableRenderable
{
    Renderable plane;
    public Camera(Vector3 pos, Vector3 rot)
    {
        super();
        position = pos;
        rotation = EngineUtil.eulerToQuaternion(rot);
        plane = new Renderable(Models.TIE);
        plane.scale = 1;
        
    }
    double curSpd = 0;
    final double accel = 20;
    final double mxSpd = 100;

    final double turnSpd = 90;
    private long nextShootTime = 0;
    private final double shootDelay = 1f; 
    
    private final double mxFuel = 100;
    private final double fuelBurn = 1;
    private double fuel = mxFuel;

    @Override
    public void Update(double delta)
    {
        super.Update(delta);
        
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


        if(Input.input.mouseDown)
        {
            Vector2 mouse = Input.input.mousePos;
            mouse = new Vector2(mouse.x - UI.ui.center.x, mouse.y - UI.ui.center.y);
            if(Math.sqrt(mouse.x * mouse.x + mouse.y * mouse.y) > 40)
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
                rotation = rotation.multiply(Quaternion.PITCH(-mouse.y * turnSpd * delta))
                .multiply(Quaternion.YAW(mouse.x * turnSpd * delta));
            }
        }
        
        
        if(Input.input.keys[KeyEvent.VK_E])
        {
            fuel = Math.max(0, fuel - fuelBurn * delta);
            rotation = rotation.multiply(Quaternion.ROLL(turnSpd * delta));
        }
        if(Input.input.keys[KeyEvent.VK_Q])
        {
            fuel = Math.max(0, fuel - fuelBurn * delta);
            rotation = rotation.multiply(Quaternion.ROLL(-turnSpd * delta));
        }
        
        if(Input.input.keyPressed[KeyEvent.VK_SPACE] && System.currentTimeMillis() >= nextShootTime)
        {
            UI.ui.DrawBullet();
            System.out.println("Shoot");
            nextShootTime = System.currentTimeMillis() + (long)(shootDelay * 1000);
            
            ArrayList<Collidable> collis = CollisionManager.instance.Raycast(position, EngineUtil.quaternionToDirection(rotation));
            for(Collidable c : collis)
            {
                if(c.getTag().equals("enemy"))
                {
                    GameState.gameState.score+=100;
                    fuel = Math.min(mxFuel, fuel + 50);
                    GameObject.DestroyObject(c.getGameObject());  
                }
                if(c.getTag().equals("asteroid"))
                {
                    GameState.gameState.score+=20;
                    fuel = Math.min(mxFuel, fuel + 10);
                    GameObject.DestroyObject(c.getGameObject());
                }
            }
        }


        Vector3 v = EngineUtil.quaternionToDirection(rotation);
        v = v.normalize();
        v.x *= curSpd * delta;
        v.y *= curSpd * delta;
        v.z *= curSpd * delta;
        

        position = position.plus(v);
        //keep the relative position and rotation between plane and this still
        plane.position = position;
        plane.rotation = rotation;
        Vector3 planeDv = new QuaternionT(-20, rotation.rotate(new Vector3(1,0,0)))
        .asQuaternion()
        .rotate(EngineUtil.quaternionToDirection(rotation)).multi(5);
        plane.position = plane.position.plus(planeDv);
        UI.ui.fuelPercentage = fuel / mxFuel;
        UI.ui.spdPercentage = curSpd / mxSpd;
        Renderer.renderer.spdPercentage = curSpd / mxSpd;
        if(fuel <= 0)
            GameState.gameState.noFuel = true;

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
        
        UI.ui.curPos = position;
        UI.ui.curRotation = EngineUtil.quaternionToEuler(rotation);
        
    }
}
