package com.jiaoshoujia.system.service.impl;

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
        ScheduleUtils.createScheduleJob(scheduler, job);
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
        SysJob updated = getById(job.getId());
        ScheduleUtils.deleteScheduleJob(scheduler, updated.getId(), existing.getJobGroup());
        ScheduleUtils.createScheduleJob(scheduler, updated);
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteJobByIds(Long[] jobIds) {
        for (Long jobId : jobIds) {
            SysJob job = getById(jobId);
            if (job != null) {
                ScheduleUtils.deleteScheduleJob(scheduler, job.getId(), job.getJobGroup());
            }
        }
        removeBatchByIds(Arrays.asList(jobIds));
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
        Integer newStatus = job.getStatus();
        if (newStatus != null && newStatus == 0) {
            ScheduleUtils.resumeJob(scheduler, existing.getId(), existing.getJobGroup());
        } else {
            ScheduleUtils.pauseJob(scheduler, existing.getId(), existing.getJobGroup());
        }
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
