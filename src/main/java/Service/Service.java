package Service;


import Metric.MetricController;
import Models.FTPServer;
import Models.User;
import com.jcraft.jsch.JSchException;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.hibernate.Session;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Stream;


@ApplicationScoped
@Path("/service")
public class Service {

    // class uses token authorization

    private static Logger logger = Logger.getLogger("file");

    @GET
    @Produces("text/plain")
    public String Hello() {

        logger.info("Hello invoked");
        return "Hello service";
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public String createUser(@Context HttpServletRequest request, String JsonString) {
        try {
            logger.info("createUser invoked");

            Properties properties = new Properties();
            properties.loadFromXML(new FileInputStream("/TomcatFiles/myProperties.properties"));

            FrontEndSessionController FESController = new FrontEndSessionController();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> requestMap = mapper.readValue(JsonString, new TypeReference<Map<String, Object>>() {
            });

            // check session token
            if (requestMap.containsKey("sessionToken")) {
                if (!FESController.isActive(request.getRemoteAddr(), requestMap.get("sessionToken").toString()))
                    return JsonResponse.SESSION_EXPIRED_ERROR;
            } else {
                logger.info("authorization failed");
                return JsonResponse.AUTHORIZATION_ERROR;
            }

            // validate password and username
            StringValidator validator = new StringValidator();
            if ((!validator.validateUserName(requestMap.get("username").toString())) ||
                    !validator.validatePassword(requestMap.get("password").toString()))
                return JsonResponse.VALIDATION_ERROR;

            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            FTPServer server = (FTPServer) session.get(FTPServer.class, Long.parseLong(requestMap.get("ServerId").toString()));
            User user = new User(requestMap.get("username").toString(), requestMap.get("password").toString(), server,
                    requestMap.get("quota").toString());


            logger.info("db");
            // save user in DB
            session.save(user);

            ConnectionController controller = new ConnectionController();

            // create user on FTP server and validate creation
            controller.keyConnect(server.getAdminName(), server.getAddress(), properties.getProperty("ftpServer"));
            //controller.Connect(server.getAdminName(), server.getAdminPass(), server.getAddress());
            controller.CreateUser(user);

            if (!controller.validateCreation(user)) {
                session.close();
                controller.closeSession();
                return JsonResponse.USER_CREATION_ERROR;
            }
            session.getTransaction().commit();
            session.close();
            controller.closeSession();
            return JsonResponse.USER_CREATION_SUCCESS;
        } catch (JSchException jE) {
            logger.error("error while opening session");
            jE.printStackTrace();
            return JsonResponse.JSCH_CONNECTION_ERROR;
        } catch (IOException e) {
            logger.error("error in converting JsonString to User");
            e.printStackTrace();
            return JsonResponse.JSON_CONVERTATION_ERROR;
        }
    }

    @POST
    @Produces("application/json")
    @Consumes("application/json")
    @Path("/user")
    public String getUser(@Context HttpServletRequest request, String JsonString) {
        try {
            FrontEndSessionController FESController = new FrontEndSessionController();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> requestMap = mapper.readValue(JsonString, new TypeReference<Map<String, Object>>() {
            });

            // check session token
            if (requestMap.containsKey("sessionToken")) {
                if (!FESController.isActive(request.getRemoteAddr(), requestMap.get("sessionToken").toString()))
                    return JsonResponse.SESSION_EXPIRED_ERROR;
            } else {
                logger.info("authorization failed");
                return JsonResponse.AUTHORIZATION_ERROR;
            }

            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            User user = (User) session.get(User.class, (Long) (requestMap.get("id")));
            session.close();
            return JsonResponse.createUserResponse(user);
        } catch (IOException e) {
            e.printStackTrace();
            return JsonResponse.JSON_CONVERTATION_ERROR;
        }
    }

    @POST
    @Produces("application/json")
    @Consumes("application/json")
    @Path("/serverRanks")
    public String getServerRanks(@Context HttpServletRequest request, String JsonString) {
        try {
            FrontEndSessionController FESController = new FrontEndSessionController();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> requestMap = mapper.readValue(JsonString, new TypeReference<Map<String, Object>>() {
            });

            // check session token
            if (requestMap.containsKey("sessionToken")) {
                if (!FESController.isActive(request.getRemoteAddr(), requestMap.get("sessionToken").toString()))
                    return JsonResponse.SESSION_EXPIRED_ERROR;
            } else {
                logger.info("authorization failed");
                return JsonResponse.AUTHORIZATION_ERROR;
            }

            // calculate metric for servers
            MetricController metricController = new MetricController();
            Map<FTPServer, Double> serversGrades = metricController.getServersGrades(requestMap.get("quota").toString());

            // sort map by value
            ArrayList<Entry<FTPServer, Double>> sortedServersGrades = new ArrayList<>();
            Stream<Entry<FTPServer, Double>> stream = serversGrades.entrySet().stream();
            stream.sorted(Comparator.comparing(e -> e.getValue())).forEach(e ->
                    sortedServersGrades.add(new SimpleEntry<>(e.getKey(), e.getValue())));
            stream.close();
            return JsonResponse.createServerGradesResponse(sortedServersGrades);

        } catch (IOException e1) {
            e1.printStackTrace();
            return JsonResponse.JSON_CONVERTATION_ERROR;
        }
    }

    @POST
    @Produces("application/json")
    @Consumes("application/json")
    @Path("/servers")
    public String getServers(@Context HttpServletRequest request, String JsonString) {
        try {
            FrontEndSessionController FESController = new FrontEndSessionController();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> requestMap = mapper.readValue(JsonString, new TypeReference<Map<String, Object>>() {
            });

            // check session token
            if (requestMap.containsKey("sessionToken")) {
                if (!FESController.isActive(request.getRemoteAddr(), requestMap.get("sessionToken").toString()))
                    return JsonResponse.SESSION_EXPIRED_ERROR;
            } else {
                logger.info("authorization failed");
                return JsonResponse.AUTHORIZATION_ERROR;
            }

            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            List<FTPServer> servers = session.createCriteria(FTPServer.class).list();

            String response = JsonResponse.createServersResponse(servers);
            session.close();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return JsonResponse.JSON_CONVERTATION_ERROR;
        }
    }

    @POST
    @Produces("application/json")
    @Consumes("application/json")
    @Path("/server")
    public String getServerUsers(@Context HttpServletRequest request, String JsonString) {
        try {

            FrontEndSessionController FESController = new FrontEndSessionController();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> requestMap = mapper.readValue(JsonString, new TypeReference<Map<String, Object>>() {
            });

            // check session token
            if (requestMap.containsKey("sessionToken")) {
                if (!FESController.isActive(request.getRemoteAddr(), requestMap.get("sessionToken").toString()))
                    return JsonResponse.SESSION_EXPIRED_ERROR;
            } else {
                logger.info("authorization failed");
                return JsonResponse.AUTHORIZATION_ERROR;
            }

            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            FTPServer server = (FTPServer) session.get(FTPServer.class, requestMap.get("serverId").toString());
            List<User> users = server.getUsers();

            ConnectionController controller = new ConnectionController();
            controller.keyConnect(server.getAdminName(), server.getAddress(), "proftpd");
            String freeSpace = controller.checkFreeSpace(server);

            String response = JsonResponse.createUserResponse(users, freeSpace);
            session.close();
            return response;
        } catch (JSchException jE) {
            logger.error("error while opening session");
            jE.printStackTrace();
            return JsonResponse.JSCH_CONNECTION_ERROR;
        } catch (IOException e) {
            e.printStackTrace();
            return JsonResponse.JSON_CONVERTATION_ERROR;
        }

    }

    @POST
    @Produces("application/json")
    @Consumes("application/json")
    @Path("/users")
    public String getUsers(@Context HttpServletRequest request, String JsonString) {
        try {

            FrontEndSessionController FESController = new FrontEndSessionController();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> requestMap = mapper.readValue(JsonString, new TypeReference<Map<String, Object>>() {
            });

            // check session token
            if (requestMap.containsKey("sessionToken")) {
                if (!FESController.isActive(request.getRemoteAddr(), requestMap.get("sessionToken").toString()))
                    return JsonResponse.SESSION_EXPIRED_ERROR;
            } else {
                logger.info("authorization failed");
                return JsonResponse.AUTHORIZATION_ERROR;
            }

            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            List<User> users = session.createCriteria(User.class).list();
            session.close();

            return JsonResponse.createUserResponse(users);
        } catch (IOException e) {
            e.printStackTrace();
            return JsonResponse.JSON_CONVERTATION_ERROR;
        }
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @Path("/auth")
    public String activateSession(@Context HttpServletRequest request, String JsonString) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            FrontEndSessionController FESController = new FrontEndSessionController();
            Map<String, Object> loginMap = mapper.readValue(JsonString, new TypeReference<Map<String, Object>>() {
            });
            return FESController.activateSession(request.getRemoteAddr(), loginMap.get("login").toString(),
                    loginMap.get("password").toString());
        } catch (IOException e) {
            logger.error("error while activating session");
            e.printStackTrace();
            return JsonResponse.AUTHORIZATION_ERROR;
        }
    }
}
