package com.persesgames.jogl;

/**
 * User: rnentjes
 * Date: 1/26/14
 * Time: 4:28 PM
 */
public class TestFader extends Fader {

    private Matrix source = new Matrix();
    private Matrix dest = new Matrix();

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
        dest.setToIdentity();
        dest.scale(aspect, 1, 1);
        dest.translate(0,0,-1);

        return dest;
    }

    public float getDestinationAlpha() {
        return (float) Math.sin(time);
    }

    @Override
    public boolean done() {
        return time > Math.PI / 2;
    }
}
