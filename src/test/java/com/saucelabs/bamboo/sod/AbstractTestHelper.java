package com.saucelabs.bamboo.sod;

import com.saucelabs.rest.Credential;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.BeforeClass;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

/**
 * @author Ross Rowe
 */
public abstract class AbstractTestHelper extends HttpServlet {

    protected static final String DEFAULT_SAUCE_DRIVER = "sauce-ondemand:?max-duration=60&os=windows 2008&browser=firefox&browser-version=4.";
    public static int code;
    private Server server;

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
        File sauceSettings = new File(new File(System.getProperty("user.home")), ".sauce-ondemand");
        if (!sauceSettings.exists()) {
            String userName = System.getProperty("sauce.user");
            String accessKey = System.getProperty("access.key");
            if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(accessKey)) {
                Credential credential = new Credential(userName, accessKey);
                credential.saveTo(sauceSettings);
            }
        }
    }

    protected Server startWebServer() throws Exception {
        this.code = new Random().nextInt();

        // start the Jetty locally and have it respond our secret code.
        this.server = new Server(5000);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(this), "/*");

        server.start();
        System.out.println("Started Jetty at 8080");
        return server;
    }


}
