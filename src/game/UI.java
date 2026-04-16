package game;

import game.engine.*;
import java.awt.*;

/**
 * Handles all HUD/UI rendering: crosshair, fuel bar, speed bar, score, death screen.
 */
public class UI
{
    public static UI ui;

    private static final int CROSSHAIR_SIZE = 10;
    private static final int CROSSHAIR_GAP = 5;
    private static final int BAR_WIDTH = 200;
    private static final int BAR_HEIGHT = 20;
    private static final int BAR_MARGIN = 20;
    private static final int SCORE_MARGIN = 20;

    private final int screenWidth;
    private final int screenHeight;
    private final int centerX;
    private final int centerY;

    public UI(int width, int height)
    {
        this.screenWidth = width;
        this.screenHeight = height;
        this.centerX = width / 2;
        this.centerY = height / 2;
    }

    public void draw(Graphics g)
    {
        if (GameState.gameState.isDead())
        {
            drawDeathScreen(g);
            return;
        }

        drawCrosshair(g);
        drawFuelBar(g);
        drawSpeedBar(g);
        drawScore(g);
    }

    private void drawCrosshair(Graphics g)
    {
        g.setColor(Color.GREEN);
        // Horizontal lines
        g.drawLine(centerX - CROSSHAIR_SIZE - CROSSHAIR_GAP, centerY,
                   centerX - CROSSHAIR_GAP, centerY);
        g.drawLine(centerX + CROSSHAIR_GAP, centerY,
                   centerX + CROSSHAIR_SIZE + CROSSHAIR_GAP, centerY);
        // Vertical lines
        g.drawLine(centerX, centerY - CROSSHAIR_SIZE - CROSSHAIR_GAP,
                   centerX, centerY - CROSSHAIR_GAP);
        g.drawLine(centerX, centerY + CROSSHAIR_GAP,
                   centerX, centerY + CROSSHAIR_SIZE + CROSSHAIR_GAP);
    }

    private void drawFuelBar(Graphics g)
    {
        if (Camera.instance == null) return;

        double fuelPercent = Camera.instance.getFuel() / Camera.instance.getInitialFuel();
        fuelPercent = Math.max(0, Math.min(1, fuelPercent));

        int x = BAR_MARGIN;
        int y = screenHeight - BAR_MARGIN - BAR_HEIGHT;

        // Background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, BAR_WIDTH, BAR_HEIGHT);

        // Fill
        g.setColor(fuelPercent > 0.25 ? Color.CYAN : Color.RED);
        g.fillRect(x, y, (int)(BAR_WIDTH * fuelPercent), BAR_HEIGHT);

        // Border
        g.setColor(Color.WHITE);
        g.drawRect(x, y, BAR_WIDTH, BAR_HEIGHT);

        // Label
        g.drawString("FUEL", x, y - 5);
    }

    private void drawSpeedBar(Graphics g)
    {
        if (Camera.instance == null) return;

        double speedPercent = Camera.instance.getSpeed() / Camera.instance.getMaxSpeed();
        speedPercent = Math.max(0, Math.min(1, speedPercent));

        int x = BAR_MARGIN;
        int y = screenHeight - BAR_MARGIN - BAR_HEIGHT * 2 - 10;

        // Background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, BAR_WIDTH, BAR_HEIGHT);

        // Fill
        g.setColor(Color.ORANGE);
        g.fillRect(x, y, (int)(BAR_WIDTH * speedPercent), BAR_HEIGHT);

        // Border
        g.setColor(Color.WHITE);
        g.drawRect(x, y, BAR_WIDTH, BAR_HEIGHT);

        // Label
        g.drawString("SPEED", x, y - 5);
    }

    private void drawScore(Graphics g)
    {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        String scoreText = "SCORE: " + GameState.gameState.getScore();
        g.drawString(scoreText, screenWidth - SCORE_MARGIN - 200, SCORE_MARGIN + 20);

        String highScoreText = "HIGH: " + GameState.gameState.getHighScore();
        g.drawString(highScoreText, screenWidth - SCORE_MARGIN - 200, SCORE_MARGIN + 45);
    }

    private void drawDeathScreen(Graphics g)
    {
        // Semi-transparent overlay
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, screenWidth, screenHeight);

        g.setFont(new Font("Monospaced", Font.BOLD, 48));
        g.setColor(Color.RED);

        String deathMsg = GameState.gameState.isCrashed() ? "DESTROYED" : "OUT OF FUEL";
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(deathMsg);
        g.drawString(deathMsg, centerX - textWidth / 2, centerY - 30);

        g.setFont(new Font("Monospaced", Font.PLAIN, 24));
        g.setColor(Color.WHITE);
        String scoreMsg = "Score: " + GameState.gameState.getScore();
        fm = g.getFontMetrics();
        textWidth = fm.stringWidth(scoreMsg);
        g.drawString(scoreMsg, centerX - textWidth / 2, centerY + 20);

        String restartMsg = "Press R to restart";
        textWidth = fm.stringWidth(restartMsg);
        g.drawString(restartMsg, centerX - textWidth / 2, centerY + 60);

        // Handle restart input
        if (Input.input.keys[java.awt.event.KeyEvent.VK_R])
        {
            GameState.gameState.setRestartGame(true);
        }
    }

    public void drawBullet(Graphics g, Vector3 bulletPos, Quaternion cameraRot, Vector3 cameraPos)
    {
        // Project bullet position to screen for HUD indicator
        Vector3 relative = bulletPos.minus(cameraPos);
        relative = cameraRot.conjugate().rotate(relative);

        if (relative.getZ() > 0)
        {
            double scale = Renderer.renderer.getScale();
            int sx = (int)(relative.getX() * scale / relative.getZ() + centerX);
            int sy = (int)(relative.getY() * scale / relative.getZ() + centerY);

            g.setColor(GameColors.LASER_RED);
            g.fillOval(sx - 2, sy - 2, 4, 4);
        }
    }
}
