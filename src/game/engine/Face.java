package game.engine;
import java.awt.*;
public class Face
{

    public Vector3 []vertex;
    public Color []colors;
    public Color fill = new Color(0,0,0,0);
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
    //copy constructor
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
