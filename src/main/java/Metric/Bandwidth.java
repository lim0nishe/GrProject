package Metric;


import Models.FTPServer;
import org.apache.log4j.Logger;

public class Bandwidth implements Metric {

    private static final int CRITICAL_LOAD_HOURS = 24;
    private static final int SECONDS_IN_HOUR = 3600;
    private static final int MBITS_TO_MB = 8;

    private static Logger logger = Logger.getLogger("metric");

    @Override
    public Double getParameter(FTPServer server, String ftpProperties, String quota) {


        // rank is not pure bandwith, but time to download 100% of quota
        // function for bandwith is e^((q / b) - 24)
        // b is Mbit/s
        Double result = Math.exp(((Double.parseDouble(quota) / (Double.parseDouble(server.getBandwith()) * MBITS_TO_MB)) / SECONDS_IN_HOUR)
                - CRITICAL_LOAD_HOURS);
        logger.info("bandwith metric for server " + server.getId() + " : " + result);
        return result;
    }


}
