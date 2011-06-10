package com.saucelabs.bamboo.sod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Ross Rowe
 */
public abstract class AbstractTestHelper extends HttpServlet {
    protected int code;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        resp.getWriter().println("<html><head><title>test" + code + "</title></head><body>it works</body></html>");
    }
    protected static final String DEFAULT_SAUCE_DRIVER = "sauce-ondemand:?max-duration=30&os=windows 2008&browser=firefox&browser-version=4.";
}
