package com.tourist.service;

import com.tourist.dto.response.FileAttachmentResponse;
import com.tourist.entity.FileAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    FileAttachmentResponse uploadFile(MultipartFile file, String relatedType, Long relatedId, Long uploaderId);

    FileAttachment downloadFile(Long fileId);

    void deleteFile(Long fileId);

    List<FileAttachmentResponse> getFilesByRelated(String relatedType, Long relatedId);

    List<FileAttachment> getFilesByRelated(String relatedType, List<Long> relatedIds);
}
