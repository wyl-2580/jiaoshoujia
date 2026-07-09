package com.jiaoshoujia.service;

import com.jiaoshoujia.domain.SysMenu;
import com.jiaoshoujia.mapper.SysMenuMapper;
import com.jiaoshoujia.mapper.SysRoleMenuMapper;
import com.jiaoshoujia.mapper.SysUserRoleMapper;
import com.jiaoshoujia.service.impl.SysMenuServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("菜单管理 Service 测试")
class SysMenuServiceTest {

    @Mock
    private SysMenuMapper menuMapper;

    @Mock
    private SysUserRoleMapper userRoleMapper;

    @Mock
    private SysRoleMenuMapper roleMenuMapper;

    @InjectMocks
    private SysMenuServiceImpl menuService;

    @Test
    @DisplayName("构建菜单树 - 两层嵌套")
    void buildMenuTree_twoLevels() {
        SysMenu dir = new SysMenu();
        dir.setId(1L);
        dir.setMenuName("系统管理");
        dir.setParentId(0L);
        dir.setOrderNum(1);
        dir.setMenuType("M");

        SysMenu menu1 = new SysMenu();
        menu1.setId(2L);
        menu1.setMenuName("用户管理");
        menu1.setParentId(1L);
        menu1.setOrderNum(1);
        menu1.setMenuType("C");

        SysMenu menu2 = new SysMenu();
        menu2.setId(3L);
        menu2.setMenuName("角色管理");
        menu2.setParentId(1L);
        menu2.setOrderNum(2);
        menu2.setMenuType("C");

        List<SysMenu> tree = menuService.buildMenuTree(List.of(dir, menu1, menu2));

        assertEquals(1, tree.size());
        assertEquals("系统管理", tree.get(0).getMenuName());
        assertEquals(2, tree.get(0).getChildren().size());
    }

    @Test
    @DisplayName("构建菜单树 - 三层嵌套")
    void buildMenuTree_threeLevels() {
        SysMenu dir = new SysMenu();
        dir.setId(1L);
        dir.setMenuName("系统管理");
        dir.setParentId(0L);

        SysMenu menu = new SysMenu();
        menu.setId(2L);
        menu.setMenuName("用户管理");
        menu.setParentId(1L);

        SysMenu button = new SysMenu();
        button.setId(3L);
        button.setMenuName("用户新增");
        button.setParentId(2L);

        List<SysMenu> tree = menuService.buildMenuTree(List.of(dir, menu, button));

        assertEquals(1, tree.size());
        SysMenu root = tree.get(0);
        assertEquals(1, root.getChildren().size());
        assertEquals(1, root.getChildren().get(0).getChildren().size());
        assertEquals("用户新增", root.getChildren().get(0).getChildren().get(0).getMenuName());
    }

    @Test
    @DisplayName("构建菜单树 - 空列表")
    void buildMenuTree_empty() {
        List<SysMenu> tree = menuService.buildMenuTree(new ArrayList<>());

        assertTrue(tree.isEmpty());
    }

    @Test
    @DisplayName("构建菜单树 - 多个顶级节点")
    void buildMenuTree_multipleRoots() {
        SysMenu dir1 = new SysMenu();
        dir1.setId(1L);
        dir1.setMenuName("系统管理");
        dir1.setParentId(0L);

        SysMenu dir2 = new SysMenu();
        dir2.setId(2L);
        dir2.setMenuName("系统监控");
        dir2.setParentId(0L);

        List<SysMenu> tree = menuService.buildMenuTree(List.of(dir1, dir2));

        assertEquals(2, tree.size());
    }
}
