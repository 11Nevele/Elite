package game.engine;

public class Matrix4
{
    public double [][]data;

    public Matrix4()
    {
        data = new double[4][4];
    }
    public Matrix4(double[][]newData)
    {
        data = newData;
    }


    public Matrix4 x(Matrix4 other)
    {
        Matrix4 res = new Matrix4();
        for(int i = 0; i < 4; ++i)
            for(int j = 0; j < 4; ++j)
                for(int k = 0; k <4; ++k)
                    res.data[i][j] += this.data[i][k] * other.data[k][j];
        return res;
    }

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
