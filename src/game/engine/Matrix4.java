package game.engine;

/**
 * Represents a 4x4 transformation matrix used for 3D graphics.
 * Provides methods for matrix multiplication and creating common transformation matrices.
 */
public class Matrix4
{
    /** Matrix data stored as a 2D array */
    public double [][]data;

    /**
     * Default constructor - creates an empty 4x4 matrix
     */
    public Matrix4()
    {
        data = new double[4][4];
    }
    
    /**
     * Constructor with data
     * @param newData 2D array containing matrix elements
     */
    public Matrix4(double[][]newData)
    {
        data = newData;
    }

    /**
     * Multiplies this matrix with another matrix
     * @param other The right-hand matrix in the multiplication
     * @return Result of matrix multiplication
     */
    public Matrix4 x(Matrix4 other)
    {
        Matrix4 res = new Matrix4();
        for(int i = 0; i < 4; ++i)
            for(int j = 0; j < 4; ++j)
                for(int k = 0; k <4; ++k)
                    res.data[i][j] += this.data[i][k] * other.data[k][j];
        return res;
    }

    /**
     * Creates a rotation matrix around the X axis
     * @param theta Rotation angle in degrees
     * @return X-axis rotation matrix
     */
    public static Matrix4 RotationX(double theta)
    {
        theta = theta / 180 * (double)Math.PI;
        Matrix4 res = new Matrix4(
            new double[][] {
                {1, 0, 0, 0},
                {0, Math.cos(theta), -Math.sin(theta), 0},
                {0, Math.sin(theta), Math.cos(theta), 0},
                {0, 0, 0, 1}
            }
        );
        return res;
    }
    
    /**
     * Creates a rotation matrix around the Y axis
     * @param theta Rotation angle in degrees
     * @return Y-axis rotation matrix
     */
    public static Matrix4 RotationY(double theta)
    {
        theta = theta / 180 * Math.PI;
        Matrix4 res = new Matrix4(
            new double[][] {
                {Math.cos(theta), 0, Math.sin(theta), 0},
                {0, 1, 0, 0},
                {-Math.sin(theta), 0, Math.cos(theta), 0},
                {0, 0, 0, 1}
            }
        );
        return res;
    }
    
    /**
     * Creates a rotation matrix around the Z axis
     * @param theta Rotation angle in degrees
     * @return Z-axis rotation matrix
     */
    public static Matrix4 RotationZ(double theta)
    {
        theta = theta / 180 * Math.PI;
        Matrix4 res = new Matrix4(
            new double[][] {
                {Math.cos(theta), -Math.sin(theta), 0, 0},
                {Math.sin(theta), Math.cos(theta), 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
            }
        );
        return res;
    }

    /**
     * Creates a translation matrix
     * @param tx X translation
     * @param ty Y translation
     * @param tz Z translation
     * @return Translation matrix
     */
    public static Matrix4 Translation(double tx, double ty, double tz)
    {
        Matrix4 res = new Matrix4(
            new double[][] {
                {1, 0, 0, tx},
                {0, 1, 0, ty},
                {0, 0, 1, tz},
                {0, 0, 0, 1}
            }
        );
        return res;
    }
}
