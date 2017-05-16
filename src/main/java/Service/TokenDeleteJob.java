package Service;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class TokenDeleteJob implements Job {

    public TokenDeleteJob(){}

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            FrontEndSessionController.killExpiredSessons();
    }
}
