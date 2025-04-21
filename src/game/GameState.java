package game;

/**
 * Manages global game state information.
 * Stores game status, scores, and control flags.
 */
public class GameState
{
    /** Singleton instance */
    public static GameState gameState = new GameState();
    
    /** Whether the player has crashed */
    public boolean crashed = false;
    
    /** Whether the player has run out of fuel */
    public boolean noFuel = false;
    
    /** Highest score achieved in the current session */
    public int highScore = 0;
    
    /** Current player score */
    public int score = 0;
    
    /** Flag to trigger game restart */
    public boolean restartGame = false;
}

