package game.engine;

import game.Input;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Main rendering system that handles 3D projection and drawing.
 * Maintains a list of triangles to be rendered each frame.
 */
public class Renderer 
{
    /** Singleton instance */
    public static Renderer renderer;
    
    /** Graphics context to render to */
    private Graphics g;
    
    /** List of triangles to be rendered in the current frame */
    ArrayList<Face> triangleList;
    
    /** Current camera position */
    private Vector3 curCameraPos;
    
    /** Current camera rotation */
    private Quaternion curCameraRot;
    
    /** Screen height */
    private double Height = 300;
    
    /** Screen width */
    private double Width = 400;
    
    /** Base scale factor for projection */
    private double baseScale = 700;
    
    /** Speed scale factor for projection */
    private final double speedScale = 200;
    
    /** Speed percentage for zoom effect */
    public double spdPercentage = 0;
    
    /**
     * Constructor for the renderer
     * @param g Graphics context to render to
     * @param width Screen width
     * @param height Screen height
     */
    public Renderer(Graphics g, double width, double height)
    {
        triangleList = new ArrayList<>();
        this.g = g;
        curCameraPos = new Vector3();
        curCameraRot = new Quaternion();
        Height = height;
        Width = width;
    }
    
    /**
     * Updates the camera position and orientation
     * @param pos New camera position
     * @param rot New camera rotation
     */
    public void UpdateCamera(Vector3 pos, Quaternion rot)
    {
        curCameraPos = pos;
        curCameraRot = rot;
    }
    
    /**
     * Gets the current projection scale
     * @return Current scale factor
     */
    public double getScale()
    {
        return baseScale;
    }
    
    /**
     * Adds a renderable object to the render queue
     * @param obj Renderable object to draw
     */
    public void Render(Renderable obj)
    {
        for (Face tri : obj.model) 
        {
            Face nTri = new Face(tri);

            for(int i = 0;  i < nTri.vertex.length; ++i)
            {
                nTri.vertex[i] = nTri.vertex[i].multi(obj.scale);
                nTri.vertex[i] = obj.rotation.rotate(nTri.vertex[i]);
                nTri.vertex[i] = nTri.vertex[i].plus(obj.position);
            }
            triangleList.add(nTri);
        }
    }
    
    /**
     * Projects a 3D point onto the 2D screen
     * @param point 3D point to project
     * @param scale Projection scale factor
     * @return 2D screen coordinates
     */
    private Vector2 project(Vector3 point, double scale)
    {
        Vector2 projected = new Vector2();
        if (point.z <= 0) {
            // Handle the point being behind the camera
            // For simplicity, we'll set the projected coordinates to a large value
            // You can adjust this logic based on your specific requirements
            projected.x = point.x* 100000;
            projected.y = point.y *100000;
        } else {
            projected.x = (double)((point.x * scale) / point.z + Width / 2);
            projected.y = (double)((point.y * scale) / point.z + Height / 2);
        }
        return projected;
    }

    /**
     * Draws a single triangle to the screen
     * @param tri Triangle to draw
     */
    private void drawTriangle(Face tri)
    {
        int[] x = new int[tri.vertex.length];
        int[] y = new int[tri.vertex.length];
        for(int i = 0; i < tri.vertex.length; i++)
        {
            Vector2 v = project(tri.vertex[i], baseScale - spdPercentage * speedScale);
            x[i] = (int)Math.round(v.x);
            y[i] = (int)Math.round(v.y);
        }
        
        g.setColor(tri.fill);
        g.fillPolygon(x, y, tri.vertex.length);
        for(int i = 0; i < tri.vertex.length - 1; i++)
        {
            g.setColor(tri.colors[i]);
            g.drawLine(x[i], y[i], x[i+1], y[i+1]);
        }
        g.setColor(tri.colors[tri.vertex.length - 1]);
        g.drawLine(x[tri.vertex.length - 1], y[tri.vertex.length - 1], x[0], y[0]);
    }
    
    /**
     * Performs main drawing operation for the frame
     * @param g Graphics context to render to
     */
    public void Draw(Graphics g)
    {       
        this.g = g;
        if(Input.input.keys[KeyEvent.VK_F])
            baseScale = 2500;
        else
            baseScale = 700;
            
        // Filter visible triangles and sort by depth
        ArrayList<Face> visibleTriangles = new ArrayList<>(); 
        for(Face tri : triangleList)
        {
            // Calculate face normal for backface culling
            Vector3 v1 = tri.vertex[1].minus(tri.vertex[0]);
            Vector3 v2 = tri.vertex[2].minus(tri.vertex[0]);
            Vector3 normal = v1.cross(v2);
            Vector3 camRay = new Vector3().minus(tri.vertex[0]);

            // Calculate lighting angle
            double rad = Math.acos(normal.dot(camRay) / (normal.magnitude() * camRay.magnitude()));
            if(!tri.fill.equals(new Color(255,0,0)))
            {
                tri.fill = EngineUtil.adjustBrightness(tri.fill, Math.cos(rad) * 0.7);
            }
            
            // Transform vertices to camera space
            for(int i = 0 ; i < tri.vertex.length; i++)
            {
                tri.vertex[i] = tri.vertex[i].plus(new Vector3(-curCameraPos.x, -curCameraPos.y, -curCameraPos.z));
                tri.vertex[i] = curCameraRot.Conjugate().rotate(tri.vertex[i]);
            }
            
            // Discard triangles behind camera
            if(tri.vertex[0].z <= 0 && tri.vertex[1].z <= 0 && tri.vertex[2].z <= 0)
            {
                continue;
            }
            
            // Backface culling
            v1 = tri.vertex[1].minus(tri.vertex[0]);
            v2 = tri.vertex[2].minus(tri.vertex[0]);
            normal = v1.cross(v2);
            camRay = tri.vertex[0];
            if(normal.dot(camRay) >= 0)
            {
                continue;
            }
            visibleTriangles.add(tri);
        }
        
        // Sort triangles by depth (furthest first)
        visibleTriangles.sort((a, b) -> 
        {
            double mxa = -100000, mxb = -100000;
            for(int i = 0; i < a.vertex.length; i++)
            {
                mxa = Math.max(mxa, a.vertex[i].z);
            }
            for(int i = 0; i < b.vertex.length; ++i)
            {
                mxb = Math.max(mxb, b.vertex[i].z);
            }
            return Double.compare(mxb, mxa);
        });
        
        // Draw all visible triangles
        for(Face tri : visibleTriangles)
        {
            drawTriangle(tri);
        }
        triangleList.clear();
    }
}
