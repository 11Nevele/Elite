package game;

import game.engine.Vector2;

public class Input 
{
    public static Input input = new Input();
    public Vector2 mousePos = new Vector2();
    public boolean mouseDown = false;
    public boolean[] keys = new boolean[256];
    public boolean[] keyPressed = new boolean[256];
    public boolean[] keyReleased = new boolean[256];
}
