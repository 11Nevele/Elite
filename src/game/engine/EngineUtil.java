package game.engine;

import java.awt.Color;
import java.util.Random;

/**
 * Utility class for common 3D graphics and math operations.
 */
public class EngineUtil 
{
    public static Quaternion eulerToQuaternion(Vector3 v) 
    {
        double halfRoll = Math.toRadians(v.getX()) / 2.0;
        double halfPitch = Math.toRadians(v.getY()) / 2.0;
        double halfYaw = Math.toRadians(v.getZ()) / 2.0;

        double cx = Math.cos(halfRoll);
        double cy = Math.cos(halfPitch);
        double cz = Math.cos(halfYaw);
        double sx = Math.sin(halfRoll);
        double sy = Math.sin(halfPitch);
        double sz = Math.sin(halfYaw);

        double w = cx * cy * cz + sx * sy * sz;
        double qx = sx * cy * cz - cx * sy * sz;
        double qy = cx * sy * cz + sx * cy * sz;
        double qz = cx * cy * sz - sx * sy * cz;

        return new Quaternion(qx, qy, qz, w);
    }

    public static Vector3 quaternionToEuler(Quaternion q) 
    {
        double sp = -2.0 * (q.getY() * q.getZ() - q.getW() * q.getX());
        sp = Math.max(-1.0, Math.min(1.0, sp));

        double pitch = Math.asin(sp);
        double yaw = Math.atan2(2.0 * (q.getX() * q.getZ() + q.getW() * q.getY()),
                                1.0 - 2.0 * (q.getY() * q.getY() + q.getZ() * q.getZ()));
        double roll = Math.atan2(2.0 * (q.getX() * q.getY() + q.getW() * q.getZ()),
                                 1.0 - 2.0 * (q.getZ() * q.getZ() + q.getW() * q.getW()));

        return new Vector3(Math.toDegrees(roll), Math.toDegrees(pitch), Math.toDegrees(yaw));
    }
    
    public static Vector3 quaternionToDirection(Quaternion q)
    {
        double vx = 2 * (q.getX() * q.getZ() + q.getW() * q.getY());
        double vy = 2 * (q.getY() * q.getZ() - q.getW() * q.getX());
        double vz = 1 - 2 * (q.getX() * q.getX() + q.getY() * q.getY());
        return new Vector3(vx, vy, vz);
    }

    public static Vector3 randomOnSphere(double radius)
    {
        Random random = new Random();
        double theta = 2 * Math.PI * random.nextDouble();
        double cosPhi = 2 * random.nextDouble() - 1;
        double phi = Math.acos(cosPhi);

        return new Vector3(
            radius * Math.cos(theta) * Math.sin(phi),
            radius * Math.sin(theta) * Math.sin(phi),
            radius * Math.cos(phi)
        );
    }

    public static Color adjustBrightness(Color color, double factor) 
    {
        factor = Math.max(-1.0, Math.min(1.0, factor));
        int red = (int) Math.min(255, Math.max(0, color.getRed() + (color.getRed() * factor)));
        int green = (int) Math.min(255, Math.max(0, color.getGreen() + (color.getGreen() * factor)));
        int blue = (int) Math.min(255, Math.max(0, color.getBlue() + (color.getBlue() * factor)));
        return new Color(red, green, blue);
    }
}
