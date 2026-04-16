package game;

import game.engine.Face;
import game.engine.Vector3;
import java.awt.Color;

/**
 * Generates procedural 3D meshes: spheres, cubes, pyramids, stars, explosions.
 */
public class ProceduralMeshes
{
    /**
     * Generates a UV sphere mesh.
     * @param radius Sphere radius
     * @param segments Number of longitudinal segments
     * @param rings Number of latitudinal rings
     * @param color Fill color
     * @param lineColor Edge color
     * @return Array of Face objects forming the sphere
     */
    public static Face[] getSphere(double radius, int segments, int rings, Color color, Color lineColor)
    {
        Face[] faces = new Face[segments * rings * 2];
        int faceIndex = 0;

        for (int i = 0; i < rings; i++)
        {
            double theta1 = (Math.PI * i) / rings;
            double theta2 = (Math.PI * (i + 1)) / rings;

            for (int j = 0; j < segments; j++)
            {
                double phi1 = (2 * Math.PI * j) / segments;
                double phi2 = (2 * Math.PI * (j + 1)) / segments;

                Vector3 v0 = sphereVertex(radius, theta1, phi1);
                Vector3 v1 = sphereVertex(radius, theta1, phi2);
                Vector3 v2 = sphereVertex(radius, theta2, phi2);
                Vector3 v3 = sphereVertex(radius, theta2, phi1);

                faces[faceIndex++] = new Face(
                    new Vector3[]{v0, v1, v2}, lineColor, color);
                faces[faceIndex++] = new Face(
                    new Vector3[]{v0, v2, v3}, lineColor, color);
            }
        }

        return faces;
    }

    /**
     * Generates a simple cube mesh.
     */
    public static Face[] getCube(double size, Color color, Color lineColor)
    {
        double h = size / 2;
        Vector3[] v = {
            new Vector3(-h, -h, -h), new Vector3(h, -h, -h),
            new Vector3(h, h, -h), new Vector3(-h, h, -h),
            new Vector3(-h, -h, h), new Vector3(h, -h, h),
            new Vector3(h, h, h), new Vector3(-h, h, h)
        };

        return new Face[] {
            new Face(new Vector3[]{v[0], v[1], v[2]}, lineColor, color),
            new Face(new Vector3[]{v[0], v[2], v[3]}, lineColor, color),
            new Face(new Vector3[]{v[4], v[6], v[5]}, lineColor, color),
            new Face(new Vector3[]{v[4], v[7], v[6]}, lineColor, color),
            new Face(new Vector3[]{v[0], v[4], v[5]}, lineColor, color),
            new Face(new Vector3[]{v[0], v[5], v[1]}, lineColor, color),
            new Face(new Vector3[]{v[2], v[6], v[7]}, lineColor, color),
            new Face(new Vector3[]{v[2], v[7], v[3]}, lineColor, color),
            new Face(new Vector3[]{v[0], v[7], v[4]}, lineColor, color),
            new Face(new Vector3[]{v[0], v[3], v[7]}, lineColor, color),
            new Face(new Vector3[]{v[1], v[5], v[6]}, lineColor, color),
            new Face(new Vector3[]{v[1], v[6], v[2]}, lineColor, color),
        };
    }

    /**
     * Generates a star/explosion particle shape.
     */
    public static Face[] getExplosionParticle(double size, Color color)
    {
        double h = size / 2;
        Vector3[] v = {
            new Vector3(0, h, 0), new Vector3(h, 0, 0),
            new Vector3(0, 0, h), new Vector3(-h, 0, 0),
            new Vector3(0, 0, -h), new Vector3(0, -h, 0)
        };
        Color transparent = GameColors.TRANSPARENT;

        return new Face[] {
            new Face(new Vector3[]{v[0], v[1], v[2]}, transparent, color),
            new Face(new Vector3[]{v[0], v[2], v[3]}, transparent, color),
            new Face(new Vector3[]{v[0], v[3], v[4]}, transparent, color),
            new Face(new Vector3[]{v[0], v[4], v[1]}, transparent, color),
            new Face(new Vector3[]{v[5], v[2], v[1]}, transparent, color),
            new Face(new Vector3[]{v[5], v[3], v[2]}, transparent, color),
            new Face(new Vector3[]{v[5], v[4], v[3]}, transparent, color),
            new Face(new Vector3[]{v[5], v[1], v[4]}, transparent, color),
        };
    }

    private static Vector3 sphereVertex(double radius, double theta, double phi)
    {
        return new Vector3(
            radius * Math.sin(theta) * Math.cos(phi),
            radius * Math.sin(theta) * Math.sin(phi),
            radius * Math.cos(theta)
        );
    }
}
