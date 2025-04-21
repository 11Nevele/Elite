package game.engine;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Utility class for loading 3D models from Wavefront OBJ files.
 * Can also read materials from MTL files.
 */
public class ObjReader 
{
    /**
     * Reads a Wavefront OBJ file and returns an array of Face objects
     * @param path Path to the OBJ file
     * @param texturePath Path to the MTL file (can be null)
     * @param lineColor Default color for edges
     * @return Array of Face objects representing the 3D model
     */
    public static Face[] ReadObj(String path, String texturePath, Color lineColor)
    {
        //read the obj file
        ArrayList<Vector3> vertices = new ArrayList<>();
        ArrayList<Face> model = new ArrayList<>();
        //hashamp key name, value color
        HashMap<String, Color> colors = new HashMap<>();
        String currentMaterialName = "";
        
        // Load material definitions if texture path is provided
        if(texturePath != null)
        {
            try(BufferedReader br = new BufferedReader(new FileReader(texturePath)))
            {
                String line;

                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] tokens = line.split("\\s+");

                    if (tokens[0].equals("newmtl")) {
                        // Create a new material
                        currentMaterialName = tokens[1];
                    } else if (tokens[0].equals("Ka") && currentMaterialName != "") {
                        // Ambient color (Ka) - not used currently
                    } else if (tokens[0].equals("Kd") && currentMaterialName != "") {
                        // Diffuse color (Kd)
                        colors.put(currentMaterialName, new Color(
                            (Float.parseFloat(tokens[1])),
                            (Float.parseFloat(tokens[2])),
                            (Float.parseFloat(tokens[3]))
                        ));
                    } else if (tokens[0].equals("Ks") && currentMaterialName != "") {
                        // Specular color (Ks) - not used currently
                    } else if (tokens[0].equals("Ns") && currentMaterialName != "") {
                        // Shininess (Ns) - not used currently
                    } else if (tokens[0].equals("map_Kd") && currentMaterialName != "") {
                        // Diffuse texture map (map_Kd) - not used currently
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Default color if no material is specified
        Color curColor = Color.gray;
        
        // Read the OBJ file
        try (BufferedReader br = new BufferedReader(new FileReader(path)))
        {
            String line;
            while ((line = br.readLine()) != null) 
            {
                String[] tokens = line.trim().split("\\s+");
                if (tokens.length == 0) continue;

                switch (tokens[0]) 
                {
                    case "v": // Vertex
                        vertices.add(new Vector3(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                            ));
                        
                        break;

                    case "vt": // Texture coordinate - not used currently
                        break;

                    case "vn": // Normal - not used currently
                        break;

                    case "f": // Face
                        Vector3[] faces = new Vector3[tokens.length - 1];
                        for (int i = 1; i < tokens.length; i++) {
                            String[] indices = tokens[i].split("/"); // v/vt/vn
                            faces[i - 1] = vertices.get(Integer.parseInt(indices[0]) - 1); // Convert to zero-based index
                        }
                        model.add(new Face(faces, lineColor, curColor));
                        break;
                    case "usemtl": // Use material
                        curColor = colors.get(tokens[1]);
                        break;
                }
            }
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return model.toArray(new Face[0]);
    }
}
