
package game.engine;

import game.Input;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class Renderer 
{
    public static Renderer renderer;
    private Graphics g;
    ArrayList<Face> triangleList;
    private Vector3 curCameraPos;
    private Quaternion curCameraRot;
    private double Height = 300;
    private double Width = 400;
    
    private double baseScale = 700;
    private final double speedScale = 200;
    public double spdPercentage = 0;
    


    public Renderer(Graphics g, double width, double height)
    {

        triangleList = new ArrayList<>();
        this.g = g;
        curCameraPos = new Vector3();
        curCameraRot = new Quaternion();
        Height = height;
        Width = width;
    }
    public void UpdateCamera(Vector3 pos, Quaternion rot)
    {
        curCameraPos = pos;
        curCameraRot = rot;
    }
    public double getScale()
    {
        return baseScale;
    }
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
    //given the position of camera and rotation of camera, height and width of screen, project 3D point on the screen
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

    private void drawTriangle(Face tri)
    {
        
        int[] x = new int[tri.vertex.length];
        int[] y = new int[tri.vertex.length];
        for(int i = 0; i < tri.vertex.length; i++)
        {
            Vector2 v = project(tri.vertex[i], baseScale - spdPercentage * speedScale);
            x[i] = (int)v.x;
            y[i] = (int)v.y;
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
    public void Draw(Graphics g)
    {       
        this.g = g;
        if(Input.input.keys[KeyEvent.VK_F])
            baseScale = 2500;
        else
            baseScale = 700;
        ArrayList<Face> visibleTriangles = new ArrayList<>(); 
        for(Face tri : triangleList)
        {
            for(int i = 0 ; i < tri.vertex.length; i++)
            {
                tri.vertex[i] = tri.vertex[i].plus(new Vector3(-curCameraPos.x, -curCameraPos.y, -curCameraPos.z));
                tri.vertex[i] = curCameraRot.Conjugate().rotate(tri.vertex[i]);
            }
            if(tri.vertex[0].z <= 0 && tri.vertex[1].z <= 0 && tri.vertex[2].z <= 0)
            {
                continue;
            }
            //check if the triangle is facing the camera
            Vector3 v1 = tri.vertex[1].minus(tri.vertex[0]);
            Vector3 v2 = tri.vertex[2].minus(tri.vertex[0]);
            Vector3 normal = v1.cross(v2);
            Vector3 camRay = tri.vertex[0];
            if(normal.dot(camRay) >= 0)
            {
                continue;
            }
            visibleTriangles.add(tri);
        }
        //sort z axis in descending order
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
        for(Face tri : visibleTriangles)
        {
            drawTriangle(tri);
        }
        triangleList.clear();
    }
}
