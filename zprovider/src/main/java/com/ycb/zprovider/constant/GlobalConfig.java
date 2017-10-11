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
    //主域名
    public static final String Z_GATEWAY_URL = "https://x.yunchongba.com/zprovider/";
    //图文消息大图片
    public static final String Z_MESSAGE_BIGIMG = "https://oalipay-dl-django.alicdn.com/rest/1.0/image?fileIds=LQ8WWI46Qde4YXipYdMEngAAACMAAQED&zoom=original";
    //图文消息小图片
    public static final String Z_MESSAGE_SMALLIMG = "https://oalipay-dl-django.alicdn.com/rest/1.0/image?fileIds=4cdPw6zgRP6dPs6RYorWDAAAACMAAQED&zoom=original";
    //微信小程序路径
    public static final String WX_MINIAPP_URL = "https://m.dev.yunchongba.com/q/";
    //支付宝授权回调地址 需要url转码
    public static final String Z_AOUTH_REDIRECT_URI = "https%3a%2f%2fx.yunchongba.com%2fzprovider%2foauth%2foauthNotify";
    //支付宝生活号关注推广链接
    public static final String Z_ATTENTION_URI = "http://p.alipay.com/P/RuMIvyjz";

}
