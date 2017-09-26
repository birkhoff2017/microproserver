package com.ycb.zprovider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.ZhimaMerchantOrderRentCancelRequest;
import com.alipay.api.response.ZhimaMerchantOrderRentCancelResponse;
import com.ycb.zprovider.constant.GlobalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Huo on 2017/9/11.
 */
@RestController
@RequestMapping("cancelOrder")
public class CreditCancelOrderController {
    public static final Logger logger = LoggerFactory.getLogger(CreditCancelOrderController.class);
    //初始化alipayClient用到的参数:支付宝网关
    //初始化alipayClient用到的参数:该appId必须设为开发者自己的生活号id
    @Value("${APPID}")
    private String appId;
    //初始化alipayClient用到的参数:该私钥为测试账号私钥  开发者必须设置自己的私钥,否则会存在安全隐患
    @Value("${PRIVATE_KEY}")
    private String privateKey;
    //初始化alipayClient用到的参数:仅支持JSON
    @Value("${FORMAT}")
    private String format;
    //初始化alipayClient用到的参数:字符编码-传递给支付宝的数据编码
    @Value("${CHARSET}")
    private String charset;
    //初始化alipayClient用到的参数:该公钥为测试账号公钥,开发者必须设置自己的公钥 ,否则会存在安全隐患
    @Value("${ALIPAY_PUBLIC_KEY}")
    private String alipayPublicKey;
    //初始化alipayClient用到的参数:签名类型
    @Value("${SIGN_TYPE}")
    private String signType;

    @RequestMapping(value = "/cancelOrder", method = RequestMethod.POST)
    @ResponseBody
    //orderNo 信用借还订单号
    public String cancelOrder(@RequestParam("orderNo") String orderNo) {
        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL, appId, privateKey, format, charset, alipayPublicKey, signType);
        ZhimaMerchantOrderRentCancelRequest request = new ZhimaMerchantOrderRentCancelRequest();
        //信用借还的产品码:w1010100000000002858
        String productCode = GlobalConfig.Z_PRODUCT_CODE;

        request.setBizContent("{" +
                "\"order_no\":\"" + orderNo + "\"," +
                "\"product_code\":\"" + productCode + "\"" +
                "  }");
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
