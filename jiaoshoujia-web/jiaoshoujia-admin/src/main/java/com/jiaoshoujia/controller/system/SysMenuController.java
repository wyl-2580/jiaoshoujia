package com.jiaoshoujia.controller.system;

import com.jiaoshoujia.common.annotation.Log;
import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.enums.BusinessType;
import com.jiaoshoujia.common.utils.SecurityUtils;
import com.jiaoshoujia.system.domain.SysMenu;
import com.jiaoshoujia.system.service.ISysMenuService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    private final ISysMenuService menuService;

    public SysMenuController(ISysMenuService menuService) {
        this.menuService = menuService;
    }

    @PreAuthorize("hasAuthority('system:menu:list')")
    @GetMapping("/list")
    public R<List<SysMenu>> list(SysMenu menu) {
        List<SysMenu> menus = menuService.selectMenuList(menu, SecurityUtils.getUserId());
        return R.ok(menus);
    }

    @PreAuthorize("hasAuthority('system:menu:query')")
    @GetMapping("/{menuId}")
    public R<SysMenu> getInfo(@PathVariable(name = "menuId") Long menuId) {
        return R.ok(menuService.selectMenuById(menuId));
    }

    @GetMapping("/treeselect")
    public R<List<SysMenu>> treeselect(SysMenu menu) {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuList(menu, userId);
        return R.ok(menuService.buildMenuTree(menus));
    }

    @PreAuthorize("hasAuthority('system:menu:add')")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@RequestBody SysMenu menu) {
        return menuService.insertMenu(menu) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:menu:edit')")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody SysMenu menu) {
        return menuService.updateMenu(menu) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:menu:remove')")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public R<Void> remove(@PathVariable(name = "menuId") Long menuId) {
        return menuService.deleteMenuById(menuId) > 0 ? R.ok() : R.fail();
    }
}
