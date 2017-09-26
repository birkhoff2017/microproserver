package com.ycb.zprovider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.ZhimaMerchantOrderRentQueryRequest;
import com.alipay.api.response.ZhimaMerchantOrderRentQueryResponse;
import com.ycb.zprovider.constant.GlobalConfig;
import com.ycb.zprovider.utils.JsonUtils;
import com.ycb.zprovider.vo.CreditOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Huo on 2017/9/11.
 */
@RestController
@RequestMapping("queryOrder")
public class CreditQueryOrderController {

    public static final Logger logger = LoggerFactory.getLogger(CreditQueryOrderController.class);
    //初始化alipayClient用到的参数:支付宝网关
    //初始化alipayClient用到的参数:该appId必须设为开发者自己的生活号id
    @Value("${APPID}")
    private static String appId;
    //初始化alipayClient用到的参数:该私钥为测试账号私钥  开发者必须设置自己的私钥,否则会存在安全隐患
    @Value("${PRIVATE_KEY}")
    private static String privateKey;
    //初始化alipayClient用到的参数:仅支持JSON
    @Value("${FORMAT}")
    private static String format;
    //初始化alipayClient用到的参数:字符编码-传递给支付宝的数据编码
    @Value("${CHARSET}")
    private static String charset;
    //初始化alipayClient用到的参数:该公钥为测试账号公钥,开发者必须设置自己的公钥 ,否则会存在安全隐患
    @Value("${ALIPAY_PUBLIC_KEY}")
    private static String alipayPublicKey;
    //初始化alipayClient用到的参数:签名类型
    @Value("${SIGN_TYPE}")
    private String signType;

    @RequestMapping(value = "/queryOrder", method = RequestMethod.POST)
    @ResponseBody
    //outOrderNo 外部订单号，需要唯一，由商户传入，芝麻内部会做幂等控制，格式为：yyyyMMddHHmmss+随机数	2016100100000xxxx
    public CreditOrder queryOrderByOutOrderNo(@RequestParam("outOrderNo") String outOrderNo) {
        CreditOrder creditOrder = new CreditOrder();
        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL, appId, privateKey, format, charset, alipayPublicKey, signType);
        ZhimaMerchantOrderRentQueryRequest request = new ZhimaMerchantOrderRentQueryRequest();
        //信用借还的产品码:w1010100000000002858
        String productCode = GlobalConfig.Z_PRODUCT_CODE;

        Map<String, Object> bizContentMap = new LinkedHashMap<>();
        bizContentMap.put("out_order_no", outOrderNo);
        bizContentMap.put("product_code", productCode);
        request.setBizContent(JsonUtils.writeValueAsString(bizContentMap));
        ZhimaMerchantOrderRentQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
            System.out.println(alipayClient.sdkExecute(request));
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
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
            logger.error("查询信用借还订单失败，错误代码：" + response.getCode() + "错误信息：" + response.getMsg() +
                    "错误子代码" + response.getSubCode() + "错误子信息：" + response.getSubMsg());
        }
        return null;
    }

}
