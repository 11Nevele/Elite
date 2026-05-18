package game;

import game.engine.CollisionLayer;

/**
 * Tracks the current game state for the rail-shooter prototype.
 */
public class GameState
{
    private static final HighScoreRecorder HIGH_SCORE_RECORDER = new HighScoreRecorder();

    public enum GameMode
    {
        MENU,
        LAUNCH_ANIMATION,
        SINGLE_PLAYER,
        TWO_PLAYER,
        COMPETITIVE
    }

    public enum Winner
    {
        NONE,
        PLAYER1,
        PLAYER2,
        DRAW
    }

    private static final double DEFAULT_MATCH_DURATION_SEC = 180;

    public static GameState gameState = new GameState();

    private boolean crashed;
    private boolean noFuel;
    private int singlePlayerHighScore;
    private int twoPlayerHighScore;
    private int score;
    private int scoreP1;
    private int scoreP2;
    private boolean player1Dead;
    private boolean player2Dead;
    private Winner winner;
    private double matchTimerSec;
    private double matchDurationSec;
    private boolean restartGame;
    private double distanceTravelled;
    private int currentWave;
    private int enemiesDestroyed;
    private GameMode gameMode;
    private int menuSelection;

    public GameState()
    {
        HighScoreRecorder.HighScores highScores = HIGH_SCORE_RECORDER.load();
        singlePlayerHighScore = highScores.getSinglePlayerHighScore();
        twoPlayerHighScore = highScores.getTwoPlayerHighScore();
        reset();
    }

    public final void reset()
    {
        crashed = false;
        noFuel = false;
        score = 0;
        scoreP1 = 0;
        scoreP2 = 0;
        player1Dead = false;
        player2Dead = false;
        winner = Winner.NONE;
        matchTimerSec = 0;
        matchDurationSec = DEFAULT_MATCH_DURATION_SEC;
        restartGame = false;
        distanceTravelled = 0;
        currentWave = 0;
        enemiesDestroyed = 0;
        gameMode = GameMode.MENU;
        menuSelection = 0;
    }

    public boolean isCrashed() { return crashed; }
    public void setCrashed(boolean crashed) { this.crashed = crashed; }

    public boolean isNoFuel() { return noFuel; }
    public void setNoFuel(boolean noFuel) { this.noFuel = noFuel; }

    public int getHighScore()
    {
        return switch (gameMode)
        {
            case SINGLE_PLAYER -> singlePlayerHighScore;
            case TWO_PLAYER -> twoPlayerHighScore;
            default -> 0;
        };
    }

    public int getSinglePlayerHighScore() { return singlePlayerHighScore; }

    public int getTwoPlayerHighScore() { return twoPlayerHighScore; }

    public int getScore() { return score; }
    public void setScore(int score)
    {
        this.score = score;
        updateRecordedHighScore(score);
    }

    public void addScore(int points)
    {
        setScore(score + points);
    }

    public int getScoreP1() { return scoreP1; }

    public int getScoreP2() { return scoreP2; }

    public void addScoreP1(int points)
    {
        scoreP1 += points;
    }

    public void addScoreP2(int points)
    {
        scoreP2 += points;
    }

    private void updateRecordedHighScore(int candidateScore)
    {
        if (gameMode == GameMode.SINGLE_PLAYER)
        {
            if (candidateScore <= singlePlayerHighScore)
            {
                return;
            }

            singlePlayerHighScore = candidateScore;
            HIGH_SCORE_RECORDER.save(singlePlayerHighScore, twoPlayerHighScore);
            return;
        }

        if (gameMode == GameMode.TWO_PLAYER)
        {
            if (candidateScore <= twoPlayerHighScore)
            {
                return;
            }

            twoPlayerHighScore = candidateScore;
            HIGH_SCORE_RECORDER.save(singlePlayerHighScore, twoPlayerHighScore);
        }
    }

    public boolean isPlayer1Dead() { return player1Dead; }

    public boolean isPlayer2Dead() { return player2Dead; }

    public Winner getWinner() { return winner; }

    public double getMatchTimerSec() { return matchTimerSec; }

    public double getMatchDurationSec() { return matchDurationSec; }

    public void setMatchDurationSec(double matchDurationSec)
    {
        this.matchDurationSec = Math.max(1, matchDurationSec);
    }

    public double getMatchTimeRemainingSec()
    {
        return Math.max(0, matchDurationSec - matchTimerSec);
    }

    public boolean isCompetitiveMode()
    {
        return gameMode == GameMode.COMPETITIVE;
    }

    public boolean isMatchOver()
    {
        return isCompetitiveMode() && winner != Winner.NONE;
    }

    public void updateCompetitiveTimer(double delta)
    {
        if (!isCompetitiveMode() || isDead() || winner != Winner.NONE)
        {
            return;
        }

        matchTimerSec += delta;
        if (matchTimerSec >= matchDurationSec)
        {
            matchTimerSec = matchDurationSec;
            resolveWinnerByScore();
        }
    }

    public void markPlayerDead(int collisionLayer)
    {
        if (gameMode == GameMode.SINGLE_PLAYER)
        {
            crashed = true;
            return;
        }

        if (gameMode != GameMode.TWO_PLAYER && gameMode != GameMode.COMPETITIVE)
        {
            crashed = true;
            return;
        }

        if (winner != Winner.NONE)
        {
            return;
        }

        if (collisionLayer == CollisionLayer.PLAYER)
        {
            player1Dead = true;
        }
        else if (collisionLayer == CollisionLayer.PLAYER2)
        {
            player2Dead = true;
        }

        if (player1Dead && player2Dead)
        {
            if (isCompetitiveMode())
            {
                resolveWinnerByScore();
            }
            else
            {
                crashed = true;
            }
            return;
        }
    }

    public void resolveWinnerByScore()
    {
        if (!isCompetitiveMode() || winner != Winner.NONE)
        {
            return;
        }

        if (scoreP1 > scoreP2)
        {
            setWinner(Winner.PLAYER1);
        }
        else if (scoreP2 > scoreP1)
        {
            setWinner(Winner.PLAYER2);
        }
        else
        {
            setWinner(Winner.DRAW);
        }
    }

    private void setWinner(Winner winner)
    {
        this.winner = winner;
        crashed = true;
    }

    public boolean isRestartGame() { return restartGame; }
    public void setRestartGame(boolean restartGame) { this.restartGame = restartGame; }

    public double getDistanceTravelled() { return distanceTravelled; }
    public void addDistance(double distanceTravelled) { this.distanceTravelled += distanceTravelled; }

    public int getCurrentWave() { return currentWave; }
    public void setCurrentWave(int currentWave) { this.currentWave = currentWave; }

    public int getEnemiesDestroyed() { return enemiesDestroyed; }
    public void recordEnemyDestroyed() { enemiesDestroyed++; }

    public GameMode getGameMode() { return gameMode; }
    public void setGameMode(GameMode gameMode) { this.gameMode = gameMode; }

    public int getMenuSelection() { return menuSelection; }
    public void setMenuSelection(int menuSelection) { this.menuSelection = menuSelection; }

    public boolean isDead()
    {
        return crashed || noFuel;
    }
}
