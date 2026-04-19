package game;

import game.engine.*;

/**
 * Spawns timed enemy waves ahead of the player rail.
 */
public class EnemySpawner
{
    private static final int MAX_ACTIVE_ENEMIES = 12;
    private static final double SPAWN_DISTANCE = 420;
    private static final double WAVE_INTERVAL = 2.4;

    private int enemyCount = 0;
    private int waveNumber = 0;
    private double waveTimer = 1.2;

    public void update(double delta, Vector3 playerPos)
    {
        if (GameState.gameState.isDead())
        {
            return;
        }

        waveTimer -= delta;
        if (waveTimer > 0 || enemyCount >= MAX_ACTIVE_ENEMIES)
        {
            return;
        }

        waveNumber++;
        GameState.gameState.setCurrentWave(waveNumber);
        spawnWave(playerPos, waveNumber);
        waveTimer = Math.max(1.5, WAVE_INTERVAL - Math.min(0.8, waveNumber * 0.05));
    }

    public void unregister()
    {
        enemyCount = Math.max(0, enemyCount - 1);
    }

    public void clear()
    {
        enemyCount = 0;
        waveNumber = 0;
        waveTimer = 1.2;
    }

    private void spawnWave(Vector3 playerPos, int waveIndex)
    {
        switch ((waveIndex - 1) % 4)
        {
            case 0 -> spawnLineFormation(playerPos);
            case 1 -> spawnSweeper(playerPos, -1);
            case 2 -> spawnSweeper(playerPos, 1);
            default -> spawnSerpentine(playerPos);
        }
    }

    private void spawnLineFormation(Vector3 playerPos)
    {
        double[] lanes = {-36, -12, 12, 36};
        for (int i = 0; i < lanes.length; i++)
        {
            spawnEnemy(
                playerPos,
                new Vector3(lanes[i], -6 + i * 4, SPAWN_DISTANCE + i * 18),
                new Vector3(0, 0, Camera.SCROLL_SPEED * 0.35),
                new Vector3(),
                0,
                0,
                0
            );
        }
    }

    private void spawnSweeper(Vector3 playerPos, double side)
    {
        double spawnX = side < 0 ? -72 : 72;
        double horizontalSpeed = side < 0 ? 24 : -24;
        for (int i = 0; i < 3; i++)
        {
            spawnEnemy(
                playerPos,
                new Vector3(spawnX, -16 + i * 16, SPAWN_DISTANCE + i * 24),
                new Vector3(horizontalSpeed, 0, Camera.SCROLL_SPEED * 0.48),
                new Vector3(0, 1, 0),
                5,
                2.2,
                i * 0.9
            );
        }
    }

    private void spawnSerpentine(Vector3 playerPos)
    {
        for (int i = 0; i < 4; i++)
        {
            double lane = -30 + i * 20;
            spawnEnemy(
                playerPos,
                new Vector3(lane, -18 + i * 8, SPAWN_DISTANCE + i * 18),
                new Vector3(0, 0, Camera.SCROLL_SPEED * 0.4),
                new Vector3(i % 2 == 0 ? 1 : -1, 0, 0),
                14,
                2.8,
                i * Math.PI / 3
            );
        }
    }

    private void spawnEnemy(Vector3 playerPos, Vector3 spawnOffset, Vector3 velocity,
                            Vector3 oscillationAxis, double oscillationAmplitude,
                            double oscillationFrequency, double oscillationPhase)
    {
        if (enemyCount >= MAX_ACTIVE_ENEMIES)
        {
            return;
        }

        Enemy enemy = new Enemy(
            Models.tieFighter,
            playerPos.plus(spawnOffset),
            velocity,
            oscillationAxis,
            oscillationAmplitude,
            oscillationFrequency,
            oscillationPhase
        );
        registerSpawnedEnemy(enemy);
    }

    private void registerSpawnedEnemy(Enemy enemy)
    {
        if (enemy != null)
        {
            enemyCount++;
        }
    }
}
