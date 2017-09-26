package com.ycb.zprovider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.ZhimaMerchantOrderRentCompleteRequest;
import com.alipay.api.response.ZhimaMerchantOrderRentCompleteResponse;
import com.ycb.zprovider.constant.GlobalConfig;
import com.ycb.zprovider.mapper.OrderMapper;
import com.ycb.zprovider.mapper.ShopMapper;
import com.ycb.zprovider.service.FeeStrategyService;
import com.ycb.zprovider.utils.JsonUtils;
import com.ycb.zprovider.vo.Order;
import com.ycb.zprovider.vo.Shop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Huo on 2017/9/11.
 * 用户在最长租借时间范围内归还电池后，由通信系统调用该controller
 * 在该controller中调用支付宝的信用订单完结接口，通知支付宝处理该订单
 */

@RestController
@RequestMapping("restoreBattery")
public class CreditCompleteOrderController {
    public static final Logger logger = LoggerFactory.getLogger(CreditCompleteOrderController.class);

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

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private FeeStrategyService feeStrategyService;

    @Autowired
    private ShopMapper shopMapper;

    @RequestMapping(value = "/restore", method = RequestMethod.POST)
    @ResponseBody
    //orderid   订单编号，是在创建信用借还订单的时候商家创建的订单编号
    public void CompleteOrder(@RequestParam("orderid") String orderid) {
        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL, appId, privateKey,
                format, charset, alipayPublicKey, signType);
        ZhimaMerchantOrderRentCompleteRequest request = new ZhimaMerchantOrderRentCompleteRequest();
        //根据orderID获得信用借还订单的支付宝的编号
        Order order = orderMapper.findOrderByOrderId(orderid);
        String orderNo = order.getOrderNo();
        //信用借还的产品码:w1010100000000002858
        String productCode = GlobalConfig.Z_PRODUCT_CODE;
        //物品归还时间	2016-10-01 12:00:00
        String restoreTime = new SimpleDateFormat("YYYY-MM-dd HH:MM:ss").format(order.getReturnTime());
        /*
        金额类型：
        RENT:租金
        DAMAGE:赔偿金
         */
        String payAmountType = "RENT";
        //支付金额	100.00
        //payAmount 需要支付的金额
        String payAmount = order.getUsefee().toString();
        //restoreShopName 物品归还门店名称,例如肯德基文三路门店
        Long returnShopId = order.getReturnShopId();
        Shop shopInfo = shopMapper.findShopById(returnShopId);
        String restoreShopName = shopInfo.getName();

        Map<String, Object> bizContentMap = new LinkedHashMap<>();
        bizContentMap.put("order_no", orderid);
        bizContentMap.put("product_code", productCode);
        bizContentMap.put("restore_time", restoreTime);
        bizContentMap.put("pay_amount_type", payAmountType);
//        bizContentMap.put("pay_amount", payAmount);
        bizContentMap.put("pay_amount", 0);
        bizContentMap.put("restore_shop_name", "朝阳区");

        request.setBizContent(JsonUtils.writeValueAsString(bizContentMap));
        ZhimaMerchantOrderRentCompleteResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功,信用借还订单完结");
            //更新订单信息
//            updateOrder(response);
            //通知用户归还成功
//            sendMessage(response.getUserId(), order, restoreShopName);
        } else {
            System.out.println("调用失败");
            System.out.println("订单完结" + response.getMsg());
            System.out.println(response.getBody());
        }
    }

    //用户归还后，更新订单表的信息
    private void updateOrder(ZhimaMerchantOrderRentCompleteResponse response) {
        //借用人支付宝userId.	例如2088202924240029
        String responseUserId = response.getUserId();
        //信用借还的订单号,例如100000
        String responseOrderNo = response.getOrderNo();
        //资金流水号，用于商户与支付宝进行对账	2088000000000000
        String responseAlipayFundOrderNo = response.getAlipayFundOrderNo();

        Order order = new Order();
        order.setLastModifiedBy("SYS:completecreditpay");
        order.setLastModifiedDate(new Date());

        //因为这里只返回了信用借还的订单号，所以需要根据信用借还的订单号进行更新订单
        order.setOrderNo(responseOrderNo);
        order.setAlipayFundOrderNo(responseAlipayFundOrderNo);
        orderMapper.updateOrderStatusByOrderNo(order);
    }

}
