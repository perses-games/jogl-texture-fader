package com.persesgames.web;

import com.persesgames.util.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: rnentjes
 * Date: 3/28/12
 * Time: 3:05 PM
 */
public class ResourceServlet extends HttpServlet {

    private static long startup = System.currentTimeMillis();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();

        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }

        if (uri.startsWith(req.getContextPath())) {
            uri = uri.substring(req.getContextPath().length());
        }

        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }

        File file = new File(uri);
        File webPath = new File("data");

        if (!file.exists()) {
            resp.sendError(404, "Cannot find resource '" + uri + "'.");
        } else if (file.getCanonicalPath().startsWith(webPath.getCanonicalPath())) {
            if (uri.endsWith("js")) {
                resp.setContentType("text/javascript");
            } else if (uri.endsWith("css")) {
                resp.setContentType("text/css");
            } else if (uri.endsWith("png")) {
                resp.setContentType("image/png");
            } else if (uri.endsWith("jpg")) {
                resp.setContentType("image/jpeg");
            } else if (uri.endsWith("gif")) {
                resp.setContentType("image/gif");
            }

            resp.setHeader("Cache-Control", "max-age=3600");
            resp.setHeader("ETag", Long.toHexString(startup));

            try (InputStream in = new FileInputStream(file)) {
                IOUtils.copy(in, resp.getOutputStream());
            }
        } else {
            resp.sendError(403);
        }
    }

}
