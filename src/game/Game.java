package game;

import game.engine.GameObject;
import game.engine.Renderable;
import game.engine.Collidable;
import game.engine.CollidableRenderable;
import game.engine.Face;
import tapplet.TApplet;
import game.Models;
import game.Input;
import game.engine.CollisionManager;
import game.engine.EngineUtil;
import game.engine.Renderer;
import game.engine.Vector2;
import game.engine.Vector3;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javafx.scene.media.AudioClip;
import java.io.File;

/**
 * Main game class that initializes and manages the game loop.
 * Handles setup, input processing, and main update cycle.
 * Extends TApplet for rendering and input handling.
 */
public class Game extends TApplet implements MouseMotionListener, MouseListener
{
    /**
     * Handles mouse drag events
     * @param e MouseEvent containing the event data
     */
    @Override
    public void mouseDragged(MouseEvent e) 
    {
        Input.input.mousePos = new Vector2(e.getX(), e.getY());
    }
    
    /**
     * Handles mouse movement events
     * @param e MouseEvent containing the event data
     */
    @Override
    public void mouseMoved(MouseEvent e)
    {
        Input.input.mousePos = new Vector2(e.getX(), e.getY());
        //System.out.println("Mouse X: " + Input.input.mousePos.x + " Mouse Y: " + Input.input.mousePos.y);
    }
    
    /**
     * Handles mouse click events
     * @param e MouseEvent containing the event data
     */
    @Override
    public void mouseClicked(MouseEvent e) 
    {
        
    }
    
    /**
     * Handles mouse enter events
     * @param e MouseEvent containing the event data
     */
    @Override
    public void mouseEntered(MouseEvent e) 
    {
        
    }
    
    /**
     * Handles mouse exit events
     * @param e MouseEvent containing the event data
     */
    @Override
    public void mouseExited(MouseEvent e) 
    {
        
    }
    
    /**
     * Handles mouse press events
     * @param e MouseEvent containing the event data
     */
    @Override
    public void mousePressed(MouseEvent e) 
    {
        Input.input.mouseDown = true;
    }
    
    /**
     * Handles mouse release events
     * @param e MouseEvent containing the event data
     */
    @Override
    public void mouseReleased(MouseEvent e) 
    {
        Input.input.mouseDown = false;
    }

    /**
     * Main entry point for the game
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) 
    {
        GameState.gameState.restartGame = true;
        while(game.GameState.gameState.restartGame == true)
        {
            GameState.gameState.restartGame = false;
            Game curGame = new Game();
            while(curGame.isVisible())
            {
                try {
                    Thread.sleep(100); // Sleep for a short time to avoid busy-waiting
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            GameState.gameState.highScore = Math.max(GameState.gameState.highScore, GameState.gameState.score);
            GameState.gameState.score = 0;
            GameState.gameState.crashed = false;
            GameState.gameState.noFuel = false;
            CollisionManager.instance.Clear();
            AstroidManager.instance.Clear();
            GameObject.gameObjects.clear();
            GameObject.removedObjects.clear();
        }
        
        return;
    }

    /** Screen width */
    final int WIDTH = 1920;
    
    /** Screen height */
    final int HEIGHT = 1200;
    
    /** Sun object in the game world */
    Star sun;
    
    /** Death Star object in the game world */
    Star deathStar;
    
    /** Array of background stars */
    Renderable []stars;
    
    /** Player camera */
    Camera camera;
    
    /** Current time in milliseconds */
    long curTime = 0;
    
    /** Previous update time in milliseconds */
    long preTime = 0;

    /**
     * Initializes the game window, resources, and game objects
     */
    public void init()
    {
        this.dispose();
        this.setUndecorated(true);
        this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setSize(WIDTH, HEIGHT);

        // Load audio resources
        Audio.ambient = new javafx.scene.media.AudioClip
        (new File(getCodeBase() + "\\Sound\\Ambient.wav").toURI().toString());
        Audio.ambient.setCycleCount(javafx.scene.media.AudioClip.INDEFINITE);
        Audio.ambient.play(0.1);

        Audio.explosionAsteroid = new javafx.scene.media.AudioClip
        (new File(getCodeBase() + "\\Sound\\explosion01.wav").toURI().toString());

        Audio.explosionShip = new javafx.scene.media.AudioClip
        (new File(getCodeBase() + "\\Sound\\explosion.wav").toURI().toString());

        Audio.lazer = new javafx.scene.media.AudioClip
        (new File(getCodeBase() + "\\Sound\\Hyper 7.wav").toURI().toString());

        curTime = System.currentTimeMillis();
        preTime = System.currentTimeMillis();
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        UI.ui = new UI(getGraphics(), WIDTH, HEIGHT);
    
        // Create background stars
        stars = new Renderable[200];
        for(int i = 0; i < 200; i++)
        {
            stars[i] = new Renderable();
            stars[i].model = new Face[2];
            //random a white blue to white red color
            Color c = new Color(0,0,0);
            for(int j = 0; j < 2; j++)
            {
                stars[i].model[j] = new Face(Models.star[j]);
            }
            stars[i].position = EngineUtil.RandomOnSphere(100000);
            stars[i].rotation.w = ((double)Math.random() - 0.5f);
            stars[i].rotation.x = ((double)Math.random() - 0.5f);
            stars[i].rotation.y = ((double)Math.random() - 0.5f);
            stars[i].rotation.z = ((double)Math.random() - 0.5f);
        }
        
        // Create player camera
        camera = new Camera(new Vector3(5000, 0, 0), new Vector3(0,-90,0));
        camera.tag = "MainCamera";

        // Create sun and Death Star
        sun = new Star(new Vector3(), 2, 2, new Vector3(1,0,0), Models.GetSphere(1000));
        sun.boundingRadius = 1000;
        sun.tag = "star";

        deathStar = new Star(new Vector3(10000, 0, 0), 60, 1200, new Vector3(0,1,0), Models.DeathStar);
        deathStar.scale = 2000;
        deathStar.boundingRadius = 200;
        deathStar.tag = "star";

        // Initialize renderer
        Renderer.renderer = new Renderer(getScreenBuffer(), WIDTH, HEIGHT);
        System.out.println(curTime);
    }

    /**
     * Processes keyboard and mouse inputs for the current frame
     */
    private void UpdateInputs()
    {
        for(int i = 0; i < 256; i++)
        {
            boolean pressed = this.keyHeld(i);
            Input.input.keyPressed[i] = (!Input.input.keys[i] && pressed);
            Input.input.keyReleased[i] = (Input.input.keys[i] && !pressed);
            Input.input.keys[i] = pressed;
        }
    }
    
    /**
     * Main game loop - updates the game state and renders each frame
     * @param g Graphics context to render to
     */
    public void movie(Graphics g)
    {
        UpdateInputs();
        if(GameState.gameState.restartGame)
        {
            this.setVisible(false);
            this.dispose();
            return;
        }
        
        // Handle game over conditions
        if(GameState.gameState.crashed)
        {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("You Crashed", WIDTH/2 - 150, HEIGHT/2 - 100);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("press space to restart", WIDTH/2 - 150, HEIGHT/2);
            repaint();
            if(Input.input.keyPressed[KeyEvent.VK_SPACE])
            {
                GameState.gameState.restartGame = true;
            }
                
            return;
        }
        if(GameState.gameState.noFuel)
        {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Out of Fuel", WIDTH/2 - 150, HEIGHT/2 -100);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("press space to restart", WIDTH/2 - 150, HEIGHT/2);
            repaint();
            if(Input.input.keyPressed[KeyEvent.VK_SPACE])
                GameState.gameState.restartGame = true;
            return;
        }
        
        // Clear screen and calculate delta time
        g.setColor(Color.BLACK);
        g.fillRect(0,0,WIDTH,HEIGHT);
        
        if(preTime == 0 || curTime == 0)
        {
            preTime = System.currentTimeMillis();
            curTime = System.currentTimeMillis();
            return;
        }
        preTime = curTime;
        curTime = System.currentTimeMillis();
        double delta = (double)(curTime - preTime) / 1000.0;
        System.out.println(1 /delta);
        
        // Update game elements
        AstroidManager.instance.Update(delta);
        GameObject.UpdateAll(delta);
        Renderer.renderer.Draw(g);
        UI.ui.Update(g);
        
        repaint();
    }
}


