package Metric;


import Models.FTPServer;

public interface Metric {

    // returns value of metric parameter
    Double getParameter(FTPServer server, String ftpProperties, String quota);
}
