package com.tourist.service;

import com.tourist.entity.HotelMarketing;
import com.tourist.entity.StarHotel;
import com.tourist.entity.User;
import com.tourist.enums.NotificationType;
import com.tourist.mapper.HotelMarketingMapper;
import com.tourist.mapper.NonstarHotelMapper;
import com.tourist.mapper.StarHotelMapper;
import com.tourist.mapper.UserMapper;
import com.tourist.service.impl.HotelMarketingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 酒店营销：录入/编辑均通知游客（标题=酒店名，内容=营销内容）；删除彻底。 */
@ExtendWith(MockitoExtension.class)
class HotelMarketingServiceImplTest {

    @Mock private HotelMarketingMapper hotelMarketingMapper;
    @Mock private StarHotelMapper starHotelMapper;
    @Mock private NonstarHotelMapper nonstarHotelMapper;
    @Mock private UserMapper userMapper;
    @Mock private NotificationService notificationService;

    @InjectMocks private HotelMarketingServiceImpl service;

    private User tourist() { User u = new User(); u.setId(6L); u.setRole("TOURIST"); u.setStatus(1); return u; }

    @Test
    void create_notifiesTouristsWithHotelNameAndContent() {
        StarHotel h = new StarHotel(); h.setName("武侯祠锦华酒店");
        when(starHotelMapper.selectById(1L)).thenReturn(h);
        when(userMapper.selectList(any())).thenReturn(List.of(tourist()));
        when(hotelMarketingMapper.insert(any())).thenAnswer(inv -> { ((HotelMarketing) inv.getArgument(0)).setId(9L); return 1; });

        HotelMarketing m = new HotelMarketing();
        m.setHotelType("STAR"); m.setHotelId(1L); m.setContent("好价来袭");
        service.create(1L, m);

        verify(notificationService).sendNotification(eq(6L), eq("武侯祠锦华酒店"), eq("好价来袭"),
                eq(NotificationType.HOTEL_MARKETING.name()), eq(9L));
    }

    @Test
    void update_republishesAndNotifies() {
        HotelMarketing existing = new HotelMarketing();
        existing.setId(9L); existing.setHotelType("STAR"); existing.setHotelId(1L); existing.setContent("旧");
        when(hotelMarketingMapper.selectById(9L)).thenReturn(existing);
        StarHotel h = new StarHotel(); h.setName("锦华酒店");
        when(starHotelMapper.selectById(1L)).thenReturn(h);
        when(userMapper.selectList(any())).thenReturn(List.of(tourist()));

        HotelMarketing input = new HotelMarketing();
        input.setHotelType("STAR"); input.setHotelId(1L); input.setContent("新内容");
        service.update(9L, input);

        verify(hotelMarketingMapper).updateById(any());
        verify(notificationService).sendNotification(eq(6L), eq("锦华酒店"), eq("新内容"),
                eq(NotificationType.HOTEL_MARKETING.name()), eq(9L));
    }

    @Test
    void delete_hardDeletes() {
        service.delete(9L);
        verify(hotelMarketingMapper).hardDeleteById(9L);
    }
}
