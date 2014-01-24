package com.persesgames.jogl.jogl;

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

    private ShaderProgram textureProgram;

    private final GLWindow glWindow;

    private float[]                 txtVerts = {
            -1.0f, -1.0f,  0.0f,
             1.0f, -1.0f,  0.0f,
             1.0f,  1.0f,  0.0f,
            -1.0f,  1.0f,  0.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
    };

    private FloatBuffer             fbTxtVertices       = Buffers.newDirectFloatBuffer(txtVerts);

    private int                     width = 100, height = 100;

    private int                     txtVertices;

    private Texture                 texture;
    private Texture                 texture2;

    private int                     textureUniformLocation;
    private int                     uAlpha;

    private Keyboard                keyboard;

    private int                     frameCount;
    private long                    frameTime;
    private long                    lastTiming;

    private long                    start = System.currentTimeMillis();

    public Renderer(GLWindow glWindow, Keyboard keyboard) {
        this.glWindow = glWindow;
        this.keyboard = keyboard;
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

        int [] result = new int[1];
        gl.glGetIntegerv(GL2.GL_MAX_VERTEX_ATTRIBS, result, 0);
        logger.info("GL_MAX_VERTEX_ATTRIBS=" + result[0]);

        gl.setSwapInterval(1);

        textureProgram = new ShaderProgram(gl, Util.loadAsText(getClass(), "textureShader.vert"), Util.loadAsText(getClass(), "textureShader.frag"));

        textureUniformLocation = textureProgram.getUniformLocation("u_texture");
        uAlpha = textureProgram.getUniformLocation("alpha");

        int[] tmpHandle = new int[1];
        gl.glGenBuffers(1, tmpHandle, 0);

        txtVertices = tmpHandle[0];

        // Select the VBO, GPU memory data, to use for vertices
        gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER, txtVertices);

        // transfer data to VBO, this perform the copy of data from CPU -> GPU memory
        gl.glBufferData(GL.GL_ARRAY_BUFFER, txtVerts.length * 4, fbTxtVertices, GL.GL_STATIC_DRAW);

        try {
            texture = TextureIO.newTexture(new File("data/eagle.jpg"), false);
            texture2 = TextureIO.newTexture(new File("data/magma.jpg"), false);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        // "Bind" the newly created texture : all future texture functions will modify this texture
        texture.bind(gl);

        // Poor filtering. Needed !
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        // "Bind" the newly created texture : all future texture functions will modify this texture

        texture2.bind(gl);

        // Poor filtering. Needed !
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {
        long frameStart = System.nanoTime();
        //logger.info("display+" + System.currentTimeMillis());

        GL2ES2 gl = drawable.getGL().getGL2ES2();

        /* Draw to screen */

        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
        gl.glViewport(0, 0, width, height);

        // Clear screen
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1f);
        gl.glClear(GL2ES2.GL_COLOR_BUFFER_BIT);

        gl.glEnable(GL2ES2.GL_BLEND);
        gl.glBlendFunc(GL2ES2.GL_SRC_ALPHA, GL2ES2.GL_ONE_MINUS_SRC_ALPHA);

        textureProgram.begin();

        //gl.glUniform1i(textureUniformLocation, 0);

        // Select the VBO, GPU memory data, to use for vertices
        gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER, txtVertices);

        // Associate Vertex attribute 0 with the last bound VBO
        gl.glVertexAttribPointer(0 /* the vertex attribute */, 3,
                GL2ES2.GL_FLOAT, false /* normalized? */, 0 /* stride */,
                0 /* The bound VBO data offset */);

        // Associate Vertex attribute 0 with the last bound VBO
        gl.glVertexAttribPointer(1 /* the vertex attribute */, 2,
                GL2ES2.GL_FLOAT, false /* normalized? */, 0 /* stride */,
                48 /* The bound VBO data offset */);

        gl.glEnableVertexAttribArray(0);
        gl.glEnableVertexAttribArray(1);

        gl.glUniform1f(uAlpha, 1.0f);
        texture.bind(gl);

        gl.glDrawArrays(GL2ES2.GL_TRIANGLE_FAN, 0, 4); //Draw the vertices as triangle

        double time = (System.currentTimeMillis() - start) / 1000.0;
        float alpha = (float) Math.abs(Math.sin(time));
        logger.info("Alpha: {}", alpha);

        gl.glUniform1f(uAlpha, alpha);

        texture2.bind(gl);

        gl.glDrawArrays(GL2ES2.GL_TRIANGLE_FAN, 0, 4); //Draw the vertices as triangle

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
