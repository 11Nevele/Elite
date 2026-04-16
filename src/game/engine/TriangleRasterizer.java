package game.engine;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 * Handles depth sorting and polygon drawing.
 */
public class TriangleRasterizer
{
    private final ProjectionTransform projection;

    public TriangleRasterizer(ProjectionTransform projection)
    {
        this.projection = projection;
    }

    /**
     * Sorts triangles by depth (furthest first) using painter's algorithm.
     */
    public void sortByDepth(ArrayList<Face> triangles)
    {
        triangles.sort((a, b) ->
        {
            double mxa = -100000, mxb = -100000;
            for (Vector3 v : a.vertex) mxa = Math.max(mxa, v.getZ());
            for (Vector3 v : b.vertex) mxb = Math.max(mxb, v.getZ());
            return Double.compare(mxb, mxa);
        });
    }

    private static Color applyBrightness(Color color, double factor)
    {
        factor = Math.max(-1.0, Math.min(1.0, factor));
        int red = (int) Math.min(255, Math.max(0, color.getRed() + (color.getRed() * factor)));
        int green = (int) Math.min(255, Math.max(0, color.getGreen() + (color.getGreen() * factor)));
        int blue = (int) Math.min(255, Math.max(0, color.getBlue() + (color.getBlue() * factor)));
        return new Color(red, green, blue);
    }

    /**
     * Draws a single triangle to the screen.
     */
    public void drawTriangle(Graphics g, Face tri, double scale)
    {
        int[] x = new int[tri.vertex.length];
        int[] y = new int[tri.vertex.length];
        for (int i = 0; i < tri.vertex.length; i++)
        {
            Vector2 v = projection.project(tri.vertex[i], scale);
            x[i] = (int) Math.round(v.getX());
            y[i] = (int) Math.round(v.getY());
        }

        Color drawFill = (tri.brightness != 0) ? applyBrightness(tri.fill, tri.brightness) : tri.fill;
        g.setColor(drawFill);
        g.fillPolygon(x, y, tri.vertex.length);

        for (int i = 0; i < tri.vertex.length - 1; i++)
        {
            g.setColor(tri.colors[i]);
            g.drawLine(x[i], y[i], x[i + 1], y[i + 1]);
        }
        g.setColor(tri.colors[tri.vertex.length - 1]);
        g.drawLine(x[tri.vertex.length - 1], y[tri.vertex.length - 1], x[0], y[0]);
    }
}
