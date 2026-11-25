package com.music.rental.service;

import com.music.rental.common.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** 文件上传：落盘到项目根 uploads/<type>/，返回可直接访问的相对路径 */
@Service
public class UploadService {

    private static final Set<String> ALLOW = Set.of("jpg", "jpeg", "png", "webp", "pdf");

    @Value("${app.upload.dir}")
    private String uploadDir;

    public Map<String, String> save(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) throw new BizException("请选择要上传的文件");
        String origin = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        String ext = origin.contains(".") ? origin.substring(origin.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOW.contains(ext)) throw new BizException("仅支持 jpg/png/webp/pdf 格式");
        if (file.getSize() > 10 * 1024 * 1024L) throw new BizException("单个文件不能超过 10MB");

        String day = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String name = UUID.randomUUID().toString().replace("-", "").substring(0, 16) + "." + ext;
        Path dir = Paths.get(uploadDir, type, day).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(name).toFile());
        } catch (IOException e) {
            throw new BizException("文件保存失败：" + e.getMessage());
        }
        Map<String, String> res = new HashMap<>();
        res.put("url", "/uploads/" + type + "/" + day + "/" + name);
        res.put("originName", origin);
        res.put("size", String.valueOf(file.getSize()));
        return res;
    }
}
