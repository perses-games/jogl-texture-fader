package com.persesgames.jogl;

/**
 * User: rnentjes
 * Date: 1/26/14
 * Time: 4:26 PM
 */
public abstract class Fader {

    protected float aspect = 1f;
    protected float time = 0;

    public void reset() {
        time = 0;
    }

    public void update(float time, float aspect) {
        this.time += time;
        this.aspect = aspect;
    }

    public abstract Matrix getSourceModelViewMatrix();
    public abstract float getSourceAlpha();
    public abstract Matrix getDestinationModelViewMatrix();
    public abstract float getDestinationAlpha();
    public abstract boolean done();

}
