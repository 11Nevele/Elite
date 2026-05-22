package game;

import game.engine.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Handles HUD rendering for the rail-shooter prototype.
 */
public class UI
{
    public static UI ui;

    private static final int BAR_MARGIN = 20;
    private static final int SCORE_MARGIN = 20;
    private static final int AIM_RETICLE_RADIUS = 10;
    private static final String[] MENU_OPTIONS = {"Single Player", "Two Player", "Competitive", "Quit Game"};

    private final int screenWidth;
    private final int screenHeight;
    private final int centerX;
    private final int centerY;

    private BufferedImage instructionImage;
    private boolean instructionImageLoaded = false;

    public UI(int width, int height)
    {
        this.screenWidth = width;
        this.screenHeight = height;
        this.centerX = width / 2;
        this.centerY = height / 2;
    }

    public void draw(Graphics g)
    {
        GameState.GameMode mode = GameState.gameState.getGameMode();

        if (mode == GameState.GameMode.MENU)
        {
            drawMainMenu(g);
            return;
        }

        if (mode == GameState.GameMode.LAUNCH_ANIMATION)
        {
            drawLaunchOverlay(g);
            return;
        }

        if (GameState.gameState.isDead())
        {
            drawDeathScreen(g);
            return;
        }

        drawStatusPanel(g);
        drawScore(g);
        drawQuitButton(g);
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

    private void drawScore(Graphics g)
    {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 20));

        if (GameState.gameState.isCompetitiveMode())
        {
            String scoreText = "P1: " + GameState.gameState.getScoreP1() + "  P2: " + GameState.gameState.getScoreP2();
            g.drawString(scoreText, screenWidth - SCORE_MARGIN - 320, SCORE_MARGIN + 20);
            return;
        }

        String scoreText = "SCORE: " + GameState.gameState.getScore();
        g.drawString(scoreText, screenWidth - SCORE_MARGIN - 200, SCORE_MARGIN + 20);

        String highScoreText = "HIGH: " + GameState.gameState.getHighScore();
        g.drawString(highScoreText, screenWidth - SCORE_MARGIN - 200, SCORE_MARGIN + 45);
    }

    private void drawHoldDistanceAim(Graphics g)
    {
        if (Camera.instance != null && !GameState.gameState.isPlayer1Dead())
        {
            drawReticleForWeapons(g, Camera.instance.getWeapons(), new Color(255, 220, 120, 220));
        }
        if (SecondPlayer.instance != null && !GameState.gameState.isPlayer2Dead())
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

            String highScoreMsg = "High Score: " + GameState.gameState.getHighScore();
            textWidth = fm.stringWidth(highScoreMsg);
            g.drawString(highScoreMsg, centerX - textWidth / 2, centerY + 50);

            String waveMsg = "Wave: " + Math.max(1, GameState.gameState.getCurrentWave());
            textWidth = fm.stringWidth(waveMsg);
            g.drawString(waveMsg, centerX - textWidth / 2, centerY + 80);
        }

        g.setFont(new Font("Monospaced", Font.PLAIN, 24));
        g.setColor(Color.WHITE);
        String restartMsg = "Press any button to return to menu";
        fm = g.getFontMetrics();
        textWidth = fm.stringWidth(restartMsg);
        g.drawString(restartMsg, centerX - textWidth / 2, centerY+200);

        if (Input.input.isAnyKeyPressed())
        {
            GameState.gameState.setRestartGame(true);
        }
    }

    private void drawMainMenu(Graphics g)
    {
        if (!instructionImageLoaded)
        {
            instructionImageLoaded = true;
            try
            {
                instructionImage = ImageIO.read(new File("Model" + File.separator + "ArcadeInstruction.png"));
            }
            catch (java.io.IOException e)
            {
                instructionImage = null;
            }
        }

        if (instructionImage != null)
        {
            int imgW = 320;
            int imgH = instructionImage.getHeight() * imgW / instructionImage.getWidth();
            int imgX = screenWidth - imgW - 20;
            int imgY = screenHeight - imgH - 20;
            g.drawImage(instructionImage, imgX, imgY, imgW, imgH, null);
        }

        // The 3D renderer already drew the star field and preview ships into the
        // back buffer.  Draw a translucent panel only behind the text so the ships
        // remain visible at the edges of the screen.
        int panelW = 480;
        int panelX = 30;
        int panelCX = panelX + panelW / 2;   // horizontal centre of the left panel
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(panelX, centerY - 195, panelW, 420, 20, 20);

        g.setFont(new Font("Monospaced", Font.BOLD, 64));
        g.setColor(Color.WHITE);
        String title = "ELITE";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, panelCX - fm.stringWidth(title) / 2, centerY - 130);

        int selection = GameState.gameState.getMenuSelection();
        g.setFont(new Font("Monospaced", Font.BOLD, 34));
        for (int i = 0; i < MENU_OPTIONS.length; i++)
        {
            boolean selected = i == selection;
            g.setColor(selected ? GameColors.LASER_RED : Color.LIGHT_GRAY);
            String option = (selected ? "> " : "  ") + MENU_OPTIONS[i];
            int y = centerY + i * 54;
            fm = g.getFontMetrics();
            g.drawString(option, panelCX - fm.stringWidth(option) / 2, y);
        }

        g.setFont(new Font("Monospaced", Font.PLAIN, 22));
        g.setColor(Color.WHITE);
        String highScoreText = switch (selection)
        {
            case 0 -> "HIGHSCORE: " + GameState.gameState.getSinglePlayerHighScore();
            case 1 -> "HIGHSCORE: " + GameState.gameState.getTwoPlayerHighScore();
            default -> "";
        };
        fm = g.getFontMetrics();
        g.drawString(highScoreText, panelCX - fm.stringWidth(highScoreText) / 2, centerY + 192);

    }

    private void drawQuitButton(Graphics g)
    {
        int buttonWidth = 240;
        int buttonHeight = 42;
        int x = screenWidth - buttonWidth - 20;
        int y = screenHeight - buttonHeight - 20;

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(x, y, buttonWidth, buttonHeight, 12, 12);
        g.setColor(Color.WHITE);
        g.drawRoundRect(x, y, buttonWidth, buttonHeight, 12, 12);

        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        String label = "ESC  QUIT TO MENU";
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (buttonWidth - fm.stringWidth(label)) / 2;
        int textY = y + ((buttonHeight - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(label, textX, textY);
    }

    private void drawLaunchOverlay(Graphics g)
    {
        g.setFont(new Font("Monospaced", Font.BOLD, 28));
        FontMetrics fm = g.getFontMetrics();
        String msg = "LAUNCHING...";
        int tw = fm.stringWidth(msg);
        // Draw a subtle shadow then the text
        g.setColor(new Color(0, 0, 0, 160));
        g.drawString(msg, centerX - tw / 2 + 2, centerY + 192);
        g.setColor(new Color(255, 255, 255, 220));
        g.drawString(msg, centerX - tw / 2, centerY + 190);
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
