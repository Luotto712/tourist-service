package com.tourist.dto.response;

import lombok.Data;

@Data
public class WeatherInfoResponse {
    private String area;
    private String date;
    private String weather;
    private String temp;
    private String wind;
}
