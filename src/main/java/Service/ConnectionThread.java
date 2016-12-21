package Service;

/**
 * Created by Lem0n on 08.11.2016.
 */
public class ConnectionThread implements Runnable {

    private String adminName;
    private String adminPassword;
    private String username;
    private String password;
    private String serverUrl;

    public ConnectionThread(String serverUrl, String adminName, String adminPassword,
                            String username, String password){
        this.username = username;
        this.password = password;
        this.adminName = adminName;
        this.adminPassword = adminPassword;
        this.serverUrl = serverUrl;
    }
    public void run() {
        ConnectionController cController = new ConnectionController();
        cController.Connect(adminName, adminPassword, serverUrl);
        cController.CreateUser(username, password);
    }
}
