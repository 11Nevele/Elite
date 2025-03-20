package game.engine;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
public class ObjReader 
{
    public static Face[] ReadObj(String path, String texturePath, Color lineColor)
    {
        //read the obj file
        ArrayList<Vector3> vertices = new ArrayList<>();
        ArrayList<Face> model = new ArrayList<>();
        //hashamp key name, value color
        HashMap<String, Color> colors = new HashMap<>();
        String currentMaterialName = "";
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

                    } else if (tokens[0].equals("Kd") && currentMaterialName != "") {
                        // Diffuse color (Kd)
                        colors.put(currentMaterialName, new Color(
                            (Float.parseFloat(tokens[1])),
                            (Float.parseFloat(tokens[2])),
                            (Float.parseFloat(tokens[3]))
                        ));
                    } else if (tokens[0].equals("Ks") && currentMaterialName != "") {
                    
                    } else if (tokens[0].equals("Ns") && currentMaterialName != "") {
                        // Shininess (Ns)
                    } else if (tokens[0].equals("map_Kd") && currentMaterialName != "") {
                        // Diffuse texture map (map_Kd)
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        
        Color curColor = Color.gray;
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

                    case "vt": // Texture coordinate
                        break;

                    case "vn": // Normal
                        break;

                    case "f": // Face
                        Vector3[] faces = new Vector3[tokens.length - 1];
                        for (int i = 1; i < tokens.length; i++) {
                            String[] indices = tokens[i].split("/"); // v/vt/vn
                            faces[i - 1] = vertices.get(Integer.parseInt(indices[0]) - 1); // Convert to zero-based index
                        }
                        model.add(new Face(faces, lineColor, curColor));
                        break;
                    case "usemtl":
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
