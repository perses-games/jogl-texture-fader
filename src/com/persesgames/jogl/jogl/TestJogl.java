package com.persesgames.jogl.jogl;

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

    public static void main(String [] args) {
        TestJogl test = new TestJogl();

        test.run();
    }

    private final Renderer renderer;

    public TestJogl() {
        GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2ES2));

        caps.setBackgroundOpaque(false);
        caps.setDoubleBuffered(true);

        GLWindow glWindow = GLWindow.create(caps);

        glWindow.setTitle("jogl-triangle");

        glWindow.setSize(1920, 1080);

        glWindow.setFullscreen(true);
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
