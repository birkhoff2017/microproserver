package com.ycb.app.provider.controller;

import com.ycb.app.provider.cache.RedisService;
import com.ycb.app.provider.mapper.FeeStrategyMapper;
import com.ycb.app.provider.mapper.OrderMapper;
import com.ycb.app.provider.mapper.UserMapper;
import com.ycb.app.provider.service.FeeStrategyService;
import com.ycb.app.provider.utils.JsonUtils;
import com.ycb.app.provider.vo.FeeStrategy;
import com.ycb.app.provider.vo.TradeLog;
import com.ycb.app.provider.vo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuhui on 17-6-19.
 */
@RestController
@RequestMapping("order")
public class OrderController {

    public static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private RedisService redisService;

    @Autowired
    private FeeStrategyService feeStrategyService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private FeeStrategyMapper feeStrategyMapper;

    /**
     * 获取某个订单的详细信息
     *
     * @param session 登录时返回的session
     * @param orderid 订单编号
     * @return 订单详情
     */
    @RequestMapping(value = "/getOrderInfo", method = RequestMethod.POST)
    @ResponseBody
    public String querySingleOrder(@RequestParam("session") String session, @RequestParam("orderid") String orderid) {
        Map<String, Object> bacMap = new HashMap<>();
        try {
            TradeLog tradeLog = this.orderMapper.findOrderByOrderId(orderid);
            FeeStrategy feeStrategyEntity = tradeLog.getFeeStrategyEntity();
            if (feeStrategyEntity == null) {
                feeStrategyEntity = feeStrategyMapper.findGlobalFeeStrategy();
                tradeLog.setFeeStrategyEntity(feeStrategyEntity);
            }
            tradeLog.setFeeStrategy(feeStrategyService.transFeeStrategy(feeStrategyEntity));
            //如果是进行着的订单，数据库中的useFee为空，需要自己计算
            tradeLog.setUseFee(feeStrategyService.calUseFee(tradeLog.getFeeStrategyEntity(),tradeLog.getDuration(),tradeLog.getStatus(),tradeLog.getUseFee()));
            Map<String, Object> data = new HashMap<>();
            data.put("order", tradeLog);
            bacMap.put("data", data);
            bacMap.put("code", 0);
            bacMap.put("msg", "成功");
        } catch (Exception e) {
            logger.error(e.getMessage());
            bacMap.put("data", null);
            bacMap.put("code", 1);
            bacMap.put("msg", "失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }

    // 获取用户的订单记录
    @RequestMapping(value = "/getOrderList", method = RequestMethod.POST)
    @ResponseBody
    public String query(@RequestParam("session") String session) {
        Map<String, Object> bacMap = new HashMap<>();
        try {
            String openid = redisService.getKeyValue(session);
            User user = this.userMapper.findUserinfoByOpenid(openid);
            List<TradeLog> tradeLogList = this.orderMapper.findTradeLogs(user.getId());
            for (int i = 0; i < tradeLogList.size(); i++) {
                tradeLogList.get(i).setFeeStrategy(feeStrategyService.transFeeStrategy(tradeLogList.get(i).getFeeStrategyEntity()));
                tradeLogList.get(i).setUseFee(feeStrategyService.calUseFee(
                        tradeLogList.get(i).getFeeStrategyEntity(),
                        tradeLogList.get(i).getDuration(),
                        tradeLogList.get(i).getStatus(),
                        tradeLogList.get(i).getUseFee()));
            }
            if (null != tradeLogList) {
                Map<String, List> data = new HashMap<String, List>();
                data.put("orders", tradeLogList);
                bacMap.put("data", data);
                bacMap.put("code", 0);
                bacMap.put("msg", "成功");
            } else {
                bacMap.put("data", null);
                bacMap.put("code", 1);
                bacMap.put("msg", "用户暂无租借记录");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            bacMap.put("data", null);
            bacMap.put("code", 1);
            bacMap.put("msg", "获取数据失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }

    /**
     * 获取订单状态
     *
     * @param session
     * @param orderid
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/getOrderStatus", method = RequestMethod.POST)
    @ResponseBody
    public String getOrderStatus(@RequestParam("session") String session,
                                 @RequestParam("orderid") String orderid) throws IOException {
        Map<String, Object> bacMap = new HashMap<>();
        try {
            Integer orderStatus = this.orderMapper.getOrderStatus(orderid);
            Map<String, Object> data = new HashMap<String, Object>();
            if (orderStatus == 2) {
                data.put("status", "1");//借出成功
            } else {
                data.put("status", "0");//还未弹出
            }
            bacMap.put("data", data);
            bacMap.put("code", 0);
            bacMap.put("msg", "成功");
        } catch (Exception e) {
            logger.error(e.getMessage());
            bacMap.put("data", null);
            bacMap.put("code", 1);
            bacMap.put("msg", "获取数据失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }
}
