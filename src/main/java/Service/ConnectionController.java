package Service;
import Models.User;
import com.jcraft.jsch.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Lem0n on 30.10.2016.
 */
public class ConnectionController {

    private static Logger logger = Logger.getLogger("simple");
    private JSch jsch;
    private Session session;

    public ConnectionController(){
        jsch = new JSch();
    }

    public void Connect(String user, String password, String hostname){
        try {
            // setting session for 22 port
            session = jsch.getSession(user, hostname, 22);


            session.setPassword(password);

            // skip host-key check
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            logger.info("session open");
        }
        catch (Exception e){
            e.printStackTrace();
            logger.error("error while opening session");
        }
    }

    public void ShellConnect() throws JSchException{

        // using for shell connection and testing users after creation

        Channel channel = session.openChannel("shell");
        channel.setInputStream(System.in);
        channel.setOutputStream(System.out);
        channel.connect();
        logger.info("Shell connection is up and running");

    }


    public void CreateUser(User user){
        try {

//            real users
//
//            String message = "useradd " + user.getName() + " --home /var/www/" + user.getName()
//                    + " --shell /bin/false --group nogroup ; sudo -S -p '' mkdir /var/www/" + user.getName()
//                    + " ; sudo -S -p '' passwd " + user.getPassword();

            String message = "ftpasswd --passwd --file=/etc/proftpd/ftpd.passwd --name=" + user.getName() +
                    " --uid=" + (10 + user.getId()) + " --gid=33 --home=/var/www/" + user.getName() +
                    " --shell=/bin/false ; sudo -S -p '' mkdir /var/www/" + user.getName();

            Channel channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand("sudo -S -p '' " + message);

            OutputStream out = channel.getOutputStream();
            ((ChannelExec)channel).setErrStream(System.err);

            channel.connect();
            logger.info("channel is connected");

            out.write((user.getServer().getAdminPass() + '\n').getBytes());
            out.flush();

            out.write((user.getPassword() + '\n').getBytes());
            out.flush();
            out.write((user.getPassword() + '\n').getBytes());
            out.flush();
            logger.info("create user method executed");

            channel.disconnect();
            logger.info("channel disconnected");
        }
        catch (Exception e){
            e.printStackTrace();
            logger.error("error while creating user");
        }
    }

    public boolean validateCreation(User user){
        try {
            Channel channel = session.openChannel("exec");
            // посмотреть, возможно надо добавить символ $ перед двоеточием
            ((ChannelExec)channel).setCommand("sudo -S -p '' grep ^" + user.getName() + ": /etc/proftpd/ftpd.passwd");
            InputStream in = channel.getInputStream();

            // use this for debug
            byte[] buffer = new byte[1024];

            int readed = in.read(buffer);
            if (readed == 0)
                return false;

            // check if there will be more than 1 result in grep

            return true;
        }
        catch (JSchException e){
            logger.error("error while testing creation");
            return false;
        }
        catch (IOException e2){
            e2.printStackTrace();
            return false;
        }
    }
}
