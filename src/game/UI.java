package game;

import game.engine.*;
import java.awt.*;
import java.awt.event.KeyEvent;

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
    private static final double SECTOR_LENGTH = 1000;
    private static final int AIM_DEBUG_PANEL_WIDTH = 250;
    private static final int AIM_DEBUG_PANEL_HEIGHT = 132;
    private static final int AIM_DEBUG_MARKER_RADIUS = 12;

    private final int screenWidth;
    private final int screenHeight;
    private final int centerX;
    private final int centerY;
    private boolean aimAssistDebugVisible = false;

    public UI(int width, int height)
    {
        this.screenWidth = width;
        this.screenHeight = height;
        this.centerX = width / 2;
        this.centerY = height / 2;
    }

    public void draw(Graphics g)
    {
        toggleAimAssistDebugIfPressed();

        if (GameState.gameState.isDead())
        {
            drawDeathScreen(g);
            return;
        }

        drawStatusPanel(g);
        drawSectorBar(g);
        drawScore(g);
        drawAimAssistDebug(g);
    }

    private void toggleAimAssistDebugIfPressed()
    {
        if (Input.input.isKeyPressed(KeyEvent.VK_F5))
        {
            aimAssistDebugVisible = !aimAssistDebugVisible;
        }
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

    private void drawAimAssistDebug(Graphics g)
    {
        if (!aimAssistDebugVisible || Camera.instance == null || Renderer.renderer == null)
        {
            return;
        }

        WeaponSystem.AimAssistDebugState debugState = Camera.instance.getWeapons().getAimAssistDebugState();
        drawAimAssistPanel(g, debugState);

        if (debugState.hasTarget())
        {
            drawAimAssistMarker(g, debugState.getTargetPosition());
        }
    }

    private void drawAimAssistPanel(Graphics g, WeaponSystem.AimAssistDebugState debugState)
    {
        int x = screenWidth - SCORE_MARGIN - AIM_DEBUG_PANEL_WIDTH;
        int y = SCORE_MARGIN + 62;

        g.setColor(new Color(0, 0, 0, 170));
        g.fillRoundRect(x, y, AIM_DEBUG_PANEL_WIDTH, AIM_DEBUG_PANEL_HEIGHT, 12, 12);
        g.setColor(new Color(120, 255, 255));
        g.drawRoundRect(x, y, AIM_DEBUG_PANEL_WIDTH, AIM_DEBUG_PANEL_HEIGHT, 12, 12);

        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        g.drawString("AIM DEBUG [F5]", x + 14, y + 22);

        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g.setColor(Color.WHITE);
        g.drawString("Target: " + (debugState.hasTarget() ? "LOCKED" : "NONE"), x + 14, y + 46);
        g.drawString(String.format("Range: %.0f  Blend: %.2f", WeaponSystem.getAimAssistRange(), WeaponSystem.getAimAssistBlend()), x + 14, y + 66);
        g.drawString(String.format("Cone: %.2f", WeaponSystem.getAimAssistConeDot()), x + 14, y + 86);

        if (debugState.hasTarget())
        {
            g.drawString(String.format("Dist: %.1f", debugState.getTargetDistance()), x + 14, y + 106);
            g.drawString(String.format("Align: %.3f  Score: %.3f", debugState.getTargetAlignment(), debugState.getTargetScore()), x + 14, y + 126);
        }
        else
        {
            g.drawString("Dist: --", x + 14, y + 106);
            g.drawString("Align: --     Score: --", x + 14, y + 126);
        }
    }

    private void drawAimAssistMarker(Graphics g, Vector3 worldPosition)
    {
        Vector2 screenPosition = projectWorldToScreen(worldPosition);
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

        g2.setColor(new Color(120, 255, 255, 220));
        g2.drawOval(sx - AIM_DEBUG_MARKER_RADIUS, sy - AIM_DEBUG_MARKER_RADIUS,
            AIM_DEBUG_MARKER_RADIUS * 2, AIM_DEBUG_MARKER_RADIUS * 2);
        g2.drawLine(centerX - 6, centerY, centerX + 6, centerY);
        g2.drawLine(centerX, centerY - 6, centerX, centerY + 6);
        g2.drawLine(centerX, centerY, sx, sy);
        g2.fillRect(sx - 2, sy - 2, 5, 5);

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
