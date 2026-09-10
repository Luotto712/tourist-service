package com.tourist.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
public class EmergencyInfoRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;

    private LocalDate validFrom;

    private LocalDate validTo;
}
