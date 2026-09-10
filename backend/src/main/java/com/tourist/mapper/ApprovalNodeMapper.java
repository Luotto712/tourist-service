package com.tourist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourist.entity.ApprovalNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ApprovalNodeMapper extends BaseMapper<ApprovalNode> {
    List<ApprovalNode> selectByFlowId(@Param("flowId") Long flowId);
    List<ApprovalNode> selectByFlowIdOrdered(@Param("flowId") Long flowId);
    int deleteByFlowId(@Param("flowId") Long flowId);
}
