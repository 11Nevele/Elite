package game.engine;

import java.awt.Color;
import java.io.*;
import java.util.*;

/**
 * Reads Wavefront OBJ files and associated MTL material files.
 * Triangulates quad faces using fan triangulation.
 */
public class ObjReader
{
    /**
     * Reads an OBJ file and its associated MTL file.
     * @param filename Path to the .obj file
     * @return Array of Face objects representing the model
     */
    public static Face[] readObj(String filename)
    {
        HashMap<String, Color> materials = new HashMap<>();
        ArrayList<Vector3> vertices = new ArrayList<>();
        ArrayList<Face> faces = new ArrayList<>();
        String currentMaterial = "";

        try
        {
            File objFile = new File(filename);
            String parentDir = objFile.getParent();

            // First pass: find and read MTL file referenced in OBJ
            BufferedReader scanner = new BufferedReader(new FileReader(objFile));
            String line;
            while ((line = scanner.readLine()) != null)
            {
                line = line.trim();
                if (line.startsWith("mtllib "))
                {
                    String mtlName = line.substring(7).trim();
                    String mtlPath = parentDir + File.separator + mtlName;
                    readMtl(mtlPath, materials);
                    break;
                }
            }
            scanner.close();

            // Second pass: read vertices and faces
            BufferedReader reader = new BufferedReader(new FileReader(objFile));
            while ((line = reader.readLine()) != null)
            {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 0 || parts[0].isEmpty()) continue;

                switch (parts[0])
                {
                    case "v":
                        double x = Double.parseDouble(parts[1]);
                        double y = Double.parseDouble(parts[2]);
                        double z = Double.parseDouble(parts[3]);
                        vertices.add(new Vector3(x, y, z));
                        break;

                    case "usemtl":
                        currentMaterial = parts[1];
                        break;

                    case "f":
                        parseFace(parts, vertices, materials, currentMaterial, faces);
                        break;
                }
            }
            reader.close();
        }
        catch (Exception e)
        {
            System.err.println("Error reading OBJ file: " + filename);
            e.printStackTrace();
        }

        return faces.toArray(new Face[0]);
    }

    private static void readMtl(String mtlPath, HashMap<String, Color> materials)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(mtlPath));
            String line;
            String matName = "";
            while ((line = reader.readLine()) != null)
            {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 0 || parts[0].isEmpty()) continue;

                if (parts[0].equals("newmtl"))
                {
                    matName = parts[1];
                }
                else if (parts[0].equals("Kd") && !matName.isEmpty())
                {
                    float r = Float.parseFloat(parts[1]);
                    float g = Float.parseFloat(parts[2]);
                    float b = Float.parseFloat(parts[3]);
                    materials.put(matName, new Color(
                        Math.min(1f, Math.max(0f, r)),
                        Math.min(1f, Math.max(0f, g)),
                        Math.min(1f, Math.max(0f, b))));
                }
            }
            reader.close();
        }
        catch (Exception e)
        {
            System.err.println("Error reading MTL file: " + mtlPath);
            e.printStackTrace();
        }
    }

    private static void parseFace(String[] parts, ArrayList<Vector3> vertices,
                                   HashMap<String, Color> materials, String currentMaterial,
                                   ArrayList<Face> faces)
    {
        int[] indices = new int[parts.length - 1];
        for (int i = 0; i < indices.length; i++)
        {
            String[] sub = parts[i + 1].split("/");
            indices[i] = Integer.parseInt(sub[0]) - 1;
        }

        Color matColor = materials.getOrDefault(currentMaterial, Color.WHITE);

        if (indices.length == 3)
        {
            Vector3[] verts = {
                new Vector3(vertices.get(indices[0])),
                new Vector3(vertices.get(indices[1])),
                new Vector3(vertices.get(indices[2]))
            };
            faces.add(new Face(verts, matColor));
        }
        else if (indices.length >= 4)
        {
            for (int i = 1; i < indices.length - 1; i++)
            {
                Vector3[] verts = {
                    new Vector3(vertices.get(indices[0])),
                    new Vector3(vertices.get(indices[i])),
                    new Vector3(vertices.get(indices[i + 1]))
                };
                faces.add(new Face(verts, matColor));
            }
        }
    }
}
