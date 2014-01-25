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

    private float aspect;

    private ShaderProgram textureProgram;

    private final GLWindow glWindow;

    private float[]                 txtVerts = {
            -1.0f, -1.0f,   0.0f,
             1.0f, -1.0f,   0.0f,
             1.0f,  1.0f,   0.0f,
            -1.0f,  1.0f,   0.0f,
             0.0f,  0.0f,
             1.0f,  0.0f,
             1.0f,  1.0f,
             0.0f,  1.0f,
    };

    private float []                projectionMatrix;
    private float []                modelViewMatrix = {
            1.0f,0.0f,0.0f,0.0f,
            0.0f,1.0f,0.0f,0.0f,
            0.0f,0.0f,1.0f,0.0f,
            0.0f,0.0f,0.0f,1.0f,
    };

    private FloatBuffer             fbTxtVertices       = Buffers.newDirectFloatBuffer(txtVerts);

    private int                     width = 100, height = 100;

    private int                     txtVertices;

    private Texture                 texture;
    private Texture                 texture2;

    private int                     textureUniformLocation;
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
        this.projectionMatrix = setPerspectiveProjection(90f, aspect, 1.0f, 50.0f);
    }

    public void setIdentityMatrix(float [] matrix) {
        assert matrix.length == 16;

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

    public void mul4x4(float [] a, float [] b, float [] result) {
        assert a.length == 16;
        assert b.length == 16;
        assert result.length == 16;

        result[ 0] =   a[ 0] * b[ 0] + a[ 1] * b[ 4] + a[ 2] * b[ 8] + a[ 3] * b[12];
        result[ 1] =   a[ 0] * b[ 1] + a[ 1] * b[ 5] + a[ 2] * b[ 9] + a[ 3] * b[13];
        result[ 2] =   a[ 0] * b[ 2] + a[ 1] * b[ 6] + a[ 2] * b[10] + a[ 3] * b[14];
        result[ 3] =   a[ 0] * b[ 3] + a[ 1] * b[ 7] + a[ 2] * b[11] + a[ 3] * b[15];
        result[ 4] =   a[ 4] * b[ 0] + a[ 5] * b[ 4] + a[ 6] * b[ 8] + a[ 7] * b[12];
        result[ 5] =   a[ 4] * b[ 1] + a[ 5] * b[ 5] + a[ 6] * b[ 9] + a[ 7] * b[13];
        result[ 6] =   a[ 4] * b[ 2] + a[ 5] * b[ 6] + a[ 6] * b[10] + a[ 7] * b[14];
        result[ 7] =   a[ 4] * b[ 3] + a[ 5] * b[ 7] + a[ 6] * b[11] + a[ 7] * b[15];
        result[ 8] =   a[ 8] * b[ 0] + a[ 9] * b[ 4] + a[10] * b[ 8] + a[11] * b[12];
        result[ 9] =   a[ 8] * b[ 1] + a[ 9] * b[ 5] + a[10] * b[ 9] + a[11] * b[13];
        result[10] =   a[ 8] * b[ 2] + a[ 9] * b[ 6] + a[10] * b[10] + a[11] * b[14];
        result[11] =   a[ 8] * b[ 3] + a[ 9] * b[ 7] + a[10] * b[11] + a[11] * b[15];
        result[12] =   a[12] * b[ 0] + a[13] * b[ 4] + a[14] * b[ 8] + a[15] * b[12];
        result[13] =   a[12] * b[ 1] + a[13] * b[ 5] + a[14] * b[ 9] + a[15] * b[13];
        result[14] =   a[12] * b[ 2] + a[13] * b[ 6] + a[14] * b[10] + a[15] * b[14];
        result[15] =   a[12] * b[ 3] + a[13] * b[ 7] + a[14] * b[11] + a[15] * b[15];
    }

    private float [] translateMatrix = new float [] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    };

    public void translate(float [] matrix, float x, float y, float z) {
        assert matrix.length == 16;

        translateMatrix[12] = x;
        translateMatrix[13] = y;
        translateMatrix[14] = z;

        mul4x4(matrix, translateMatrix, matrix);
    }

    private float [] scaleMatrix = new float [] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    };

    public void scale(float [] matrix, float x, float y, float z) {
        assert matrix.length == 16;

        scaleMatrix[0] = x;
        scaleMatrix[5] = y;
        scaleMatrix[10] = z;

        mul4x4(matrix, scaleMatrix, matrix);
    }

    private float [] rotateXMatrix = new float [] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    };

    public void rotateX(float [] matrix, float angle) {
        assert matrix.length == 16;

        rotateXMatrix[5] = (float)Math.cos(angle);
        rotateXMatrix[6] = (float)-Math.sin(angle);
        rotateXMatrix[9] = (float)Math.sin(angle);
        rotateXMatrix[10] = (float)Math.cos(angle);

        mul4x4(matrix, rotateXMatrix, matrix);
    }

    private float [] rotateYMatrix = new float [] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    };

    public void rotateY(float [] matrix, float angle) {
        assert matrix.length == 16;

        rotateYMatrix[ 0] = (float)Math.cos(angle);
        rotateYMatrix[ 2] = (float)Math.sin(angle);
        rotateYMatrix[ 8] = (float)-Math.sin(angle);
        rotateYMatrix[10] = (float)Math.cos(angle);

        mul4x4(matrix, rotateYMatrix, matrix);
    }

    private float [] rotateZMatrix = new float [] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    };

    public void rotateZ(float [] matrix, float angle) {
        assert matrix.length == 16;

        rotateZMatrix[ 0] = (float)Math.cos(angle);
        rotateZMatrix[ 1] = (float)Math.sin(angle);
        rotateZMatrix[ 4] = (float)-Math.sin(angle);
        rotateZMatrix[ 5] = (float)Math.cos(angle);

        mul4x4(matrix, rotateZMatrix, matrix);
    }

    public float [] setPerspectiveProjection(float angle, float imageAspectRatio, float near, float far) {
        float [] matrix = new float[16];

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

        return matrix;
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
        uProjection = textureProgram.getUniformLocation("projection");
        uModelView = textureProgram.getUniformLocation("modelView");

        int[] tmpHandle = new int[1];
        gl.glGenBuffers(1, tmpHandle, 0);

        txtVertices = tmpHandle[0];

        // Select the VBO, GPU memory data, to use for vertices
        gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER, txtVertices);

        // transfer data to VBO, this perform the copy of data from CPU -> GPU memory
        gl.glBufferData(GL.GL_ARRAY_BUFFER, txtVerts.length * 4, fbTxtVertices, GL.GL_STATIC_DRAW);

        try {
            texture = TextureIO.newTexture(new File("data/eagle.jpg"), false);
            texture2 = TextureIO.newTexture(new File("data/dragon.jpg"), false);
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
        gl.glEnableVertexAttribArray(0);
        gl.glEnableVertexAttribArray(1);

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

        double time = (System.currentTimeMillis() - start) / 1000.0;

        gl.glUniformMatrix4fv(uProjection, 1, false, projectionMatrix, 0);

        setIdentityMatrix(modelViewMatrix);
        //rotateZ(modelViewMatrix, (float)time);
        scale(modelViewMatrix, aspect, 1, 1);
        translate(modelViewMatrix, 0, 0, -1);

        //translate(modelViewMatrix, 0, 0, -1);

        gl.glUniform1f(uAlpha, 1.0f);
        gl.glUniformMatrix4fv(uModelView , 1, false, modelViewMatrix,  0);

        texture.bind(gl);

        gl.glDrawArrays(GL2ES2.GL_TRIANGLE_FAN, 0, 4); //Draw the vertices as triangle

        float alpha = (float) Math.abs(Math.sin(time));

        gl.glUniform1f(uAlpha, alpha);
        gl.glUniformMatrix4fv(uModelView , 1, false, modelViewMatrix,  0);

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
