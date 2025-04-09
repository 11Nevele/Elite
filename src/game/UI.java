package game;

import game.engine.Renderer;
import game.engine.Vector2;
import game.engine.Vector3;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class UI
{
    public static UI ui;
    public Vector2 center = new Vector2(1920/2, 1080/2);
    private Graphics g;
    private long bulletFinishedTime = 0;
    public final int WIDTH;
    public final int HEIGHT;

    public double fuelPercentage = 1;

    public double spdPercentage = 1;

    public String currentView = "Front View";

    public Vector3 curPos = new Vector3();

    public Vector3 curRotation = new Vector3();

    public UI(Graphics g, int WIDTH, int HEIGHT)
    {
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        
        
        this .g = g;
    }
    
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
                g.drawLine(WIDTH / 2 + 25 - (int)(spdPercentage * 4), 80 - (int)(spdPercentage * 50), WIDTH/2 + 2, HEIGHT/2);

            }
        }
        //aiming line at center of the screen
        g.setColor(Color.yellow);
        g.drawLine(WIDTH/2 - 80, HEIGHT/2, WIDTH/2 -40, HEIGHT/2);
        g.drawLine(WIDTH/2 + 80, HEIGHT/2, WIDTH/2 + 40, HEIGHT/2);
        g.drawLine(WIDTH/2, HEIGHT/2 - 80, WIDTH/2, HEIGHT/2 - 40);
        g.drawLine(WIDTH/2, HEIGHT/2 + 80, WIDTH/2, HEIGHT/2 + 40);
    }

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

    public void DrawBullet()
    {
        bulletFinishedTime = System.currentTimeMillis() + 100;
        g.setColor(Color.WHITE);

    }

    
    
}
