package com.jiaoshoujia.system.service;

import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaoshoujia.system.domain.SysMenu;

public interface ISysMenuService extends IService<SysMenu> {

    Set<String> selectMenuPermsByUserId(Long userId);

    List<SysMenu> selectMenuTreeByUserId(Long userId);

    List<SysMenu> selectMenuList(SysMenu menu, Long userId);

    SysMenu selectMenuById(Long menuId);

    List<SysMenu> buildMenuTree(List<SysMenu> menus);

    int insertMenu(SysMenu menu);

    int updateMenu(SysMenu menu);

    int deleteMenuById(Long menuId);

    boolean hasChildByMenuId(Long menuId);
}
