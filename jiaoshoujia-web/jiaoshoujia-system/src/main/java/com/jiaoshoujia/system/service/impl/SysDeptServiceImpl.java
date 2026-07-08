package com.jiaoshoujia.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaoshoujia.common.exception.BusinessException;
import com.jiaoshoujia.common.utils.MessageUtils;
import com.jiaoshoujia.common.utils.StringUtils;
import com.jiaoshoujia.system.domain.SysDept;
import com.jiaoshoujia.system.domain.SysUser;
import com.jiaoshoujia.system.mapper.SysDeptMapper;
import com.jiaoshoujia.system.mapper.SysUserMapper;
import com.jiaoshoujia.system.service.ISysDeptService;
import org.springframework.stereotype.Service;

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    private final SysUserMapper userMapper;

    public SysDeptServiceImpl(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

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
        return save(dept) ? 1 : 0;
    }

    @Override
    public int updateDept(SysDept dept) {
        SysDept oldDept = getById(dept.getId());
        if (dept.getParentId() != null && dept.getParentId().equals(dept.getId())) {
            throw new BusinessException("上级部门不能是自己或下级部门");
        }
        if (dept.getParentId() != null && dept.getParentId() != 0L) {
            SysDept parent = getById(dept.getParentId());
            if (parent != null && parent.getAncestors() != null) {
                if (java.util.Arrays.asList(parent.getAncestors().split(",")).contains(String.valueOf(dept.getId()))) {
                    throw new BusinessException("上级部门不能是自己或下级部门");
                }
            }
        }
        String newAncestors;
        if (dept.getParentId() != null && dept.getParentId() != 0L) {
            SysDept parent = getById(dept.getParentId());
            newAncestors = (parent != null) ? parent.getAncestors() + "," + parent.getId() : "0";
        } else {
            newAncestors = "0";
        }
        dept.setAncestors(newAncestors);
        updateById(dept);

        if (oldDept != null && !newAncestors.equals(oldDept.getAncestors())) {
            updateChildrenAncestors(dept.getId(), oldDept.getAncestors() + "," + oldDept.getId(),
                    newAncestors + "," + dept.getId());
        }
        return 1;
    }

    private void updateChildrenAncestors(Long parentId, String oldPrefix, String newPrefix) {
        List<SysDept> children = lambdaQuery().eq(SysDept::getParentId, parentId).list();
        for (SysDept child : children) {
            String childAncestors = child.getAncestors();
            if (childAncestors != null && childAncestors.startsWith(oldPrefix)) {
                child.setAncestors(newPrefix + childAncestors.substring(oldPrefix.length()));
            } else {
                child.setAncestors(newPrefix);
            }
            updateById(child);
            updateChildrenAncestors(child.getId(),
                    oldPrefix + "," + child.getId(),
                    child.getAncestors() + "," + child.getId());
        }
    }

    @Override
    public int deleteDeptById(Long deptId) {
        if (hasChildByDeptId(deptId)) {
            throw new BusinessException(MessageUtils.message("dept.has.children"));
        }
        if (hasUserByDeptId(deptId)) {
            throw new BusinessException("部门下存在用户，不允许删除");
        }
        return removeById(deptId) ? 1 : 0;
    }

    private boolean hasUserByDeptId(Long deptId) {
        return userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, deptId)) > 0;
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
