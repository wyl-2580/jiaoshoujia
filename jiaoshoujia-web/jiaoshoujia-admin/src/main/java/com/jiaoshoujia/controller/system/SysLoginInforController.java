package com.jiaoshoujia.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiaoshoujia.common.annotation.Log;
import com.jiaoshoujia.common.core.PageResult;
import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.enums.BusinessType;
import com.jiaoshoujia.common.utils.ExcelUtils;
import com.jiaoshoujia.system.domain.SysLoginInfor;
import com.jiaoshoujia.system.service.ISysLoginInforService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/logininfor")
public class SysLoginInforController {

    private final ISysLoginInforService loginInforService;

    public SysLoginInforController(ISysLoginInforService loginInforService) {
        this.loginInforService = loginInforService;
    }

    private static final int MAX_PAGE_SIZE = 100;

    @PreAuthorize("hasAuthority('monitor:logininfor:list')")
    @GetMapping("/list")
    public R<PageResult<SysLoginInfor>> list(SysLoginInfor loginInfor,
                                             @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<SysLoginInfor> page = new Page<>(pageNum, Math.min(pageSize, MAX_PAGE_SIZE));
        Page<SysLoginInfor> result = loginInforService.selectLoginInforPage(page, loginInfor);
        return R.ok(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @PreAuthorize("hasAuthority('monitor:logininfor:export')")
    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysLoginInfor loginInfor) {
        List<SysLoginInfor> list = loginInforService.selectLoginInforList(loginInfor);
        ExcelUtils.exportExcel(response, "登录日志", SysLoginInfor.class, list);
    }

    @PreAuthorize("hasAuthority('monitor:logininfor:remove')")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public R<Void> remove(@PathVariable(name = "infoIds") Long[] infoIds) {
        return loginInforService.deleteLoginInforByIds(infoIds) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('monitor:logininfor:clear')")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public R<Void> clean() {
        loginInforService.cleanLoginInfor();
        return R.ok();
    }
}
