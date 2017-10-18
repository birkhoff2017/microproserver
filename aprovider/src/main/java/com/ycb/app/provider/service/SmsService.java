package com.ycb.app.provider.service;

import com.ycb.app.provider.utils.HttpRequest;
import com.ycb.app.provider.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
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

    /**
     * 调取第三方短信平台,发送验证码
     *
     * @param mobile
     * @param content
     * @return
     */
    public boolean senSms(String mobile, String content) {
        HashMap postParam = new HashMap();
        postParam.put("P0_biztype", "message");
        postParam.put("P1_agentcode", agentcode);
        postParam.put("P2_mobile", mobile);
        postParam.put("P3_smscontent", content);
        postParam.put("P4_productcode", "HYSMS");
        postParam.put("P5_encode", "UTF-8");
        postParam.put("P6_extendinfo", "message");
        postParam.put("P7_requestid", "message");
        postParam.put("hmac", "message");
        String data = JsonUtils.writeValueAsString(postParam);
        //发送请求
        try {
            String msgResult = HttpRequest.sendPost(smsUrl, data);  //发送post请求
            if (StringUtils.isEmpty(msgResult)) {
                logger.error("获取验证码失败,mobile=: " + mobile);
            } else {
                Map<String, Object> msgResultMap = JsonUtils.readValue(msgResult);
                String code = (String) msgResultMap.get("code");
                String errmsg = (String) msgResultMap.get("orderid");
                String extendinfo = (String) msgResultMap.get("extendinfo");
                if ("000000" == code) {
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
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("调用三方异常" + e.getMessage());
        }
        return false;
    }
}
