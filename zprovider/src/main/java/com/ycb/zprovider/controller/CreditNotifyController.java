package com.ycb.zprovider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayOpenPublicMessageCustomSendRequest;
import com.alipay.api.response.AlipayOpenPublicMessageCustomSendResponse;
import com.ycb.zprovider.mapper.OrderMapper;
import com.ycb.zprovider.mapper.StationMapper;
import com.ycb.zprovider.service.CreditQueryOrderService;
import com.ycb.zprovider.service.SocketService;
import com.ycb.zprovider.utils.RequestUtil;
import com.ycb.zprovider.vo.AlipayClientFactory;
import com.ycb.zprovider.vo.CreditOrder;
import com.ycb.zprovider.vo.Order;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Huo on 2017/9/26.
 * 应用网关
 * 支付宝回调接口，负责处理信用借还订单创建和完结回调后的业务逻辑
 */
@Controller
@RequestMapping("/gateway")
public class CreditNotifyController {

    public static final Logger logger = LoggerFactory.getLogger(CreditNotifyController.class);

    @Value("${CHARSET}")
    private String charset;
    @Value("${PRIVATE_KEY}")
    private String privateKey;
    @Value("${ALIPAY_PUBLIC_KEY}")
    private String alipayPublicKey;
    @Value("${SIGN_TYPE}")
    private String signType;
    @Value("${APPID}")
    private String appId;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private StationMapper stationMapper;
    @Autowired
    private SocketService socketService;
    @Autowired
    private AlipayClientFactory alipayClientFactory;

    @Autowired
    private CreditQueryOrderService creditQueryOrderService;

    /**
     * 应用网关
     * 异步通知请求入口.
     */
    @RequestMapping(value = "/notify", method = {RequestMethod.POST})
    public void notify(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Map<String, String> params = RequestUtil.getRequestParams(request);

        //签名验证
        boolean flag = false;
        try {
            flag = AlipaySignature.rsaCheckV1(params, alipayPublicKey, charset, signType);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }finally {
            String responseMsg = "";
            //获取内容信息
            String bizContent = params.get("biz_content");
            if (!StringUtils.isEmpty(bizContent)) {
                //将XML转化成json对象
                JSONObject bizContentJson = (JSONObject) new XMLSerializer().read(bizContent);
                // 1. 获取事件类型
                String eventType = bizContentJson.getString("EventType");
                // 服务窗关注事件
                if (eventType.equals("follow")) {
                    JSONObject actionParam = JSONObject.fromObject(bizContentJson.getString("ActionParam"));
                    JSONObject scene = JSONObject.fromObject(actionParam.get("scene"));
                    String sceneId = scene.getString("sceneId");
                    System.out.println("sceneId:" + sceneId);
                    //取得发起请求的支付宝账号id
                    String fromUserId = bizContentJson.getString("FromUserId");

                    //1. 首先同步构建ACK响应
                    responseMsg = this.buildBaseAckMsg(fromUserId);

                    //2. 异步发送消息，根据不同的sceneId推送不同的消息（这里的sceneId的意义由商户自己定义）
                    if("scene_p_1505704951554".equals(sceneId)){
                        //发消息
                        AlipayClient alipayClient = alipayClientFactory.newInstance();
                        AlipayOpenPublicMessageCustomSendRequest req = new AlipayOpenPublicMessageCustomSendRequest();
                        req.setBizContent("");//---
                        Map<String,Object> map = new HashMap<String,Object>();
                        map.put("to_user_id",fromUserId);
                        map.put("msg_type","image-text");
                        List<Map> articleList = new ArrayList<Map>();
                        Map<String,String> map1 = new HashMap<String,String>();
                        map1.put("action_name","立即查看");
                        map1.put("desc","图文内容");
                        map1.put("image_url","http: //example.com/abc.jpg");
                        map1.put("title","标题");
                        map1.put("url","");
                        Map<String,String> map2 = new HashMap<String,String>();
                        map2.put("action_name","立即查看");
                        map2.put("desc","图文内容");
                        map2.put("image_url","http: //example.com/abc.jpg");
                        map2.put("title","标题");
                        map2.put("url","");
                        articleList.add(map1);
                        articleList.add(map2);

                        AlipayOpenPublicMessageCustomSendResponse res = null;
                        try {
                            res = alipayClient.execute(req);
                        } catch (AlipayApiException e) {
                            e.printStackTrace();
                        }
                        if(res.isSuccess()){
                            System.out.println("调用成功");
                        } else {
                            System.out.println("调用失败");
                        }
                        //5. 响应结果加签及返回
                        try {
                            responseMsg = encryptAndSign(responseMsg,
                                    alipayPublicKey,
                                    privateKey,charset,
                                    false, true, signType);

                            //http 内容应答
                            response.reset();
                            response.setContentType("text/xml;charset=GBK");
                            PrintWriter printWriter = response.getWriter();
                            printWriter.print(responseMsg);
                            response.flushBuffer();

                        }catch (AlipayApiException alipayApiException) {
                            alipayApiException.printStackTrace();
                        }
                    }
                }
            }
        }

        //当验证签名成功的时候
        if (flag) {
            //notify_type	取值范围：
            //ORDER_CREATE_NOTIFY (订单创建异步事件)
            //ORDER_COMPLETE_NOTIFY (订单完结异步事件)
            String notifyType = params.get("notify_type");
            //信用借还平台订单号 芝麻信用借还平台生成的订单号
            String orderNo = params.get("order_no");
            //外部商户订单号 外部商户生成的订单号，与芝麻信用借还平台生成的订单号存在关联关系
            String outOrderNo = params.get("out_order_no");
            //根据外部订单号查询订单信息
            CreditOrder creditOrder = creditQueryOrderService.queryOrderByOutOrderNo(outOrderNo);
            //查询数据库中订单的信息
            Order order = orderMapper.findOrderByOrderId(outOrderNo);
            //对于订单创建事件
            if ("ORDER_CREATE_NOTIFY".equals(notifyType)) {
                //根据查询到的信息更新订单信息
                updateOrder(order, orderNo);
                //弹出电池
                //borrowBattery(outOrderNo, creditOrder, order);
            } else if ("ORDER_COMPLETE_NOTIFY".equals(notifyType)) {
                //更新订单信息
                updateComplateOrder(creditOrder, order);
            }
            //返回 success
            printResponse(response, "success");
        } else {
            printResponse(response, "error");
        }
    }

    //弹出电池
    private void borrowBattery(String outOrderNo, CreditOrder creditOrder, Order order) throws IOException {
        //从订单中获取设备的sid和cabletype
        String sid = order.getBorrowStationId().toString();
        String cableType = order.getCable().toString();
        //获取设备的mac，在弹出电池时会使用
        String mac = stationMapper.getStationMac(Long.valueOf(sid));
        socketService.SendCmd("ACT:borrow_battery;EVENT_CODE:1;STATIONID:" + sid + ";MAC:" + mac + ";ORDERID:" + outOrderNo + ";COLORID:7;CABLE:" + cableType + ";\r\n");
    }

    /*
    当调用支付宝完结订单接口成功时，更新订单的状态
     */
    private void updateComplateOrder(CreditOrder creditOrder, Order order) {
        //资金流水号，用于商户与支付宝进行对账	2088000000000000
        String alipayFundOrderNo = creditOrder.getAlipayFundOrderNo();
        //更新订单信息
        order.setLastModifiedBy("SYS:completecreditpay");
        order.setLastModifiedDate(new Date());
        order.setAlipayFundOrderNo(alipayFundOrderNo);
        orderMapper.updateOrderByOrderId(order);
        //判断是正常归还订单还是逾期订单，如果是逾期未还的完结订单，再将status设置为92'租金已扣完(未归还)'
        if ("DAMAGE".equals(creditOrder.getPayAmountType())) {
            order.setStatus(92);
            orderMapper.updateOverdueOrderStatusByOrderId(order);
        }
    }

    /*
    当调用支付宝的创建信用借还订单接口成功时，更新订单的状态
     */
    private void updateOrder(Order order, String outOrderNo) {
        order.setLastModifiedBy("SYS:updatecreditpay");
        order.setLastModifiedDate(new Date());
        order.setStatus(1);//支付状态,0为未支付，1为已经支付
        order.setOrderNo(outOrderNo);
        orderMapper.updateOrderStatusByOrderId(order);
    }

    //返回 success
    private void printResponse(HttpServletResponse response, String content) throws IOException {
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.write(content);
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * 构造基础的响应消息
     *
     * @return
     */
    public String buildBaseAckMsg(String fromUserId) {
        StringBuilder sb = new StringBuilder();
        sb.append("<XML>");
        sb.append("<ToUserId><![CDATA[" + fromUserId + "]]></ToUserId>");
        sb.append("<AppId><![CDATA[" + appId + "]]></AppId>");
        sb.append("<CreateTime>" + Calendar.getInstance().getTimeInMillis() + "</CreateTime>");
        sb.append("<MsgType><![CDATA[ack]]></MsgType>");
        sb.append("</XML>");
        return sb.toString();
    }

    public static String encryptAndSign(String bizContent, String alipayPublicKey, String cusPrivateKey, String charset,
                                        boolean isEncrypt, boolean isSign, String signType) throws AlipayApiException {
        StringBuilder sb = new StringBuilder();
        if (com.alipay.api.internal.util.StringUtils.isEmpty(charset)) {
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
