package com.jiaoshoujia.common.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class ExcelUtils {

    private static final Logger log = LoggerFactory.getLogger(ExcelUtils.class);

    private ExcelUtils() {}

    public static <T> void exportExcel(HttpServletResponse response, String fileName,
                                       String sheetName, Class<T> clazz, List<T> data) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), clazz).sheet(sheetName).doWrite(data);
        } catch (IOException e) {
            log.error("Excel export failed", e);
            throw new RuntimeException("Excel导出失败: " + e.getMessage());
        }
    }

    public static <T> void exportExcel(HttpServletResponse response, String fileName,
                                       Class<T> clazz, List<T> data) {
        exportExcel(response, fileName, "Sheet1", clazz, data);
    }

    public static <T> List<T> importExcel(InputStream inputStream, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        EasyExcel.read(inputStream, clazz, new PageReadListener<T>(result::addAll)).sheet().doRead();
        return result;
    }
}
