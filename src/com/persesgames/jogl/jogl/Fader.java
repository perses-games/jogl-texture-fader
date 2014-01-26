package com.persesgames.jogl.jogl;

/**
 * User: rnentjes
 * Date: 1/26/14
 * Time: 4:26 PM
 */
public abstract class Fader {

    public abstract void init();
    public abstract void update(float time);
    public abstract Matrix getSourceModelViewMatrix();
    public abstract float getSourceAlpha();
    public abstract Matrix getDestinationModelViewMatrix();
    public abstract float getDestinationAlpha();
    public abstract boolean done();

}
