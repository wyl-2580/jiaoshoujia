package com.jiaoshoujia.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiaoshoujia.common.annotation.Log;
import com.jiaoshoujia.common.core.PageResult;
import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.enums.BusinessType;
import com.jiaoshoujia.common.utils.ExcelUtils;
import com.jiaoshoujia.system.domain.SysOperLog;
import com.jiaoshoujia.system.service.ISysOperLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/operlog")
public class SysOperLogController {

    private final ISysOperLogService operLogService;

    public SysOperLogController(ISysOperLogService operLogService) {
        this.operLogService = operLogService;
    }

    private static final int MAX_PAGE_SIZE = 100;

    @PreAuthorize("hasAuthority('monitor:operlog:list')")
    @GetMapping("/list")
    public R<PageResult<SysOperLog>> list(SysOperLog operLog,
                                           @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<SysOperLog> page = new Page<>(pageNum, Math.min(pageSize, MAX_PAGE_SIZE));
        Page<SysOperLog> result = operLogService.selectOperLogPage(page, operLog);
        return R.ok(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @PreAuthorize("hasAuthority('monitor:operlog:export')")
    @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysOperLog operLog) {
        List<SysOperLog> list = operLogService.selectOperLogList(operLog);
        ExcelUtils.exportExcel(response, "操作日志", SysOperLog.class, list);
    }

    @PreAuthorize("hasAuthority('monitor:operlog:remove')")
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{operIds}")
    public R<Void> remove(@PathVariable(name = "operIds") Long[] operIds) {
        return operLogService.deleteOperLogByIds(operIds) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('monitor:operlog:clear')")
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public R<Void> clean() {
        operLogService.cleanOperLog();
        return R.ok();
    }
}
