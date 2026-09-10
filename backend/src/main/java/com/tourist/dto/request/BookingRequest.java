package com.tourist.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class BookingRequest {
    @NotNull(message = "酒店类型不能为空")
    private String hotelType;

    @NotNull(message = "酒店不能为空")
    private Long hotelId;

    @NotNull(message = "房型不能为空")
    private String roomType;

    @NotNull(message = "入住日期不能为空")
    private LocalDate checkIn;

    @NotNull(message = "离开日期不能为空")
    private LocalDate checkOut;

    private Integer guests;
}
