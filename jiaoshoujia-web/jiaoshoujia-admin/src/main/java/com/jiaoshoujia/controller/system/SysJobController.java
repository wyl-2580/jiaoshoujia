package com.jiaoshoujia.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiaoshoujia.common.annotation.Log;
import com.jiaoshoujia.common.core.PageResult;
import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.enums.BusinessType;
import com.jiaoshoujia.system.domain.SysJob;
import com.jiaoshoujia.system.service.ISysJobService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/job")
public class SysJobController {

    private final ISysJobService jobService;

    public SysJobController(ISysJobService jobService) {
        this.jobService = jobService;
    }

    private static final int MAX_PAGE_SIZE = 100;

    @PreAuthorize("hasAuthority('monitor:job:list')")
    @GetMapping("/list")
    public R<PageResult<SysJob>> list(SysJob job,
                                      @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<SysJob> page = new Page<>(pageNum, Math.min(pageSize, MAX_PAGE_SIZE));
        Page<SysJob> result = jobService.selectJobPage(page, job);
        return R.ok(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @PreAuthorize("hasAuthority('monitor:job:query')")
    @GetMapping("/{jobId}")
    public R<SysJob> getInfo(@PathVariable(name = "jobId") Long jobId) {
        return R.ok(jobService.selectJobById(jobId));
    }

    @PreAuthorize("hasAuthority('monitor:job:add')")
    @Log(title = "定时任务", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@RequestBody SysJob job) {
        return jobService.insertJob(job) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('monitor:job:edit')")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody SysJob job) {
        return jobService.updateJob(job) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('monitor:job:remove')")
    @Log(title = "定时任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{jobIds}")
    public R<Void> remove(@PathVariable(name = "jobIds") Long[] jobIds) {
        return jobService.deleteJobByIds(jobIds) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('monitor:job:changeStatus')")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysJob job) {
        return jobService.changeStatus(job) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('monitor:job:run')")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping("/run")
    public R<Void> run(@RequestBody SysJob job) {
        jobService.run(job);
        return R.ok();
    }
}
