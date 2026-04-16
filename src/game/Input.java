package game;

/**
 * Manages keyboard input state.
 * Tracks which keys are currently pressed.
 */
public class Input
{
    public static Input input = new Input();

    public boolean[] keys = new boolean[256];
    private boolean[] prevKeys = new boolean[256];

    public void keyDown(int keyCode)
    {
        if (keyCode >= 0 && keyCode < keys.length)
        {
            keys[keyCode] = true;
        }
    }

    public void keyUp(int keyCode)
    {
        if (keyCode >= 0 && keyCode < keys.length)
        {
            keys[keyCode] = false;
        }
    }

    public void update()
    {
        System.arraycopy(keys, 0, prevKeys, 0, keys.length);
    }

    public boolean isKeyDown(int keyCode)
    {
        return keyCode >= 0 && keyCode < keys.length && keys[keyCode];
    }

    public boolean isKeyPressed(int keyCode)
    {
        return keyCode >= 0 && keyCode < keys.length && keys[keyCode] && !prevKeys[keyCode];
    }
}
