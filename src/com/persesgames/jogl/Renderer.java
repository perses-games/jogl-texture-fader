package com.persesgames.jogl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.opengl.GLWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.opengl.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.zip.GZIPInputStream;

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

    private int                     source;
    private int                     dest;
    private int []                  textures = new int[4];
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

        faders = new Fader[5];

        faders[0] = new SlideFader( SlideFader.SlideDirection.RIGHT);
        faders[1] = new ZoomInFader();
        faders[2] = new RotateInFader();
        faders[3] = new RotateInFader2();
        faders[4] = new TestFader();

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

    public int loadRawTexture(GL gl, int width, int height, InputStream input) throws IOException {
        int [] handles = new int[1];

        gl.glGenTextures(1, handles, 0);

        int result = handles[0];

        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GLES2.GL_TEXTURE_2D, result);

        int imageSize = width * height * 3;
        byte [] buffer = new byte[imageSize];

        try (BufferedInputStream in = new BufferedInputStream(input)) {
            int readBytes = in.read(buffer);

            assert readBytes == imageSize;

            ByteBuffer data = ByteBuffer.wrap(buffer);

            gl.glTexImage2D(GLES2.GL_TEXTURE_2D, 0, GLES2.GL_RGB, width, height, 0, GLES2.GL_RGB, GLES2.GL_UNSIGNED_BYTE, data);

            logger.info("Loading texture: {}", gl.glGetError());

//            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
//            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

            // Poor filtering. Needed !
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);

            return result;
        }
    }

    public int loadETC1Texture(GL gl, InputStream input) throws IOException {
        int [] handles = new int[1];

        gl.glGenTextures(1, handles, 0);

        int result = handles[0];
        //textures[0] = TextureIO.newTexture(image1, false, "jpg");

        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GLES2.GL_TEXTURE_2D, result);

        byte [] buffer = new byte[1<<16];

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(input)))) {
            int fileSize = in.readInt();
            ByteBuffer compressedData = ByteBuffer.allocate(fileSize);
            compressedData.order(ByteOrder.BIG_ENDIAN);
            int readBytes = 0;
            while ((readBytes = in.read(buffer)) != -1) {
                compressedData.put(buffer, 0, readBytes);//Exception occurs here
            }
            compressedData.position(0);
            compressedData.limit(compressedData.capacity());

            // 8 * ((2048 + 3) >> 2) * ((1024 + 3) >> 2);
            int uiSize = 8 * 512 * 256;

            gl.glCompressedTexImage2D(GLES2.GL_TEXTURE_2D, 0, GLES2.GL_ETC1_RGB8_OES, 2048, 1024, 0, uiSize, compressedData);

            logger.info("Loading compressed texture: {}", gl.glGetError());

            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

            // Poor filtering. Needed !
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);

            return result;
        }
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GLES2 gl = drawable.getGL().getGLES2();

        logger.info("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
        logger.info("INIT GL IS: " + gl.getClass().getName());
        logger.info("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
        logger.info("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
        logger.info("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));
        logger.info("GL_GLSL_VERSION: " + gl.glGetString(GL2.GL_SHADING_LANGUAGE_VERSION));

        int [] result = new int[1];
        gl.glGetIntegerv(GLES2.GL_MAX_VERTEX_ATTRIBS, result, 0);
        logger.info("GL_MAX_VERTEX_ATTRIBS=" + result[0]);

        gl.glGetIntegerv(GLES2.GL_MAX_TEXTURE_SIZE, result, 0);
        logger.info("GL_MAX_TEXTURE_SIZE=" + result[0]);

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
            textures[0] = loadRawTexture(gl, 2048, 1024, new FileInputStream("data/magma.raw"));
            //textures[0] = TextureIO.newTexture(new File("data/magma.jpg"), false);
            long start2 = System.nanoTime();
            logger.info("Load texture 1: {}ms", (start2-start1) / 1000000f);

            textures[1] = loadRawTexture(gl, 1920, 1080, new FileInputStream("data/dragons.data"));
            //textures[1] = TextureIO.newTexture(new File("data/dragons.jpg"), false);
            long start3 = System.nanoTime();
            logger.info("Load texture 2: {}ms", (start3-start2) / 1000000f);

            start1 = System.nanoTime();
            textures[2] = loadRawTexture(gl, 2048, 1024, new FileInputStream("data/eagles.raw"));
            //textures[2] = TextureIO.newTexture(new File("data/eagles.jpg"), false);
            start2 = System.nanoTime();
            logger.info("Load texture 3: {}ms", (start2-start1) / 1000000f);

            textures[3] = loadRawTexture(gl, 2048, 1024, new FileInputStream("data/moonshade.raw"));
            //textures[3] = TextureIO.newTexture(new File("data/moonshade.jpg"), false);
            start3 = System.nanoTime();
            logger.info("Load texture 4: {}ms", (start3-start2) / 1000000f);

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        source = textures[0];
        dest = textures[currentDest];
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    private long lastDeltaCalc = System.nanoTime();
    private float calculateDelta() {
        long result = System.nanoTime() - lastDeltaCalc;
        lastDeltaCalc = System.nanoTime();
        return (float)result / 1000000000f;
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        float delta = calculateDelta();
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

        GLES2 gl = drawable.getGL().getGLES2();

        /* Draw to screen */
        gl.glViewport(0, 0, width, height);
        aspect = (float)width / (float)height;
        this.projectionMatrix.setPerspectiveProjection(90f, aspect, 0.999f, 50.0f);

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
            modelViewMatrix.translate(0,0,-1f);

            gl.glUniform1f(uAlpha, 1.0f);
            gl.glUniformMatrix4fv(uModelView , 1, false, modelViewMatrix.get(),  0);

            gl.glBindTexture(GLES2.GL_TEXTURE_2D, source);

            gl.glDrawArrays(GL2ES2.GL_TRIANGLE_FAN, 0, 4); //Draw the vertices as triangle fan
        } else {
            faders[currentFader].update(delta, aspect);

            gl.glUniform1f(uAlpha, faders[currentFader].getSourceAlpha());
            gl.glUniformMatrix4fv(uModelView , 1, false, faders[currentFader].getSourceModelViewMatrix().get(),  0);

            gl.glBindTexture(GLES2.GL_TEXTURE_2D, source);

            gl.glDrawArrays(GL2ES2.GL_TRIANGLE_FAN, 0, 4);

            gl.glUniform1f(uAlpha, faders[currentFader].getDestinationAlpha());
            gl.glUniformMatrix4fv(uModelView , 1, false, faders[currentFader].getDestinationModelViewMatrix().get(),  0);

            gl.glBindTexture(GLES2.GL_TEXTURE_2D, dest);

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
        logger.info("reshape {},{} {},{}", x, y, w, h);

        this.width = w;
        this.height = h;
    }

}
