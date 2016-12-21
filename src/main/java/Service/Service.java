package Service;


import Models.User;
import org.apache.log4j.Logger;

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

    // TODO: put this into DB
    private static String ADMIN_NAME = "andrey";
    private static String ADMIN_PASS = "adamova";
    private static String SERVER_URL = "";

    private static Logger logger = Logger.getLogger("simple");

    @GET
    @Produces("text/plain")
    public String Hello(){
        logger.info("Hello invoked");
        return "Hello service";
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(User user){
        ConnectionController controller = new ConnectionController();
        // TODO: take server data from DB
        controller.Connect(ADMIN_NAME, ADMIN_PASS, SERVER_URL);
        controller.CreateUser(user);

        // TODO: verify user creation by parsing configuration file

        URI location = URI.create("/service" + user.getId());
        return Response.created(location).build();
    }

    @GET
    @Path("{id}")
    public User getUser(@PathParam("id") Long id){
        // TODO: take user from DB by uuid
        User user = new User();
        return user;
    }
}
