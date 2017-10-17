package com.ycb.zprovider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.ZhimaMerchantOrderRentCompleteRequest;
import com.alipay.api.response.ZhimaMerchantOrderRentCompleteResponse;
import com.ycb.zprovider.constant.GlobalConfig;
import com.ycb.zprovider.mapper.OrderMapper;
import com.ycb.zprovider.mapper.ShopMapper;
import com.ycb.zprovider.utils.JsonUtils;
import com.ycb.zprovider.vo.AlipayClientFactory;
import com.ycb.zprovider.vo.Order;
import com.ycb.zprovider.vo.Shop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Huo on 2017/9/25.
 * 对于逾期订单，定时调用支付宝的信用借还订单完结接口催缴费用
 */
@Controller
public class CreditOverdueOrders {

    public static final Logger logger = LoggerFactory.getLogger(CreditOverdueOrders.class);

    @Autowired
    private AlipayClientFactory alipayClientFactory;

    //#最长可借用时间，超时后视为逾期订单，单位：天
    @Value("${MAX_CAN_BORROW_TIME}")
    public Integer maxCanBorrowTime;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ShopMapper shopMapper;

    //每隔fixedDelay（毫秒）执行一次
    @Scheduled(fixedRate = 2000000)
    public void dealWithOverdueUsers() {
        //查询出还没有归还的订单
        List<Order> overdueOrders = orderMapper.findOverdueOrders(maxCanBorrowTime);

        for (Order order : overdueOrders) {
            //向支付宝发送完结订单的请求
            sendCompleteOverdueRequest(order);
        }
    }

    //对于逾期未归还的用户，向支付宝发送完结订单的请求
    private void sendCompleteOverdueRequest(Order order) {

        String orderNo = order.getOrderNo();
        //信用借还的产品码:w1010100000000002858
        String productCode = GlobalConfig.Z_PRODUCT_CODE;
        //物品归还时间	2016-10-01 12:00:00
        String restoreTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        /*
        金额类型：
        RENT:租金
        DAMAGE:赔偿金
         */
        String payAmountType = "DAMAGE";
        //支付金额
        //需要支付的金额
        //借出设备id
        Long sid = order.getBorrowStationId();
        //根据设备的sid查询店铺信息
        Shop shopInfo = shopMapper.getShopInfoBySid(sid.toString());
        //查询出设备押金,也就是用户需要支付的赔偿金
        String payAmount = shopInfo.getDefaultPay().toString();

        AlipayClient alipayClient = alipayClientFactory.newInstance();
        ZhimaMerchantOrderRentCompleteRequest request = new ZhimaMerchantOrderRentCompleteRequest();

        Map<String, Object> bizContentMap = new LinkedHashMap<>();
        bizContentMap.put("order_no", orderNo);
        bizContentMap.put("product_code", productCode);
        bizContentMap.put("restore_time", restoreTime);
        bizContentMap.put("pay_amount_type", payAmountType);
        bizContentMap.put("pay_amount", payAmount);
        request.setBizContent(JsonUtils.writeValueAsString(bizContentMap));
        ZhimaMerchantOrderRentCompleteResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response == null || !response.isSuccess()) {
            logger.error("订单号为：" + orderNo + "的逾期订单完结失败!"+ response.getBody());
            if (response != null) {
                logger.error(response.getMsg());
            }
        }
    }
}
