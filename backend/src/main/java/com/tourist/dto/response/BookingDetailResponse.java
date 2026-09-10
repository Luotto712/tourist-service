package com.tourist.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 某房型的预订明细（酒店管理员查看）。 */
@Data
public class BookingDetailResponse {
    private Long id;
    private String userName;
    private String roomType;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer guests;
    private BigDecimal totalPrice;
    private String status;
}
