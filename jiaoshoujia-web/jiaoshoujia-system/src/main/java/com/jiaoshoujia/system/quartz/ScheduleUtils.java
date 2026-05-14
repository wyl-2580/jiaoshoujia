package com.jiaoshoujia.system.quartz;

import com.jiaoshoujia.common.exception.BusinessException;
import com.jiaoshoujia.system.domain.SysJob;
import org.quartz.*;

public final class ScheduleUtils {

    private ScheduleUtils() {}

    private static final String TASK_PROPERTIES = "TASK_PROPERTIES";

    public static JobKey getJobKey(Long jobId, String jobGroup) {
        return JobKey.jobKey("TASK_" + jobId, jobGroup);
    }

    public static TriggerKey getTriggerKey(Long jobId, String jobGroup) {
        return TriggerKey.triggerKey("TASK_" + jobId, jobGroup);
    }

    public static void createScheduleJob(Scheduler scheduler, SysJob job) {
        try {
            Class<? extends Job> jobClass = getQuartzJobClass(job);
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(getJobKey(job.getId(), job.getJobGroup()))
                    .build();
            jobDetail.getJobDataMap().put(TASK_PROPERTIES, job);

            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
            cronScheduleBuilder = handleMisfirePolicy(job, cronScheduleBuilder);

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(getTriggerKey(job.getId(), job.getJobGroup()))
                    .withSchedule(cronScheduleBuilder)
                    .build();

            if (scheduler.checkExists(getJobKey(job.getId(), job.getJobGroup()))) {
                scheduler.deleteJob(getJobKey(job.getId(), job.getJobGroup()));
            }
            scheduler.scheduleJob(jobDetail, trigger);

            if (job.getStatus() != null && job.getStatus() == 1) {
                scheduler.pauseJob(getJobKey(job.getId(), job.getJobGroup()));
            }
        } catch (SchedulerException e) {
            throw new BusinessException("创建定时任务失败: " + e.getMessage());
        }
    }

    public static void deleteScheduleJob(Scheduler scheduler, Long jobId, String jobGroup) {
        try {
            scheduler.deleteJob(getJobKey(jobId, jobGroup));
        } catch (SchedulerException e) {
            throw new BusinessException("删除定时任务失败: " + e.getMessage());
        }
    }

    public static void pauseJob(Scheduler scheduler, Long jobId, String jobGroup) {
        try {
            scheduler.pauseJob(getJobKey(jobId, jobGroup));
        } catch (SchedulerException e) {
            throw new BusinessException("暂停定时任务失败: " + e.getMessage());
        }
    }

    public static void resumeJob(Scheduler scheduler, Long jobId, String jobGroup) {
        try {
            scheduler.resumeJob(getJobKey(jobId, jobGroup));
        } catch (SchedulerException e) {
            throw new BusinessException("恢复定时任务失败: " + e.getMessage());
        }
    }

    public static void triggerJob(Scheduler scheduler, SysJob job) {
        try {
            JobDataMap dataMap = new JobDataMap();
            dataMap.put(TASK_PROPERTIES, job);
            scheduler.triggerJob(getJobKey(job.getId(), job.getJobGroup()), dataMap);
        } catch (SchedulerException e) {
            throw new BusinessException("执行定时任务失败: " + e.getMessage());
        }
    }

    private static Class<? extends Job> getQuartzJobClass(SysJob job) {
        return job.getConcurrent() != null && job.getConcurrent() == 0
                ? QuartzJobExecution.class
                : QuartzDisallowConcurrentExecution.class;
    }

    private static CronScheduleBuilder handleMisfirePolicy(SysJob job, CronScheduleBuilder cb) {
        if (job.getMisfirePolicy() == null) return cb;
        return switch (job.getMisfirePolicy()) {
            case 1 -> cb.withMisfireHandlingInstructionFireAndProceed();
            case 2 -> cb.withMisfireHandlingInstructionDoNothing();
            case 3 -> cb.withMisfireHandlingInstructionIgnoreMisfires();
            default -> cb;
        };
    }
}
