package com.jiaoshoujia.system.quartz;

import com.jiaoshoujia.system.domain.SysJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

@DisallowConcurrentExecution
public class QuartzDisallowConcurrentExecution extends QuartzJobBean {

    private static final Logger log = LoggerFactory.getLogger(QuartzDisallowConcurrentExecution.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        SysJob sysJob = (SysJob) context.getMergedJobDataMap().get("TASK_PROPERTIES");
        try {
            log.info("Executing job (no concurrent): {} - {}", sysJob.getJobName(), sysJob.getInvokeTarget());
            QuartzInvokeUtils.invokeMethod(sysJob);
        } catch (Exception e) {
            log.error("Job execution failed: {}", sysJob.getJobName(), e);
            throw new JobExecutionException(e);
        }
    }
}
