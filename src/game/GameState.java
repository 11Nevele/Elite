package game;

/**
 * Tracks the current game state for the rail-shooter prototype.
 */
public class GameState
{
    public static GameState gameState = new GameState();

    private boolean crashed;
    private boolean noFuel;
    private int highScore;
    private int score;
    private boolean restartGame;
    private double distanceTravelled;
    private int currentWave;
    private int enemiesDestroyed;

    public GameState()
    {
        reset();
    }

    public final void reset()
    {
        crashed = false;
        noFuel = false;
        score = 0;
        restartGame = false;
        distanceTravelled = 0;
        currentWave = 0;
        enemiesDestroyed = 0;
    }

    public boolean isCrashed() { return crashed; }
    public void setCrashed(boolean crashed) { this.crashed = crashed; }

    public boolean isNoFuel() { return noFuel; }
    public void setNoFuel(boolean noFuel) { this.noFuel = noFuel; }

    public int getHighScore() { return highScore; }
    public void setHighScore(int highScore) { this.highScore = highScore; }

    public int getScore() { return score; }
    public void setScore(int score)
    {
        this.score = score;
        highScore = Math.max(highScore, score);
    }
    public void addScore(int points) { setScore(score + points); }

    public boolean isRestartGame() { return restartGame; }
    public void setRestartGame(boolean restartGame) { this.restartGame = restartGame; }

    public double getDistanceTravelled() { return distanceTravelled; }
    public void addDistance(double distanceTravelled) { this.distanceTravelled += distanceTravelled; }

    public int getCurrentWave() { return currentWave; }
    public void setCurrentWave(int currentWave) { this.currentWave = currentWave; }

    public int getEnemiesDestroyed() { return enemiesDestroyed; }
    public void recordEnemyDestroyed() { enemiesDestroyed++; }

    public boolean isDead()
    {
        return crashed || noFuel;
    }
}
