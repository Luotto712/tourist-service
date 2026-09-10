package com.tourist.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ComplaintAssignRequest {
    @NotNull(message = "处理人员不能为空")
    private Long handlerId;
}
