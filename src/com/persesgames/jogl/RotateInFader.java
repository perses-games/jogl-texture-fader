package com.persesgames.jogl;

/**
 * User: rnentjes
 * Date: 1/26/14
 * Time: 4:28 PM
 */
public class RotateInFader extends Fader {


    private Matrix source = new Matrix();
    private Matrix dest = new Matrix();

    private float z;

    @Override
    public void reset() {
        super.reset();

        z = -50.0f;
    }

    @Override
    public void update(float time, float aspect) {
        super.update(time, aspect);

        this.z += time * 50;

        if (z > -1) {
            z = -1;
        }
    }

    @Override
    public Matrix getSourceModelViewMatrix() {
        source.setToIdentity();
        source.scale(aspect, 1, 1);
        source.translate(0,0,-1);

        return source;
    }

    public float getSourceAlpha() {
        return 1f;
    }

    @Override
    public Matrix getDestinationModelViewMatrix() {
        float angle = z + 1;

        angle = -(angle / 49); // angle van 1 => 0
        angle = angle * 5;
        angle = (float) (angle * Math.PI / 2);

        dest.setToIdentity();
        dest.scale(aspect, 1, 1);
        dest.rotateX(angle);
        dest.translate(0,0,z);

        return dest;
    }

    public float getDestinationAlpha() {
        if (z > -5) {
            return 1f;
        } else {
            return (z + 65f) / 60;
        }
    }

    @Override
    public boolean done() {
        return z == -1;
    }
}
