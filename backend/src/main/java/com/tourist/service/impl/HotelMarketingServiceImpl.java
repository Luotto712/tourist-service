package com.tourist.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourist.common.BusinessException;
import com.tourist.common.ResultCode;
import com.tourist.common.RoleConstants;
import com.tourist.entity.HotelMarketing;
import com.tourist.entity.NonstarHotel;
import com.tourist.entity.StarHotel;
import com.tourist.entity.User;
import com.tourist.enums.NotificationType;
import com.tourist.mapper.HotelMarketingMapper;
import com.tourist.mapper.NonstarHotelMapper;
import com.tourist.mapper.StarHotelMapper;
import com.tourist.mapper.UserMapper;
import com.tourist.service.HotelMarketingService;
import com.tourist.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class HotelMarketingServiceImpl implements HotelMarketingService {

    @Resource
    private HotelMarketingMapper hotelMarketingMapper;

    @Resource
    private StarHotelMapper starHotelMapper;

    @Resource
    private NonstarHotelMapper nonstarHotelMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private NotificationService notificationService;

    @Override
    public List<HotelMarketing> list(String hotelType, Long hotelId) {
        LambdaQueryWrapper<HotelMarketing> w = new LambdaQueryWrapper<HotelMarketing>()
                .orderByDesc(HotelMarketing::getCreateTime);
        if (hotelType != null && !hotelType.isEmpty()) {
            w.eq(HotelMarketing::getHotelType, hotelType);
        }
        if (hotelId != null) {
            w.eq(HotelMarketing::getHotelId, hotelId);
        }
        return hotelMarketingMapper.selectList(w);
    }

    @Override
    @Transactional
    public HotelMarketing create(Long userId, HotelMarketing marketing) {
        validate(marketing);
        marketing.setCreateBy(userId);
        hotelMarketingMapper.insert(marketing);
        notifyTourists(marketing);
        return marketing;
    }

    @Override
    @Transactional
    public HotelMarketing update(Long id, HotelMarketing marketing) {
        HotelMarketing existing = hotelMarketingMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "营销记录不存在");
        }
        validate(marketing);
        existing.setHotelType(marketing.getHotelType());
        existing.setHotelId(marketing.getHotelId());
        existing.setContent(marketing.getContent());
        hotelMarketingMapper.updateById(existing);
        // 保存=重新发布：再次通知游客
        notifyTourists(existing);
        return existing;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        hotelMarketingMapper.hardDeleteById(id);
    }

    private void validate(HotelMarketing marketing) {
        if (marketing.getHotelId() == null || marketing.getHotelType() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "hotelId/hotelType 不能为空");
        }
    }

    /** 通知所有游客：标题=酒店名，内容=营销内容 */
    private void notifyTourists(HotelMarketing marketing) {
        String hotelName = hotelName(marketing.getHotelType(), marketing.getHotelId());
        List<User> tourists = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, RoleConstants.TOURIST).eq(User::getStatus, 1));
        for (User t : tourists) {
            notificationService.sendNotification(t.getId(), hotelName, marketing.getContent(),
                    NotificationType.HOTEL_MARKETING.name(), marketing.getId());
        }
    }

    private String hotelName(String hotelType, Long hotelId) {
        if ("STAR".equals(hotelType)) {
            StarHotel h = starHotelMapper.selectById(hotelId);
            return h == null ? "" : h.getName();
        }
        NonstarHotel h = nonstarHotelMapper.selectById(hotelId);
        return h == null ? "" : h.getName();
    }
}
