package game.engine;

import game.Input;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Main rendering system that handles 3D projection and drawing.
 * Coordinates ProjectionTransform, LightingCalculator, and TriangleRasterizer.
 */
public class Renderer 
{
    public static Renderer renderer;

    private static final double BASE_SCALE = 700;
    private static final double ZOOM_SCALE = 2500;
    private static final double SPEED_SCALE = 200;

    private Graphics g;
    private ArrayList<Face> triangleList;
    private Vector3 curCameraPos;
    private Quaternion curCameraRot;
    private double baseScale = BASE_SCALE;
    public double spdPercentage = 0;

    private final ProjectionTransform projection;
    private final LightingCalculator lighting;
    private final TriangleRasterizer rasterizer;

    public Renderer(Graphics g, double width, double height)
    {
        triangleList = new ArrayList<>();
        this.g = g;
        curCameraPos = new Vector3();
        curCameraRot = new Quaternion();
        projection = new ProjectionTransform(width, height);
        lighting = new LightingCalculator();
        rasterizer = new TriangleRasterizer(projection);
    }

    public void updateCamera(Vector3 pos, Quaternion rot)
    {
        curCameraPos = pos;
        curCameraRot = rot;
    }

    public double getScale()
    {
        return baseScale;
    }

    public void render(Renderable obj)
    {
        for (Face tri : obj.model)
        {
            Face nTri = new Face(tri);
            for (int i = 0; i < nTri.vertex.length; ++i)
            {
                nTri.vertex[i] = nTri.vertex[i].multiply(obj.scale);
                nTri.vertex[i] = obj.rotation.rotate(nTri.vertex[i]);
                nTri.vertex[i] = nTri.vertex[i].plus(obj.position);
            }
            triangleList.add(nTri);
        }
    }

    public void draw(Graphics g)
    {
        this.g = g;
        if (Input.input.keys[KeyEvent.VK_F])
            baseScale = ZOOM_SCALE;
        else
            baseScale = BASE_SCALE;

        int totalTris = triangleList.size();

        // Lighting pass
        long tLight0 = System.nanoTime();
        for (Face tri : triangleList)
        {
            lighting.applyLighting(tri, curCameraPos);
        }
        long tLight1 = System.nanoTime();
        game.Profiler.instance.setLightingTime(tLight1 - tLight0);

        // Camera transform + culling pass
        long tCam0 = System.nanoTime();
        ArrayList<Face> visibleTriangles = new ArrayList<>();
        for (Face tri : triangleList)
        {
            // Transform to camera space
            for (int i = 0; i < tri.vertex.length; i++)
            {
                tri.vertex[i] = tri.vertex[i].plus(new Vector3(
                    -curCameraPos.getX(), -curCameraPos.getY(), -curCameraPos.getZ()));
                tri.vertex[i] = curCameraRot.conjugate().rotate(tri.vertex[i]);
            }

            // Discard triangles behind camera
            if (tri.vertex[0].getZ() <= 0 && tri.vertex[1].getZ() <= 0 && tri.vertex[2].getZ() <= 0)
            {
                continue;
            }

            // Backface culling
            Vector3 v1 = tri.vertex[1].minus(tri.vertex[0]);
            Vector3 v2 = tri.vertex[2].minus(tri.vertex[0]);
            Vector3 normal = v1.cross(v2);
            Vector3 camRay = tri.vertex[0];
            if (normal.dot(camRay) >= 0)
            {
                continue;
            }
            visibleTriangles.add(tri);
        }
        long tCam1 = System.nanoTime();
        game.Profiler.instance.setCameraTransformTime(tCam1 - tCam0);

        // Depth sort pass
        long tSort0 = System.nanoTime();
        rasterizer.sortByDepth(visibleTriangles);
        long tSort1 = System.nanoTime();
        game.Profiler.instance.setDepthSortTime(tSort1 - tSort0);

        // Rasterize pass
        long tRast0 = System.nanoTime();
        double scale = baseScale - spdPercentage * SPEED_SCALE;
        for (Face tri : visibleTriangles)
        {
            rasterizer.drawTriangle(g, tri, scale);
        }
        long tRast1 = System.nanoTime();
        game.Profiler.instance.setRasterizeTime(tRast1 - tRast0);

        // Report triangle counts
        game.Profiler.instance.setTriangleCount(totalTris);
        game.Profiler.instance.setVisibleTriangleCount(visibleTriangles.size());

        triangleList.clear();
    }
}
