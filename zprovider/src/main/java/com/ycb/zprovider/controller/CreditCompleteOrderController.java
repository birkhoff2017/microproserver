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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Huo on 2017/9/11.
 * 用户在最长租借时间范围内归还电池后，由通信系统调用该controller
 * 在该controller中调用支付宝的信用订单完结接口，通知支付宝处理该订单
 */

@RestController
@RequestMapping("/creditcomplete")
public class CreditCompleteOrderController {
    public static final Logger logger = LoggerFactory.getLogger(CreditCompleteOrderController.class);

    @Autowired
    private AlipayClientFactory alipayClientFactory;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ShopMapper shopMapper;

    @RequestMapping(value = "/restore", method = RequestMethod.POST)
    //orderid   订单编号，是在创建信用借还订单的时候商家创建的订单编号
    public void CompleteOrder(@RequestParam("orderid") String orderid) {
        AlipayClient alipayClient = alipayClientFactory.newInstance();
        ZhimaMerchantOrderRentCompleteRequest request = new ZhimaMerchantOrderRentCompleteRequest();
        //根据orderID获得信用借还订单的支付宝的编号
        Order order = orderMapper.findOrderByOrderId(orderid);
        //String orderNo = order.getOrderNo();
        //信用借还的产品码:w1010100000000002858
        String productCode = GlobalConfig.Z_PRODUCT_CODE;
        //物品归还时间	2016-10-01 12:00:00
//        String restoreTime = new SimpleDateFormat("YYYY-MM-dd HH:MM:ss").format(order.getReturnTime());
        String restoreTime = new SimpleDateFormat("YYYY-MM-dd HH:MM:ss").format(new Date());
        /*
        金额类型：
        RENT:租金
        DAMAGE:赔偿金
         */
        String payAmountType = "RENT";
        //支付金额	100.00
        //payAmount 需要支付的金额
//        String payAmount = order.getUsefee().toString();
        String payAmount = "0";
        //restoreShopName 物品归还门店名称,例如肯德基文三路门店
//        Long returnShopId = order.getReturnShopId();
//        Shop shopInfo = shopMapper.findShopById(returnShopId);
//        String restoreShopName = shopInfo.getName();
        String restoreShopName = "马记拉面";

        Map<String, Object> bizContentMap = new LinkedHashMap<>();
        bizContentMap.put("order_no", orderid);
        bizContentMap.put("product_code", productCode);
        bizContentMap.put("restore_time", restoreTime);
        bizContentMap.put("pay_amount_type", payAmountType);
        bizContentMap.put("pay_amount", payAmount);
        bizContentMap.put("pay_amount", 0);
        bizContentMap.put("restore_shop_name", restoreShopName);

        request.setBizContent(JsonUtils.writeValueAsString(bizContentMap));
        ZhimaMerchantOrderRentCompleteResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功,信用借还订单完结");
        } else {
            System.out.println("调用失败");
            System.out.println("订单完结" + response.getMsg());
            System.out.println(response.getBody());
        }
    }
}
