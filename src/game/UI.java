package game;

import game.engine.*;
import java.awt.*;

/**
 * Handles HUD rendering for the rail-shooter prototype.
 */
public class UI
{
    public static UI ui;

    private static final int BAR_WIDTH = 200;
    private static final int BAR_HEIGHT = 20;
    private static final int BAR_MARGIN = 20;
    private static final int SCORE_MARGIN = 20;
    private static final int AIM_RETICLE_RADIUS = 10;
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

        drawStatusPanel(g);
        drawSectorBar(g);
        drawScore(g);
        drawHoldDistanceAim(g);
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

    private void drawHoldDistanceAim(Graphics g)
    {
        if (Camera.instance == null)
        {
            return;
        }

        WeaponSystem weapons = Camera.instance.getWeapons();
        if (!weapons.hasCurrentAimPointAtHoldDistance())
        {
            return;
        }

        Vector2 screenPosition = projectWorldToScreen(weapons.getCurrentAimPointAtHoldDistance());
        if (screenPosition == null)
        {
            return;
        }

        int sx = (int) Math.round(screenPosition.getX());
        int sy = (int) Math.round(screenPosition.getY());
        if (sx < 0 || sx > screenWidth || sy < 0 || sy > screenHeight)
        {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        Stroke previousStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2f));

        g2.setColor(new Color(255, 220, 120, 220));
        g2.drawOval(sx - AIM_RETICLE_RADIUS, sy - AIM_RETICLE_RADIUS,
            AIM_RETICLE_RADIUS * 2, AIM_RETICLE_RADIUS * 2);
        g2.drawLine(sx - AIM_RETICLE_RADIUS - 6, sy, sx - 4, sy);
        g2.drawLine(sx + 4, sy, sx + AIM_RETICLE_RADIUS + 6, sy);
        g2.drawLine(sx, sy - AIM_RETICLE_RADIUS - 6, sx, sy - 4);
        g2.drawLine(sx, sy + 4, sx, sy + AIM_RETICLE_RADIUS + 6);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.drawString(String.format("AIM @ %.0f", EnemySpawner.getHoldDistance()), sx + 14, sy - 10);

        g2.setStroke(previousStroke);
    }

    private Vector2 projectWorldToScreen(Vector3 worldPosition)
    {
        Renderer renderer = Renderer.renderer;
        if (renderer == null)
        {
            return null;
        }

        Vector3 cameraPosition = renderer.getCameraPosition();
        Quaternion cameraRotation = renderer.getCameraRotation();
        Vector3 relative = worldPosition.minus(cameraPosition);
        relative = cameraRotation.conjugate().rotate(relative);

        if (relative.getZ() <= 0)
        {
            return null;
        }

        double scale = renderer.getScale();
        return new Vector2(
            relative.getX() * scale / relative.getZ() + centerX,
            relative.getY() * scale / relative.getZ() + centerY
        );
    }

    private void drawDeathScreen(Graphics g)
    {
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

        if (Input.input.keys[java.awt.event.KeyEvent.VK_R])
        {
            GameState.gameState.setRestartGame(true);
        }
    }

    public void drawBullet(Graphics g, Vector3 bulletPos, Quaternion cameraRot, Vector3 cameraPos)
    {
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
