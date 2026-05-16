package game;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

/**
 * Manages game audio using javax.sound.sampled.
 */
public class Audio
{
    private static final int LASER_VOICE_COUNT = 8;
    private static final int EXPLOSION_VOICE_COUNT = 8;

    private static Clip[] player1LaserClips;
    private static Clip[] player2LaserClips;
    private static int player1LaserIndex;
    private static int player2LaserIndex;

    private static Clip[] explosionClips;
    private static int explosionIndex;
    private static Clip ambientClip;
    private static Clip battleMusicClip;
    private static Clip menuSelectClip;
    private static boolean initialized = false;

    public static void init()
    {
        if (initialized) return;
        String soundDir = "Sound" + File.separator;
        player1LaserClips = loadClipPoolSafely(soundDir + "Hyper 3.wav", LASER_VOICE_COUNT);
        player2LaserClips = loadClipPoolSafely(soundDir + "Hyper 4.wav", LASER_VOICE_COUNT);
        explosionClips = loadClipPoolSafely(soundDir + "explosion.wav", EXPLOSION_VOICE_COUNT);
        ambientClip = loadClipSafely(soundDir + "through space.wav");
        battleMusicClip = loadClipSafely(soundDir + "spacebattle.wav");
        menuSelectClip = loadClipSafely(soundDir + "di.wav");
        initialized = true;
    }

    private static Clip loadClipSafely(String path)
    {
        try
        {
            return loadClip(path);
        }
        catch (UnsupportedAudioFileException | IOException | LineUnavailableException | IllegalArgumentException e)
        {
            System.err.println("Failed to load audio file '" + path + "': " + e.getMessage());
            return null;
        }
    }

    private static Clip[] loadClipPoolSafely(String path, int count)
    {
        Clip[] clips = new Clip[count];
        for (int i = 0; i < count; i++)
        {
            clips[i] = loadClipSafely(path);
        }
        return clips;
    }

    private static Clip loadClip(String path)
        throws UnsupportedAudioFileException, IOException, LineUnavailableException
    {
        File file = new File(path);
        if (!file.exists()) return null;

        AudioInputStream sourceStream = AudioSystem.getAudioInputStream(file);
        AudioFormat sourceFormat = sourceStream.getFormat();
        AudioFormat decodedFormat = sourceFormat;

        if (sourceFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED || sourceFormat.getSampleSizeInBits() != 16)
        {
            decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sourceFormat.getSampleRate(),
                16,
                sourceFormat.getChannels(),
                sourceFormat.getChannels() * 2,
                sourceFormat.getSampleRate(),
                false
            );
        }

        AudioInputStream stream = sourceStream;
        if (!sourceFormat.matches(decodedFormat))
        {
            stream = AudioSystem.getAudioInputStream(decodedFormat, sourceStream);
        }

        Clip clip = AudioSystem.getClip();
        clip.open(stream);
        return clip;
    }

    public static void playPlayer1Laser()
    {
        player1LaserIndex = playFromPool(player1LaserClips, player1LaserIndex);
    }

    public static void playPlayer2Laser()
    {
        player2LaserIndex = playFromPool(player2LaserClips, player2LaserIndex);
    }

    public static void playExplosion()
    {
        explosionIndex = playFromPool(explosionClips, explosionIndex);
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

    public static void playBattleMusic()
    {
        if (battleMusicClip != null)
        {
            battleMusicClip.setFramePosition(0);
            battleMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public static void stopBattleMusic()
    {
        if (battleMusicClip != null) battleMusicClip.stop();
    }


    public static void playMenuSelect()
    {
        if (menuSelectClip != null)
        {
            if (menuSelectClip.isRunning()) menuSelectClip.stop();
            menuSelectClip.setFramePosition(0);
            menuSelectClip.start();
        }
    }

    private static int playFromPool(Clip[] clips, int currentIndex)
    {
        if (clips == null || clips.length == 0)
        {
            return 0;
        }

        Clip clip = clips[currentIndex];
        if (clip != null)
        {
            if (clip.isRunning())
            {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        }

        return (currentIndex + 1) % clips.length;
    }
}
