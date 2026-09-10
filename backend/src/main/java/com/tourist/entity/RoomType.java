package com.tourist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 房型基准：每酒店每房型一行，酒店管理员维护总房量/基准已预定/单价。 */
@Data
@TableName("room_type")
public class RoomType {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("hotel_type")
    private String hotelType;

    @TableField("hotel_id")
    private Long hotelId;

    @TableField("room_type")
    private String roomType;

    @TableField("total")
    private Integer total;

    @TableField("base_booked")
    private Integer baseBooked;

    @TableField("price")
    private BigDecimal price;

    @TableField("update_user_id")
    private Long updateUserId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
