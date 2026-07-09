package com.jiaoshoujia.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaoshoujia.common.exception.BusinessException;
import com.jiaoshoujia.common.utils.MessageUtils;
import com.jiaoshoujia.common.utils.StringUtils;
import com.jiaoshoujia.domain.SysMenu;
import com.jiaoshoujia.domain.SysRoleMenu;
import com.jiaoshoujia.domain.SysUserRole;
import com.jiaoshoujia.mapper.SysMenuMapper;
import com.jiaoshoujia.mapper.SysRoleMenuMapper;
import com.jiaoshoujia.mapper.SysUserRoleMapper;
import com.jiaoshoujia.service.ISysMenuService;
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
        // 三权分立：权限严格来自角色-菜单授权，不再存在绕过授权的"万能超管"
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
        List<Long> menuIds = getMenuIdsByUserId(userId);
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<SysMenu> menus = lambdaQuery()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getStatus, 0)
                .in(SysMenu::getMenuType, "M", "C")
                .orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getOrderNum)
                .list();
        return buildMenuTree(menus);
    }

    @Override
    public List<SysMenu> selectMenuList(SysMenu menu, Long userId) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(menu.getMenuName()), SysMenu::getMenuName, menu.getMenuName())
                .eq(menu.getStatus() != null, SysMenu::getStatus, menu.getStatus())
                .orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getOrderNum);

        List<Long> menuIds = getMenuIdsByUserId(userId);
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }
        wrapper.in(SysMenu::getId, menuIds);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<SysMenu> selectMenuListAll(SysMenu menu) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(menu.getMenuName()), SysMenu::getMenuName, menu.getMenuName())
                .eq(menu.getStatus() != null, SysMenu::getStatus, menu.getStatus())
                .orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getOrderNum);
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
        return save(menu) ? 1 : 0;
    }

    @Override
    public int updateMenu(SysMenu menu) {
        return updateById(menu) ? 1 : 0;
    }

    @Override
    public int deleteMenuById(Long menuId) {
        if (hasChildByMenuId(menuId)) {
            throw new BusinessException(MessageUtils.message("menu.has.children"));
        }
        return removeById(menuId) ? 1 : 0;
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
