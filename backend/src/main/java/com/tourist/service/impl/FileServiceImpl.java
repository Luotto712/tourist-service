package com.tourist.service.impl;

import com.tourist.common.BusinessException;
import com.tourist.common.ResultCode;
import com.tourist.dto.response.FileAttachmentResponse;
import com.tourist.entity.FileAttachment;
import com.tourist.mapper.FileAttachmentMapper;
import com.tourist.mapper.UserMapper;
import com.tourist.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "mp4", "jpg", "jpeg", "png"
    ));

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    @Value("${file.upload.path:./uploads}")
    private String uploadBaseDir;

    @Autowired
    private FileAttachmentMapper fileAttachmentMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public FileAttachmentResponse uploadFile(MultipartFile file, String relatedType, Long relatedId, Long uploaderId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件不能为空");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件大小不能超过50MB");
        }

        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件格式不支持");
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的文件格式: " + ext + "。支持的格式: pdf, doc, docx, xls, xlsx, mp4, jpg, jpeg, png");
        }

        // Generate unique filename
        String yearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String uniqueName = UUID.randomUUID().toString() + "." + ext;
        String typePath = (relatedType != null && !relatedType.isEmpty()) ? relatedType : "general";
        String relativePath = typePath + "/" + yearMonth + "/" + uniqueName;
        Path targetPath = Paths.get(uploadBaseDir, relativePath);

        // Create directories and save file
        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件保存失败: " + e.getMessage());
        }

        // Save database record
        FileAttachment attachment = new FileAttachment();
        attachment.setRelatedType(relatedType);
        attachment.setRelatedId(relatedId);
        attachment.setFileName(originalFilename);
        attachment.setFilePath(relativePath);
        attachment.setFileSize(file.getSize());
        attachment.setFileExt(ext);
        attachment.setUploaderId(uploaderId);
        attachment.setCreateTime(LocalDateTime.now());
        fileAttachmentMapper.insert(attachment);

        return mapToResponse(attachment);
    }

    @Override
    public FileAttachment downloadFile(Long fileId) {
        FileAttachment attachment = fileAttachmentMapper.selectById(fileId);
        if (attachment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }
        return attachment;
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId) {
        FileAttachment attachment = fileAttachmentMapper.selectById(fileId);
        if (attachment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }

        // Delete from disk
        if (attachment.getFilePath() != null) {
            Path filePath = Paths.get(uploadBaseDir, attachment.getFilePath());
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log but continue with DB deletion
            }
        }

        // Delete from database
        fileAttachmentMapper.deleteById(fileId);
    }

    @Override
    public List<FileAttachmentResponse> getFilesByRelated(String relatedType, Long relatedId) {
        List<FileAttachment> files = fileAttachmentMapper.selectByRelated(relatedType, relatedId);
        return files.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<FileAttachment> getFilesByRelated(String relatedType, List<Long> relatedIds) {
        java.util.List<FileAttachment> allFiles = new java.util.ArrayList<>();
        for (Long relatedId : relatedIds) {
            allFiles.addAll(fileAttachmentMapper.selectByRelated(relatedType, relatedId));
        }
        return allFiles;
    }

    private FileAttachmentResponse mapToResponse(FileAttachment attachment) {
        FileAttachmentResponse response = new FileAttachmentResponse();
        response.setId(attachment.getId());
        response.setRelatedType(attachment.getRelatedType());
        response.setRelatedId(attachment.getRelatedId());
        response.setFileName(attachment.getFileName());
        response.setFilePath(attachment.getFilePath());
        response.setFileSize(attachment.getFileSize());
        response.setFileExt(attachment.getFileExt());
        response.setUploaderId(attachment.getUploaderId());
        response.setUploaderName(attachment.getUploaderName());
        response.setCreateTime(attachment.getCreateTime());
        return response;
    }
}
