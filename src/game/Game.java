package game;

import game.engine.*;
import game.engine.Renderer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import javax.swing.*;

/**
 * Main game class. Sets up the window, game loop, and coordinates all systems.
 */
public class Game extends JFrame implements Runnable
{
    public static final int WIDTH = 1280;// do not change this
    public static final int HEIGHT = 720;

    // 3D scene renders at half resolution, then upscaled
    public static final int RENDER_WIDTH = 1280;
    public static final int RENDER_HEIGHT = 720;

    private long lastTime;
    private Star[] stars;
    private static final int STAR_COUNT = 200;

    private final BufferedImage screenBuffer;
    private final BufferedImage renderBuffer;
    private volatile boolean running;
    private final int targetFPS = 60;
    private static final int MENU_OPTION_SINGLE = 0;
    private static final int MENU_OPTION_TWO = 1;
    private static final int MENU_OPTION_COMPETITIVE = 2;

    private MenuScene menuScene;
    private GameState.GameMode pendingGameMode = GameState.GameMode.SINGLE_PLAYER;

    public Game()
    {
        setTitle("Elite");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        screenBuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        renderBuffer = new BufferedImage(RENDER_WIDTH, RENDER_HEIGHT, BufferedImage.TYPE_INT_ARGB);

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

        // Create renderer at reduced resolution with direct pixel buffer access
        Renderer.renderer = new Renderer(renderBuffer.getGraphics(), RENDER_WIDTH, RENDER_HEIGHT, renderBuffer);
        bootstrapWorldSystems();
        UI.ui = new UI(WIDTH, HEIGHT);

        // Create background stars (2D points, not 3D meshes)
        stars = new Star[STAR_COUNT];
        for (int i = 0; i < STAR_COUNT; i++)
        {
            stars[i] = new Star(800 + Math.random() * 200);
        }

        menuScene = new MenuScene();

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

            Graphics renderGraphics = renderBuffer.getGraphics();
            Graphics screenGraphics = screenBuffer.getGraphics();
            gameLoop(renderGraphics, screenGraphics);
            renderGraphics.dispose();
            screenGraphics.dispose();

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

    private void gameLoop(Graphics renderG, Graphics screenG)
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

        updateMenuInput();

        GameState.GameMode gameMode = GameState.gameState.getGameMode();
        boolean inMenu   = gameMode == GameState.GameMode.MENU;
        boolean inLaunch = gameMode == GameState.GameMode.LAUNCH_ANIMATION;

        if (!inMenu && !inLaunch)
        {
            GameState.gameState.updateCompetitiveTimer(delta);
        }

        // Update all game objects
        long t0 = System.nanoTime();
        if (!inMenu && !inLaunch)
        {
            GameObject.updateAll(delta);
        }
        else
        {
            // In menu / launch animation: only tick stars so the star field is alive.
            // Full updateAll() is skipped to prevent enemy/asteroid spawners from running.
            for (Star star : stars)
            {
                star.update(delta);
            }
        }
        long t1 = System.nanoTime();
        Profiler.instance.setUpdateTime(t1 - t0);
        Profiler.instance.setGameObjectCount(GameObject.gameObjects.size());

        // Menu / launch animation: set camera and queue preview ship triangles
        // before the renderer flushes its triangle list.
        if (inMenu)
        {
            menuScene.update(delta, GameState.gameState.getMenuSelection());
        }
        else if (inLaunch)
        {
            boolean showBoth = pendingGameMode == GameState.GameMode.TWO_PLAYER
                || pendingGameMode == GameState.GameMode.COMPETITIVE;

            if (menuScene.updateLaunch(delta, showBoth))
            {
                // Animation complete – transition to the selected game mode
                GameState.gameState.setGameMode(pendingGameMode);
                createPlayer();
            }
        }

        // Clear render buffer pixel array directly (rasterizer writes to same array)
        int[] renderPixels = ((DataBufferInt) renderBuffer.getRaster().getDataBuffer()).getData();
        Arrays.fill(renderPixels, 0xFF000000);

        // Render 3D scene at reduced resolution
        Renderer.renderer.draw(renderG);

        // Upscale render buffer to full-resolution screen buffer
        screenG.drawImage(renderBuffer, 0, 0, WIDTH, HEIGHT, null);

        // Draw UI at full resolution on top
        long t2 = System.nanoTime();
        UI.ui.draw(screenG);
        long t3 = System.nanoTime();
        Profiler.instance.setUITime(t3 - t2);

        // Draw profiler overlay at full resolution
        Profiler.instance.setTotalFrameTime(System.nanoTime() - frameStart);
        Profiler.instance.endFrame();
        Profiler.instance.draw(screenG, WIDTH);

        Input.input.update();
    }

    private void restartGame()
    {
        // Clear all game objects
        GameObject.gameObjects.clear();
        GameObject.removedObjects.clear();
        GameObject.newObjects.clear();

        // Reset systems
        GameState.gameState.reset();
        Camera.instance = null;
        SecondPlayer.instance = null;
        Audio.stopBattleMusic();
        bootstrapWorldSystems();
        menuScene.resetLaunch();

        // Recreate stars
        for (int i = 0; i < STAR_COUNT; i++)
        {
            stars[i] = new Star(800 + Math.random() * 200);
        }
    }

    public static void main(String[] args)
    {
        Game game = new Game();
        game.start();
    }

    private void createPlayer()
    {
        Camera.instance = null;
        SecondPlayer.instance = null;

        GameState.GameMode mode = GameState.gameState.getGameMode();
        if (mode == GameState.GameMode.SINGLE_PLAYER)
        {
            Camera.instance = new Camera(new Vector3(0, 0, 0), new Quaternion());
        }
        else if (mode == GameState.GameMode.TWO_PLAYER || mode == GameState.GameMode.COMPETITIVE)
        {
            Camera.instance = new Camera(new Vector3(-6, 0, 0), new Quaternion(), true, KeyEvent.VK_E);
            SecondPlayer.instance = new SecondPlayer(new Vector3(6, 0, 0), new Quaternion());
        }

        activatePendingObjects();
    }

    private void updateMenuInput()
    {
        if (GameState.gameState.getGameMode() != GameState.GameMode.MENU)
        {
            return;
        }

        int selection = GameState.gameState.getMenuSelection();

        if (Input.input.isKeyPressed(KeyEvent.VK_UP) || Input.input.isKeyPressed(KeyEvent.VK_W))
        {
            selection = Math.max(MENU_OPTION_SINGLE, selection - 1);
        }
        if (Input.input.isKeyPressed(KeyEvent.VK_DOWN) || Input.input.isKeyPressed(KeyEvent.VK_S))
        {
            selection = Math.min(MENU_OPTION_COMPETITIVE, selection + 1);
        }

        if (selection != GameState.gameState.getMenuSelection())
        {
            Audio.playMenuSelect();
        }
        GameState.gameState.setMenuSelection(selection);

        if (Input.input.isKeyPressed(KeyEvent.VK_Q) || Input.input.isKeyPressed(KeyEvent.VK_U))
        {
            GameState.GameMode selectedMode;
            if (selection == MENU_OPTION_TWO)
            {
                selectedMode = GameState.GameMode.TWO_PLAYER;
            }
            else if (selection == MENU_OPTION_COMPETITIVE)
            {
                selectedMode = GameState.GameMode.COMPETITIVE;
            }
            else
            {
                selectedMode = GameState.GameMode.SINGLE_PLAYER;
            }

            GameState.gameState.setGameMode(GameState.GameMode.LAUNCH_ANIMATION);
            pendingGameMode = selectedMode;
            menuScene.resetLaunch();
            Audio.stopAmbient();
            Audio.playBattleMusic();
        }
    }

    private void bootstrapWorldSystems()
    {
        CollisionManager.instance = new CollisionManager();
        AsteroidManager.instance = new AsteroidManager();
        activatePendingObjects();
    }

    private void activatePendingObjects()
    {
        if (GameObject.newObjects.isEmpty())
        {
            return;
        }

        GameObject.gameObjects.addAll(GameObject.newObjects);
        GameObject.newObjects.clear();
    }
}


