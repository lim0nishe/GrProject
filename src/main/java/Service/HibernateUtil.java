package Service;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import java.io.FileInputStream;
import java.util.Properties;


public class HibernateUtil {

    private static SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {

        try{
            Properties properties = new Properties();

            // Tomcat
            properties.loadFromXML(new FileInputStream("/TomcatFiles/myProperties.properties"));

            // WildFly
            //properties.loadFromXML(new FileInputStream(System.getProperty("jboss.server.data.dir") +
            //        "/myProperties.properties"));

            Configuration configuration = new Configuration();

            configuration.configure().setProperty("hibernate.connection.username", properties.getProperty("connection.username"))
                    .setProperty("hibernate.connection.password", properties.getProperty("connection.password"))
                    .setProperty("hibernate.connection.url", properties.getProperty("connection.url"));


            ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(
                    configuration.getProperties()).buildServiceRegistry();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            return sessionFactory;
        }
        catch(Exception e){
            System.out.println("cant find properties :c");
            e.printStackTrace();
            return null;
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        // Close caches and connection pools
        getSessionFactory().close();
    }
}
