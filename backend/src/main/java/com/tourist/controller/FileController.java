package com.tourist.controller;

import com.tourist.annotation.CurrentUser;
import com.tourist.annotation.CurrentUserInfo;
import com.tourist.common.BusinessException;
import com.tourist.common.Result;
import com.tourist.common.ResultCode;
import com.tourist.dto.response.FileAttachmentResponse;
import com.tourist.entity.FileAttachment;
import com.tourist.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<FileAttachmentResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "relatedType", required = false, defaultValue = "") String relatedType,
            @RequestParam(value = "relatedId", required = false) Long relatedId,
            @CurrentUser CurrentUserInfo currentUser) {
        FileAttachmentResponse response = fileService.uploadFile(file, relatedType, relatedId, currentUser.getUserId());
        return Result.success(response);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            FileAttachment fileInfo = fileService.downloadFile(id);
            if (fileInfo == null) {
                return ResponseEntity.notFound().build();
            }

            String filePath = uploadPath;
            if (!filePath.endsWith("/") && !filePath.endsWith("\\")) {
                filePath += File.separator;
            }
            filePath += fileInfo.getFilePath();

            File file = new File(filePath);
            if (!file.exists()) {
                log.warn("File not found on disk: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);
            String encodedFileName = URLEncoder.encode(fileInfo.getFileName(), StandardCharsets.UTF_8.name())
                    .replace("+", "%20");

            String contentType = getContentType(fileInfo.getFileExt());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encodedFileName)
                    .body(resource);

        } catch (Exception e) {
            log.error("File download error: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件下载失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        return Result.success();
    }

    @PostMapping("/batch-download")
    public void batchDownload(@RequestBody java.util.Map<String, Object> body,
                              HttpServletResponse response) throws Exception {
        String relatedType = (String) body.get("relatedType");
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("relatedIds");
        List<Long> relatedIds = rawIds.stream().map(Long::valueOf).collect(java.util.stream.Collectors.toList());

        List<FileAttachment> files = fileService.getFilesByRelated(relatedType, relatedIds);
        if (files == null || files.isEmpty()) {
            response.setStatus(404);
            return;
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=files.zip");
        ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());

        for (FileAttachment fileInfo : files) {
            String filePath = uploadPath;
            if (!filePath.endsWith("/") && !filePath.endsWith("\\")) {
                filePath += File.separator;
            }
            filePath += fileInfo.getFilePath();
            File file = new File(filePath);
            if (file.exists()) {
                zos.putNextEntry(new ZipEntry(fileInfo.getFileName()));
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
                zos.closeEntry();
            }
        }
        zos.finish();
        zos.flush();
    }

    private String getContentType(String fileExt) {
        if (fileExt == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        switch (fileExt.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";
            default:
                return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

}
