package com.ycb.wpc.provider.constant;

/**
 * duxinyaun
 */
public class GlobalConfig {

    public GlobalConfig() {
    }

    public static final String WX_USERINFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info";

    public static final String WX_AUTH2_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";

    public static final String WX_CREATORDER_URL = "https://api.mch.weixin.qq.com/secapi/pay/refund";

    public static final String APICLIENT_CERT_P12 = "apiclient_cert.p12";

    public static final String WX_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";

    //发送模板消息接口
    public static final String WX_SEND_TEMPLATE_MESSAGE = "https://api.weixin.qq.com/cgi-bin/message/template/send";
}
