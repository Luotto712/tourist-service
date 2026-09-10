package com.tourist.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FileAttachmentResponse {
    private Long id;
    private String relatedType;
    private Long relatedId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileExt;
    private Long uploaderId;
    private String uploaderName;
    private LocalDateTime createTime;
}
