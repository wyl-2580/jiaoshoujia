package com.jiaoshoujia.system.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaoshoujia.system.domain.SysDictData;

public interface ISysDictDataService extends IService<SysDictData> {

    Page<SysDictData> selectDictDataPage(Page<SysDictData> page, SysDictData dictData);

    SysDictData selectDictDataById(Long dictCode);

    List<SysDictData> selectDictDataByType(String dictType);

    int insertDictData(SysDictData dictData);

    int updateDictData(SysDictData dictData);

    int deleteDictDataByIds(Long[] ids);
}
