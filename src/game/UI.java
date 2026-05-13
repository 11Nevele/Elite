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
    private static final String[] MENU_OPTIONS = {"Single Player", "Two Player", "Competitive"};

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
        if (GameState.gameState.getGameMode() == GameState.GameMode.MENU)
        {
            drawMainMenu(g);
            return;
        }

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
        boolean competitive = GameState.gameState.isCompetitiveMode();
        int x = BAR_MARGIN;
        int y = BAR_MARGIN;
        int width = 280;
        int height = competitive ? 136 : 84;

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(x, y, width, height, 12, 12);
        g.setColor(Color.WHITE);
        g.drawRoundRect(x, y, width, height, 12, 12);

        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.drawString("WAVE " + Math.max(1, GameState.gameState.getCurrentWave()), x + 16, y + 28);
        g.drawString("KILLS " + GameState.gameState.getEnemiesDestroyed(), x + 16, y + 52);
        g.drawString("DIST " + (int) GameState.gameState.getDistanceTravelled(), x + 16, y + 76);

        if (competitive)
        {
            int secondsLeft = (int) Math.ceil(GameState.gameState.getMatchTimeRemainingSec());
            g.drawString("TIME " + secondsLeft, x + 16, y + 100);
            g.drawString("P1 " + GameState.gameState.getScoreP1() + "   P2 " + GameState.gameState.getScoreP2(), x + 16, y + 124);
        }
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

        if (GameState.gameState.isCompetitiveMode())
        {
            String scoreText = "P1: " + GameState.gameState.getScoreP1() + "  P2: " + GameState.gameState.getScoreP2();
            g.drawString(scoreText, screenWidth - SCORE_MARGIN - 320, SCORE_MARGIN + 20);

            String highScoreText = "HIGH: " + GameState.gameState.getHighScore();
            g.drawString(highScoreText, screenWidth - SCORE_MARGIN - 320, SCORE_MARGIN + 45);
            return;
        }

        String scoreText = "SCORE: " + GameState.gameState.getScore();
        g.drawString(scoreText, screenWidth - SCORE_MARGIN - 200, SCORE_MARGIN + 20);

        String highScoreText = "HIGH: " + GameState.gameState.getHighScore();
        g.drawString(highScoreText, screenWidth - SCORE_MARGIN - 200, SCORE_MARGIN + 45);
    }

    private void drawHoldDistanceAim(Graphics g)
    {
        if (Camera.instance != null)
        {
            drawReticleForWeapons(g, Camera.instance.getWeapons(), new Color(255, 220, 120, 220));
        }
        if (SecondPlayer.instance != null)
        {
            drawReticleForWeapons(g, SecondPlayer.instance.getWeapons(), new Color(120, 220, 255, 220));
        }
    }

    private void drawReticleForWeapons(Graphics g, WeaponSystem weapons, Color color)
    {
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

        g2.setColor(color);
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

        FontMetrics fm;
        int textWidth;

        if (GameState.gameState.isCompetitiveMode() && GameState.gameState.getWinner() != GameState.Winner.NONE)
        {
            g.setFont(new Font("Monospaced", Font.BOLD, 48));
            g.setColor(Color.RED);
            String winnerMsg = switch (GameState.gameState.getWinner())
            {
                case PLAYER1 -> "PLAYER 1 WINS";
                case PLAYER2 -> "PLAYER 2 WINS";
                case DRAW -> "DRAW";
                default -> "MATCH OVER";
            };
            fm = g.getFontMetrics();
            textWidth = fm.stringWidth(winnerMsg);
            g.drawString(winnerMsg, centerX - textWidth / 2, centerY - 40);

            g.setFont(new Font("Monospaced", Font.PLAIN, 24));
            g.setColor(Color.WHITE);
            String scoreMsg = "P1: " + GameState.gameState.getScoreP1() + "    P2: " + GameState.gameState.getScoreP2();
            fm = g.getFontMetrics();
            textWidth = fm.stringWidth(scoreMsg);
            g.drawString(scoreMsg, centerX - textWidth / 2, centerY + 10);
        }
        else
        {
            g.setFont(new Font("Monospaced", Font.BOLD, 48));
            g.setColor(Color.RED);

            String deathMsg = GameState.gameState.isCrashed() ? "DESTROYED" : "MISSION FAILED";
            fm = g.getFontMetrics();
            textWidth = fm.stringWidth(deathMsg);
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
        }

        g.setFont(new Font("Monospaced", Font.PLAIN, 24));
        g.setColor(Color.WHITE);
        String restartMsg = "Press any button to return to menu";
        fm = g.getFontMetrics();
        textWidth = fm.stringWidth(restartMsg);
        g.drawString(restartMsg, centerX - textWidth / 2, centerY + 90);

        if (Input.input.isAnyKeyPressed())
        {
            GameState.gameState.setRestartGame(true);
        }
    }

    private void drawMainMenu(Graphics g)
    {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screenWidth, screenHeight);

        g.setFont(new Font("Monospaced", Font.BOLD, 64));
        g.setColor(Color.WHITE);
        String title = "ELITE";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, centerX - fm.stringWidth(title) / 2, centerY - 140);

        g.setFont(new Font("Monospaced", Font.PLAIN, 22));
        String hint = "Select Mode: Up/Down + E/I";
        fm = g.getFontMetrics();
        g.drawString(hint, centerX - fm.stringWidth(hint) / 2, centerY - 80);

        int selection = GameState.gameState.getMenuSelection();
        g.setFont(new Font("Monospaced", Font.BOLD, 34));
        for (int i = 0; i < MENU_OPTIONS.length; i++)
        {
            boolean selected = i == selection;
            g.setColor(selected ? GameColors.LASER_RED : Color.LIGHT_GRAY);
            String option = (selected ? "> " : "  ") + MENU_OPTIONS[i];
            int y = centerY + i * 54;
            fm = g.getFontMetrics();
            g.drawString(option, centerX - fm.stringWidth(option) / 2, y);
        }

        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g.setColor(Color.GRAY);
        String controls = "P1: WASD + Space    P2: Arrow Keys + Right Ctrl";
        fm = g.getFontMetrics();
        g.drawString(controls, centerX - fm.stringWidth(controls) / 2, centerY + 150);
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
