package com.tourist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourist.entity.ApprovalFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApprovalFlowMapper extends BaseMapper<ApprovalFlow> {
    ApprovalFlow selectBySubject(@Param("subjectType") String subjectType, @Param("subjectId") Long subjectId);
    int deleteBySubject(@Param("subjectType") String subjectType, @Param("subjectId") Long subjectId);
}
