package game.engine;

import java.awt.Color;
import java.util.Random;

import javafx.scene.paint.Color;

public class EngineUtil 
{
    public static Quaternion eulerToQuaternion(Vector3 v) 
    {
        // Convert angles to radians
        double halfRoll = Math.toRadians(v.x) / 2.0;
        double halfPitch = Math.toRadians(v.y) / 2.0;
        double halfYaw = Math.toRadians(v.z) / 2.0;

        // Compute sine and cosine of half angles
        double cx = Math.cos(halfRoll);
        double cy = Math.cos(halfPitch);
        double cz = Math.cos(halfYaw);
        double sx = Math.sin(halfRoll);
        double sy = Math.sin(halfPitch);
        double sz = Math.sin(halfYaw);

        // Compute quaternion components
        double w = cx * cy * cz + sx * sy * sz;
        double qx = sx * cy * cz - cx * sy * sz;
        double qy = cx * sy * cz + sx * cy * sz;
        double qz = cx * cy * sz - sx * sy * cz;

        return new Quaternion(qx, qy, qz, w);
    }

    //Quaternion to euler
    public static Vector3 quaternionToEuler(Quaternion q) 
    {
        // Extract sin and cos of pitch angle
        double sp = -2.0 * (q.y * q.z - q.w * q.x);
        if (sp > 1.0) sp = 1.0;
        if (sp < -1.0) sp = -1.0;

        // Compute pitch angle
        double pitch = Math.asin(sp);

        // Compute yaw angle
        double yaw = Math.atan2(2.0 * (q.x * q.z + q.w * q.y), 1.0 - 2.0 * (q.y * q.y + q.z * q.z));

        // Compute roll angle
        double roll = Math.atan2(2.0 * (q.x * q.y + q.w * q.z), 1.0 - 2.0 * (q.z * q.z + q.w * q.w));

        // Convert angles to degrees
        return new Vector3((double)Math.toDegrees(roll), (double)Math.toDegrees(pitch), (double)Math.toDegrees(yaw));
    }
    public static Vector3 quaternionToDirection(Quaternion q) {

        // Apply quaternion rotation to the forward vector (0, 0, 1)
        double vx = 2 * (q.x * q.z + q.w * q.y);
        double vy = 2 * (q.y * q.z - q.w * q.x);
        double vz = 1 - 2 * (q.x * q.x + q.y * q.y);

        return new Vector3(vx, vy, vz);  // Normalized direction
    }
    public static Vector3 RandomOnSphere(double radius)
    {
        Vector3 v = new Vector3();
        Random random = new Random();
        double theta = 2 * (double)Math.PI * random.nextDouble(); // Azimuthal angle
        double cosPhi = 2 * random.nextDouble() - 1; // Uniformly sample cos(phi)
        double phi = (double)Math.acos(cosPhi); // Compute phi
        
        v.x = radius * (double)Math.cos(theta) * (double)Math.sin(phi);
        v.y = radius * (double)Math.sin(theta) * (double)Math.sin(phi);
        v.z = radius * (double)Math.cos(phi);
        return v;
    }

    public static Color adjustBrightness(Color color, double factor) 
    {
        // Clamp the factor to the range [-1.0, 1.0]
        factor = Math.max(-1.0, Math.min(1.0, factor));

        // Extract RGB components
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        // Adjust each component
        red = (int) Math.min(255, Math.max(0, red + (red * factor)));
        green = (int) Math.min(255, Math.max(0, green + (green * factor)));
        blue = (int) Math.min(255, Math.max(0, blue + (blue * factor)));

        // Return the new color
        return new Color(red, green, blue);
    }
}
