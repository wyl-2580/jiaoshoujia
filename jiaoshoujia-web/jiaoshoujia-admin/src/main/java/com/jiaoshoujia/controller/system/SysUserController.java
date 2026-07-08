package com.jiaoshoujia.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiaoshoujia.common.annotation.Log;
import com.jiaoshoujia.common.core.PageResult;
import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.enums.BusinessType;
import com.jiaoshoujia.common.utils.ExcelUtils;
import com.jiaoshoujia.common.utils.SecurityUtils;
import com.jiaoshoujia.system.domain.SysUser;
import com.jiaoshoujia.system.domain.dto.SysUserQuery;
import com.jiaoshoujia.system.service.ISysUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/system/user")
public class SysUserController {

    private final ISysUserService userService;

    public SysUserController(ISysUserService userService) {
        this.userService = userService;
    }

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_EXPORT_SIZE = 100000;

    @PreAuthorize("hasAuthority('system:user:list')")
    @GetMapping("/list")
    public R<PageResult<SysUser>> list(SysUserQuery query,
                                       @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<SysUser> page = new Page<>(pageNum, Math.min(pageSize, MAX_PAGE_SIZE));
        Page<SysUser> result = userService.selectUserPage(page, query);
        return R.ok(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @PreAuthorize("hasAuthority('system:user:export')")
    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysUserQuery query) {
        Page<SysUser> page = new Page<>(1, MAX_EXPORT_SIZE);
        List<UserExportRow> list = userService.selectUserPage(page, query).getRecords().stream()
                .map(UserExportRow::from)
                .toList();
        ExcelUtils.exportExcel(response, "用户数据", UserExportRow.class, list);
    }

    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping("/{userId}")
    public R<SysUser> getInfo(@PathVariable(name = "userId") Long userId) {
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
    public R<Void> remove(@PathVariable(name = "userIds") Long[] userIds) {
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

    public static class UserExportRow {
        private String username;
        private String nickname;
        private String deptName;
        private String email;
        private String phone;
        private Integer sex;
        private Integer status;
        private LocalDateTime createTime;

        public static UserExportRow from(SysUser user) {
            UserExportRow row = new UserExportRow();
            row.setUsername(user.getUsername());
            row.setNickname(user.getNickname());
            row.setDeptName(user.getDeptName());
            row.setEmail(user.getEmail());
            row.setPhone(user.getPhone());
            row.setSex(user.getSex());
            row.setStatus(user.getStatus());
            row.setCreateTime(user.getCreateTime());
            return row;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getDeptName() {
            return deptName;
        }

        public void setDeptName(String deptName) {
            this.deptName = deptName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public Integer getSex() {
            return sex;
        }

        public void setSex(Integer sex) {
            this.sex = sex;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }
    }
}
