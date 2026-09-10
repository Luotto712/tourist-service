package com.tourist.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/** 房型 + 指定日期的可用/已预定（已预定=基准+覆盖日期的预订数）。 */
@Data
public class RoomAvailabilityResponse {
    private Long roomTypeId;
    private String roomType;
    private Integer total;
    private Integer baseBooked;
    private Integer booked;
    private Integer remaining;
    private BigDecimal price;
}
