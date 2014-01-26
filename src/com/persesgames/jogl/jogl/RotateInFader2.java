package com.persesgames.jogl.jogl;

/**
 * User: rnentjes
 * Date: 1/26/14
 * Time: 4:28 PM
 */
public class RotateInFader2 extends Fader {

    private float aspect;
    private float time = 0;

    private Matrix source = new Matrix();
    private Matrix dest = new Matrix();

    private float z;

    public RotateInFader2(float aspect) {
        this.aspect = aspect;
    }

    @Override
    public void reset() {
        time = 0;

        z = -10.8f;
    }

    @Override
    public void update(float time) {
        this.time += time;
        this.z += time * 20;

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
        dest.rotateY(angle);
//        dest.rotateZ(angle);
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
