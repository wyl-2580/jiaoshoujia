package com.jiaoshoujia.generator.util;

import com.jiaoshoujia.generator.domain.GenTable;
import com.jiaoshoujia.generator.domain.GenTableColumn;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public final class VelocityUtils {

    private VelocityUtils() {
    }

    public static VelocityEngine initVelocityEngine() {
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
        engine.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
        engine.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8");
        engine.init();
        return engine;
    }

    public static VelocityContext prepareContext(GenTable table) {
        VelocityContext context = new VelocityContext();
        context.put("tableName", table.getTableName());
        context.put("tableComment", table.getTableComment());
        context.put("className", table.getClassName());
        context.put("classname", uncapitalize(table.getClassName()));
        context.put("packageName", table.getPackageName());
        context.put("moduleName", table.getModuleName());
        context.put("businessName", table.getBusinessName());
        context.put("functionName", table.getFunctionName());
        context.put("author", table.getAuthor());
        context.put("datetime", LocalDate.now().toString());
        context.put("columns", table.getColumns());

        List<GenTableColumn> pkColumns = Optional.ofNullable(table.getColumns())
                .orElse(Collections.emptyList()).stream()
                .filter(c -> c.getIsPk() != null && c.getIsPk() == 0)
                .collect(Collectors.toList());
        context.put("pkColumn", pkColumns.isEmpty() ? null : pkColumns.get(0));

        Set<String> importSet = new TreeSet<>();
        if (table.getColumns() != null) {
            for (GenTableColumn col : table.getColumns()) {
                String javaType = col.getJavaType();
                if ("BigDecimal".equals(javaType)) {
                    importSet.add("java.math.BigDecimal");
                } else if ("LocalDateTime".equals(javaType)) {
                    importSet.add("java.time.LocalDateTime");
                } else if ("LocalDate".equals(javaType)) {
                    importSet.add("java.time.LocalDate");
                } else if ("LocalTime".equals(javaType)) {
                    importSet.add("java.time.LocalTime");
                }
            }
        }
        context.put("importList", importSet);

        return context;
    }

    public static List<String> getTemplateList() {
        List<String> templates = new ArrayList<>();
        templates.add("vm/java/domain.java.vm");
        templates.add("vm/java/mapper.java.vm");
        templates.add("vm/java/service.java.vm");
        templates.add("vm/java/serviceImpl.java.vm");
        templates.add("vm/java/controller.java.vm");
        templates.add("vm/xml/mapper.xml.vm");
        templates.add("vm/vue/index.vue.vm");
        templates.add("vm/sql/sql.vm");
        return templates;
    }

    public static String getFileName(String template, GenTable table) {
        String packagePath = table.getPackageName().replace(".", "/");
        String className = table.getClassName();
        String moduleName = table.getModuleName();
        String businessName = table.getBusinessName();

        if (template.contains("domain.java.vm")) {
            return packagePath + "/" + moduleName + "/domain/" + className + ".java";
        }
        if (template.contains("mapper.java.vm")) {
            return packagePath + "/" + moduleName + "/mapper/" + className + "Mapper.java";
        }
        if (template.contains("service.java.vm")) {
            return packagePath + "/" + moduleName + "/service/I" + className + "Service.java";
        }
        if (template.contains("serviceImpl.java.vm")) {
            return packagePath + "/" + moduleName + "/service/impl/" + className + "ServiceImpl.java";
        }
        if (template.contains("controller.java.vm")) {
            return packagePath + "/controller/" + moduleName + "/" + className + "Controller.java";
        }
        if (template.contains("mapper.xml.vm")) {
            return "mapper/" + moduleName + "/" + className + "Mapper.xml";
        }
        if (template.contains("index.vue.vm")) {
            return "vue/views/" + moduleName + "/" + businessName + "/index.vue";
        }
        if (template.contains("sql.vm")) {
            return "sql/" + businessName + "_menu.sql";
        }
        return template;
    }

    private static String uncapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
