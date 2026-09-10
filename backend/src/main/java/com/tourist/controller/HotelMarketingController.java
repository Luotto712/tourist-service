package com.tourist.controller;

import com.tourist.annotation.CurrentUser;
import com.tourist.annotation.CurrentUserInfo;
import com.tourist.annotation.RequireRole;
import com.tourist.common.Result;
import com.tourist.common.RoleConstants;
import com.tourist.entity.HotelMarketing;
import com.tourist.service.HotelMarketingService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/hotel-marketing")
public class HotelMarketingController {

    @Resource
    private HotelMarketingService hotelMarketingService;

    /** 查看营销推荐（游客查看 + 平台管理员管理） */
    @GetMapping
    @RequireRole({RoleConstants.TOURIST, RoleConstants.PLATFORM_ADMIN})
    public Result<List<HotelMarketing>> list(@RequestParam(required = false) String hotelType,
                                             @RequestParam(required = false) Long hotelId) {
        return Result.success(hotelMarketingService.list(hotelType, hotelId));
    }

    /** 平台管理员录入营销推荐（并通知游客） */
    @PostMapping
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<HotelMarketing> create(@CurrentUser CurrentUserInfo currentUser, @RequestBody HotelMarketing marketing) {
        return Result.success(hotelMarketingService.create(currentUser.getUserId(), marketing));
    }

    /** 平台管理员编辑并重新发布 */
    @PutMapping("/{id}")
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<HotelMarketing> update(@PathVariable Long id, @RequestBody HotelMarketing marketing) {
        return Result.success(hotelMarketingService.update(id, marketing));
    }

    /** 平台管理员彻底删除营销推荐 */
    @DeleteMapping("/{id}")
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<String> delete(@PathVariable Long id) {
        hotelMarketingService.delete(id);
        return Result.success("删除成功");
    }
}
