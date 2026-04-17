package game.engine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Z-buffer scanline triangle rasterizer. Renders directly to a pixel buffer
 * with per-pixel depth testing, eliminating the need for depth sorting.
 */
public class TriangleRasterizer
{
    private final ProjectionTransform projection;

    private int[] pixelBuffer;
    private float[] depthBuffer;
    private int bufWidth;
    private int bufHeight;

    // Reusable projection output arrays to avoid allocations
    private final double[] proj0 = new double[2];
    private final double[] proj1 = new double[2];
    private final double[] proj2 = new double[2];

    public TriangleRasterizer(ProjectionTransform projection)
    {
        this.projection = projection;
    }

    /**
     * Binds a BufferedImage for direct pixel writing and allocates the depth buffer.
     */
    public void bind(BufferedImage image)
    {
        this.bufWidth = image.getWidth();
        this.bufHeight = image.getHeight();
        this.pixelBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        this.depthBuffer = new float[bufWidth * bufHeight];
    }

    /**
     * Clears the depth buffer at the start of each frame.
     */
    public void clearDepthBuffer()
    {
        Arrays.fill(depthBuffer, 0);
    }

    /**
     * Legacy depth sort — kept as no-op since z-buffer handles occlusion.
     */
    public void sortByDepth(ArrayList<Face> triangles)
    {
        // No-op: z-buffer replaces painter's algorithm
    }

    private static int applyBrightnessRGB(Color color, double factor)
    {
        factor = Math.max(-1.0, Math.min(1.0, factor));
        int r = (int) Math.min(255, Math.max(0, color.getRed() + (color.getRed() * factor)));
        int g = (int) Math.min(255, Math.max(0, color.getGreen() + (color.getGreen() * factor)));
        int b = (int) Math.min(255, Math.max(0, color.getBlue() + (color.getBlue() * factor)));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * Rasterizes a triangle with z-buffer depth testing using scanline filling.
     * Writes directly to the pixel buffer.
     */
    public void drawTriangle(Graphics g, Face tri, double scale)
    {
        // Project vertices to screen space
        projection.projectInPlace(tri.vertex[0], scale, proj0);
        projection.projectInPlace(tri.vertex[1], scale, proj1);
        projection.projectInPlace(tri.vertex[2], scale, proj2);

        double x0 = proj0[0], y0 = proj0[1], z0 = tri.vertex[0].getZ();
        double x1 = proj1[0], y1 = proj1[1], z1 = tri.vertex[1].getZ();
        double x2 = proj2[0], y2 = proj2[1], z2 = tri.vertex[2].getZ();

        // Use inverse Z for depth interpolation (perspective correct)
        double w0 = (z0 > 0) ? 1.0 / z0 : 0;
        double w1 = (z1 > 0) ? 1.0 / z1 : 0;
        double w2 = (z2 > 0) ? 1.0 / z2 : 0;

        // Compute bounding box
        int minX = Math.max(0, (int) Math.floor(Math.min(x0, Math.min(x1, x2))));
        int maxX = Math.min(bufWidth - 1, (int) Math.ceil(Math.max(x0, Math.max(x1, x2))));
        int minY = Math.max(0, (int) Math.floor(Math.min(y0, Math.min(y1, y2))));
        int maxY = Math.min(bufHeight - 1, (int) Math.ceil(Math.max(y0, Math.max(y1, y2))));

        // Trivial reject if triangle is entirely off-screen
        if (minX > maxX || minY > maxY) return;

        // Compute color once for flat shading
        int rgb;
        if (tri.brightness != 0)
        {
            rgb = applyBrightnessRGB(tri.fill, tri.brightness);
        }
        else
        {
            rgb = 0xFF000000 | (tri.fill.getRed() << 16) | (tri.fill.getGreen() << 8) | tri.fill.getBlue();
        }

        // Edge function setup for barycentric coordinates
        // f01(x,y) = (y0-y1)*x + (x1-x0)*y + (x0*y1 - x1*y0)
        double dy01 = y0 - y1, dx10 = x1 - x0;
        double dy12 = y1 - y2, dx21 = x2 - x1;
        double dy20 = y2 - y0, dx02 = x0 - x2;

        // Total area (2x) via cross product
        double area = dy01 * (x2 - x0) + dx10 * (y2 - y0);
        if (Math.abs(area) < 0.001) return; // Degenerate triangle
        double invArea = 1.0 / area;

        // Evaluate edge functions at (minX + 0.5, minY + 0.5)
        double px = minX + 0.5;
        double py = minY + 0.5;
        double row_w0 = (dy12 * (px - x1) + dx21 * (py - y1)) * invArea;
        double row_w1 = (dy20 * (px - x2) + dx02 * (py - y2)) * invArea;
        double row_w2 = 1.0 - row_w0 - row_w1;

        double step_w0_x = dy12 * invArea;
        double step_w1_x = dy20 * invArea;
        double step_w0_y = dx21 * invArea;
        double step_w1_y = dx02 * invArea;

        for (int y = minY; y <= maxY; y++)
        {
            double bary0 = row_w0;
            double bary1 = row_w1;
            double bary2 = row_w2;
            int yOffset = y * bufWidth;

            for (int x = minX; x <= maxX; x++)
            {
                if (bary0 >= 0 && bary1 >= 0 && bary2 >= 0)
                {
                    // Interpolate inverse depth
                    float depth = (float) (bary0 * w0 + bary1 * w1 + bary2 * w2);
                    int idx = yOffset + x;

                    // Depth test: larger 1/z means closer
                    if (depth > depthBuffer[idx])
                    {
                        depthBuffer[idx] = depth;
                        pixelBuffer[idx] = rgb;
                    }
                }

                bary0 += step_w0_x;
                bary1 += step_w1_x;
                bary2 = 1.0 - bary0 - bary1;
            }

            row_w0 += step_w0_y;
            row_w1 += step_w1_y;
            row_w2 = 1.0 - row_w0 - row_w1;
        }
    }

    /**
     * Draws a filled oval directly to the pixel buffer (for stars etc.)
     */
    public void drawPoint(int sx, int sy, int size, Color color)
    {
        int rgb = 0xFF000000 | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
        int halfSize = size / 2;
        int r2 = halfSize * halfSize;
        int startX = Math.max(0, sx - halfSize);
        int endX = Math.min(bufWidth - 1, sx + halfSize);
        int startY = Math.max(0, sy - halfSize);
        int endY = Math.min(bufHeight - 1, sy + halfSize);

        for (int py = startY; py <= endY; py++)
        {
            int dy = py - sy;
            int yOff = py * bufWidth;
            for (int px = startX; px <= endX; px++)
            {
                int ddx = px - sx;
                if (ddx * ddx + dy * dy <= r2)
                {
                    pixelBuffer[yOff + px] = rgb;
                }
            }
        }
    }
}
