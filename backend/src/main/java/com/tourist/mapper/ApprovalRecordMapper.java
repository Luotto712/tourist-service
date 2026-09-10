package com.tourist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourist.entity.ApprovalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecord> {
    List<ApprovalRecord> selectBySubject(@Param("subjectType") String subjectType, @Param("subjectId") Long subjectId);
    int deleteBySubject(@Param("subjectType") String subjectType, @Param("subjectId") Long subjectId);
    IPage<ApprovalRecord> listApprovalRecords(Page<ApprovalRecord> page,
                                              @Param("subjectType") String subjectType,
                                              @Param("subjectId") Long subjectId,
                                              @Param("action") String action);
}
