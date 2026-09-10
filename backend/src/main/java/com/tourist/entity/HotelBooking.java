package com.tourist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 游客预订记录：房型 + 入住/离开日期 + 人数 + 单价快照 + 总价 + 状态。 */
@Data
@TableName("hotel_booking")
public class HotelBooking {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("hotel_type")
    private String hotelType;

    @TableField("hotel_id")
    private Long hotelId;

    @TableField("hotel_name")
    private String hotelName;

    @TableField("room_type")
    private String roomType;

    @TableField("price")
    private BigDecimal price;

    @TableField("check_in")
    private LocalDate checkIn;

    @TableField("check_out")
    private LocalDate checkOut;

    @TableField("guests")
    private Integer guests;

    @TableField("total_price")
    private BigDecimal totalPrice;

    @TableField("status")
    private String status;

    @TableField("create_time")
    private LocalDateTime createTime;
}
