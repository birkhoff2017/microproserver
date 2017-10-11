package com.ycb.zprovider.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.ZhimaMerchantOrderRentQueryRequest;
import com.alipay.api.response.ZhimaMerchantOrderRentQueryResponse;
import com.ycb.zprovider.constant.GlobalConfig;
import com.ycb.zprovider.utils.JsonUtils;
import com.ycb.zprovider.vo.AlipayClientFactory;
import com.ycb.zprovider.vo.CreditOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Huo on 2017/9/11.
 *
 * 根据商户的订单号，调用支付宝的查询接口查询订单的详细信息
 */
@Service
public class CreditQueryOrderService {

    public static final Logger logger = LoggerFactory.getLogger(CreditQueryOrderService.class);

    @Autowired
    private AlipayClientFactory alipayClientFactory;

    //outOrderNo 外部订单号，需要唯一，由商户传入，芝麻内部会做幂等控制，格式为：yyyyMMddHHmmss+随机数	2016100100000xxxx
    public CreditOrder queryOrderByOutOrderNo(String outOrderNo) {
        CreditOrder creditOrder = new CreditOrder();
        AlipayClient alipayClient = alipayClientFactory.newInstance();
        ZhimaMerchantOrderRentQueryRequest request = new ZhimaMerchantOrderRentQueryRequest();
        //信用借还的产品码
        String productCode = GlobalConfig.Z_PRODUCT_CODE;

        Map<String, Object> bizContentMap = new LinkedHashMap<>();
        bizContentMap.put("out_order_no", outOrderNo);
        bizContentMap.put("product_code", productCode);
        request.setBizContent(JsonUtils.writeValueAsString(bizContentMap));
        ZhimaMerchantOrderRentQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (null != response && response.isSuccess()) {
            creditOrder.setAdmitState(response.getAdmitState());
            creditOrder.setAlipayFundOrderNo(response.getAlipayFundOrderNo());
            creditOrder.setBorrowTime(response.getBorrowTime());
            creditOrder.setGoodsName(response.getGoodsName());
            creditOrder.setOrderNo(response.getOrderNo());
            creditOrder.setPayAmount(response.getPayAmount());
            creditOrder.setPayAmountType(response.getPayAmountType());
            creditOrder.setPayStatus(response.getPayStatus());
            creditOrder.setPayTime(response.getPayTime());
            creditOrder.setRestoreTime(response.getRestoreTime());
            creditOrder.setUserId(response.getUserId());
            creditOrder.setUseState(response.getUseState());
            return creditOrder;
        } else {
            if (response != null) {
                logger.error("查询信用借还订单失败，错误代码：" + response.getCode() + "错误信息：" + response.getMsg() +
                        "错误子代码" + response.getSubCode() + "错误子信息：" + response.getSubMsg());
            }
        }
        return null;
    }
}
