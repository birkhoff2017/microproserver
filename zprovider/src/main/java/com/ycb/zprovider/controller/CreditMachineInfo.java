package com.ycb.zprovider.controller;

import com.ycb.zprovider.cache.RedisService;
import com.ycb.zprovider.mapper.OrderMapper;
import com.ycb.zprovider.mapper.ShopMapper;
import com.ycb.zprovider.mapper.StationMapper;
import com.ycb.zprovider.mapper.UserMapper;
import com.ycb.zprovider.service.FeeStrategyService;
import com.ycb.zprovider.utils.JsonUtils;
import com.ycb.zprovider.vo.FeeStrategy;
import com.ycb.zprovider.vo.User;
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
 * 查询设备信息
 */
@RestController
@RequestMapping("borrow")
public class CreditMachineInfo {

    public static final Logger logger = LoggerFactory.getLogger(CreditMachineInfo.class);

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

    /**
     *根据设备的sid查询设备的信息
     * @param session   用户session
     * @param sid       设备sid
     */
    @RequestMapping(value = "/getMachineInfo", method = RequestMethod.POST)
    @ResponseBody
    public String getMachineInfo(@RequestParam("session") String session,
                                 @RequestParam("sid") String sid) {
        Map<String, Object> bacMap = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        try {
            User user = this.userMapper.findUserinfoByOpenid(redisService.getKeyValue(session));
            // 解析qrcode，根据机器sid，获取机器状态属性值
            //String[] urlArr = qrcode.trim().toLowerCase().split("/");
            //String sid = urlArr[urlArr.length - 1];
            String cable_type = stationMapper.getUsableBatteries(Long.valueOf(sid));
            // 判断设备是否在线
            String lastHearTime = redisService.getKeyValue(sid);
            if (StringUtils.isEmpty(lastHearTime) || (new Date().getTime() / 1000 - Long.valueOf(lastHearTime)) > 60 * 5) {
                cable_type = "{\"1\":\"0\",\"2\":\"0\",\"3\":\"0\"}";
            }
            FeeStrategy feeStrategy = feeStrategyService.findFeeStrategyByStation(Long.valueOf(sid));
            String feeStr = feeStrategyService.transFeeStrategy(feeStrategy);
            Boolean exitOrder = orderMapper.findTodayOrder(Long.valueOf(sid), user.getId());
            //获取押金金额
            BigDecimal defaultPay = shopMapper.getShopDefaultPayInfoBySid(sid);
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
