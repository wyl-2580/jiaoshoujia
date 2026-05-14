package com.jiaoshoujia.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaoshoujia.common.exception.BusinessException;
import com.jiaoshoujia.system.domain.SysDept;
import com.jiaoshoujia.system.mapper.SysDeptMapper;
import com.jiaoshoujia.system.service.impl.SysDeptServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("部门管理 Service 测试")
class SysDeptServiceTest {

    @Mock
    private SysDeptMapper deptMapper;

    @InjectMocks
    private SysDeptServiceImpl deptService;

    private SysDept rootDept;
    private SysDept childDept;

    @BeforeEach
    void setUp() {
        rootDept = new SysDept();
        rootDept.setId(1L);
        rootDept.setDeptName("总公司");
        rootDept.setParentId(0L);
        rootDept.setAncestors("0");
        rootDept.setOrderNum(1);
        rootDept.setStatus(0);

        childDept = new SysDept();
        childDept.setId(2L);
        childDept.setDeptName("研发部");
        childDept.setParentId(1L);
        childDept.setAncestors("0,1");
        childDept.setOrderNum(1);
        childDept.setStatus(0);
    }

    @Test
    @DisplayName("构建部门树 - 正确的父子结构")
    void buildDeptTree() {
        List<SysDept> depts = List.of(rootDept, childDept);

        List<SysDept> tree = deptService.buildDeptTree(depts);

        assertEquals(1, tree.size());
        assertEquals("总公司", tree.get(0).getDeptName());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals("研发部", tree.get(0).getChildren().get(0).getDeptName());
    }

    @Test
    @DisplayName("构建部门树 - 空列表")
    void buildDeptTree_empty() {
        List<SysDept> tree = deptService.buildDeptTree(new ArrayList<>());

        assertTrue(tree.isEmpty());
    }

    @Test
    @DisplayName("新增部门 - 设置正确的 ancestors")
    void insertDept_withParent() {
        SysDept newDept = new SysDept();
        newDept.setDeptName("测试部");
        newDept.setParentId(1L);

        when(deptMapper.selectById(1L)).thenReturn(rootDept);
        when(deptMapper.insert(any(SysDept.class))).thenReturn(1);

        int result = deptService.insertDept(newDept);

        assertEquals(1, result);
        assertEquals("0,1", newDept.getAncestors());
    }

    @Test
    @DisplayName("新增顶级部门 - ancestors 为 0")
    void insertDept_root() {
        SysDept newDept = new SysDept();
        newDept.setDeptName("新集团");
        newDept.setParentId(0L);

        when(deptMapper.insert(any(SysDept.class))).thenReturn(1);

        deptService.insertDept(newDept);

        assertEquals("0", newDept.getAncestors());
    }

    @Test
    @DisplayName("删除部门 - 存在子部门时抛异常")
    void deleteDept_hasChildren() {
        when(deptMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        assertThrows(BusinessException.class, () -> deptService.deleteDeptById(1L));
        verify(deptMapper, never()).deleteById(any());
    }

    @Test
    @DisplayName("删除部门 - 无子部门正常删除")
    void deleteDept_noChildren() {
        when(deptMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(deptMapper.deleteById(2L)).thenReturn(1);

        int result = deptService.deleteDeptById(2L);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("判断是否有子部门")
    void hasChildByDeptId() {
        when(deptMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertTrue(deptService.hasChildByDeptId(1L));
    }
}
