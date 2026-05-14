package com.jiaoshoujia.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiaoshoujia.common.annotation.Log;
import com.jiaoshoujia.common.core.PageResult;
import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.enums.BusinessType;
import com.jiaoshoujia.common.utils.SecurityUtils;
import com.jiaoshoujia.system.domain.SysUser;
import com.jiaoshoujia.system.domain.dto.SysUserQuery;
import com.jiaoshoujia.system.service.ISysUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/user")
public class SysUserController {

    private final ISysUserService userService;

    public SysUserController(ISysUserService userService) {
        this.userService = userService;
    }

    private static final int MAX_PAGE_SIZE = 100;

    @PreAuthorize("hasAuthority('system:user:list')")
    @GetMapping("/list")
    public R<PageResult<SysUser>> list(SysUserQuery query,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysUser> page = new Page<>(pageNum, Math.min(pageSize, MAX_PAGE_SIZE));
        Page<SysUser> result = userService.selectUserPage(page, query);
        return R.ok(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping("/{userId}")
    public R<SysUser> getInfo(@PathVariable Long userId) {
        return R.ok(userService.selectUserById(userId));
    }

    @PreAuthorize("hasAuthority('system:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@RequestBody SysUser user) {
        userService.checkPasswordStrength(user.getPassword());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return userService.insertUser(user) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody SysUser user) {
        return userService.updateUser(user) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public R<Void> remove(@PathVariable Long[] userIds) {
        return userService.deleteUserByIds(userIds) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:user:resetPwd')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public R<Void> resetPwd(@RequestBody SysUser user) {
        userService.checkPasswordStrength(user.getPassword());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return userService.resetPwd(user) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysUser user) {
        return userService.updateUserStatus(user) > 0 ? R.ok() : R.fail();
    }
}
