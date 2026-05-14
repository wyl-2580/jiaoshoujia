package com.jiaoshoujia.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiaoshoujia.common.annotation.Log;
import com.jiaoshoujia.common.core.PageResult;
import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.enums.BusinessType;
import com.jiaoshoujia.system.domain.SysRole;
import com.jiaoshoujia.system.service.ISysRoleService;
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

    @PreAuthorize("hasAuthority('system:role:list')")
    @GetMapping("/list")
    public R<PageResult<SysRole>> list(SysRole role,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        Page<SysRole> result = roleService.selectRolePage(page, role);
        return R.ok(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/{roleId}")
    public R<SysRole> getInfo(@PathVariable Long roleId) {
        return R.ok(roleService.selectRoleById(roleId));
    }

    @PreAuthorize("hasAuthority('system:role:add')")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@RequestBody SysRole role) {
        return roleService.insertRole(role) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody SysRole role) {
        return roleService.updateRole(role) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:role:remove')")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleIds}")
    public R<Void> remove(@PathVariable Long[] roleIds) {
        return roleService.deleteRoleByIds(roleIds) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysRole role) {
        return roleService.updateRoleStatus(role) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/optionselect")
    public R<List<SysRole>> optionselect() {
        return R.ok(roleService.selectRoleAll());
    }
}
