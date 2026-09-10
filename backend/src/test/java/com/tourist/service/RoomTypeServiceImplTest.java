package com.tourist.service;

import com.tourist.entity.RoomType;
import com.tourist.mapper.HotelBookingMapper;
import com.tourist.mapper.RoomTypeMapper;
import com.tourist.service.impl.RoomTypeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/** 房型：删除（物理，连带其预订）。 */
@ExtendWith(MockitoExtension.class)
class RoomTypeServiceImplTest {

    @Mock private RoomTypeMapper roomTypeMapper;
    @Mock private HotelBookingMapper hotelBookingMapper;

    @InjectMocks private RoomTypeServiceImpl service;

    @Test
    void delete_physicalDeleteAndRemovesBookings() {
        RoomType rt = new RoomType();
        rt.setId(5L);
        rt.setHotelType("STAR");
        rt.setHotelId(1L);
        rt.setRoomType("亲子房");
        when(roomTypeMapper.selectById(5L)).thenReturn(rt);

        service.delete(5L);

        verify(hotelBookingMapper).delete(any());
        verify(roomTypeMapper).hardDeleteById(5L);
    }

    @Test
    void listByHotel_ordersByIdAsc() {
        service.listByHotel("STAR", 1L);
        verify(roomTypeMapper).selectList(any());
    }
}
