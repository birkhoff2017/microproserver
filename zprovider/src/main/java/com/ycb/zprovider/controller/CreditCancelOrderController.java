package com.ycb.zprovider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.ZhimaMerchantOrderRentCancelRequest;
import com.alipay.api.response.ZhimaMerchantOrderRentCancelResponse;
import com.ycb.zprovider.constant.GlobalConfig;
import com.ycb.zprovider.utils.JsonUtils;
import com.ycb.zprovider.vo.AlipayClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Huo on 2017/9/11.
 */
@RestController
@RequestMapping("/creditcancel")
public class CreditCancelOrderController {
    public static final Logger logger = LoggerFactory.getLogger(CreditCancelOrderController.class);

    @Autowired
    private AlipayClientFactory alipayClientFactory;

    @RequestMapping(value = "/cancelOrder", method = RequestMethod.POST)
    @ResponseBody
    //orderNo 信用借还订单号
    public String cancelOrder(@RequestParam("orderNo") String orderNo) {
        AlipayClient alipayClient = alipayClientFactory.newInstance();
        ZhimaMerchantOrderRentCancelRequest request = new ZhimaMerchantOrderRentCancelRequest();
        //信用借还的产品码:w1010100000000002858
        String productCode = GlobalConfig.Z_PRODUCT_CODE;

        Map<String, Object> bizContentMap = new LinkedHashMap<>();
        bizContentMap.put("order_no", orderNo);
        bizContentMap.put("product_code", productCode);
        request.setBizContent(JsonUtils.writeValueAsString(bizContentMap));
        ZhimaMerchantOrderRentCancelResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功");
            System.out.println("订单取消" + response.getMsg());
            System.out.println(response.getBody());
        } else {
            System.out.println("调用失败");
        }
        return null;
    }
}
