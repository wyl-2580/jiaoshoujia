package com.jiaoshoujia.system.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaoshoujia.common.utils.SecurityUtils;
import com.jiaoshoujia.common.utils.StringUtils;
import com.jiaoshoujia.system.domain.SysMenu;
import com.jiaoshoujia.system.domain.SysRoleMenu;
import com.jiaoshoujia.system.domain.SysUserRole;
import com.jiaoshoujia.system.mapper.SysMenuMapper;
import com.jiaoshoujia.system.mapper.SysRoleMenuMapper;
import com.jiaoshoujia.system.mapper.SysUserRoleMapper;
import com.jiaoshoujia.system.service.ISysMenuService;
import org.springframework.stereotype.Service;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public SysMenuServiceImpl(SysUserRoleMapper userRoleMapper, SysRoleMenuMapper roleMenuMapper) {
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public Set<String> selectMenuPermsByUserId(Long userId) {
        if (SecurityUtils.isAdmin(userId)) {
            List<SysMenu> allMenus = lambdaQuery()
                    .eq(SysMenu::getStatus, 0)
                    .isNotNull(SysMenu::getPerms)
                    .ne(SysMenu::getPerms, "")
                    .list();
            return allMenus.stream().map(SysMenu::getPerms).collect(Collectors.toSet());
        }
        List<Long> menuIds = getMenuIdsByUserId(userId);
        if (menuIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<SysMenu> menus = lambdaQuery()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getStatus, 0)
                .isNotNull(SysMenu::getPerms)
                .ne(SysMenu::getPerms, "")
                .list();
        return menus.stream().map(SysMenu::getPerms).collect(Collectors.toSet());
    }

    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        List<SysMenu> menus;
        if (SecurityUtils.isAdmin(userId)) {
            menus = lambdaQuery()
                    .eq(SysMenu::getStatus, 0)
                    .in(SysMenu::getMenuType, "M", "C")
                    .orderByAsc(SysMenu::getParentId)
                    .orderByAsc(SysMenu::getOrderNum)
                    .list();
        } else {
            List<Long> menuIds = getMenuIdsByUserId(userId);
            if (menuIds.isEmpty()) {
                return Collections.emptyList();
            }
            menus = lambdaQuery()
                    .in(SysMenu::getId, menuIds)
                    .eq(SysMenu::getStatus, 0)
                    .in(SysMenu::getMenuType, "M", "C")
                    .orderByAsc(SysMenu::getParentId)
                    .orderByAsc(SysMenu::getOrderNum)
                    .list();
        }
        return buildMenuTree(menus);
    }

    @Override
    public List<SysMenu> selectMenuList(SysMenu menu, Long userId) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(menu.getMenuName()), SysMenu::getMenuName, menu.getMenuName())
                .eq(menu.getStatus() != null, SysMenu::getStatus, menu.getStatus())
                .orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getOrderNum);

        if (!SecurityUtils.isAdmin(userId)) {
            List<Long> menuIds = getMenuIdsByUserId(userId);
            if (menuIds.isEmpty()) {
                return Collections.emptyList();
            }
            wrapper.in(SysMenu::getId, menuIds);
        }
        return baseMapper.selectList(wrapper);
    }

    @Override
    public SysMenu selectMenuById(Long menuId) {
        return getById(menuId);
    }

    @Override
    public List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        Set<Long> idSet = menus.stream().map(SysMenu::getId).collect(Collectors.toSet());
        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getParentId() == null || menu.getParentId() == 0L || !idSet.contains(menu.getParentId())) {
                roots.add(menu);
            }
        }
        for (SysMenu root : roots) {
            buildChildren(root, menus);
        }
        return roots;
    }

    @Override
    public int insertMenu(SysMenu menu) {
        save(menu);
        return 1;
    }

    @Override
    public int updateMenu(SysMenu menu) {
        updateById(menu);
        return 1;
    }

    @Override
    public int deleteMenuById(Long menuId) {
        removeById(menuId);
        return 1;
    }

    @Override
    public boolean hasChildByMenuId(Long menuId) {
        return lambdaQuery().eq(SysMenu::getParentId, menuId).count() > 0;
    }

    private List<Long> getMenuIdsByUserId(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));
        if (roleMenus.isEmpty()) {
            return Collections.emptyList();
        }
        return roleMenus.stream().map(SysRoleMenu::getMenuId).distinct().collect(Collectors.toList());
    }

    private void buildChildren(SysMenu parent, List<SysMenu> allMenus) {
        List<SysMenu> children = allMenus.stream()
                .filter(m -> parent.getId().equals(m.getParentId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        for (SysMenu child : children) {
            buildChildren(child, allMenus);
        }
    }
}
