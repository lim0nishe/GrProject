package Service;


import Models.FTPServer;
import Models.User;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.hibernate.Session;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;


/**
 * Created by Lem0n on 19.11.2016.
 */
@ApplicationScoped
@Path("/service")
public class Service {

    private static Logger logger = Logger.getLogger("simple");
    private String createUsersResponse(List<User> users){
        StringBuilder result = new StringBuilder();
        for (User user : users){
            result.append(user.getId());
            result.append(' ');
            result.append(user.getName());
            result.append('\n');
        }
        return result.toString();
    }

    @GET
    @Produces("text/plain")
    public String Hello(){

        logger.info("Hello invoked");
        return "Hello service";
    }

    @POST
    @Consumes("application/json")
    public Response createUser(String JsonString){
        try {
            logger.info("createUser invoked");
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> userMap = mapper.readValue(JsonString, new TypeReference<Map<String, Object>>(){});

            StringValidator validator = new StringValidator();
            if ((!validator.validateUserName(userMap.get("username").toString())) ||
                    !validator.validatePassword(userMap.get("password").toString()))
                return null;
            // TODO: add correct responses to frontend

            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            FTPServer server = (FTPServer)session.get(FTPServer.class, Long.parseLong(userMap.get("ServerId").toString()));
            User user = new User(userMap.get("username").toString(), userMap.get("password").toString(), server);


            logger.info("db");
            // save user in DB
            session.save(user);
            session.getTransaction().commit();
            session.close();

            // у пользователя нет id, пока мы не сохранили его в бд
            // по хорошему, надо как-то это обойти
            ConnectionController controller = new ConnectionController();

            controller.Connect(server.getAdminName(), server.getAdminPass(), server.getAddress());
            controller.CreateUser(user);

            // TODO: add correct responses to frontend
            if(!controller.validateCreation(user))
                return null;

            URI location = URI.create("/def/service/user" + user.getId());
            return Response.created(location).build();
        }
        catch (IOException e){
            logger.error("error in converting JsonString to User");
            e.printStackTrace();
            return null;
        }
    }

    @GET
    @Produces("text/plain")
    @Path("/user{id}")
    public String getUser(@PathParam("id") Long id){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        User user = (User)session.get(User.class, id);
        session.close();
        return user.getName();
    }

    @GET
    @Produces("text/plain")
    @Path("/servers")
    public String getServers(){
        // TODO: продумать оформление

        StringBuilder result = new StringBuilder();

        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        List<FTPServer> servers = session.createCriteria(FTPServer.class).list();
        session.close();

        for (FTPServer server : servers){
            result.append(server.getId());
            result.append(' ');
            result.append(server.getAddress());
            result.append('\n');
        }
        return result.toString();
    }

    @GET
    @Produces("text/plain")
    @Path("/server{id}")
    public String getServerUsers(@PathParam("id") Long serverId){

        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        List<User> users = ((FTPServer)session.get(FTPServer.class, serverId)).getUsers();
        session.close();


        return createUsersResponse(users);
    }

    @GET
    @Produces("text/plain")
    @Path("/users")
    public String getUsers(){

        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        List<User> users = session.createCriteria(User.class).list();
        session.close();

        return createUsersResponse(users);
    }
}
