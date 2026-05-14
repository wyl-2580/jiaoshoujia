package com.jiaoshoujia.controller.system;

import com.jiaoshoujia.common.annotation.Log;
import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.enums.BusinessType;
import com.jiaoshoujia.system.domain.SysDept;
import com.jiaoshoujia.system.service.ISysDeptService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/dept")
public class SysDeptController {

    private final ISysDeptService deptService;

    public SysDeptController(ISysDeptService deptService) {
        this.deptService = deptService;
    }

    @PreAuthorize("hasAuthority('system:dept:list')")
    @GetMapping("/list")
    public R<List<SysDept>> list(SysDept dept) {
        List<SysDept> depts = deptService.selectDeptList(dept);
        return R.ok(depts);
    }

    @PreAuthorize("hasAuthority('system:dept:list')")
    @GetMapping("/list/exclude/{deptId}")
    public R<List<SysDept>> excludeChild(@PathVariable Long deptId) {
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        depts.removeIf(d -> d.getId().equals(deptId)
                || (d.getAncestors() != null && d.getAncestors().contains(String.valueOf(deptId))));
        return R.ok(depts);
    }

    @PreAuthorize("hasAuthority('system:dept:query')")
    @GetMapping("/{deptId}")
    public R<SysDept> getInfo(@PathVariable Long deptId) {
        return R.ok(deptService.selectDeptById(deptId));
    }

    @PreAuthorize("hasAuthority('system:dept:add')")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@RequestBody SysDept dept) {
        return deptService.insertDept(dept) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:dept:edit')")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody SysDept dept) {
        return deptService.updateDept(dept) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:dept:remove')")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public R<Void> remove(@PathVariable Long deptId) {
        return deptService.deleteDeptById(deptId) > 0 ? R.ok() : R.fail();
    }
}
