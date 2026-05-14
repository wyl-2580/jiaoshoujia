package com.jiaoshoujia.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaoshoujia.common.utils.StringUtils;
import com.jiaoshoujia.generator.domain.GenTable;
import com.jiaoshoujia.generator.domain.GenTableColumn;
import com.jiaoshoujia.generator.mapper.GenTableColumnMapper;
import com.jiaoshoujia.generator.mapper.GenTableMapper;
import com.jiaoshoujia.generator.service.IGenTableService;
import com.jiaoshoujia.generator.util.VelocityUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class GenTableServiceImpl implements IGenTableService {

    private static final Logger log = LoggerFactory.getLogger(GenTableServiceImpl.class);

    private static final Map<String, String> COLUMN_TYPE_MAP = new LinkedHashMap<>();

    static {
        COLUMN_TYPE_MAP.put("bigint", "Long");
        COLUMN_TYPE_MAP.put("int", "Integer");
        COLUMN_TYPE_MAP.put("integer", "Integer");
        COLUMN_TYPE_MAP.put("tinyint", "Integer");
        COLUMN_TYPE_MAP.put("smallint", "Integer");
        COLUMN_TYPE_MAP.put("mediumint", "Integer");
        COLUMN_TYPE_MAP.put("bit", "Boolean");
        COLUMN_TYPE_MAP.put("float", "BigDecimal");
        COLUMN_TYPE_MAP.put("double", "BigDecimal");
        COLUMN_TYPE_MAP.put("decimal", "BigDecimal");
        COLUMN_TYPE_MAP.put("numeric", "BigDecimal");
        COLUMN_TYPE_MAP.put("char", "String");
        COLUMN_TYPE_MAP.put("varchar", "String");
        COLUMN_TYPE_MAP.put("text", "String");
        COLUMN_TYPE_MAP.put("mediumtext", "String");
        COLUMN_TYPE_MAP.put("longtext", "String");
        COLUMN_TYPE_MAP.put("enum", "String");
        COLUMN_TYPE_MAP.put("set", "String");
        COLUMN_TYPE_MAP.put("date", "LocalDate");
        COLUMN_TYPE_MAP.put("datetime", "LocalDateTime");
        COLUMN_TYPE_MAP.put("timestamp", "LocalDateTime");
        COLUMN_TYPE_MAP.put("time", "LocalTime");
        COLUMN_TYPE_MAP.put("blob", "byte[]");
    }

    private final GenTableMapper genTableMapper;
    private final GenTableColumnMapper genTableColumnMapper;

    public GenTableServiceImpl(GenTableMapper genTableMapper,
                               GenTableColumnMapper genTableColumnMapper) {
        this.genTableMapper = genTableMapper;
        this.genTableColumnMapper = genTableColumnMapper;
    }

    @Override
    public List<GenTable> selectGenTableList(GenTable genTable) {
        LambdaQueryWrapper<GenTable> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(genTable.getTableName())) {
            wrapper.like(GenTable::getTableName, genTable.getTableName());
        }
        if (StringUtils.isNotEmpty(genTable.getTableComment())) {
            wrapper.like(GenTable::getTableComment, genTable.getTableComment());
        }
        wrapper.orderByDesc(GenTable::getUpdateTime);
        return genTableMapper.selectList(wrapper);
    }

    @Override
    public GenTable selectGenTableById(Long id) {
        GenTable genTable = genTableMapper.selectGenTableById(id);
        if (genTable != null) {
            List<GenTableColumn> columns = genTableColumnMapper.selectGenTableColumnListByTableId(id);
            genTable.setColumns(columns);
        }
        return genTable;
    }

    @Override
    public List<Map<String, String>> selectDbTableList() {
        return genTableMapper.selectDbTableList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importTable(String[] tableNames) {
        for (String tableName : tableNames) {
            GenTable genTable = buildGenTable(tableName);
            genTableMapper.insert(genTable);

            List<Map<String, String>> dbColumns = genTableMapper.selectDbTableColumnsByName(tableName);
            for (Map<String, String> columnInfo : dbColumns) {
                GenTableColumn column = buildGenTableColumn(genTable.getId(), columnInfo);
                genTableColumnMapper.insert(column);
            }
        }
    }

    @Override
    public byte[] generateCode(Long tableId) {
        GenTable table = selectGenTableById(tableId);
        if (table == null) {
            throw new IllegalArgumentException("表信息不存在: " + tableId);
        }

        VelocityEngine engine = VelocityUtils.initVelocityEngine();
        VelocityContext context = VelocityUtils.prepareContext(table);
        List<String> templates = VelocityUtils.getTemplateList();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (String templatePath : templates) {
                StringWriter sw = new StringWriter();
                Template tpl = engine.getTemplate(templatePath, "UTF-8");
                tpl.merge(context, sw);

                String zipEntryName = VelocityUtils.getFileName(templatePath, table);
                zos.putNextEntry(new ZipEntry(zipEntryName));
                zos.write(sw.toString().getBytes());
                zos.flush();
                zos.closeEntry();
            }
            zos.finish();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("代码生成失败", e);
            throw new RuntimeException("代码生成失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGenTableByIds(Long[] ids) {
        genTableMapper.deleteBatchIds(Arrays.asList(ids));
        genTableColumnMapper.deleteGenTableColumnByTableIds(ids);
    }

    private GenTable buildGenTable(String tableName) {
        List<Map<String, String>> tableInfo = genTableMapper.selectDbTableList();
        String tableComment = "";
        for (Map<String, String> info : tableInfo) {
            if (tableName.equalsIgnoreCase(info.get("tableName"))) {
                tableComment = info.get("tableComment");
                break;
            }
        }

        GenTable genTable = new GenTable();
        genTable.setTableName(tableName);
        genTable.setTableComment(tableComment);
        genTable.setClassName(convertClassName(tableName));
        genTable.setPackageName("com.jiaoshoujia");
        genTable.setModuleName(getModuleName(tableName));
        genTable.setBusinessName(getBusinessName(tableName));
        genTable.setFunctionName(tableComment);
        genTable.setAuthor("jiaoshoujia");
        genTable.setGenType("0");
        return genTable;
    }

    private GenTableColumn buildGenTableColumn(Long tableId, Map<String, String> columnInfo) {
        String columnName = columnInfo.get("columnName");
        String columnType = columnInfo.get("columnType");
        String columnComment = columnInfo.getOrDefault("columnComment", "");
        String columnKey = columnInfo.getOrDefault("columnKey", "");

        GenTableColumn column = new GenTableColumn();
        column.setTableId(tableId);
        column.setColumnName(columnName);
        column.setColumnComment(columnComment);
        column.setColumnType(columnType);
        column.setJavaType(convertJavaType(columnType));
        column.setJavaField(toCamelCase(columnName));
        column.setIsPk("PRI".equalsIgnoreCase(columnKey) ? 0 : 1);
        column.setIsInsert(1);
        column.setIsEdit(1);
        column.setIsList(1);
        column.setIsQuery(0);
        column.setQueryType("EQ");
        column.setHtmlType(resolveHtmlType(columnType));
        return column;
    }

    private String convertClassName(String tableName) {
        String name = tableName;
        if (name.contains("_")) {
            int firstUnderscore = name.indexOf('_');
            name = name.substring(firstUnderscore + 1);
        }
        StringBuilder sb = new StringBuilder();
        for (String part : name.split("_")) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    sb.append(part.substring(1).toLowerCase());
                }
            }
        }
        return sb.toString();
    }

    private String toCamelCase(String name) {
        StringBuilder sb = new StringBuilder();
        String[] parts = name.split("_");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            if (i == 0) {
                sb.append(parts[i].toLowerCase());
            } else {
                sb.append(Character.toUpperCase(parts[i].charAt(0)));
                if (parts[i].length() > 1) {
                    sb.append(parts[i].substring(1).toLowerCase());
                }
            }
        }
        return sb.toString();
    }

    private String convertJavaType(String columnType) {
        String baseType = columnType.toLowerCase().replaceAll("\\(.*\\)", "").trim();
        return COLUMN_TYPE_MAP.getOrDefault(baseType, "String");
    }

    private String resolveHtmlType(String columnType) {
        String baseType = columnType.toLowerCase().replaceAll("\\(.*\\)", "").trim();
        if ("datetime".equals(baseType) || "timestamp".equals(baseType) || "date".equals(baseType)) {
            return "datetime";
        }
        if ("text".equals(baseType) || "mediumtext".equals(baseType) || "longtext".equals(baseType)) {
            return "textarea";
        }
        return "input";
    }

    private String getModuleName(String tableName) {
        int idx = tableName.indexOf('_');
        return idx > 0 ? tableName.substring(0, idx) : tableName;
    }

    private String getBusinessName(String tableName) {
        int idx = tableName.lastIndexOf('_');
        return idx > 0 ? tableName.substring(idx + 1) : tableName;
    }
}
