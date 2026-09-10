package com.tourist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("hotel_room")
public class HotelRoom {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("hotel_id")
    private Long hotelId;

    @TableField("hotel_type")
    private String hotelType;

    @TableField("room_type")
    private String roomType;

    @TableField("total")
    private Integer total;

    @TableField("booked")
    private Integer booked;

    @TableField("price")
    private BigDecimal price;

    @TableField("date")
    private LocalDate date;

    @TableField("update_user_id")
    private Long updateUserId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
