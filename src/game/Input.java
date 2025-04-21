package game;

import game.engine.Vector2;

/**
 * Handles and stores input state for the game.
 * Keeps track of keyboard and mouse inputs.
 */
public class Input 
{
    /** Singleton instance */
    public static Input input = new Input();
    
    /** Current mouse position */
    public Vector2 mousePos = new Vector2();
    
    /** Whether mouse button is currently pressed */
    public boolean mouseDown = false;
    
    /** Array of current key states */
    public boolean[] keys = new boolean[256];
    
    /** Array tracking keys that were just pressed this frame */
    public boolean[] keyPressed = new boolean[256];
    
    /** Array tracking keys that were just released this frame */
    public boolean[] keyReleased = new boolean[256];
}
