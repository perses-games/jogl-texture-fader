package com.persesgames.jogl;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;

/**
 * Date: 10/25/13
 * Time: 7:27 PM
 */
public class TestJogl {

    public static void main(String [] args) throws Exception {
//        WebServer server = new WebServer(8901);

        boolean debug = args.length > 0 && args[0].equals("true");
        TestJogl test = new TestJogl(debug);

        test.run();

//        server.stop();
    }

    private final Renderer renderer;

    public TestJogl(boolean debug) {
        GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GLES2));

        caps.setBackgroundOpaque(true);
        caps.setDoubleBuffered(true);

        GLWindow glWindow = GLWindow.create(caps);

        glWindow.setTitle("jogl-triangle");

        if (debug) {
            glWindow.setSize(1920/2, 1080/2);
            glWindow.setFullscreen(false);
        } else {
            glWindow.setSize(glWindow.getWidth(), glWindow.getHeight());
            glWindow.setFullscreen(true);
        }

        glWindow.setUndecorated(true);
        glWindow.setPointerVisible(false);
        glWindow.setVisible(true);

        Keyboard keyboard = new Keyboard();
        glWindow.addKeyListener(keyboard);

        renderer = new Renderer(glWindow, keyboard);

        glWindow.addGLEventListener(renderer);

        glWindow.addWindowListener(new WindowAdapter() {
            public void windowDestroyNotify(WindowEvent arg0) {
                renderer.stop();
            }
        });
    }

    public void run() {
        renderer.run();
    }

}
