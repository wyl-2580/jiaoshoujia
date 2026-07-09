package com.jiaoshoujia.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiaoshoujia.common.annotation.Log;
import com.jiaoshoujia.common.core.PageResult;
import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.enums.BusinessType;
import com.jiaoshoujia.common.utils.ExcelUtils;
import com.jiaoshoujia.domain.SysRole;
import com.jiaoshoujia.service.ISysRoleService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    private final ISysRoleService roleService;

    public SysRoleController(ISysRoleService roleService) {
        this.roleService = roleService;
    }

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_EXPORT_SIZE = 100000;

    @PreAuthorize("hasAuthority('system:role:list')")
    @GetMapping("/list")
    public R<PageResult<SysRole>> list(SysRole role,
                                       @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<SysRole> page = new Page<>(pageNum, Math.min(pageSize, MAX_PAGE_SIZE));
        Page<SysRole> result = roleService.selectRolePage(page, role);
        return R.ok(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @PreAuthorize("hasAuthority('system:role:export')")
    @Log(title = "角色管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysRole role) {
        Page<SysRole> page = new Page<>(1, MAX_EXPORT_SIZE);
        List<SysRole> list = roleService.selectRolePage(page, role).getRecords();
        ExcelUtils.exportExcel(response, "角色数据", SysRole.class, list);
    }

    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/{roleId}")
    public R<SysRole> getInfo(@PathVariable(name = "roleId") Long roleId) {
        return R.ok(roleService.selectRoleById(roleId));
    }

    @PreAuthorize("hasAuthority('system:role:add')")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysRole role) {
        return roleService.insertRole(role) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysRole role) {
        return roleService.updateRole(role) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:role:remove')")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleIds}")
    public R<Void> remove(@PathVariable(name = "roleIds") Long[] roleIds) {
        return roleService.deleteRoleByIds(roleIds) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysRole role) {
        return roleService.updateRoleStatus(role) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAnyAuthority('system:role:query', 'system:user:list')")
    @GetMapping("/optionselect")
    public R<List<SysRole>> optionselect() {
        return R.ok(roleService.selectRoleAll());
    }
}
