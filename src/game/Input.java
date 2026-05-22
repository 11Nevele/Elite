package game;

import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * Manages keyboard input state.
 * Tracks which keys are currently pressed.
 */
public class Input
{
    public static Input input = new Input();

    private final boolean[] keys = new boolean[256];
    private final boolean[] pressedKeys = new boolean[256];
    private final ArrayDeque<Integer> pendingEvents = new ArrayDeque<>();

    public synchronized void keyDown(int keyCode)
    {
        if (isValidKeyCode(keyCode))
        {
            pendingEvents.addLast(keyCode + 1);
        }
    }

    public synchronized void keyUp(int keyCode)
    {
        if (isValidKeyCode(keyCode))
        {
            pendingEvents.addLast(-(keyCode + 1));
        }
    }

    public synchronized void update()
    {
        Arrays.fill(pressedKeys, false);

        while (!pendingEvents.isEmpty())
        {
            int encodedEvent = pendingEvents.removeFirst();
            boolean isPressEvent = encodedEvent > 0;
            int keyCode = Math.abs(encodedEvent) - 1;

            if (isPressEvent)
            {
                if (!keys[keyCode])
                {
                    pressedKeys[keyCode] = true;
                }

                keys[keyCode] = true;
            }
            else
            {
                keys[keyCode] = false;
            }
        }
    }

    public synchronized boolean isKeyDown(int keyCode)
    {
        return isValidKeyCode(keyCode) && keys[keyCode];
    }

    public synchronized boolean isKeyPressed(int keyCode)
    {
        return isValidKeyCode(keyCode) && pressedKeys[keyCode];
    }

    public synchronized boolean isAnyKeyPressed()
    {
        for (boolean pressedKey : pressedKeys)
        {
            if (pressedKey)
            {
                return true;
            }
        }
        return false;
    }

    private boolean isValidKeyCode(int keyCode)
    {
        return keyCode >= 0 && keyCode < keys.length;
    }
}
