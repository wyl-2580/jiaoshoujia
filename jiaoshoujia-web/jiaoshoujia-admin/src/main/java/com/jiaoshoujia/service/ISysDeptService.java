package com.jiaoshoujia.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaoshoujia.domain.SysDept;

public interface ISysDeptService extends IService<SysDept> {

    List<SysDept> selectDeptList(SysDept dept);

    SysDept selectDeptById(Long deptId);

    List<SysDept> buildDeptTree(List<SysDept> depts);

    int insertDept(SysDept dept);

    int updateDept(SysDept dept);

    int deleteDeptById(Long deptId);

    boolean hasChildByDeptId(Long deptId);
}
