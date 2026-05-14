package com.jiaoshoujia.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaoshoujia.common.annotation.DataScope;
import com.jiaoshoujia.common.exception.BusinessException;
import com.jiaoshoujia.common.utils.MessageUtils;
import com.jiaoshoujia.common.utils.StringUtils;
import com.jiaoshoujia.system.domain.SysDept;
import com.jiaoshoujia.system.mapper.SysDeptMapper;
import com.jiaoshoujia.system.service.ISysDeptService;
import org.springframework.stereotype.Service;

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    @DataScope(deptAlias = "sys_dept")
    @Override
    public List<SysDept> selectDeptList(SysDept dept) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(dept.getDeptName()), SysDept::getDeptName, dept.getDeptName())
                .eq(dept.getStatus() != null, SysDept::getStatus, dept.getStatus())
                .orderByAsc(SysDept::getParentId)
                .orderByAsc(SysDept::getOrderNum);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public SysDept selectDeptById(Long deptId) {
        return getById(deptId);
    }

    @Override
    public List<SysDept> buildDeptTree(List<SysDept> depts) {
        Set<Long> idSet = depts.stream().map(SysDept::getId).collect(Collectors.toSet());
        List<SysDept> roots = new ArrayList<>();
        for (SysDept dept : depts) {
            if (dept.getParentId() == null || dept.getParentId() == 0L || !idSet.contains(dept.getParentId())) {
                roots.add(dept);
            }
        }
        for (SysDept root : roots) {
            buildChildren(root, depts);
        }
        return roots;
    }

    @Override
    public int insertDept(SysDept dept) {
        if (dept.getParentId() != null && dept.getParentId() != 0L) {
            SysDept parent = getById(dept.getParentId());
            if (parent != null) {
                dept.setAncestors(parent.getAncestors() + "," + parent.getId());
            }
        } else {
            dept.setAncestors("0");
        }
        save(dept);
        return 1;
    }

    @Override
    public int updateDept(SysDept dept) {
        if (dept.getParentId() != null && dept.getParentId() != 0L) {
            SysDept parent = getById(dept.getParentId());
            if (parent != null) {
                dept.setAncestors(parent.getAncestors() + "," + parent.getId());
            }
        }
        updateById(dept);
        return 1;
    }

    @Override
    public int deleteDeptById(Long deptId) {
        if (hasChildByDeptId(deptId)) {
            throw new BusinessException(MessageUtils.message("dept.has.children"));
        }
        removeById(deptId);
        return 1;
    }

    @Override
    public boolean hasChildByDeptId(Long deptId) {
        return lambdaQuery().eq(SysDept::getParentId, deptId).count() > 0;
    }

    private void buildChildren(SysDept parent, List<SysDept> allDepts) {
        List<SysDept> children = allDepts.stream()
                .filter(d -> parent.getId().equals(d.getParentId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        for (SysDept child : children) {
            buildChildren(child, allDepts);
        }
    }
}
