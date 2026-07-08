package com.jiaoshoujia.controller.common;

import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/common")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Value("${app.file.upload-path:./uploads}")
    private String uploadPath;

    @Value("${app.file.allowed-extensions:jpg,png,gif,bmp,doc,docx,xls,xlsx,pdf,txt,zip}")
    private String allowedExtensions;

    @Value("${app.file.max-size:10485760}")
    private long maxSize;

    @PostMapping("/upload")
    public R<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("上传文件不能为空");
        }
        if (file.getSize() > maxSize) {
            return R.fail("文件大小不能超过 " + (maxSize / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        if (!isAllowedExtension(extension)) {
            return R.fail("不支持的文件类型: " + extension);
        }

        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path targetDir = Paths.get(uploadPath, datePath);
            Files.createDirectories(targetDir);

            Path targetFile = targetDir.resolve(fileName);
            file.transferTo(targetFile.toFile());

            String relativePath = datePath + "/" + fileName;
            Map<String, String> result = new HashMap<>();
            result.put("fileName", relativePath);
            result.put("url", "/common/download?fileName=" + relativePath);
            return R.ok(result);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return R.fail("文件上传失败");
        }
    }

    @GetMapping("/download")
    public void download(@RequestParam(name = "fileName") String fileName, HttpServletResponse response) {
        if (StringUtils.isEmpty(fileName) || fileName.contains("..")) {
            throw new IllegalArgumentException("文件名不合法");
        }

        Path basePath = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path filePath = basePath.resolve(fileName).normalize();
        if (!filePath.startsWith(basePath) || !Files.exists(filePath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            String realFileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(realFileName, StandardCharsets.UTF_8));

            try (InputStream is = Files.newInputStream(filePath);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        } catch (IOException e) {
            log.error("文件下载失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String getExtension(String filename) {
        if (StringUtils.isEmpty(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isAllowedExtension(String extension) {
        if (StringUtils.isEmpty(extension)) {
            return false;
        }
        Set<String> allowed = new HashSet<>(Arrays.asList(allowedExtensions.split(",")));
        return allowed.contains(extension.toLowerCase());
    }
}
