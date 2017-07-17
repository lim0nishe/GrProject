package Metric;


import Models.FTPServer;
import Service.ConnectionController;
import com.jcraft.jsch.JSchException;
import org.apache.log4j.Logger;

public class Uptime implements Metric {

    private static final double SECONDS_IN_DAY = 86400;
    private static final int MINIMUM_UPTIME_DAYS_NUMBER = 11;

    private static final int TEST_UPTIME_CRITICAL_VALUE = 1;
    private static final int TEST_SECONDS_IN_10_MINUTES = 600;
    private static Logger logger = Logger.getLogger("metric");

    @Override
    public Double getParameter(FTPServer server, String ftpProperties, String quota) {
        try {
            ConnectionController connectionController = new ConnectionController();
            connectionController.keyConnect(server.getAdminName(), server.getAddress(), ftpProperties);
            String resultString = connectionController.executeCommand("cat /proc/uptime\n");

            String[] uptimeData = resultString.split("[ ]");

            // uptime calcuated in seconds, should be converted to days
            // function for uptime is e^(-u + 11)

            //Double result = Math.exp( - (Double.parseDouble(uptimeData[0]) / TEST_SECONDS_IN_10_MINUTES) + TEST_UPTIME_CRITICAL_VALUE);
            Double result = Math.exp( - (Double.parseDouble(uptimeData[0]) / SECONDS_IN_DAY) + MINIMUM_UPTIME_DAYS_NUMBER);

            logger.info("uptime metric for server " + server.getId() + " : " + result);
            connectionController.closeSession();
            return result;
        }
        catch (Exception e1){
            e1.printStackTrace();
            logger.error("uprime exception");
            return 0.0;
        }
    }

}
