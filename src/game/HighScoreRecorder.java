package game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Persists high scores for the local game installation.
 */
public final class HighScoreRecorder
{
    private static final String FILE_NAME = "elite-highscores.properties";
    private static final String SINGLE_PLAYER_KEY = "singlePlayer";
    private static final String TWO_PLAYER_KEY = "twoPlayer";

    private final Path filePath;

    public HighScoreRecorder()
    {
        filePath = Paths.get(FILE_NAME);
    }

    public HighScores load()
    {
        Properties properties = new Properties();

        if (Files.isRegularFile(filePath))
        {
            try (InputStream input = Files.newInputStream(filePath))
            {
                properties.load(input);
            }
            catch (IOException ignored)
            {
                return new HighScores(0, 0);
            }
        }

        return new HighScores(
            parseScore(properties, SINGLE_PLAYER_KEY),
            parseScore(properties, TWO_PLAYER_KEY)
        );
    }

    public void save(int singlePlayerHighScore, int twoPlayerHighScore)
    {
        Properties properties = new Properties();
        properties.setProperty(SINGLE_PLAYER_KEY, Integer.toString(Math.max(0, singlePlayerHighScore)));
        properties.setProperty(TWO_PLAYER_KEY, Integer.toString(Math.max(0, twoPlayerHighScore)));

        try (OutputStream output = Files.newOutputStream(filePath))
        {
            properties.store(output, "Elite high scores");
        }
        catch (IOException ignored)
        {
            // Keep the in-memory high score even if the file cannot be written.
        }
    }

    private int parseScore(Properties properties, String key)
    {
        String value = properties.getProperty(key, "0").trim();
        try
        {
            return Math.max(0, Integer.parseInt(value));
        }
        catch (NumberFormatException ignored)
        {
            return 0;
        }
    }

    public static final class HighScores
    {
        private final int singlePlayerHighScore;
        private final int twoPlayerHighScore;

        public HighScores(int singlePlayerHighScore, int twoPlayerHighScore)
        {
            this.singlePlayerHighScore = Math.max(0, singlePlayerHighScore);
            this.twoPlayerHighScore = Math.max(0, twoPlayerHighScore);
        }

        public int getSinglePlayerHighScore()
        {
            return singlePlayerHighScore;
        }

        public int getTwoPlayerHighScore()
        {
            return twoPlayerHighScore;
        }
    }
}