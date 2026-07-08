package com.jiaoshoujia.system.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaoshoujia.common.exception.BusinessException;
import com.jiaoshoujia.common.utils.StringUtils;
import com.jiaoshoujia.system.domain.SysJob;
import com.jiaoshoujia.system.mapper.SysJobMapper;
import com.jiaoshoujia.system.quartz.ScheduleUtils;
import com.jiaoshoujia.system.service.ISysJobService;
import jakarta.annotation.PostConstruct;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements ISysJobService {

    private final Scheduler scheduler;

    public SysJobServiceImpl(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostConstruct
    public void init() throws SchedulerException {
        scheduler.clear();
        List<SysJob> jobs = list();
        for (SysJob job : jobs) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
    }

    @Override
    public Page<SysJob> selectJobPage(Page<SysJob> page, SysJob job) {
        LambdaQueryWrapper<SysJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(job.getJobName()), SysJob::getJobName, job.getJobName())
                .eq(StringUtils.isNotEmpty(job.getJobGroup()), SysJob::getJobGroup, job.getJobGroup())
                .eq(job.getStatus() != null, SysJob::getStatus, job.getStatus())
                .like(StringUtils.isNotEmpty(job.getInvokeTarget()), SysJob::getInvokeTarget, job.getInvokeTarget());
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public SysJob selectJobById(Long jobId) {
        return getById(jobId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertJob(SysJob job) {
        job.setStatus(1);
        save(job);
        final Long jobId = job.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                SysJob savedJob = getById(jobId);
                ScheduleUtils.createScheduleJob(scheduler, savedJob);
            }
        });
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateJob(SysJob job) {
        SysJob existing = getById(job.getId());
        if (existing == null) {
            throw new BusinessException("任务不存在");
        }
        updateById(job);
        final Long jobId = job.getId();
        final String oldJobGroup = existing.getJobGroup();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                SysJob updated = getById(jobId);
                ScheduleUtils.deleteScheduleJob(scheduler, updated.getId(), oldJobGroup);
                ScheduleUtils.createScheduleJob(scheduler, updated);
            }
        });
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteJobByIds(Long[] jobIds) {
        List<SysJob> jobsToDelete = new ArrayList<>();
        for (Long jobId : jobIds) {
            SysJob job = getById(jobId);
            if (job != null) {
                jobsToDelete.add(job);
            }
        }
        removeBatchByIds(Arrays.asList(jobIds));
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (SysJob job : jobsToDelete) {
                    ScheduleUtils.deleteScheduleJob(scheduler, job.getId(), job.getJobGroup());
                }
            }
        });
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int changeStatus(SysJob job) {
        SysJob existing = getById(job.getId());
        if (existing == null) {
            throw new BusinessException("任务不存在");
        }
        boolean updated = lambdaUpdate().eq(SysJob::getId, job.getId())
                .set(SysJob::getStatus, job.getStatus()).update();
        if (!updated) {
            return 0;
        }
        final Integer newStatus = job.getStatus();
        final Long existingId = existing.getId();
        final String existingGroup = existing.getJobGroup();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (newStatus != null && newStatus == 0) {
                    ScheduleUtils.resumeJob(scheduler, existingId, existingGroup);
                } else {
                    ScheduleUtils.pauseJob(scheduler, existingId, existingGroup);
                }
            }
        });
        return 1;
    }

    @Override
    public void run(SysJob job) {
        SysJob existing = getById(job.getId());
        if (existing == null) {
            throw new BusinessException("任务不存在");
        }
        ScheduleUtils.triggerJob(scheduler, existing);
    }
}
