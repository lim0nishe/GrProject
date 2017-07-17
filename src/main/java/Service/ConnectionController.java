package Service;
import Models.FTPServer;
import Models.User;
import com.jcraft.jsch.*;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Formatter;

import java.io.*;
import java.util.Properties;


public class ConnectionController {

    // using for ssh connection
    // all commands for FTP server in FTPProperties file (current FTP server is ProFTPD)

    private static Logger logger = Logger.getLogger("file");
    private JSch jsch;
    private Session session;
    private Properties properties;

    public ConnectionController(){
        jsch = new JSch();
        properties = new Properties();
    }

    public void keyConnect(String user, String hostname, String FTPProperties) throws JSchException{
        try{

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try(InputStream resourceStream = classLoader.getResourceAsStream(FTPProperties + ".properties")){
                properties.loadFromXML(resourceStream);
            }

            //properties.loadFromXML(new FileInputStream(FTPProperties));

            // DONT LOOK AT THIS, JUST TEMPORARY DECISION
            File tmp = File.createTempFile("id_rsa", "");
            OutputStream out = new FileOutputStream(tmp);
            InputStream in = getClass().getResourceAsStream("/id_rsa");

            IOUtils.copy(in, out);

            out.close();
            in.close();
            // OK THIS OVER

            jsch.addIdentity(tmp.getAbsolutePath());

            session = jsch.getSession(user, hostname, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            logger.info("session open by public key");
        }
        catch (IOException e){
            e.printStackTrace();
            logger.error("error in reading key file");
        }
    }

    // connect without key authentication, idk why I didnt delete this
    public void Connect(String user, String password, String hostname) throws JSchException{
        // setting session for 22 port
        session = jsch.getSession(user, hostname, 22);


        session.setPassword(password);

        // skip host-key check
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        logger.info("session open");
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

            Formatter formatter = new Formatter();

            String message = formatter.format(properties.getProperty("createUser"), user.getName(),
                    (10 + user.getId()), user.getName(), user.getName()).toString();

            System.out.println(message);
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

            // remove command to proftpd.properties
            ((ChannelExec)channel).setCommand("sudo -S -p '' grep ^" + user.getName() + ": /etc/proftpd/ftpd.passwd");

            InputStream in = channel.getInputStream();
            OutputStream out = channel.getOutputStream();
            channel.connect();
            out.write((user.getServer().getAdminPass() + '\n').getBytes());
            out.flush();
            // use this for debug
            byte[] buffer = new byte[1024];

            int readed = in.read(buffer);
            System.out.println(buffer);
            if (readed == 0) {
                channel.disconnect();
                return false;
            }

            logger.info("validation success");
            // check if there will be more than 1 result in grep
            channel.disconnect();
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

    public void setQuota(User user, String quota){
        try{
            // TODO: test this method
            logger.info("setQuota method invoked");
            Channel channel = session.openChannel("exec");

            Formatter formatter = new Formatter();
            String message = formatter.format(properties.getProperty("setQuota"), user.getName(), quota).toString();

            //String message = "ftpquota --name=" + user.getName() + " --bytes-upload=" + quota +
            //        " --add-record --type=limit --quota-type=user --units=Mb --verbose" +
            //        " --table-path=/usr/local/etc/proftpd.quota.limittab";

            ((ChannelExec)channel).setCommand("sudo -S -p '' " + message);

            OutputStream out = channel.getOutputStream();
            ((ChannelExec)channel).setErrStream(System.err);

            channel.connect();
            logger.info("channel is connected");

            out.write((user.getServer().getAdminPass() + '\n').getBytes());
            out.flush();
            channel.disconnect();
            logger.info("channel disconnected");
        }
        catch (JSchException e){
            logger.error("setQuota JSchException");
            e.printStackTrace();
        }
        catch (IOException e2){
            logger.error("setQuota IOException");
            e2.printStackTrace();
        }
    }

    public void closeSession(){
        session.disconnect();
    }

    public String checkFreeSpace(FTPServer server){
        logger.info("check free space method invoked");
        try {
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand("df -h /");
            InputStream in = channel.getInputStream();
            OutputStream out = channel.getOutputStream();
            channel.connect();
            out.write((server.getAdminPass() + '\n').getBytes());
            out.flush();

            byte[] buffer = new byte[1024];

            int readed = in.read(buffer);
            System.out.println(buffer);
            if (readed == -1) {
                logger.error("cant read size");
                channel.disconnect();
                return null;
            }
            channel.disconnect();
            return new String(buffer, 0, readed, "UTF-8");
        }
        catch (Exception e){
            logger.error("exception in checkFreeSpace method");
            e.printStackTrace();
            return null;
        }
    }
    public String executeCommand(String command){
        try {
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            InputStream is = channel.getInputStream();
            channel.connect();

            String result = "";

            int readed;
            byte[] buffer = new byte[1024];
            while ((readed = is.read(buffer)) > 0){
                result = new String(buffer, 0 , readed, "UTF-8");
                System.out.println(result);
            }
            // закрой channel, try with resources не работает, т.к. Channel не
            // имплементит AutoClosable
            return result;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
