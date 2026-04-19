package game;

import game.engine.*;
import java.awt.*;

/**
 * Handles HUD rendering for the rail-shooter prototype.
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
    private static final double SECTOR_LENGTH = 1000;

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
        drawStatusPanel(g);
        drawSectorBar(g);
        drawScore(g);
    }

    private void drawCrosshair(Graphics g)
    {
        g.setColor(GameColors.STAR_WHITE);
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

    private void drawStatusPanel(Graphics g)
    {
        int x = BAR_MARGIN;
        int y = BAR_MARGIN;
        int width = 240;
        int height = 84;

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(x, y, width, height, 12, 12);
        g.setColor(Color.WHITE);
        g.drawRoundRect(x, y, width, height, 12, 12);

        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.drawString("WAVE " + Math.max(1, GameState.gameState.getCurrentWave()), x + 16, y + 28);
        g.drawString("KILLS " + GameState.gameState.getEnemiesDestroyed(), x + 16, y + 52);
        g.drawString("DIST " + (int) GameState.gameState.getDistanceTravelled(), x + 16, y + 76);
    }

    private void drawSectorBar(Graphics g)
    {
        double sectorProgress = (GameState.gameState.getDistanceTravelled() % SECTOR_LENGTH) / SECTOR_LENGTH;

        int x = BAR_MARGIN;
        int y = screenHeight - BAR_MARGIN - BAR_HEIGHT;

        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, BAR_WIDTH, BAR_HEIGHT);

        g.setColor(GameColors.LASER_RED);
        g.fillRect(x, y, (int)(BAR_WIDTH * sectorProgress), BAR_HEIGHT);

        g.setColor(Color.WHITE);
        g.drawRect(x, y, BAR_WIDTH, BAR_HEIGHT);
        g.drawString("SECTOR", x, y - 5);
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

        String deathMsg = GameState.gameState.isCrashed() ? "DESTROYED" : "MISSION FAILED";
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(deathMsg);
        g.drawString(deathMsg, centerX - textWidth / 2, centerY - 30);

        g.setFont(new Font("Monospaced", Font.PLAIN, 24));
        g.setColor(Color.WHITE);
        String scoreMsg = "Score: " + GameState.gameState.getScore();
        fm = g.getFontMetrics();
        textWidth = fm.stringWidth(scoreMsg);
        g.drawString(scoreMsg, centerX - textWidth / 2, centerY + 20);

        String waveMsg = "Wave: " + Math.max(1, GameState.gameState.getCurrentWave());
        textWidth = fm.stringWidth(waveMsg);
        g.drawString(waveMsg, centerX - textWidth / 2, centerY + 50);

        String restartMsg = "Press R to restart";
        textWidth = fm.stringWidth(restartMsg);
        g.drawString(restartMsg, centerX - textWidth / 2, centerY + 90);

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
