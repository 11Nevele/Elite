package game;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

/**
 * Manages game audio using javax.sound.sampled.
 */
public class Audio
{
    private static Clip laserClip;
    private static Clip explosionClip;
    private static Clip ambientClip;
    private static boolean initialized = false;

    public static void init()
    {
        if (initialized) return;
        try
        {
            String soundDir = "Sound" + File.separator;
            laserClip = loadClip(soundDir + "Hyper 7.wav");
            explosionClip = loadClip(soundDir + "explosion.wav");
            ambientClip = loadClip(soundDir + "Ambient.wav");
            initialized = true;
        }
        catch (Exception e)
        {
            System.err.println("Failed to load audio files: " + e.getMessage());
        }
    }

    private static Clip loadClip(String path)
        throws UnsupportedAudioFileException, IOException, LineUnavailableException
    {
        File file = new File(path);
        if (!file.exists()) return null;
        AudioInputStream stream = AudioSystem.getAudioInputStream(file);
        Clip clip = AudioSystem.getClip();
        clip.open(stream);
        return clip;
    }

    public static void playLaser()
    {
        playOnce(laserClip);
    }

    public static void playExplosion()
    {
        playOnce(explosionClip);
    }

    public static void playAmbient()
    {
        if (ambientClip != null)
        {
            ambientClip.setFramePosition(0);
            ambientClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public static void stopAmbient()
    {
        if (ambientClip != null) ambientClip.stop();
    }

    private static void playOnce(Clip clip)
    {
        if (clip != null)
        {
            clip.setFramePosition(0);
            clip.start();
        }
    }
}
