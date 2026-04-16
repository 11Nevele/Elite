package game;

import java.awt.*;

/**
 * Performance profiler that tracks timing for each game subsystem.
 * Toggle with F3 key. Displays an overlay with per-frame timing breakdowns,
 * object counts, and triangle counts.
 */
public class Profiler
{
    public static Profiler instance = new Profiler();

    private boolean visible = false;
    private boolean toggleHeld = false;

    // Timing accumulators (nanoseconds for current frame)
    private long updateTime;
    private long collisionTime;
    private long lightingTime;
    private long cameraTransformTime;
    private long depthSortTime;
    private long rasterizeTime;
    private long uiTime;
    private long totalFrameTime;

    // Smoothed values (exponential moving average, in ms)
    private double avgUpdate;
    private double avgCollision;
    private double avgLighting;
    private double avgCameraTransform;
    private double avgDepthSort;
    private double avgRasterize;
    private double avgUI;
    private double avgTotal;
    private double avgFPS;

    private static final double SMOOTH = 0.05;

    // Counters
    private int gameObjectCount;
    private int collidableCount;
    private int triangleCount;
    private int visibleTriangleCount;

    // Frame counter
    private long frameCount;
    private long lastFPSTime = System.nanoTime();
    private int fpsFrames;
    private double measuredFPS;

    public void toggleIfPressed(boolean f3Down)
    {
        if (f3Down && !toggleHeld)
        {
            visible = !visible;
        }
        toggleHeld = f3Down;
    }

    public boolean isVisible()
    {
        return visible;
    }

    // --- Timing setters (pass nanoseconds) ---

    public void setUpdateTime(long nanos)       { updateTime = nanos; }
    public void setCollisionTime(long nanos)     { collisionTime = nanos; }
    public void setLightingTime(long nanos)      { lightingTime = nanos; }
    public void setCameraTransformTime(long nanos) { cameraTransformTime = nanos; }
    public void setDepthSortTime(long nanos)     { depthSortTime = nanos; }
    public void setRasterizeTime(long nanos)     { rasterizeTime = nanos; }
    public void setUITime(long nanos)            { uiTime = nanos; }
    public void setTotalFrameTime(long nanos)    { totalFrameTime = nanos; }

    // --- Count setters ---

    public void setGameObjectCount(int count)      { gameObjectCount = count; }
    public void setCollidableCount(int count)       { collidableCount = count; }
    public void setTriangleCount(int count)         { triangleCount = count; }
    public void setVisibleTriangleCount(int count)  { visibleTriangleCount = count; }

    /**
     * Call once per frame after all timings are set. Smooths values.
     */
    public void endFrame()
    {
        frameCount++;
        fpsFrames++;

        long now = System.nanoTime();
        double elapsed = (now - lastFPSTime) / 1_000_000_000.0;
        if (elapsed >= 0.5)
        {
            measuredFPS = fpsFrames / elapsed;
            fpsFrames = 0;
            lastFPSTime = now;
        }

        avgUpdate          = lerp(avgUpdate,          updateTime / 1_000_000.0, SMOOTH);
        avgCollision       = lerp(avgCollision,       collisionTime / 1_000_000.0, SMOOTH);
        avgLighting        = lerp(avgLighting,        lightingTime / 1_000_000.0, SMOOTH);
        avgCameraTransform = lerp(avgCameraTransform, cameraTransformTime / 1_000_000.0, SMOOTH);
        avgDepthSort       = lerp(avgDepthSort,       depthSortTime / 1_000_000.0, SMOOTH);
        avgRasterize       = lerp(avgRasterize,       rasterizeTime / 1_000_000.0, SMOOTH);
        avgUI              = lerp(avgUI,              uiTime / 1_000_000.0, SMOOTH);
        avgTotal           = lerp(avgTotal,           totalFrameTime / 1_000_000.0, SMOOTH);
        avgFPS             = lerp(avgFPS,             measuredFPS, SMOOTH);
    }

    /**
     * Draws the profiler overlay.
     */
    public void draw(Graphics g, int screenWidth)
    {
        if (!visible) return;

        int panelW = 310;
        int panelH = 340;
        int x = screenWidth - panelW - 10;
        int y = 10;
        int lineH = 16;

        // Background
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(x, y, panelW, panelH);
        g.setColor(new Color(0, 255, 0, 100));
        g.drawRect(x, y, panelW, panelH);

        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.setColor(Color.GREEN);

        int tx = x + 8;
        int ty = y + 16;

        g.drawString("=== PERFORMANCE PROFILER (F3) ===", tx, ty); ty += lineH + 4;

        g.setColor(Color.WHITE);
        g.drawString(String.format("FPS: %.1f  Frame: %.2f ms", avgFPS, avgTotal), tx, ty);
        ty += lineH;

        double budget = 16.67; // ms at 60 FPS
        double usage = (avgTotal / budget) * 100;
        g.setColor(usage > 90 ? Color.RED : usage > 60 ? Color.YELLOW : Color.GREEN);
        g.drawString(String.format("Budget: %.1f%%  (%.2f / %.2f ms)", usage, avgTotal, budget), tx, ty);
        ty += lineH + 6;

        // Timing breakdown
        g.setColor(new Color(180, 180, 180));
        g.drawString("--- Timing Breakdown ---", tx, ty); ty += lineH;

        drawBar(g, tx, ty, "Update      ", avgUpdate, avgTotal, panelW - 16);  ty += lineH;
        drawBar(g, tx, ty, "Collision   ", avgCollision, avgTotal, panelW - 16); ty += lineH;
        drawBar(g, tx, ty, "Lighting    ", avgLighting, avgTotal, panelW - 16); ty += lineH;
        drawBar(g, tx, ty, "CamTransform", avgCameraTransform, avgTotal, panelW - 16); ty += lineH;
        drawBar(g, tx, ty, "DepthSort   ", avgDepthSort, avgTotal, panelW - 16); ty += lineH;
        drawBar(g, tx, ty, "Rasterize   ", avgRasterize, avgTotal, panelW - 16); ty += lineH;
        drawBar(g, tx, ty, "UI          ", avgUI, avgTotal, panelW - 16); ty += lineH + 6;

        // Counts
        g.setColor(new Color(180, 180, 180));
        g.drawString("--- Object Counts ---", tx, ty); ty += lineH;
        g.setColor(Color.WHITE);
        g.drawString(String.format("GameObjects:  %d", gameObjectCount), tx, ty); ty += lineH;
        g.drawString(String.format("Collidables:  %d", collidableCount), tx, ty); ty += lineH;
        g.drawString(String.format("Triangles:    %d total / %d visible",
            triangleCount, visibleTriangleCount), tx, ty); ty += lineH;
        g.drawString(String.format("Culled:       %d (%.0f%%)",
            triangleCount - visibleTriangleCount,
            triangleCount > 0 ? (triangleCount - visibleTriangleCount) * 100.0 / triangleCount : 0),
            tx, ty);
    }

    private void drawBar(Graphics g, int x, int y, String label, double ms, double total, int maxW)
    {
        double pct = total > 0 ? (ms / total) * 100 : 0;
        String text = String.format("%s %5.2f ms  %4.1f%%", label, ms, pct);
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);

        // Mini bar
        int barX = x + 240;
        int barW = maxW - 240;
        int barH = 10;
        int barY = y - 9;
        g.setColor(new Color(40, 40, 40));
        g.fillRect(barX, barY, barW, barH);
        int fillW = (int)(barW * Math.min(1, ms / Math.max(total, 0.01)));
        g.setColor(pct > 50 ? Color.RED : pct > 25 ? Color.YELLOW : Color.GREEN);
        g.fillRect(barX, barY, fillW, barH);
    }

    private double lerp(double current, double target, double factor)
    {
        return current + (target - current) * factor;
    }
}
