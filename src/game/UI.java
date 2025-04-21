package game;

import game.engine.Renderer;
import game.engine.Vector2;
import game.engine.Vector3;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 * Handles all user interface elements for the game.
 * Responsible for drawing HUD elements, status bars, and game messages.
 */
public class UI
{
    /** Singleton instance */
    public static UI ui;
    
    /** Center point of the screen */
    public Vector2 center = new Vector2(1920/2, 1080/2);
    
    /** Graphics context for rendering */
    private Graphics g;
    
    /** Timer for bullet effect display */
    private long bulletFinishedTime = 0;
    
    /** Screen width */
    public final int WIDTH;
    
    /** Screen height */
    public final int HEIGHT;

    /** Current fuel level (0.0 to 1.0) */
    public double fuelPercentage = 1;

    /** Current speed level (0.0 to 1.0) */
    public double spdPercentage = 1;

    /** Text showing the current view direction */
    public String currentView = "Front View";

    /** Current player position */
    public Vector3 curPos = new Vector3();

    /** Current player rotation in Euler angles */
    public Vector3 curRotation = new Vector3();

    /**
     * Constructor for the UI system
     * @param g Graphics context to render to
     * @param WIDTH Screen width
     * @param HEIGHT Screen height
     */
    public UI(Graphics g, int WIDTH, int HEIGHT)
    {
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        this.g = g;
    }
    
    /**
     * Draws UI elements that only appear in front view
     */
    private void FrontOnlyUI()
    {
        long curTime = System.currentTimeMillis();
        if(curTime <= bulletFinishedTime)
        {
            g.setColor(Color.green);
            if(Renderer.renderer.getScale() > 700)
            {
                g.drawLine(WIDTH / 2 - 200, HEIGHT, WIDTH/2 - 2, HEIGHT/2);
                g.drawLine(WIDTH / 2 + 200, HEIGHT, WIDTH/2 + 2, HEIGHT/2);

            }
            else
            {
                //two white line from buttom left to middle and buttom right to middle
                g.drawLine(WIDTH / 2 - 25 + (int)(spdPercentage * 4), 800 - (int)(spdPercentage * 50), WIDTH/2 - 2, HEIGHT/2);
                g.drawLine(WIDTH / 2 + 25 - (int)(spdPercentage * 4), 800 - (int)(spdPercentage * 50), WIDTH/2 + 2, HEIGHT/2);

            }
        }
        //aiming line at center of the screen
        g.setColor(Color.yellow);
        g.drawLine(WIDTH/2 - 80, HEIGHT/2, WIDTH/2 -40, HEIGHT/2);
        g.drawLine(WIDTH/2 + 80, HEIGHT/2, WIDTH/2 + 40, HEIGHT/2);
        g.drawLine(WIDTH/2, HEIGHT/2 - 80, WIDTH/2, HEIGHT/2 - 40);
        g.drawLine(WIDTH/2, HEIGHT/2 + 80, WIDTH/2, HEIGHT/2 + 40);
    }

    /**
     * Updates and draws all UI elements
     * @param g Graphics context to render to
     */
    public void Update(Graphics g)
    {
        this.g = g;
        
        if(currentView.charAt(0) == 'F')
        {
            FrontOnlyUI();
        }

        //Current view
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString(currentView, WIDTH/2 - 50, 50);

        int UIX = 0, UIY = 0;
        //UI bar
        g.setColor(Color.black);
        g.fillRect(0, UIY, 240, HEIGHT);
        g.setColor(Color.yellow);
        g.drawRect(0, UIY, 240, HEIGHT);

        //Instructions
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("Shift - Accelerate    Ctrl - Deccelerate", 20, 50);
        g.drawString("Mouse - Rotate    Q/E - Roll", 20, 70);
        g.drawString("A/S/D Change View", 20, 90);
        g.drawString("Space - Shoot", 20, 110);
        g.drawString("F - Aim", 20, 130);

        //fuel bar
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("FUEL", 55, 390);
        g.drawString(((int)(fuelPercentage * 100)) + "%", 55, 420 + 600);
        g.fillRect(48, 400 + (int)(600 - 600 * fuelPercentage), 48, (int)(600 * fuelPercentage));
        g.setColor(Color.yellow);
        g.drawRect(48, 400, 48, 600);

        
        //speed bar
        g.setColor(Color.WHITE);
        g.drawString("SPEED", 150, 390);
        g.fillRect(144, 400 + (int)(600 - 600 * spdPercentage), 48, (int)(600 * spdPercentage));
        g.drawString(((int)(spdPercentage * 100)) + "%", 150, 420 + 600);
        g.setColor(Color.yellow);
        g.drawRect(144, 400, 48, 600);

        //right side UI
        UIX = WIDTH - 240; UIY = 0;
        g.setColor(Color.black);
        g.fillRect(UIX, UIY, 240, HEIGHT);
        g.setColor(Color.yellow);
        g.drawRect(UIX, UIY, 240, HEIGHT);
        //Position and Rotation
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Position: ", UIX + 20, 50);
        g.drawString("X: " + curPos.x, UIX + 40, 80);
        g.drawString("Y: " + curPos.y, UIX + 40, 110);
        g.drawString("Z: " + curPos.z, UIX + 40, 140);
        g.drawString("Rotation: ", UIX + 20, 170);
        g.drawString("X: " + curRotation.x, UIX + 40, 200);
        g.drawString("Y: " + curRotation.y, UIX + 40, 230);
        g.drawString("Z: " + curRotation.z, UIX + 40, 260);

        //Score and Highscore
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + GameState.gameState.score, UIX + 60, 900);
        g.drawString("Highscore: " + GameState.gameState.highScore, UIX + 20, 930);
    }

    /**
     * Triggers the bullet firing visualization effect
     */
    public void DrawBullet()
    {
        bulletFinishedTime = System.currentTimeMillis() + 100;
        g.setColor(Color.WHITE);
    }
}
