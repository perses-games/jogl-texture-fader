package com.persesgames.jogl;

/**
 * User: rnentjes
 * Date: 1/26/14
 * Time: 4:28 PM
 */
public class TestFader extends Fader {

    private float aspect;
    private float time = 0;

    private Matrix source = new Matrix();
    private Matrix dest = new Matrix();

    public TestFader(float aspect) {
        this.aspect = aspect;
    }

    @Override
    public void reset() {
        time = 0;

        source.setToIdentity();
        source.scale(aspect, 1, 1);
        source.translate(0,0,-1);

        dest.setToIdentity();
        dest.scale(aspect, 1, 1);
        dest.translate(0,0,-1);
    }

    @Override
    public void update(float time) {
        this.time += time;
    }

    @Override
    public Matrix getSourceModelViewMatrix() {
        return source;
    }

    public float getSourceAlpha() {
        return 1f;
    }

    @Override
    public Matrix getDestinationModelViewMatrix() {
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
