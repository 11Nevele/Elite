package game;

import game.engine.Face;
import game.engine.ObjReader;
import game.engine.Vector3;
import java.awt.Color;
public class Models 
{
    //define common colors
    public final static Color red = new Color(255, 0, 0);
    public final static Color lightRed = new Color(255, 180, 180);
    public final static Color green = new Color(0, 255, 0);
    public final static Color blue = new Color(0, 0, 255);
    public final static Color yellow = new Color(255, 255, 0);
    public final static Color lightYellow = new Color(255, 255, 180);
    public final static Color cyan = new Color(180, 255, 255);
    public final static Color magenta = new Color(255, 0, 255);
    public final static Color white = new Color(255, 255, 255);
    public final static Color black = new Color(0, 0, 0);
    public final static Color clear = new Color(0,0,0,0);

    //length of 100
    static final Vector3[] cubeVerteies = 
    {
        new Vector3(-50, -50, -50),
        new Vector3(-50, 50, -50),
        new Vector3(50, 50, -50),
        new Vector3(50, -50, -50),
        new Vector3(-50, -50, 50),
        new Vector3(-50, 50, 50),
        new Vector3(50, 50, 50),
        new Vector3(50, -50, 50)
    };
    //all white, triangles are in clockwise order when faced from outside
    static final Face []cube =
    {
        new Face(cubeVerteies[0], cubeVerteies[2], cubeVerteies[1], clear, red, red, black),
        new Face(cubeVerteies[0], cubeVerteies[3], cubeVerteies[2], red, red, clear, black),
        new Face(cubeVerteies[0], cubeVerteies[1], cubeVerteies[5], red, red, clear, black),
        new Face(cubeVerteies[0], cubeVerteies[5], cubeVerteies[4], clear, red, red, black),
        new Face(cubeVerteies[1], cubeVerteies[2], cubeVerteies[6], red, red, clear, black),
        new Face(cubeVerteies[1], cubeVerteies[6], cubeVerteies[5], clear, red, red, black),
        new Face(cubeVerteies[2], cubeVerteies[3], cubeVerteies[7], red, red, clear, black),
        new Face(cubeVerteies[2], cubeVerteies[7], cubeVerteies[6], clear, red, red, black),
        new Face(cubeVerteies[3], cubeVerteies[0], cubeVerteies[4], red, red, clear, black),
        new Face(cubeVerteies[3], cubeVerteies[4], cubeVerteies[7], clear, red, red, black),
        new Face(cubeVerteies[4], cubeVerteies[5], cubeVerteies[6], red, red, clear, black),
        new Face(cubeVerteies[4], cubeVerteies[6], cubeVerteies[7], clear, red, red, black)
    };

    static final Face[] star = {
        new Face(new Vector3[]{new Vector3(-500, 0, 0), new Vector3(0,866,0), new Vector3(500, 0, 0)}, white, white),
        new Face(new Vector3[]{new Vector3(-500, 0, 0), new Vector3(0,866,0), new Vector3(500, 0, 0)}, white, white)
    };


    static final Face[] GetSphere(double radius)
    {
        //20 rings, each ring 20 points
        Face[] res = new Face[20 * 20 * 2];
        for(int i = 0; i < 20; i++)
        {

            for(int j = 0; j < 20; j++)
            {
                double theta = 2 * (double)Math.PI * i / 20;
                double phi = (double)Math.PI * j / 20;
                double theta2 = 2 * (double)Math.PI * (i + 1) / 20;
                double phi2 = (double)Math.PI * (j + 1) / 20;
                Vector3 v1 = new Vector3(
                    radius * (double)Math.sin(phi) * (double)Math.cos(theta),
                    radius * (double)Math.sin(phi) * (double)Math.sin(theta),
                    radius * (double)Math.cos(phi)
                );
                Vector3 v2 = new Vector3(
                    radius * (double)Math.sin(phi) * (double)Math.cos(theta2),
                    radius * (double)Math.sin(phi) * (double)Math.sin(theta2),
                    radius * (double)Math.cos(phi)
                );
                Vector3 v3 = new Vector3(
                    radius * (double)Math.sin(phi2) * (double)Math.cos(theta),
                    radius * (double)Math.sin(phi2) * (double)Math.sin(theta),
                    radius * (double)Math.cos(phi2)
                );
                Vector3 v4 = new Vector3(
                    radius * (double)Math.sin(phi2) * (double)Math.cos(theta2),
                    radius * (double)Math.sin(phi2) * (double)Math.sin(theta2),
                    radius * (double)Math.cos(phi2)
                );
                if(j == 0)
                {
                    res[i * 40 + j * 2] = new Face(v3, v4, v1, clear, clear, clear,red);
                    res[i * 40 + j * 2 + 1] = new Face(v3, v4, v1,clear, clear, clear, red);
                    continue;
                }
                if(j == 19)
                {
                    res[i * 40 + j * 2] = new Face(v3, v2, v1, clear, clear, clear,red);
                    res[i * 40 + j * 2 + 1] = new Face(v3, v2, v1, clear, clear, clear,red);
                    continue;
                }
                res[i * 40 + j * 2] = new Face(v3, v2, v1,clear, clear, clear, red);
                res[i * 40 + j * 2 + 1] = new Face(v3, v4, v2,clear, clear, clear, red);
            }
        }
        return res;
    }


    //piramid base 10 * 10, height 20
    static final Vector3[] pv =
    {
        new Vector3(-5, -5, 10),
        new Vector3(-5, 5, 10),
        new Vector3(5, 5, 10),
        new Vector3(5, -5, 10),
        new Vector3(0, 0, 30)
    };
    static final Face[] pyramid =
    {
        new Face(pv[0], pv[1], pv[4], red),
        new Face(pv[1], pv[2], pv[4], red),
        new Face(pv[2], pv[3], pv[4], red),
        new Face(pv[3], pv[0], pv[4], red),
        new Face(pv[0], pv[2], pv[1], red),
        new Face(pv[0], pv[3], pv[2], red)
    };


    static final Face[] Plane = ObjReader.ReadObj(System.getProperty("user.dir") + "\\Model\\Plane.obj",
    System.getProperty("user.dir") + "\\Model\\Plane.mtl", lightRed);

    static final Face[] DeathStar = ObjReader.ReadObj(System.getProperty("user.dir") + "\\Model\\untitled.obj",
    System.getProperty("user.dir") + "\\Model\\untitled.mtl", Color.gray);

    //get current project path

    static final Face[][] Astroids = 
    {
        ObjReader.ReadObj(System.getProperty("user.dir") + "\\Model\\asteroids0.obj", null, lightYellow),
        //ObjReader.ReadObj(System.getProperty("user.dir") + "\\Model\\asteroids1.obj", null, Color.yellow),
        //ObjReader.ReadObj(System.getProperty("user.dir") + "\\Model\\asteroids2.obj", null, Color.yellow),
    };

    static final Face[] TIE = ObjReader.ReadObj(System.getProperty("user.dir") + "\\Model\\TIE.obj", 
    System.getProperty("user.dir") + "\\Model\\TIE.mtl", Color.white);

    //a cluster of small triangles red or orange
    static final Face[] explosion = 
    {
        new Face(new Vector3[]{new Vector3(0, 0, 0), new Vector3(1, 0, 0), new Vector3(0, 1, 0)}),
        new Face(new Vector3[]{new Vector3(0, 0, 0), new Vector3(0, 1, 0), new Vector3(1, 0, 0)})

    };

    
}
