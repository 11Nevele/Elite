package game;

/**
 * Tracks the current game state: score, fuel, crash status, restart flag.
 */
public class GameState
{
    public static GameState gameState = new GameState();

    private boolean crashed;
    private boolean noFuel;
    private int highScore;
    private int score;
    private boolean restartGame;

    public GameState()
    {
        reset();
    }

    public void reset()
    {
        crashed = false;
        noFuel = false;
        score = 0;
        restartGame = false;
    }

    public boolean isCrashed() { return crashed; }
    public void setCrashed(boolean crashed) { this.crashed = crashed; }

    public boolean isNoFuel() { return noFuel; }
    public void setNoFuel(boolean noFuel) { this.noFuel = noFuel; }

    public int getHighScore() { return highScore; }
    public void setHighScore(int highScore) { this.highScore = highScore; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public void addScore(int points) { this.score += points; }

    public boolean isRestartGame() { return restartGame; }
    public void setRestartGame(boolean restartGame) { this.restartGame = restartGame; }

    public boolean isDead()
    {
        return crashed || noFuel;
    }
}
