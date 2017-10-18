package com.ycb.zprovider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.internal.util.StringUtils;
import com.ycb.zprovider.utils.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by duxinyuan
 * 应用网关 配置应用网关时 将路径改为gateway/notify
 */
@Controller
@RequestMapping("/gateway_test")
public class NotifyController {

    public static final Logger logger = LoggerFactory.getLogger(NotifyController.class);

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

    //TODO 开发者必须设置自己的公钥 ,否则会存在安全隐患
    public static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjOJEuBxIQX3p13xhqHnZJIfx8PvnoBCo08Fs1LKk6LndSYzyBKe+pAlGPkgaBdOOwFB0iUis5siNhWjgC4gI1OApx5pLsjnuRCb8Js27RRyXdy4sY44/Xg/1Zr8roSXACUg2JUT6NmNIhVcOTgEqFTys95O2a3fWOALSbe7/k8lL6VLPVcUwOyRkbhG+HxlxRqEBwG5npAkxC0KqQ7zHGxmcbwzTIkCn2k7p/p/cTAJjLL/FxL/oM3XZsCV1ZTmMmBpuOpjwLzgmAvpiol/Z++rF9hQ3By/Fz031Zdb5u6aMR4yxXTD8iL5F7byGj/twG07AqTWaVQTnjTsjW1N27QIDAQAB";

    /**
     * 应用网关
     * 异步通知请求入口.
     */
    @RequestMapping(value = "/notify", method = {RequestMethod.POST})
    public void notify(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String, String> params = RequestUtil.getRequestParams(request);
        //支付宝响应消息
        String responseMsg = "";
        //1、签名验证
        //验证
        //验证签名是否成功
        boolean flag = false;
        try {
            flag = AlipaySignature.rsaCheckV2(params, alipayPublicKey, charset, signType);

            StringBuilder builder = new StringBuilder();
            builder.append("<success>").append(Boolean.TRUE.toString()).append("</success>");
            builder.append("<biz_content>").append(PUBLIC_KEY)
                    .append("</biz_content>");


            responseMsg = builder.toString();


        } catch (AlipayApiException e) {
            e.printStackTrace();
        } finally {
            //5. 响应结果加签及返回
            try {
                //对响应内容加签
                responseMsg = encryptAndSign(responseMsg,
                        alipayPublicKey,
                        privateKey, charset,
                        false, true, signType);

                //http 内容应答
                response.reset();
                response.setContentType("text/xml;charset=GBK");
                PrintWriter printWriter = response.getWriter();
                printWriter.print(responseMsg);
                response.flushBuffer();

            } catch (AlipayApiException alipayApiException) {
                //开发者可以根据异常自行进行处理
                alipayApiException.printStackTrace();
            }
        }
    }

    public static String encryptAndSign(String bizContent, String alipayPublicKey, String cusPrivateKey, String charset,
                                        boolean isEncrypt, boolean isSign, String signType) throws AlipayApiException {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isEmpty(charset)) {
            charset = "GBK";
        }
        sb.append("<?xml version=\"1.0\" encoding=\"" + charset + "\"?>");
        if (isEncrypt) {// 加密
            sb.append("<alipay>");
            String encrypted = AlipaySignature.rsaEncrypt(bizContent, alipayPublicKey, charset);
            sb.append("<response>" + encrypted + "</response>");
            sb.append("<encryption_type>AES</encryption_type>");
            if (isSign) {
                String sign = AlipaySignature.rsaSign(encrypted, cusPrivateKey, charset, signType);
                sb.append("<sign>" + sign + "</sign>");
                sb.append("<sign_type>");
                sb.append(signType);
                sb.append("</sign_type>");
            }
            sb.append("</alipay>");
        } else if (isSign) {// 不加密，但需要签名
            sb.append("<alipay>");
            sb.append("<response>" + bizContent + "</response>");
            String sign = AlipaySignature.rsaSign(bizContent, cusPrivateKey, charset, signType);
            sb.append("<sign>" + sign + "</sign>");
            sb.append("<sign_type>");
            sb.append(signType);
            sb.append("</sign_type>");
            sb.append("</alipay>");
        } else {// 不加密，不加签
            sb.append(bizContent);
        }
        return sb.toString();
    }
}
