package com.persesgames.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: rnentjes
 * Date: 3/28/12
 * Time: 3:05 PM
 */
public class CommandServlet extends HttpServlet {
    private final static Logger logger = LoggerFactory.getLogger(CommandServlet.class);

    private static long startup = System.currentTimeMillis();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getRequestURI().startsWith("/load")) {
            String url = req.getParameter("url");
            String texture = req.getParameter("texture");

            logger.info("URL: {}", url);
        } else if (req.getRequestURI().startsWith("/switch")) {
            String target = req.getParameter("target");
            String fader = req.getParameter("fader");

        }
    }

}
