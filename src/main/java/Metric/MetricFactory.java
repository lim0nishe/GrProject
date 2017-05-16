package Metric;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class MetricFactory {
    private Properties metricList;

    public MetricFactory(){ metricList = new Properties(); }

    public void loadClasses(String fileName){
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream iStream = classLoader.getResourceAsStream(fileName);
            metricList.load(iStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Metric> getMetricList() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        List<Metric> result = new ArrayList<Metric>();
        Enumeration enumeration = metricList.keys();
        while(enumeration.hasMoreElements()){
            result.add((Metric)Class.forName((String)metricList.get(enumeration.nextElement())).newInstance());
        }
        return result;
    }
}
