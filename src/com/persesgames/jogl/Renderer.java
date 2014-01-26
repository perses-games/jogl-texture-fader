package com.persesgames.jogl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.opengl.*;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;

/**
 * Date: 10/25/13
 * Time: 7:42 PM
 */
public class Renderer implements GLEventListener  {
    private final static Logger logger = LoggerFactory.getLogger(Renderer.class);

    private volatile boolean stopped    = false;
    private volatile boolean dirty      = true;

    private float aspect;

    private ShaderProgram textureProgram;

    private final GLWindow glWindow;

    private float[]                 txtVerts = {
            -1.0f, -1.0f,
            -1.0f,  1.0f,
             1.0f,  1.0f,
             1.0f,  -1.0f,
    };

    private Matrix                  projectionMatrix = new Matrix();
    private Matrix                  modelViewMatrix  = new Matrix();

    private FloatBuffer             fbTxtVertices       = Buffers.newDirectFloatBuffer(txtVerts);

    private int                     width = 256, height = 256;

    private int                     txtVertices;

    private Texture                 source;
    private Texture                 dest;
    private Texture []              textures = new Texture[4];
    private int                     currentDest = 1;

    private Fader []                faders;
    private int                     currentFader = 0;

    private int                     uTexture;
    private int                     uAlpha;
    private int                     uProjection;
    private int                     uModelView;

    private Keyboard                keyboard;

    private int                     frameCount;
    private long                    frameTime;
    private long                    lastTiming;

    private long                    start = System.currentTimeMillis();

    public Renderer(GLWindow glWindow, Keyboard keyboard) {
        this.glWindow = glWindow;
        this.keyboard = keyboard;

        aspect = 1920f/1080f;
        this.projectionMatrix.setPerspectiveProjection(90f, aspect, 1.0f, 50.0f);

        faders = new Fader[5];

        faders[0] = new SlideFader(aspect, SlideFader.SlideDirection.RIGHT);
        faders[1] = new ZoomInFader(aspect);
        faders[2] = new RotateInFader(aspect);
        faders[3] = new RotateInFader2(aspect);
        faders[4] = new TestFader(aspect);

        this.faders[currentFader].reset();
    }

    public void stop() {
        stopped = true;
    }

    public void redraw() {
        dirty = true;
    }

    public void run() {
        Renderer.this.glWindow.display();

        while(!stopped) {
            if (dirty) {
                //logger.info("rendering+" + System.currentTimeMillis());
                Renderer.this.glWindow.display();
                //Renderer.this.glWindow.swapBuffers();
                dirty = true;
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    logger.warn(e.getMessage(), e);
                }
            }

            stopped = stopped || keyboard.isPressed(KeyEvent.VK_ESCAPE);
        }

        Renderer.this.glWindow.destroy();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2ES2 gl = drawable.getGL().getGL2ES2();

        logger.info("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
        logger.info("INIT GL IS: " + gl.getClass().getName());
        logger.info("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
        logger.info("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
        logger.info("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));
        logger.info("GL_GLSL_VERSION: " + gl.glGetString(GL2.GL_SHADING_LANGUAGE_VERSION));

        int [] result = new int[1];
        gl.glGetIntegerv(GL2.GL_MAX_VERTEX_ATTRIBS, result, 0);
        logger.info("GL_MAX_VERTEX_ATTRIBS=" + result[0]);

        gl.setSwapInterval(1);

        textureProgram = new ShaderProgram(gl, Util.loadAsText(getClass(), "textureShader.vert"), Util.loadAsText(getClass(), "textureShader.frag"));

        textureProgram.bindAttributeLocation(1, "attribute_Position");
        textureProgram.bindAttributeLocation(0, "a_texCoord");

        uTexture = textureProgram.getUniformLocation("u_texture");
        uAlpha = textureProgram.getUniformLocation("alpha");
        uProjection = textureProgram.getUniformLocation("projection");
        uModelView = textureProgram.getUniformLocation("modelView");

        int[] tmpHandle = new int[1];
        gl.glGenBuffers(1, tmpHandle, 0);

        txtVertices = tmpHandle[0];

        // Select the VBO, GPU memory data, to use for vertices
        gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER, txtVertices);

        // transfer data to VBO, this perform the copy of data from CPU -> GPU memory
        gl.glBufferData(GL.GL_ARRAY_BUFFER, fbTxtVertices.limit() * 4, fbTxtVertices, GL.GL_STATIC_DRAW);

        try {
            long start1 = System.nanoTime();
            textures[0] = TextureIO.newTexture(new File("data/magma.jpg"), false);
            long start2 = System.nanoTime();
            textures[1] = TextureIO.newTexture(new File("data/dragons.jpg"), false);
            long start3 = System.nanoTime();
            logger.info("Load texture 1: {}ms", (start2-start1) / 1000000f);
            logger.info("Load texture 2: {}ms", (start3-start2) / 1000000f);
             start1 = System.nanoTime();
            textures[2] = TextureIO.newTexture(new File("data/eagles.jpg"), false);
             start2 = System.nanoTime();
            textures[3] = TextureIO.newTexture(new File("data/moonshade.jpg"), false);
             start3 = System.nanoTime();
            logger.info("Load texture 3: {}ms", (start2-start1) / 1000000f);
            logger.info("Load texture 4: {}ms", (start3-start2) / 1000000f);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        for (int i = 0; i < textures.length; i++) {
            // "Bind" the newly created texture : all future texture functions will modify this texture
            textures[i].bind(gl);

            // Poor filtering. Needed !
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NICEST);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NICEST);
        }

        source = textures[0];
        dest = textures[currentDest];

        gl.glViewport(0, 0, width, height);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {
        long frameStart = System.nanoTime();
        //logger.info("display+" + System.currentTimeMillis());

        if (faders[currentFader] != null && faders[currentFader].done() && keyboard.isPressed(KeyEvent.VK_SPACE)) {
            currentDest++;
            currentDest = currentDest % textures.length;
            dest = textures[currentDest];

            currentFader++;
            currentFader = currentFader % faders.length;
            faders[currentFader].reset();
        }

        GL2ES2 gl = drawable.getGL().getGL2ES2();

        /* Draw to screen */

        //gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
        gl.glViewport(0, 0, width, height);

        // Clear screen
        gl.glClearColor(0.3f, 0.0f, 0.3f, 0.5f);
        gl.glClear(GL2ES2.GL_COLOR_BUFFER_BIT);

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL2ES2.GL_BLEND);
        gl.glBlendFunc(GL2ES2.GL_SRC_ALPHA, GL2ES2.GL_ONE_MINUS_SRC_ALPHA);

        textureProgram.begin();

        //gl.glUniform1i(textureUniformLocation, 0);
        gl.glEnableVertexAttribArray(0);
        //gl.glEnableVertexAttribArray(1);

        // Select the VBO, GPU memory data, to use for vertices
        gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER, txtVertices);

        // Associate Vertex attribute 0 with the last bound VBO
        gl.glVertexAttribPointer(0 /* the vertex attribute */, 2,
                GL2ES2.GL_FLOAT, false /* normalized? */, 0 /* stride */,
                0 /* The bound VBO data offset */);

        double time = (System.currentTimeMillis() - start) / 1000.0;

        gl.glUniformMatrix4fv(uProjection, 1, false, projectionMatrix.get(), 0);

        if (faders[currentFader] == null || faders[currentFader].done()) {
            modelViewMatrix.setToIdentity();
            modelViewMatrix.scale(aspect, 1, 1);
            modelViewMatrix.translate(0,0,-1);

            gl.glUniform1f(uAlpha, 1.0f);
            gl.glUniformMatrix4fv(uModelView , 1, false, modelViewMatrix.get(),  0);

            source.bind(gl);

            gl.glDrawArrays(GL2ES2.GL_TRIANGLE_FAN, 0, 4); //Draw the vertices as triangle fan
        } else {
            faders[currentFader].update(0.016f);

            gl.glUniform1f(uAlpha, faders[currentFader].getSourceAlpha());
            gl.glUniformMatrix4fv(uModelView , 1, false, faders[currentFader].getSourceModelViewMatrix().get(),  0);

            source.bind(gl);

            gl.glDrawArrays(GL2ES2.GL_TRIANGLE_FAN, 0, 4);

            gl.glUniform1f(uAlpha, faders[currentFader].getDestinationAlpha());
            gl.glUniformMatrix4fv(uModelView , 1, false, faders[currentFader].getDestinationModelViewMatrix().get(),  0);

            dest.bind(gl);

            gl.glDrawArrays(GL2ES2.GL_TRIANGLE_FAN, 0, 4);

            if (faders[currentFader].done()) {
                source = dest;
            }
        }

//        modelViewMatrix.setToIdentity();
//        modelViewMatrix.scale(aspect, 1, 1);
//        modelViewMatrix.rotateZ((float)time*3);
//        modelViewMatrix.translate(0, 0, (float) (-2 + Math.sin(time)));
//        //modelViewMatrix.scale(aspect, 1, 1);
//
//        gl.glUniform1f(uAlpha, 1.0f);
//        gl.glUniformMatrix4fv(uModelView , 1, false, modelViewMatrix.get(),  0);
//
//        texture.bind(gl);
//
//        gl.glDrawArrays(GL2ES2.GL_TRIANGLE_FAN, 0, 4); //Draw the vertices as triangle fan

//        float alpha = (float) Math.abs(Math.sin(time));
//        //logger.info("Alpha: {}", alpha);
//
//        gl.glUniform1f(uAlpha, alpha);
//        gl.glUniformMatrix4fv(uModelView, 1, false, modelViewMatrix.get(), 0);
//
//        texture2.bind(gl);
//
//        gl.glDrawArrays(GL2ES2.GL_TRIANGLE_FAN, 0, 4); //Draw the vertices as triangle

        gl.glDisableVertexAttribArray(1);
        gl.glDisableVertexAttribArray(0); // Allow release of vertex position memory

        textureProgram.end();

        frameTime += (System.nanoTime() - frameStart);
        frameCount++;

        if (lastTiming < (System.currentTimeMillis() - 1000)) {
            logger.info("FPS: {}, drawing time/s: {}ns, per frame: {}", frameCount, frameTime, frameCount > 0 ? (frameTime/frameCount) : 0);

            frameTime  = 0L;
            frameCount = 0;
            lastTiming = System.currentTimeMillis();
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        logger.info("reshape+" + System.currentTimeMillis());

        this.width = w;
        this.height = h;
    }

}
