package com.ycb.wxxcx.provider.controller;

import com.ycb.wxxcx.provider.cache.RedisService;
import com.ycb.wxxcx.provider.mapper.OrderMapper;
import com.ycb.wxxcx.provider.mapper.ShopMapper;
import com.ycb.wxxcx.provider.mapper.StationMapper;
import com.ycb.wxxcx.provider.mapper.UserMapper;
import com.ycb.wxxcx.provider.service.FeeStrategyService;
import com.ycb.wxxcx.provider.utils.JsonUtils;
import com.ycb.wxxcx.provider.vo.FeeStrategy;
import com.ycb.wxxcx.provider.vo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhuhui on 17-8-7.
 */
@RestController
@RequestMapping("borrow")
public class BorrowController {

    public static final Logger logger = LoggerFactory.getLogger(BorrowController.class);

    @Autowired
    private RedisService redisService;

    @Autowired
    private FeeStrategyService feeStrategyService;

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ShopMapper shopMapper;

    @RequestMapping(value = "/getMachineInfo", method = RequestMethod.POST)
    @ResponseBody
    public String getMachineInfo(@RequestParam("session") String session,
                                 @RequestParam("qrcode") String qrcode) {
        Map<String, Object> bacMap = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        try {
            User user = this.userMapper.findUserinfoByOpenid(redisService.getKeyValue(session));
            // 解析qrcode，根据机器sid，获取机器状态属性值
            String[] urlArr = qrcode.trim().toLowerCase().split("/");
            String sid = urlArr[urlArr.length - 1];
            String cable_type = stationMapper.getUsableBatteries(Long.valueOf(sid));
            // 判断设备是否在线
            String lastHearTime = redisService.getKeyValue(sid);
            Boolean offLine = false;
            if (StringUtils.isEmpty(lastHearTime) || (new Date().getTime() / 1000 - Long.valueOf(lastHearTime)) > 60 * 5) {
                cable_type = "{\"1\":\"0\",\"2\":\"0\",\"3\":\"0\"}";
                offLine = true;
            }
            FeeStrategy feeStrategy = feeStrategyService.findFeeStrategyByStation(Long.valueOf(sid));
            String feeStr = feeStrategyService.transFeeStrategy(feeStrategy);
            Boolean exitOrder = orderMapper.findTodayOrder(Long.valueOf(sid), user.getId());
            //获取押金金额
            BigDecimal defaultPay = shopMapper.getShopDefaultPayInfoBySid(sid);
            data.put("offLine", offLine);
            data.put("sid", sid);
            data.put("tid", session);
            data.put("need_pay", feeStrategyService.calNeedPay(defaultPay, user.getUsablemoney()));//用户需支付金额
            data.put("deposite_need", defaultPay);//当用户余额为零时，所需押金
            data.put("usable_money", user.getUsablemoney());//用户可用金额
            data.put("fee_strategy", feeStr);//收费策略
            data.put("free_display", !exitOrder);// 显示免费
            data.put("cable_type", JsonUtils.readValue(cable_type));
            bacMap.put("data", data);
            bacMap.put("code", 0);
            bacMap.put("msg", "成功");
        } catch (Exception e) {
            logger.error(e.getMessage());
            bacMap.put("data", null);
            bacMap.put("code", 5);
            bacMap.put("msg", "session过期");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }
}
