package Service;

import javax.servlet.*;
import java.io.File;
import java.io.InputStream;

import org.apache.log4j.PropertyConfigurator;

public class Log4jInit implements ServletContextListener {

    public void contextInitialized(ServletContextEvent event) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try(InputStream resourceStream = classLoader.getResourceAsStream("log4j.properties")){
            PropertyConfigurator.configure(resourceStream);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void contextDestroyed(ServletContextEvent event) {}
}