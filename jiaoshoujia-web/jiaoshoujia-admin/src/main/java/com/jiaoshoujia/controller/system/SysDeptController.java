package com.jiaoshoujia.controller.system;

import com.jiaoshoujia.common.annotation.Log;
import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.enums.BusinessType;
import com.jiaoshoujia.system.domain.SysDept;
import com.jiaoshoujia.system.service.ISysDeptService;
import jakarta.validation.Valid;
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
    public R<List<SysDept>> excludeChild(@PathVariable(name = "deptId") Long deptId) {
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        String idStr = String.valueOf(deptId);
        depts.removeIf(d -> d.getId().equals(deptId)
                || (d.getAncestors() != null && java.util.Arrays.asList(d.getAncestors().split(",")).contains(idStr)));
        return R.ok(depts);
    }

    @PreAuthorize("hasAnyAuthority('system:dept:list', 'system:user:list')")
    @GetMapping("/treeselect")
    public R<List<SysDept>> treeselect(SysDept dept) {
        List<SysDept> depts = deptService.selectDeptList(dept);
        return R.ok(deptService.buildDeptTree(depts));
    }

    @PreAuthorize("hasAuthority('system:dept:query')")
    @GetMapping("/{deptId}")
    public R<SysDept> getInfo(@PathVariable(name = "deptId") Long deptId) {
        return R.ok(deptService.selectDeptById(deptId));
    }

    @PreAuthorize("hasAuthority('system:dept:add')")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysDept dept) {
        return deptService.insertDept(dept) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:dept:edit')")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysDept dept) {
        return deptService.updateDept(dept) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:dept:remove')")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public R<Void> remove(@PathVariable(name = "deptId") Long deptId) {
        return deptService.deleteDeptById(deptId) > 0 ? R.ok() : R.fail();
    }
}
