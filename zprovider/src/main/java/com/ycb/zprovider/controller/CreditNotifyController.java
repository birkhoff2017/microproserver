package com.ycb.zprovider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.ycb.zprovider.mapper.OrderMapper;
import com.ycb.zprovider.mapper.StationMapper;
import com.ycb.zprovider.service.CreditQueryOrderService;
import com.ycb.zprovider.service.SocketService;
import com.ycb.zprovider.utils.RequestUtil;
import com.ycb.zprovider.vo.CreditOrder;
import com.ycb.zprovider.vo.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

/**
 * Created by Huo on 2017/9/26.
 * 支付宝回调接口，负责处理信用借还订单创建和完结回调后的业务逻辑
 */
@Controller
@RequestMapping("/creditnotify")
public class CreditNotifyController {

    public static final Logger logger = LoggerFactory.getLogger(CreditNotifyController.class);

    //初始化alipayClient用到的参数:字符编码-传递给支付宝的数据编码
    @Value("${CHARSET}")
    private String charset;
    //初始化alipayClient用到的参数:该公钥为测试账号公钥,开发者必须设置自己的公钥 ,否则会存在安全隐患
    @Value("${ALIPAY_PUBLIC_KEY}")
    private String alipayPublicKey;
    //初始化alipayClient用到的参数:签名类型
    @Value("${SIGN_TYPE}")
    private String signType;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private StationMapper stationMapper;
    @Autowired
    private SocketService socketService;
    @Autowired
    private CreditQueryOrderService creditQueryOrderService;

    /**
     * 异步通知请求入口.
     */
    @RequestMapping(value = "/notify", method = {RequestMethod.POST})
    public void notify(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Map<String, String> params = RequestUtil.getRequestParams(request);

        //1、签名验证
        //验证
        //验证签名是否成功
        boolean flag = false;
        try {
            flag = AlipaySignature.rsaCheckV2(params, alipayPublicKey, charset, signType);
        } catch (AlipayApiException e) {
            e.printStackTrace();
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
                updateOrder(creditOrder, order, outOrderNo);
                //弹出电池
                borrowBattery(outOrderNo, creditOrder, order);
            } else if ("ORDER_COMPLETE_NOTIFY".equals(notifyType)) {
                //更新订单信息
                updateComplateOrder(orderNo, creditOrder, order);
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
        //借用者的userId
        String responseUserId = creditOrder.getUserId();
        socketService.SendCmd("ACT:borrow_battery;EVENT_CODE:1;STATIONID:" + sid + ";MAC:" + mac + ";ORDERID:" + outOrderNo + ";COLORID:7;CABLE:" + cableType + ";\r\n");
    }

    /*
    当调用支付宝完结订单接口成功时，更新订单的状态
     */
    private void updateComplateOrder(String orderNo, CreditOrder creditOrder, Order order) {
        //资金流水号，用于商户与支付宝进行对账	2088000000000000
        String alipayFundOrderNo = creditOrder.getAlipayFundOrderNo();
        //更新订单信息
        order.setLastModifiedBy("SYS:completecreditpay");
        order.setLastModifiedDate(new Date());
        //因为这里只返回了信用借还的订单号，所以需要根据信用借还的订单号进行更新订单
        order.setOrderNo(orderNo);
        order.setAlipayFundOrderNo(alipayFundOrderNo);
        orderMapper.updateOrderStatusByOrderNo(order);
    }

    /*
    当调用支付宝的创建信用借还订单接口成功时，更新订单的状态
     */
    private void updateOrder(CreditOrder creditOrder, Order order, String outOrderNo) {
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
}
