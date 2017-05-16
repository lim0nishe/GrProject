package Metric;

import Models.FTPServer;
import Service.ConnectionController;
import com.jcraft.jsch.JSchException;
import org.apache.log4j.Logger;

public class LoadAverage implements Metric {

    private static final int CRITICAL_LOAD_AVARAGE_VALUE = 8;
    private static Logger logger = Logger.getLogger("metric");

    @Override
    public Double getParameter(FTPServer server, String ftpProperties, String quota){
        try {
            ConnectionController connectionController = new ConnectionController();
            connectionController.keyConnect(server.getAdminName(), server.getAddress(), ftpProperties);
            String resultString = connectionController.executeCommand("cat /proc/loadavg\n");

            String[] loadData = resultString.split("[ ]");

            // function for load average
            // e^(l-8)
            Double result = Math.exp(Double.parseDouble(loadData[2]) - CRITICAL_LOAD_AVARAGE_VALUE);
            logger.info("load average metric for server " + server.getId() + " : " + result);
            connectionController.closeSession();
            return result;
        }
        catch (Exception e1){
            e1.printStackTrace();
            logger.error("load average exception");
            return 0.0;
        }
    }
}
