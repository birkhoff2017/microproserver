package com.ycb.zprovider.constant;

/**
 * duxinyaun
 */
public class GlobalConfig {

    public GlobalConfig() {
    }

    //信用借还支付宝接口
    public static final String Z_CREDIT_SERVER_URL = "https://openapi.alipay.com/gateway.do";
    //发起的网页授权链接
    public static final String ZFB_SEND_AUTH_URL = "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?";
    //信用借还的产品码:w1010100000000002858
    public static final String Z_PRODUCT_CODE = "w1010100000000002858";
}
