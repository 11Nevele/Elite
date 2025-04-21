package game.engine;
import java.awt.*;

/**
 * Represents a polygon face in 3D space, primarily a triangle.
 * Stores vertex positions and colors for both edges and fill.
 */
public class Face
{
    /** Array of 3D vertex positions */
    public Vector3 []vertex;
    
    /** Array of colors for each vertex/edge */
    public Color []colors;
    
    /** Fill color for the face */
    public Color fill = new Color(0,0,0,0);
    
    /**
     * Default constructor - creates an empty triangle with transparent colors
     */
    public Face()
    {
        vertex = new Vector3[3];
        colors = new Color[3];
        for(int i = 0; i < 3; ++i)
        {
            vertex[i] = new Vector3();
            colors[i] = new Color(0,0,0,0);
        }
        
    }
    
    /**
     * Copy constructor - creates a deep copy of another Face
     * @param tri The Face to copy
     */
    public Face(Face tri)
    {
        vertex = new Vector3[tri.vertex.length];
        colors = new Color[tri.vertex.length];
        for(int i = 0; i < tri.vertex.length; ++i)
        {
            vertex[i] = new Vector3(tri.vertex[i]);
            colors[i] = new Color(tri.colors[i].getRGB(), true);
        }
        //copy fill color
        fill = new Color(tri.fill.getRGB(), true);
        return;
    }
    
    /**
     * Constructor with vertices and a fill color
     * @param v Array of vertices
     * @param fill Fill color for the face
     */
    public Face(Vector3[] v, Color fill)
    {
        vertex = new Vector3[v.length];
        colors = new Color[v.length];
        for(int i = 0; i < v.length; ++i)
        {
            vertex[i] = new Vector3(v[i]);
            colors[i] = new Color(0,0,0,0);
        }
        this.fill = fill;
    }
    
    /**
     * Constructor with vertices, line colors, and fill color
     * @param v Array of vertices
     * @param line Color for the edges
     * @param fill Fill color for the face
     */
    public Face(Vector3[] v, Color line, Color fill)
    {
        vertex = new Vector3[v.length];
        colors = new Color[v.length];
        for(int i = 0; i < v.length; ++i)
        {
            vertex[i] = new Vector3(v[i]);
            colors[i] = new Color(line.getRGB(), true);
        }
        this.fill = new Color(fill.getRGB(), true);
    }
    
    /**
     * Constructor with vertices only, defaults to white fill
     * @param v Array of vertices
     */
    public Face(Vector3[]v)
    {
        vertex = new Vector3[v.length];
        colors = new Color[v.length];
        for(int i = 0; i < v.length; ++i)
        {
            vertex[i] = new Vector3(v[i]);
            colors[i] = new Color(0,0,0,0);
        }
        fill = Color.white;
    }
    
    /**
     * Constructor for a triangle with distinct vertex colors
     * @param v0 First vertex
     * @param v1 Second vertex
     * @param v2 Third vertex
     * @param c0 Color for first vertex
     * @param c1 Color for second vertex
     * @param c2 Color for third vertex
     */
    public Face(Vector3 v0, Vector3 v1, Vector3 v2, Color c0, Color c1, Color c2)
    {
        vertex = new Vector3[3];
        colors = new Color[3];

        vertex[0] = v0;
        vertex[1] = v1;
        vertex[2] = v2;
        colors[0] = c0;
        colors[1] = c1;
        colors[2] = c2;   
    }
    
    /**
     * Constructor for a triangle with distinct vertex colors and fill
     * @param v0 First vertex
     * @param v1 Second vertex
     * @param v2 Third vertex
     * @param c0 Color for first vertex
     * @param c1 Color for second vertex
     * @param c2 Color for third vertex
     * @param fill Fill color for the face
     */
    public Face(Vector3 v0, Vector3 v1, Vector3 v2, Color c0, Color c1, Color c2, Color fill)
    {
        vertex = new Vector3[3];
        colors = new Color[3];

        vertex[0] = v0;
        vertex[1] = v1;
        vertex[2] = v2;
        colors[0] = c0;
        colors[1] = c1;
        colors[2] = c2;   
        this.fill = fill;
    }
    
    /**
     * Constructor for a triangle with vertices and fill color
     * @param v0 First vertex
     * @param v1 Second vertex
     * @param v2 Third vertex
     * @param fill Fill color for the face
     */
    public Face(Vector3 v0, Vector3 v1, Vector3 v2, Color fill)
    {
        vertex = new Vector3[3];
        colors = new Color[3];
        vertex[0] = v0;
        vertex[1] = v1;
        vertex[2] = v2;
        this.fill = fill;
        colors[0] = new Color(0,0,0, 0);
        colors[1] = new Color(0,0,0,0);
        colors[2] = new Color(0,0,0, 0);
    }
}
