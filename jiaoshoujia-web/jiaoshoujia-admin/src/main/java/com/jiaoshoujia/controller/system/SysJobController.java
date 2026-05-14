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

    @PreAuthorize("hasAuthority('system:job:list')")
    @GetMapping("/list")
    public R<PageResult<SysJob>> list(SysJob job,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysJob> page = new Page<>(pageNum, Math.min(pageSize, MAX_PAGE_SIZE));
        Page<SysJob> result = jobService.selectJobPage(page, job);
        return R.ok(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @PreAuthorize("hasAuthority('system:job:query')")
    @GetMapping("/{jobId}")
    public R<SysJob> getInfo(@PathVariable Long jobId) {
        return R.ok(jobService.selectJobById(jobId));
    }

    @PreAuthorize("hasAuthority('system:job:add')")
    @Log(title = "定时任务", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@RequestBody SysJob job) {
        return jobService.insertJob(job) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:job:edit')")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody SysJob job) {
        return jobService.updateJob(job) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:job:remove')")
    @Log(title = "定时任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{jobIds}")
    public R<Void> remove(@PathVariable Long[] jobIds) {
        return jobService.deleteJobByIds(jobIds) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:job:changeStatus')")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysJob job) {
        return jobService.changeStatus(job) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:job:run')")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping("/run")
    public R<Void> run(@RequestBody SysJob job) {
        jobService.run(job);
        return R.ok();
    }
}
