package webapp;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;

/**
 * Created by suat on 17-Jun-17.
 */
@Path("/")
public class RestApi {
    @Path("devicetoken")
    @POST
    @Consumes("text/plain")
    @Produces("text/plain")
    public String postDeviceToken() {
        return "Hello! It's ";
    }

    @Path("reaction")
    @POST
    @Consumes("text/plain")
    @Produces("text/plain")
    public String postReactionResult() {
        return "Hello!";
    }

    @Path("wifiname")
    @POST
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response postWifiName(String wifiNameJson) {
        System.out.println("wifi json: " + wifiNameJson);
        return Response.ok().build();
    }

    @Path("de")
    @GET
    @Produces("text/plain")
    public Response deneme() {
        return Response.ok().build();
    }
}
