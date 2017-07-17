package Service;

import Models.FTPServer;
import Models.User;

import java.util.List;
import java.util.Map.Entry;


public class JsonResponse {

    // class for creating JSON responses

    static final String VALIDATION_ERROR = "{ \"status\" : \"error\"," +
            " \"message\" : \"username or password is not valid\" }";
    static final String USER_CREATION_ERROR = "{ \"status\" : \"error\"," +
            " \"message\" : \"error while creating user\" }";
    static final String USER_CREATION_SUCCESS = "{ \"status\" : \"success\"," +
            " \"message\" : \"user creation success\" }";
    static final String JSON_CONVERTATION_ERROR = "{ \"status\" : \"error\"," +
            " \"message\" : \"error while converting JSON to User\" }";
    static final String SESSION_EXPIRED_ERROR = "{ \"status\" : \"error\"," +
            " \"message\" : \"session expired\" }";
    static final String AUTHORIZATION_ERROR = "{ \"status\" : \"error\"," +
            " \"message\" : \"front end authorization error\" }";
    static final String TOKEN_EXISTS_ERROR = "{ \"status\" : \"error\"," +
            " \"message\" : \"token with that login is already exists\" }";
    static final String JSCH_CONNECTION_ERROR = "{ \"status\" : \"error\"," +
            " \"message\" : \"error while connecting ftp server\" }";
    static final String DEACTIVATION_ERROR = "{ \"status\" : \"error\"," +
            " \"message\" : \"error while deactivating session\" }";
    static final String DEACTIVATION_SUCCESS = "{ \"status\" : \"success\"," +
            " \"message\" : \"deactivation success\" }";

    static String createAuthorizationResponse(String sessionToken){
        return "{ \"status\" : \"success\"," +
                " \"message\" : \"authorization success\"," +
                " \"sessionToken\" : \"" + sessionToken + "\" }";
    }

    static String createUserResponse(List<User> users){
        StringBuilder response = new StringBuilder();
        response.append("{ \"users\" : [ \n");
        for (User user : users){
            response.append("{\n");
            response.append("\"id\" : \"");
            response.append(user.getId());
            response.append("\",\n");
            response.append("\"username\" : \"");
            response.append(user.getName());
            response.append("\",\n\"quota\" : \"");
            response.append(user.getQuota());
            response.append("\"\n},");
        }
        response.deleteCharAt(response.length() - 1);
        response.append("\n]\n}");
        return response.toString();
    }
    static String createUserResponse(List<User> users, String freeSpace){
        StringBuilder response = new StringBuilder();
        response.append("{ \"freeSpace\" : \"");
        response.append(freeSpace);
        response.append("\"\n");
        response.append(" \"users\" : [ \n");
        response.append(createUserResponse(users));

        response.deleteCharAt(response.length() - 1);
        response.append("\n]\n}");
        return response.toString();
    }
    static String createUserResponse(User user){
        return "{\n \"id\" : \"" +
                user.getId() +
                "\",\n \"username\" : \"" +
                user.getName() +
                "\"\n}";
    }
    static String createServersResponse(List<FTPServer> servers){
        StringBuilder response = new StringBuilder();
        response.append("{\n \"servers\" : [");
        for(FTPServer server : servers){
            response.append("\n {\n  \"id\" : \"");
            response.append(server.getId());
            response.append("\",\n  \"address\" : \"");
            response.append(server.getAddress());
            response.append("\"\n },");
        }
        response.deleteCharAt(response.length() - 1);
        response.append("\n]\n}");
        return response.toString();
    }
    static String createServerGradesResponse(List<Entry<FTPServer, Double>> sortedServerGrades){
        StringBuilder response = new StringBuilder();
        response.append("{\n \"servers\" : [");
        for(Entry<FTPServer, Double> tmp : sortedServerGrades){
            response.append("\n {\n  \"id\" : \"");
            response.append(tmp.getKey().getId());
            response.append("\",\n  \"address\" : \"");
            response.append(tmp.getKey().getAddress());
            response.append("\",\n  \"grade\" : \"");
            response.append(tmp.getValue());
            response.append("\"\n },");
        }
        response.deleteCharAt(response.length() - 1);
        response.append("\n]\n}");
        return response.toString();
    }
}
