package game;

import game.engine.*;
import game.engine.Renderer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * Main game class. Sets up the window, game loop, and coordinates all systems.
 */
public class Game extends JFrame implements Runnable
{
    public static final int WIDTH = 1920;
    public static final int HEIGHT = 1200;

    private long lastTime;
    private Star[] stars;
    private static final int STAR_COUNT = 200;

    private BufferedImage screenBuffer;
    private volatile boolean running;
    private int targetFPS = 60;

    public Game()
    {
        setTitle("Elite");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        screenBuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);

        addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                Input.input.keyDown(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                Input.input.keyUp(e.getKeyCode());
            }
        });

        init();
        setVisible(true);
    }

    private void init()
    {
        // Initialize systems
        Input.input = new Input();
        GameState.gameState = new GameState();
        Models.init();
        Audio.init();

        // Create renderer
        Renderer.renderer = new Renderer(screenBuffer.getGraphics(), WIDTH, HEIGHT);
        CollisionManager.instance = new CollisionManager();
        AsteroidManager.instance = new AsteroidManager();
        UI.ui = new UI(WIDTH, HEIGHT);

        // Create player camera
        new Camera(new Vector3(0, 0, 0), new Quaternion());

        // Create background stars
        stars = new Star[STAR_COUNT];
        Face[] starModel = ProceduralMeshes.getSphere(
            1, 4, 4, GameColors.STAR_WHITE, GameColors.STAR_YELLOW);
        for (int i = 0; i < STAR_COUNT; i++)
        {
            stars[i] = new Star(starModel, 800 + Math.random() * 200);
        }

        Audio.playAmbient();
        lastTime = System.nanoTime();
    }

    public void start()
    {
        running = true;
        new Thread(this).start();
    }

    public void stop()
    {
        running = false;
    }

    @Override
    public void run()
    {
        while (running)
        {
            long frameStart = System.nanoTime();

            Graphics bufferGraphics = screenBuffer.getGraphics();
            gameLoop(bufferGraphics);
            bufferGraphics.dispose();

            Graphics g = getGraphics();
            if (g != null)
            {
                g.drawImage(screenBuffer, 0, 0, null);
                g.dispose();
            }

            long elapsed = System.nanoTime() - frameStart;
            long targetNanos = 1_000_000_000L / targetFPS;
            long sleepMillis = (targetNanos - elapsed) / 1_000_000;
            if (sleepMillis > 0)
            {
                try { Thread.sleep(sleepMillis); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
    }

    private void gameLoop(Graphics g)
    {
        long frameStart = System.nanoTime();
        long now = System.nanoTime();
        double delta = (now - lastTime) / 1_000_000_000.0;
        lastTime = now;

        // Cap delta to prevent physics issues
        delta = Math.min(delta, 0.1);

        // Toggle profiler
        Profiler.instance.toggleIfPressed(Input.input.isKeyDown(java.awt.event.KeyEvent.VK_F3));

        // Check restart
        if (GameState.gameState.isRestartGame())
        {
            restartGame();
        }

        // Update all game objects
        long t0 = System.nanoTime();
        GameObject.updateAll(delta);
        long t1 = System.nanoTime();
        Profiler.instance.setUpdateTime(t1 - t0);
        Profiler.instance.setGameObjectCount(GameObject.gameObjects.size());

        // Clear screen
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Render 3D scene
        Renderer.renderer.draw(g);

        // Draw UI
        long t2 = System.nanoTime();
        UI.ui.draw(g);
        long t3 = System.nanoTime();
        Profiler.instance.setUITime(t3 - t2);

        // Draw profiler overlay
        Profiler.instance.setTotalFrameTime(System.nanoTime() - frameStart);
        Profiler.instance.endFrame();
        Profiler.instance.draw(g, WIDTH);
    }

    private void restartGame()
    {
        // Clear all game objects
        GameObject.gameObjects.clear();
        GameObject.removedObjects.clear();
        GameObject.newObjects.clear();

        // Reset systems
        GameState.gameState.reset();
        CollisionManager.instance.clear();
        AsteroidManager.instance.clear();

        // Recreate player
        new Camera(new Vector3(0, 0, 0), new Quaternion());

        // Recreate stars
        Face[] starModel = ProceduralMeshes.getSphere(
            1, 4, 4, GameColors.STAR_WHITE, GameColors.STAR_YELLOW);
        for (int i = 0; i < STAR_COUNT; i++)
        {
            stars[i] = new Star(starModel, 800 + Math.random() * 200);
        }
    }

    public static void main(String[] args)
    {
        Game game = new Game();
        game.start();
    }
}


