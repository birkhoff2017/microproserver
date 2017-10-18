package com.ycb.app.provider.service;

import com.ycb.app.provider.utils.Digest;
import com.ycb.app.provider.utils.HttpRequest;
import com.ycb.app.provider.utils.JsonUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by zhuhui on 17-10-18.
 */
@Service
public class SmsService {

    public static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    @Value("${smsUrl}")
    private String smsUrl;

    @Value("${agentcode}")
    private String agentcode;

    @Value("${smsKey}")
    private String smsKey;

    /**
     * 调取第三方短信平台,发送验证码
     *
     * @param mobile
     * @param content
     * @return
     */
    public boolean senSms(String mobile, String content) throws UnsupportedEncodingException {
        LinkedHashMap postParam = new LinkedHashMap();
        postParam.put("P0_biztype", "message");
        postParam.put("P1_agentcode", agentcode);
        postParam.put("P2_mobile", mobile);
        postParam.put("P3_smscontent", content);
        postParam.put("P4_productcode", "HYSMS");
        postParam.put("P5_encode", "UTF-8");
        postParam.put("P6_extendinfo", "");
        String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String requestid = date + "_" + RandomStringUtils.random(4, "0123456789");
        postParam.put("P7_requestid", requestid);
        String aValue = postParam.get("P0_biztype").toString()
                .concat(postParam.get("P1_agentcode").toString())
                .concat(postParam.get("P2_mobile").toString())
                .concat(postParam.get("P3_smscontent").toString())
                .concat(postParam.get("P4_productcode").toString())
                .concat(postParam.get("P5_encode").toString())
                .concat(postParam.get("P6_extendinfo").toString())
                .concat(postParam.get("P7_requestid").toString());
        String hmac = Digest.hmacSign(aValue, smsKey);
        postParam.put("hmac", hmac);
        // String data = JsonUtils.writeValueAsString(postParam);
        //发送请求
        try {
            String param = "P0_biztype=" + postParam.get("P0_biztype").toString() +
                    "&P1_agentcode=" + postParam.get("P1_agentcode").toString() +
                    "&P2_mobile=" + postParam.get("P2_mobile").toString() +
                    "&P3_smscontent=" + URLEncoder.encode(content, "UTF-8") +
                    "&P4_productcode=" + postParam.get("P4_productcode").toString() +
                    "&P5_encode=" + postParam.get("P5_encode").toString() +
                    "&P6_extendinfo=" + postParam.get("P6_extendinfo").toString() +
                    "&P7_requestid=" + postParam.get("P7_requestid").toString() +
                    "&hmac=" + hmac;

            String msgResult = HttpRequest.sendGet(smsUrl, param);  //发送post请求
            //String msgResult = HttpRequest.sendPost(smsUrl, data);  //发送post请求
            if (StringUtils.isEmpty(msgResult)) {
                logger.error("获取验证码失败,mobile=: " + mobile);
            } else {
                Map<String, Object> msgResultMap = JsonUtils.readValue(msgResult);
                String code = (String) msgResultMap.get("code");
                String errmsg = (String) msgResultMap.get("orderid");
                String extendinfo = (String) msgResultMap.get("extendinfo");
                if ("000000".equals(code)) {
                    return true;
                } else if ("111111".equals(code)) {
                    logger.error("发送失败");
                } else if ("222222".equals(code)) {
                    logger.error("发送异常");
                } else if ("100000".equals(code)) {
                    logger.error("无参数");
                } else if ("100001".equals(code)) {
                    logger.error("P0_biztype为空");
                } else if ("100002".equals(code)) {
                    logger.error("P0_biztype超长");
                } else if ("100003".equals(code)) {
                    logger.error("P0_biztype错误");
                } else if ("100004".equals(code)) {
                    logger.error("P1_agentcode为空");
                } else if ("100005".equals(code)) {
                    logger.error("P1_agentcode位数不对(16位)");
                } else if ("100006".equals(code)) {
                    logger.error("P1_agentcode不存在");
                } else if ("100007".equals(code)) {
                    logger.error("P2_mobile手机号为空");
                } else if ("100008".equals(code)) {
                    logger.error("P2_mobile位数不对");
                } else if ("100009".equals(code)) {
                    logger.error("P2_mobile不支持该号段");
                } else if ("100010".equals(code)) {
                    logger.error("P3_smscontent为空");
                } else if ("100011".equals(code)) {
                    logger.error("P3_smscontent过长(最多支持500个汉字)");
                } else if ("100012".equals(code)) {
                    logger.error("P4_productcode为空");
                } else if ("100013".equals(code)) {
                    logger.error("P4_productcode超长");
                } else if ("100014".equals(code)) {
                    logger.error("P5_encode为空");
                } else if ("100015".equals(code)) {
                    logger.error("P5_encode超长");
                } else if ("100016".equals(code)) {
                    logger.error("P4_productcode不合法");
                } else if ("100017".equals(code)) {
                    logger.error("P6_extendinfo扩展信息超长");
                } else if ("100018".equals(code)) {
                    logger.error("hmac为空");
                } else if ("100019".equals(code)) {
                    logger.error("hmac超长");
                } else if ("100020".equals(code)) {
                    logger.error("产品未开通");
                } else if ("100021".equals(code)) {
                    logger.error("余额不足");
                } else if ("100022".equals(code)) {
                    logger.error("hmac签名错误");
                } else if ("100023".equals(code)) {
                    logger.error("扣款失败");
                } else if ("100024".equals(code)) {
                    logger.error("P2_mobile手机号码包含非数字字符");
                } else if ("100025".equals(code)) {
                    logger.error("P7_requestid为空");
                } else if ("100026".equals(code)) {
                    logger.error("P7_requestid超长");
                } else if ("100027".equals(code)) {
                    logger.error("P7_requestid已存在");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("调用三方异常" + e.getMessage(), e);
        }
        return false;
    }
}
