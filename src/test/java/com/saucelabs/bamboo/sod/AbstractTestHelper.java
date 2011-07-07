package com.saucelabs.bamboo.sod;

import org.junit.BeforeClass;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author Ross Rowe
 */
public abstract class AbstractTestHelper extends HttpServlet {
    
    protected static final String DEFAULT_SAUCE_DRIVER = "sauce-ondemand:?max-duration=30&os=windows 2008&browser=firefox&browser-version=4.";
    public static int code;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        resp.getWriter().println("<html><head><title>test" + code + "</title></head><body>it works</body></html>");
    }
    
    @BeforeClass
    public static void loadProperties() throws Exception {
        InputStream stream = AbstractTestHelper.class.getClassLoader().getResourceAsStream("test.properties");
        Properties properties = new Properties();
        properties.load(stream);
        for (Map.Entry property : properties.entrySet()) {
            System.setProperty((String) property.getKey(), (String) property.getValue());
        }
    }
    
    
    
}
