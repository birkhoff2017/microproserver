package com.ycb.wpc.provider.service;

import com.ycb.wpc.provider.cache.RedisService;
import com.ycb.wpc.provider.constant.GlobalConfig;
import com.ycb.wpc.provider.mapper.MessageMapper;
import com.ycb.wpc.provider.mapper.OrderMapper;
import com.ycb.wpc.provider.mapper.RefundMapper;
import com.ycb.wpc.provider.utils.HttpRequest;
import com.ycb.wpc.provider.utils.JsonUtils;
import com.ycb.wpc.provider.utils.MD5;
import com.ycb.wpc.provider.vo.Refund;
import com.ycb.wpc.provider.vo.TradeLog;
import com.ycb.wpc.provider.vo.WechatTemplateMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Bruce on 17-10-10.
 */
@Service
public class MessageService {

    public static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    public static final String WPC_ACCESS_TOKEN = "WPC_ACCESS_TOKEN";

    @Autowired(required = false)
    private MessageMapper messageMapper;

    @Autowired(required = false)
    private RefundMapper refundMapper;

    @Autowired(required = false)
    private OrderMapper orderMapper;

    @Autowired
    private RedisService redisService;

    @Value("${appID}")
    private String appID;

    @Value("${appSecret}")
    private String appSecret;

    @Value("${redirect_uri}")
    private String redirect_uri;

    /**
     * 获取access_token
     *
     * @param appID     凭证
     * @param appSecret 密钥
     * @return
     */
    public String getAccessToken(String appID, String appSecret) {
        String param = "grant_type=client_credential&appid=" + appID + "&secret=" + appSecret;
        try {
            String accessToken = redisService.getKeyValue(WPC_ACCESS_TOKEN);
            if (org.apache.commons.lang.StringUtils.isBlank(accessToken)) {
                String tokenInfo = HttpRequest.sendGet(GlobalConfig.WX_ACCESS_TOKEN_URL, param);
                Map<String, Object> tokenInfoMap = JsonUtils.readValue(tokenInfo);
                accessToken = tokenInfoMap.get("access_token").toString();
                redisService.setKeyValueTimeout(WPC_ACCESS_TOKEN, accessToken, Long.valueOf("7200"));
            }
            return accessToken;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }


    //提现成功 推送消息
    public void refundSendTemplate(String openid, String templateid, BigDecimal usablemoney) {
        //根据具体模板参数组装
        WechatTemplateMsg wechatTemplateMsg = new WechatTemplateMsg();
        wechatTemplateMsg.setTemplate_id(templateid);
        wechatTemplateMsg.setTouser(openid);
        wechatTemplateMsg.setUrl(redirect_uri + "depositrecord.html?openid=" + openid);
        TreeMap<String, TreeMap<String, String>> params = new TreeMap<String, TreeMap<String, String>>();
        params.put("first", WechatTemplateMsg.item("您好，您已发起提现申请！", "#000000")); //提现金额
        params.put("keyword1", WechatTemplateMsg.item(usablemoney.toString() + "元", "#000000"));
        params.put("keyword2", WechatTemplateMsg.item(new Date().toString(), "#000000")); //提现时间
        params.put("remark", WechatTemplateMsg.item("您好！发起提现后，项款将原路返回您原支付账户", "#00FF7F")); //温馨提示
        wechatTemplateMsg.setData(params);
        String data = JsonUtils.writeValueAsString(wechatTemplateMsg);
        this.sendMessage(data);
    }

    //租借成功 推送消息
    public void borrowSendTemplate(String openid, String templateid, String srOpenid) {

        TradeLog tradeLog = this.orderMapper.findOrderBySrOpenid(srOpenid);
        //根据具体模板参数组装
        WechatTemplateMsg wechatTemplateMsg = new WechatTemplateMsg();
        wechatTemplateMsg.setTemplate_id("11");
        wechatTemplateMsg.setTouser(openid);
        wechatTemplateMsg.setUrl(redirect_uri + "/rent.html?openid=" + openid);
        TreeMap<String, TreeMap<String, String>> params = new TreeMap<String, TreeMap<String, String>>();
        params.put("first", WechatTemplateMsg.item("设备已租借成功！", "#000000"));
        params.put("keyword1", WechatTemplateMsg.item(tradeLog.getBorrowStationId(), "#000000"));
        params.put("keyword2", WechatTemplateMsg.item(tradeLog.getBorrowTime(), "#000000"));
        params.put("keyword3", WechatTemplateMsg.item(tradeLog.getPaid() == null ? "" : tradeLog.getPaid().toString(), "#000000"));
        params.put("keyword4", WechatTemplateMsg.item(tradeLog.getOrderid(), "#000000"));
        params.put("remark", WechatTemplateMsg.item("1小时免费时长，超出后每小时收费1元。每天最高收费10元。充电宝自带一根多功能充电线，插头的AB面分别支持苹果/安卓，如插入后没响应，更换另一面即可", "#00FF7F"));
        wechatTemplateMsg.setData(params);
        String data = JsonUtils.writeValueAsString(wechatTemplateMsg);
        this.sendMessage(data);
    }

    //归还 推送消息
    public void returnSendTemplate(String openid, String templateid, String srOpenid) {

        TradeLog tradeLog = this.orderMapper.findOrderBySrOpenid(srOpenid);

        //根据具体模板参数组装
        WechatTemplateMsg wechatTemplateMsg = new WechatTemplateMsg();
        wechatTemplateMsg.setTemplate_id("11");
        wechatTemplateMsg.setTouser(openid);
        wechatTemplateMsg.setUrl(redirect_uri + "/deposit.html?openid=" + openid);
        TreeMap<String, TreeMap<String, String>> params = new TreeMap<String, TreeMap<String, String>>();
        params.put("first", WechatTemplateMsg.item("您好，您租借的充电宝已经成功归还。", "#000000"));
        params.put("keynote1", WechatTemplateMsg.item(tradeLog.getBorrowTime(), "#000000"));
        params.put("keynote2", WechatTemplateMsg.item(tradeLog.getBorrowName(), "#000000"));
        params.put("keynote3", WechatTemplateMsg.item(tradeLog.getReturnTime(), "#000000"));
        params.put("keynote4", WechatTemplateMsg.item(tradeLog.getReturnName(), "#000000"));
        params.put("keynote5", WechatTemplateMsg.item(tradeLog.getOrderid(), "#000000"));
        params.put("remark", WechatTemplateMsg.item("可在【用户中心-云账户-余额提现】进行提现。云充吧期待再次为您服务。", "#00FF7F"));
        wechatTemplateMsg.setData(params);
        String data = JsonUtils.writeValueAsString(wechatTemplateMsg);
        this.sendMessage(data);
    }

    //超时 推送消息
    public void overtimeSendTemplate(String openid, String templateid, Long refundId) {
        //查询提现记录
        Refund refund = this.refundMapper.findRefundByRefundId(refundId);
        //根据具体模板参数组装
        WechatTemplateMsg wechatTemplateMsg = new WechatTemplateMsg();
        wechatTemplateMsg.setTemplate_id("11");
        wechatTemplateMsg.setTouser(openid);
        wechatTemplateMsg.setUrl("http://www.wangrulin.top/depositrecord.html?openid=" + openid);
        String requestTime = refund.getRequestTime();
        TreeMap<String, TreeMap<String, String>> params = new TreeMap<String, TreeMap<String, String>>();
        params.put("first", WechatTemplateMsg.item("提现申请通知", "#000000"));
        params.put("keynote1", WechatTemplateMsg.item("您好，您已发起提现申请！", "#000000")); //提现金额
        params.put("keynote2", WechatTemplateMsg.item("提现金额：" + refund.getRefund().toString() + "元", "#000000"));
        params.put("keynote3", WechatTemplateMsg.item("发起时间：" + requestTime.substring(0, requestTime.length() - 2), "#000000")); //提现时间
        params.put("keynote4", WechatTemplateMsg.item("您好！发起提现后，项款将原路返回您原支付账户", "#000000")); //温馨提示
        wechatTemplateMsg.setData(params);
        String data = JsonUtils.writeValueAsString(wechatTemplateMsg);
        this.sendMessage(data);
    }

    private void sendMessage(String data) {
        try {
            String accessToken = getAccessToken(appID, appSecret);
            String msgUrl = GlobalConfig.WX_SEND_TEMPLATE_MESSAGE + "?access_token=" + accessToken;
            String msgResult = HttpRequest.sendPost(msgUrl, data);  //发送post请求
            if (StringUtils.isEmpty(msgResult)) {
                logger.info("模板消息发送失败");
            } else {
                Map<String, Object> msgResultMap = JsonUtils.readValue(msgResult);
                Integer errcode = (Integer) msgResultMap.get("errcode");
                String errmsg = (String) msgResultMap.get("errmsg");
                if (0 == errcode) {
                    logger.info("模板消息发送成功errorCode:{" + errcode + "},errmsg:{" + errmsg + "}");
                } else {
                    logger.info("模板消息发送失败errorCode:{" + errcode + "},errmsg:{" + errmsg + "}");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("模板消息发送失败");
        }
    }
}