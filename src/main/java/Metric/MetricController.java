package Metric;


import Models.FTPServer;
import Models.User;
import Service.HibernateUtil;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class MetricController {
    private MetricFactory metricFactory;
    private static Logger logger = Logger.getLogger("file");
    private static Logger logger2 = Logger.getLogger("metric");

    public MetricController(){
        metricFactory = new MetricFactory();

        // load all available parameters for metric
        metricFactory.loadClasses("metricClasses.txt");
    }

    // calculate metric for FTP server
    public Double calculateMetric(FTPServer server, String ftpProperties, String quota){
        try {
            Double result = 0.0;
            List<Metric> metricList = metricFactory.getMetricList();
            for(Metric tmp : metricList){
                result += tmp.getParameter(server, ftpProperties, quota);
            }
            return result;
        }
        catch (Exception e){
            e.printStackTrace();
            return -1.0;
        }
    }

    public Map<FTPServer, Double> getServersGrades(String quota){
        try {
            Properties properties = new Properties();
            properties.loadFromXML(new FileInputStream("/TomcatFiles/myProperties.properties"));

            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            // choose appropriate servers
            List<FTPServer> servers = session.createCriteria(FTPServer.class).list();
            HashMap<FTPServer, Double> result = new HashMap<>();

            logger2.info("======================================================================");
            for (FTPServer tmpServer : servers) {
                Long reservedSpace = 0L;
                for (User tmpUser : tmpServer.getUsers()) {
                    reservedSpace += Long.parseLong(tmpUser.getQuota());
                }
                // checking free space for being enough and calculating function for space
                // function for space is (F / (-q + F)) - 1
                long freeSpace = Long.parseLong(tmpServer.getMountedSpace()) - reservedSpace
                        - Long.parseLong(properties.getProperty("reservedSpace"));
                if (Long.parseLong(quota) < freeSpace) {
                    Double spaceMetricResult = ((double)freeSpace
                            / ( - Long.parseLong(quota) + freeSpace)) - 1;
                    logger2.info("space metric for server " + tmpServer.getId() + " : " + spaceMetricResult);
                    result.put(tmpServer, spaceMetricResult);
                }
            }

            // calculate other metrics
            for (FTPServer tmpServer : result.keySet()){
                Double resultRank = result.get(tmpServer)
                        + calculateMetric(tmpServer, properties.getProperty("ftpServer"), quota);
                logger2.info("result rank for server " + tmpServer.getId() + " : " + resultRank);
                result.put(tmpServer, resultRank);
            }

            // unsorted
            return result;
        }
        catch (IOException e){
            e.printStackTrace();
            logger.error("Cant find properties file");
            return null;
        }
    }
}
