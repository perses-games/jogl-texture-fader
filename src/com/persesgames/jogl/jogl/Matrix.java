package com.persesgames.jogl.jogl;

/**
 * Date: 1/26/14
 * Time: 11:09 AM
 */
public class Matrix {

    float [] matrix = new float[] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    };

    float [] temp   = new float[16];

    private float [] translateMatrix = new float [] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    };

    private float [] scaleMatrix = new float [] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    };

    private float [] rotateXMatrix = new float [] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    };

    private float [] rotateYMatrix = new float [] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    };

    private float [] rotateZMatrix = new float [] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    };

    public Matrix() {
        setToIdentity();
    }

    public float [] get() {
        return matrix;
    }

    public void set(float [] values) {
        assert values.length == 16;

        matrix = values;
    }

    public void setPerspectiveProjection(float angle, float imageAspectRatio, float near, float far) {
        float r = (float) (angle / 180f * Math.PI);
        float f = (float) (1.0f / Math.tan(r / 2.0f));

        matrix[0] = f / imageAspectRatio;
        matrix[1] = 0.0f;
        matrix[2] = 0.0f;
        matrix[3] = 0.0f;

        matrix[4] = 0.0f;
        matrix[5] = f;
        matrix[6] = 0.0f;
        matrix[7] = 0.0f;

        matrix[8] = 0.0f;
        matrix[9] = 0.0f;
        matrix[10] = -(far + near) / (far - near);
        matrix[11] = -1.0f;

        matrix[12] = 0.0f;
        matrix[13] = 0.0f;
        matrix[14] = -(2.0f * far * near) / (far - near);
        matrix[15] = 0.0f;
    }

    public void setToIdentity() {
        matrix[ 0] = 1.0f;
        matrix[ 1] = 0.0f;
        matrix[ 2] = 0.0f;
        matrix[ 3] = 0.0f;
        matrix[ 4] = 0.0f;
        matrix[ 5] = 1.0f;
        matrix[ 6] = 0.0f;
        matrix[ 7] = 0.0f;
        matrix[ 8] = 0.0f;
        matrix[ 9] = 0.0f;
        matrix[10] = 1.0f;
        matrix[11] = 0.0f;
        matrix[12] = 0.0f;
        matrix[13] = 0.0f;
        matrix[14] = 0.0f;
        matrix[15] = 1.0f;
    }

    public void mul(Matrix other) {
        mul(other.get());
    }

    protected void mul(float [] other) {
        assert other.length == 16;

        temp[ 0] =   matrix[ 0] * other[ 0] + matrix[ 1] * other[ 4] + matrix[ 2] * other[ 8] + matrix[ 3] * other[12];
        temp[ 1] =   matrix[ 0] * other[ 1] + matrix[ 1] * other[ 5] + matrix[ 2] * other[ 9] + matrix[ 3] * other[13];
        temp[ 2] =   matrix[ 0] * other[ 2] + matrix[ 1] * other[ 6] + matrix[ 2] * other[10] + matrix[ 3] * other[14];
        temp[ 3] =   matrix[ 0] * other[ 3] + matrix[ 1] * other[ 7] + matrix[ 2] * other[11] + matrix[ 3] * other[15];
        temp[ 4] =   matrix[ 4] * other[ 0] + matrix[ 5] * other[ 4] + matrix[ 6] * other[ 8] + matrix[ 7] * other[12];
        temp[ 5] =   matrix[ 4] * other[ 1] + matrix[ 5] * other[ 5] + matrix[ 6] * other[ 9] + matrix[ 7] * other[13];
        temp[ 6] =   matrix[ 4] * other[ 2] + matrix[ 5] * other[ 6] + matrix[ 6] * other[10] + matrix[ 7] * other[14];
        temp[ 7] =   matrix[ 4] * other[ 3] + matrix[ 5] * other[ 7] + matrix[ 6] * other[11] + matrix[ 7] * other[15];
        temp[ 8] =   matrix[ 8] * other[ 0] + matrix[ 9] * other[ 4] + matrix[10] * other[ 8] + matrix[11] * other[12];
        temp[ 9] =   matrix[ 8] * other[ 1] + matrix[ 9] * other[ 5] + matrix[10] * other[ 9] + matrix[11] * other[13];
        temp[10] =   matrix[ 8] * other[ 2] + matrix[ 9] * other[ 6] + matrix[10] * other[10] + matrix[11] * other[14];
        temp[11] =   matrix[ 8] * other[ 3] + matrix[ 9] * other[ 7] + matrix[10] * other[11] + matrix[11] * other[15];
        temp[12] =   matrix[12] * other[ 0] + matrix[13] * other[ 4] + matrix[14] * other[ 8] + matrix[15] * other[12];
        temp[13] =   matrix[12] * other[ 1] + matrix[13] * other[ 5] + matrix[14] * other[ 9] + matrix[15] * other[13];
        temp[14] =   matrix[12] * other[ 2] + matrix[13] * other[ 6] + matrix[14] * other[10] + matrix[15] * other[14];
        temp[15] =   matrix[12] * other[ 3] + matrix[13] * other[ 7] + matrix[14] * other[11] + matrix[15] * other[15];

        matrix[ 0] = temp[ 0];
        matrix[ 1] = temp[ 1];
        matrix[ 2] = temp[ 2];
        matrix[ 3] = temp[ 3];
        matrix[ 4] = temp[ 4];
        matrix[ 5] = temp[ 5];
        matrix[ 6] = temp[ 6];
        matrix[ 7] = temp[ 7];
        matrix[ 8] = temp[ 8];
        matrix[ 9] = temp[ 9];
        matrix[10] = temp[10];
        matrix[11] = temp[11];
        matrix[12] = temp[12];
        matrix[13] = temp[13];
        matrix[14] = temp[14];
        matrix[15] = temp[15];
    }

    public void translate(float x, float y, float z) {
        translateMatrix[12] = x;
        translateMatrix[13] = y;
        translateMatrix[14] = z;

        mul(translateMatrix);
    }

    public void scale(float x, float y, float z) {
        scaleMatrix[0] = x;
        scaleMatrix[5] = y;
        scaleMatrix[10] = z;

        mul(scaleMatrix);
    }

    public void rotateX(float angle) {
        rotateXMatrix[5] = (float)Math.cos(angle);
        rotateXMatrix[6] = (float)-Math.sin(angle);
        rotateXMatrix[9] = (float)Math.sin(angle);
        rotateXMatrix[10] = (float)Math.cos(angle);

        mul(rotateXMatrix);
    }

    public void rotateY(float angle) {
        rotateYMatrix[ 0] = (float)Math.cos(angle);
        rotateYMatrix[ 2] = (float)Math.sin(angle);
        rotateYMatrix[ 8] = (float)-Math.sin(angle);
        rotateYMatrix[10] = (float)Math.cos(angle);

        mul(rotateYMatrix);
    }

    public void rotateZ(float angle) {
        rotateZMatrix[ 0] = (float)Math.cos(angle);
        rotateZMatrix[ 1] = (float)Math.sin(angle);
        rotateZMatrix[ 4] = (float)-Math.sin(angle);
        rotateZMatrix[ 5] = (float)Math.cos(angle);

        mul(rotateZMatrix);
    }
}
