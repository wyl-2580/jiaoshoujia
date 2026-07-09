package com.jiaoshoujia.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaoshoujia.domain.SysDictData;
import com.jiaoshoujia.domain.SysDictType;

public interface ISysDictTypeService extends IService<SysDictType> {

    Page<SysDictType> selectDictTypePage(Page<SysDictType> page, SysDictType dictType);

    SysDictType selectDictTypeById(Long dictId);

    List<SysDictType> selectDictTypeAll();

    List<SysDictData> selectDictDataByType(String dictType);

    int insertDictType(SysDictType dictType);

    int updateDictType(SysDictType dictType);

    int deleteDictTypeByIds(Long[] ids);
}
