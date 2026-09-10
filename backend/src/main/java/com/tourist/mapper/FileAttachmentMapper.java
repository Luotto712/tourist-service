package com.tourist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourist.entity.FileAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FileAttachmentMapper extends BaseMapper<FileAttachment> {
    List<FileAttachment> selectByRelated(@Param("relatedType") String relatedType, @Param("relatedId") Long relatedId);
    int deleteByRelated(@Param("relatedType") String relatedType, @Param("relatedId") Long relatedId);
}
