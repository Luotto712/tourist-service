package com.tourist.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ComplaintSubmitRequest {
    @NotBlank(message = "投诉内容不能为空")
    private String content;
}
