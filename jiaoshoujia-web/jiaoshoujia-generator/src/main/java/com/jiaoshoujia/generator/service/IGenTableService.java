package com.jiaoshoujia.generator.service;

import com.jiaoshoujia.generator.domain.GenTable;

import java.util.List;
import java.util.Map;

public interface IGenTableService {

    List<GenTable> selectGenTableList(GenTable genTable);

    GenTable selectGenTableById(Long id);

    List<Map<String, String>> selectDbTableList();

    void importTable(String[] tableNames);

    Map<String, String> previewCode(Long tableId);

    byte[] generateCode(Long tableId);

    void deleteGenTableByIds(Long[] ids);
}
