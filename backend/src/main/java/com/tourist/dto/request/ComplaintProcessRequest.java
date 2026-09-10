package com.tourist.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ComplaintProcessRequest {
    @NotBlank(message = "处理结果不能为空")
    private String result;
}
