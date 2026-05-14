package com.jiaoshoujia.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaoshoujia.system.domain.SysJob;

public interface ISysJobService extends IService<SysJob> {

    Page<SysJob> selectJobPage(Page<SysJob> page, SysJob job);

    SysJob selectJobById(Long jobId);

    int insertJob(SysJob job);

    int updateJob(SysJob job);

    int deleteJobByIds(Long[] jobIds);

    int changeStatus(SysJob job);

    void run(SysJob job);
}
