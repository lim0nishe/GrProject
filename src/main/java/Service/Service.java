package Service;


import Models.FTPServer;
import Models.User;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

/**
 * Created by Lem0n on 19.11.2016.
 */
@ApplicationScoped
@Path("/service")
public class Service {

    private static Logger logger = Logger.getLogger("simple");

    @GET
    @Produces("text/plain")
    public String Hello(){

        logger.info("Hello invoked");
        return "Hello service";
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(User user){
        ConnectionController controller = new ConnectionController();

        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        FTPServer server = user.getServer();


        controller.Connect(server.getAdminName(), server.getAdminPass(), server.getAddress());
        controller.CreateUser(user);

        // TODO: verify user creation by parsing configuration file

        // save user in DB
        session.save(user);
        session.getTransaction().commit();
        session.close();

        URI location = URI.create("/service" + user.getId());
        return Response.created(location).build();
    }

    @GET
    @Path("{id}")
    public User getUser(@PathParam("id") Long id){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        User user = (User)session.get(User.class, id);
        session.close();
        return user;
    }
}
