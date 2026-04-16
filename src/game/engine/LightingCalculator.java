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
        Vector3 v1 = tri.vertex[1].minus(tri.vertex[0]);
        Vector3 v2 = tri.vertex[2].minus(tri.vertex[0]);
        Vector3 normal = v1.cross(v2);
        Vector3 camRay = cameraPos.minus(tri.vertex[0]);

        double normalMag = normal.magnitude();
        double camRayMag = camRay.magnitude();
        if (normalMag == 0 || camRayMag == 0) return;

        double cosAngle = normal.dot(camRay) / (normalMag * camRayMag);
        cosAngle = Math.max(-1, Math.min(1, cosAngle));
        double rad = Math.acos(cosAngle);

        if (!tri.fill.equals(EMISSIVE_COLOR))
        {
            tri.fill = EngineUtil.adjustBrightness(tri.fill, Math.cos(rad) * LIGHT_INTENSITY);
        }
    }
}
