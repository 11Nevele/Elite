package game.engine;

import java.awt.*;

/**
 * Represents a polygon face in 3D space, primarily a triangle.
 * Stores vertex positions and colors for both edges and fill.
 */
public class Face
{
    public Vector3[] vertex;
    public Color[] colors;
    public Color fill = new Color(0, 0, 0, 0);
    public double brightness = 0;

    public Face()
    {
        vertex = new Vector3[3];
        colors = new Color[3];
        for (int i = 0; i < 3; ++i)
        {
            vertex[i] = new Vector3();
            colors[i] = new Color(0, 0, 0, 0);
        }
    }

    public Face(Face tri)
    {
        vertex = new Vector3[tri.vertex.length];
        colors = new Color[tri.vertex.length];
        for (int i = 0; i < tri.vertex.length; ++i)
        {
            vertex[i] = new Vector3(tri.vertex[i]);
            colors[i] = new Color(tri.colors[i].getRGB(), true);
        }
        fill = new Color(tri.fill.getRGB(), true);
    }

    public Face(Vector3[] v, Color fill)
    {
        vertex = new Vector3[v.length];
        colors = new Color[v.length];
        for (int i = 0; i < v.length; ++i)
        {
            vertex[i] = new Vector3(v[i]);
            colors[i] = new Color(0, 0, 0, 0);
        }
        this.fill = fill;
    }

    public Face(Vector3[] v, Color line, Color fill)
    {
        vertex = new Vector3[v.length];
        colors = new Color[v.length];
        for (int i = 0; i < v.length; ++i)
        {
            vertex[i] = new Vector3(v[i]);
            colors[i] = new Color(line.getRGB(), true);
        }
        this.fill = new Color(fill.getRGB(), true);
    }

    public Face(Vector3[] v)
    {
        vertex = new Vector3[v.length];
        colors = new Color[v.length];
        for (int i = 0; i < v.length; ++i)
        {
            vertex[i] = new Vector3(v[i]);
            colors[i] = new Color(0, 0, 0, 0);
        }
        fill = Color.white;
    }

    public Face(Vector3 v0, Vector3 v1, Vector3 v2, Color c0, Color c1, Color c2)
    {
        vertex = new Vector3[]{ v0, v1, v2 };
        colors = new Color[]{ c0, c1, c2 };
    }

    public Face(Vector3 v0, Vector3 v1, Vector3 v2, Color c0, Color c1, Color c2, Color fill)
    {
        vertex = new Vector3[]{ v0, v1, v2 };
        colors = new Color[]{ c0, c1, c2 };
        this.fill = fill;
    }

    public Face(Vector3 v0, Vector3 v1, Vector3 v2, Color fill)
    {
        vertex = new Vector3[]{ v0, v1, v2 };
        colors = new Color[]{ new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 0) };
        this.fill = fill;
    }

    /**
     * Copies data from another Face into this Face in-place, avoiding allocations.
     * Vertex data is deep-copied; Color refs are shared (immutable).
     */
    public void copyFrom(Face src)
    {
        if (vertex.length != src.vertex.length)
        {
            vertex = new Vector3[src.vertex.length];
            colors = new Color[src.vertex.length];
            for (int i = 0; i < src.vertex.length; i++)
            {
                vertex[i] = new Vector3(src.vertex[i]);
            }
        }
        else
        {
            for (int i = 0; i < src.vertex.length; i++)
            {
                vertex[i].set(src.vertex[i]);
            }
        }
        System.arraycopy(src.colors, 0, colors, 0, src.colors.length);
        fill = src.fill;
        brightness = 0;
    }
}
