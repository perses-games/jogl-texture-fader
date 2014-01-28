package com.persesgames.jogl;

/**
 * User: rnentjes
 * Date: 1/26/14
 * Time: 4:28 PM
 */
public class SlideFader extends Fader {

    public enum SlideDirection {
        UP,
        LEFT,
        DOWN,
        RIGHT;
    }

    private SlideDirection direction;

    private Matrix source = new Matrix();
    private Matrix dest = new Matrix();

    private float xoffset, yoffset;

    public SlideFader(SlideDirection direction) {
        this.direction = direction;
    }

    @Override
    public void reset() {
        super.reset();

        xoffset = -2 * aspect;
        yoffset = 0;
    }

    @Override
    public void update(float time, float aspect) {
        super.update(time, aspect);

        this.xoffset += time * 4;

        if (xoffset > 0) {
            xoffset = 0;
        }
    }

    @Override
    public Matrix getSourceModelViewMatrix() {
        source.setToIdentity();
        source.scale(aspect, 1, 1);
        source.translate(xoffset + 2 * aspect,0,-1);

        return source;
    }

    public float getSourceAlpha() {
        return 1f;
    }

    @Override
    public Matrix getDestinationModelViewMatrix() {
        dest.setToIdentity();
        dest.scale(aspect, 1, 1);
        dest.translate(xoffset,0,-1);

        return dest;
    }

    public float getDestinationAlpha() {
        return 1f;
    }

    @Override
    public boolean done() {
        return xoffset == 0;
    }
}
