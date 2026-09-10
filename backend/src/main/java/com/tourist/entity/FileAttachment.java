package com.tourist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_attachment")
public class FileAttachment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("related_type")
    private String relatedType;

    @TableField("related_id")
    private Long relatedId;

    @TableField("file_name")
    private String fileName;

    @TableField("file_path")
    private String filePath;

    @TableField("file_size")
    private Long fileSize;

    @TableField("file_ext")
    private String fileExt;

    @TableField("uploader_id")
    private Long uploaderId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String uploaderName;
}
