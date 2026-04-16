package game.engine;

import java.awt.Color;

/**
 * Calculates per-face lighting/brightness.
 */
public class LightingCalculator
{
    private static final Color EMISSIVE_COLOR = new Color(255, 0, 0);
    private static final double LIGHT_INTENSITY = 0.7;

    /**
     * Applies lighting to a face based on camera position.
     * Emissive (red) faces are not affected by lighting.
     */
    public void applyLighting(Face tri, Vector3 cameraPos)
    {
        if (tri.fill.equals(EMISSIVE_COLOR)) return;

        double v1x = tri.vertex[1].getX() - tri.vertex[0].getX();
        double v1y = tri.vertex[1].getY() - tri.vertex[0].getY();
        double v1z = tri.vertex[1].getZ() - tri.vertex[0].getZ();
        double v2x = tri.vertex[2].getX() - tri.vertex[0].getX();
        double v2y = tri.vertex[2].getY() - tri.vertex[0].getY();
        double v2z = tri.vertex[2].getZ() - tri.vertex[0].getZ();

        double nx = v1y * v2z - v1z * v2y;
        double ny = v1z * v2x - v1x * v2z;
        double nz = v1x * v2y - v1y * v2x;

        double crx = cameraPos.getX() - tri.vertex[0].getX();
        double cry = cameraPos.getY() - tri.vertex[0].getY();
        double crz = cameraPos.getZ() - tri.vertex[0].getZ();

        double normalMagSq = nx * nx + ny * ny + nz * nz;
        double camRayMagSq = crx * crx + cry * cry + crz * crz;
        if (normalMagSq == 0 || camRayMagSq == 0) return;

        double dot = nx * crx + ny * cry + nz * crz;
        double cosAngle = dot / Math.sqrt(normalMagSq * camRayMagSq);

        tri.brightness = Math.max(-1, Math.min(1, cosAngle)) * LIGHT_INTENSITY;
    }
}
