package com.tourist.dto.response;

import lombok.Data;

@Data
public class RoadInfoResponse {
    private String area;
    private String road;
    private String condition;
    private String note;
    private String updateTime;
}
