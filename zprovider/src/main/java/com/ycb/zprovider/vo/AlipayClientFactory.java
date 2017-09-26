package com.ycb.zprovider.vo;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.ycb.zprovider.constant.GlobalConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by Huo on 2017/9/26.
 */
@Component
public class AlipayClientFactory {

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

    public AlipayClient newInstance() {
        return new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL,
                appId, privateKey, format, charset, alipayPublicKey,
                signType);
    }
}
