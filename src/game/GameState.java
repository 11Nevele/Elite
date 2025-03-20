package game;


public class GameState
{
    public static GameState gameState = new GameState();
    public boolean crashed = false;
    public boolean noFuel = false;
    public int highScore = 0;
    public int score = 0;
    public boolean restartGame = false;
}

