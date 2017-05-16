package Metric;


import Models.FTPServer;

public interface Metric {
    Double getParameter(FTPServer server, String ftpProperties, String quota);
}
