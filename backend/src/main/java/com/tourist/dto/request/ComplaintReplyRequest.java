package com.tourist.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ComplaintReplyRequest {
    @NotBlank(message = "回复内容不能为空")
    private String content;
}
