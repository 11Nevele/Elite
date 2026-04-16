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

    // Pre-allocated scratch buffers to avoid per-frame allocations
    private Face[] scratchFaces;
    private int scratchSize = 0;
    private static final int INITIAL_SCRATCH_SIZE = 2048;

    // Point rendering list for stars etc.
    private ArrayList<PointEntry> pointList;

    private static class PointEntry
    {
        Vector3 position;
        Color color;
        int size;

        PointEntry(Vector3 position, Color color, int size)
        {
            this.position = position;
            this.color = color;
            this.size = size;
        }
    }

    public Renderer(Graphics g, double width, double height)
    {
        triangleList = new ArrayList<>();
        pointList = new ArrayList<>();
        this.g = g;
        curCameraPos = new Vector3();
        curCameraRot = new Quaternion();
        projection = new ProjectionTransform(width, height);
        lighting = new LightingCalculator();
        rasterizer = new TriangleRasterizer(projection);

        // Pre-allocate scratch Face buffer
        scratchFaces = new Face[INITIAL_SCRATCH_SIZE];
        for (int i = 0; i < INITIAL_SCRATCH_SIZE; i++)
        {
            scratchFaces[i] = new Face();
        }
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

    private Face getScratchFace(Face src)
    {
        if (scratchSize >= scratchFaces.length)
        {
            // Grow the scratch buffer
            Face[] newBuf = new Face[scratchFaces.length * 2];
            System.arraycopy(scratchFaces, 0, newBuf, 0, scratchFaces.length);
            for (int i = scratchFaces.length; i < newBuf.length; i++)
            {
                newBuf[i] = new Face();
            }
            scratchFaces = newBuf;
        }
        Face f = scratchFaces[scratchSize++];
        f.copyFrom(src);
        return f;
    }

    public void renderPoint(Vector3 position, Color color, int size)
    {
        pointList.add(new PointEntry(position, color, size));
    }

    public void render(Renderable obj)
    {
        for (Face tri : obj.model)
        {
            Face nTri = getScratchFace(tri);
            for (int i = 0; i < nTri.vertex.length; ++i)
            {
                nTri.vertex[i].multiplyInPlace(obj.scale);
                obj.rotation.rotateInPlace(nTri.vertex[i]);
                nTri.vertex[i].addInPlace(obj.position);
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

        // Cache camera transform values once per frame
        double negCamX = -curCameraPos.getX();
        double negCamY = -curCameraPos.getY();
        double negCamZ = -curCameraPos.getZ();
        Quaternion camRotConj = curCameraRot.conjugate();

        // Camera transform + culling pass
        long tCam0 = System.nanoTime();
        ArrayList<Face> visibleTriangles = new ArrayList<>();
        for (Face tri : triangleList)
        {
            // Transform to camera space
            for (int i = 0; i < tri.vertex.length; i++)
            {
                tri.vertex[i].addXYZ(negCamX, negCamY, negCamZ);
                camRotConj.rotateInPlace(tri.vertex[i]);
            }

            // Discard triangles behind camera
            if (tri.vertex[0].getZ() <= 0 && tri.vertex[1].getZ() <= 0 && tri.vertex[2].getZ() <= 0)
            {
                continue;
            }

            // Backface culling (inline to avoid allocations)
            double e1x = tri.vertex[1].getX() - tri.vertex[0].getX();
            double e1y = tri.vertex[1].getY() - tri.vertex[0].getY();
            double e1z = tri.vertex[1].getZ() - tri.vertex[0].getZ();
            double e2x = tri.vertex[2].getX() - tri.vertex[0].getX();
            double e2y = tri.vertex[2].getY() - tri.vertex[0].getY();
            double e2z = tri.vertex[2].getZ() - tri.vertex[0].getZ();
            double nx = e1y * e2z - e1z * e2y;
            double ny = e1z * e2x - e1x * e2z;
            double nz = e1x * e2y - e1y * e2x;
            double dotCam = nx * tri.vertex[0].getX() + ny * tri.vertex[0].getY() + nz * tri.vertex[0].getZ();
            if (dotCam >= 0)
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

        // Draw points (stars) after triangles
        for (PointEntry p : pointList)
        {
            double px = p.position.getX() + negCamX;
            double py = p.position.getY() + negCamY;
            double pz = p.position.getZ() + negCamZ;
            // Apply camera rotation
            Vector3 camSpace = camRotConj.rotate(new Vector3(px, py, pz));
            if (camSpace.getZ() > 0)
            {
                Vector2 screen = projection.project(camSpace, scale);
                g.setColor(p.color);
                int sx = (int) Math.round(screen.getX());
                int sy = (int) Math.round(screen.getY());
                g.fillOval(sx - p.size / 2, sy - p.size / 2, p.size, p.size);
            }
        }

        long tRast1 = System.nanoTime();
        game.Profiler.instance.setRasterizeTime(tRast1 - tRast0);

        // Report triangle counts
        game.Profiler.instance.setTriangleCount(totalTris);
        game.Profiler.instance.setVisibleTriangleCount(visibleTriangles.size());

        triangleList.clear();
        pointList.clear();
        scratchSize = 0;
    }
}
