package com.persesgames.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * Date: 1/26/14
 * Time: 9:31 PM
 */
public class WebServer {

    private int port;
    private Server server;

    public WebServer(int port) {
        this.port = port;

        new Thread() {

            @Override
            public void run() {
                try {
                    startJetty();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void startJetty() throws Exception {
        server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);

        context.setContextPath("/");

        server.setHandler(context);

        context.addServlet(ResourceServlet.class, "/data/*");
        context.addServlet(CommandServlet.class, "/*");

        server.start();
    }


    public void stop() throws Exception {
        server.stop();
    }


}
