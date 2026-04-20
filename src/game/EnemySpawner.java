package game;

import game.engine.*;

/**
 * Spawns timed enemy waves ahead of the player rail.
 */
public class EnemySpawner
{
    private static final int MAX_ACTIVE_ENEMIES = 12;
    private static final double HOLD_DISTANCE = 120;
    private static final double FRONT_ENTRY_DEPTH = 180;
    private static final double BACK_ENTRY_DEPTH = 170;
    private static final double FRONT_ENTRY_SIDE_OFFSET = 340;
    private static final double BACK_ENTRY_SIDE_JITTER = 48;
    private static final double BACK_ENTRY_VERTICAL_JITTER = 24;
    private static final double MIN_HOLD_DURATION = 1.4;
    private static final double MAX_HOLD_DURATION = 3.6;
    private static final double FRONT_ENTRY_SPEED = 180;
    private static final double BACK_ENTRY_SPEED = 300;
    private static final double BASE_EXIT_SPEED = 220;
    private static final double EXIT_SPEED_VARIANCE = 40;
    private static final double EXIT_VERTICAL_SPEED = 18;
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
                new Vector3(lanes[i], -6 + i * 4, HOLD_DISTANCE + i * 12),
                new Vector3(),
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
                new Vector3(spawnX, -16 + i * 16, HOLD_DISTANCE + i * 18),
                new Vector3(horizontalSpeed, 0, 0),
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
                new Vector3(lane, -18 + i * 8, HOLD_DISTANCE + i * 14),
                new Vector3(),
                new Vector3(i % 2 == 0 ? 1 : -1, 0, 0),
                14,
                2.8,
                i * Math.PI / 3
            );
        }
    }

    private void spawnEnemy(Vector3 playerPos, Vector3 holdOffset, Vector3 holdDriftVelocity,
                            Vector3 oscillationAxis, double oscillationAmplitude,
                            double oscillationFrequency, double oscillationPhase)
    {
        if (enemyCount >= MAX_ACTIVE_ENEMIES)
        {
            return;
        }

        Enemy.EntryType entryType = Math.random() < 0.5 ? Enemy.EntryType.FRONT : Enemy.EntryType.BACK;
        Vector3 holdPosition = playerPos.plus(holdOffset);
        Vector3 spawnPosition = createSpawnPosition(playerPos, holdPosition, entryType);
        double holdDuration = randomBetween(MIN_HOLD_DURATION, MAX_HOLD_DURATION);
        double entrySpeed = entryType == Enemy.EntryType.FRONT ? FRONT_ENTRY_SPEED : BACK_ENTRY_SPEED;
        double exitDirection = Math.random() < 0.5 ? -1 : 1;
        double exitSpeed = BASE_EXIT_SPEED + Math.random() * EXIT_SPEED_VARIANCE;
        double verticalExitSpeed = randomBetween(-EXIT_VERTICAL_SPEED, EXIT_VERTICAL_SPEED);

        Enemy enemy = new Enemy(
            Models.tieFighter,
            spawnPosition,
            holdPosition,
            entryType,
            entrySpeed,
            holdDriftVelocity,
            oscillationAxis,
            oscillationAmplitude,
            oscillationFrequency,
            oscillationPhase,
            holdDuration,
            new Vector3(exitDirection * exitSpeed, verticalExitSpeed, 0)
        );
        registerSpawnedEnemy(enemy);
    }

    private Vector3 createSpawnPosition(Vector3 playerPos, Vector3 holdPosition, Enemy.EntryType entryType)
    {
        if (entryType == Enemy.EntryType.FRONT)
        {
            double side = Math.random() < 0.5 ? -1 : 1;
            return new Vector3(
                holdPosition.getX() + side * FRONT_ENTRY_SIDE_OFFSET,
                holdPosition.getY() + randomBetween(-20, 20),
                holdPosition.getZ() + FRONT_ENTRY_DEPTH + Math.random() * 60
            );
        }

        return new Vector3(
            holdPosition.getX() + randomBetween(-BACK_ENTRY_SIDE_JITTER, BACK_ENTRY_SIDE_JITTER),
            holdPosition.getY() + randomBetween(-BACK_ENTRY_VERTICAL_JITTER, BACK_ENTRY_VERTICAL_JITTER),
            playerPos.getZ() - BACK_ENTRY_DEPTH - Math.random() * 70
        );
    }

    private double randomBetween(double min, double max)
    {
        return min + Math.random() * (max - min);
    }

    private void registerSpawnedEnemy(Enemy enemy)
    {
        if (enemy != null)
        {
            enemyCount++;
        }
    }
}
