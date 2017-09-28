package com.ycb.zprovider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.ZhimaMerchantOrderRentCreateRequest;
import com.alipay.api.response.ZhimaMerchantOrderRentCreateResponse;
import com.ycb.zprovider.constant.GlobalConfig;
import com.ycb.zprovider.mapper.OrderMapper;
import com.ycb.zprovider.mapper.ShopMapper;
import com.ycb.zprovider.mapper.StationMapper;
import com.ycb.zprovider.service.AlipayOrderService;
import com.ycb.zprovider.service.SocketService;
import com.ycb.zprovider.utils.JsonUtils;
import com.ycb.zprovider.vo.AlipayClientFactory;
import com.ycb.zprovider.vo.Shop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Huo on 2017/9/8.
 * 创建信用借还订单
 */
@RestController
@RequestMapping("/creditcreate")
public class CreditCreateOrderController {
    public static final Logger logger = LoggerFactory.getLogger(CreditCreateOrderController.class);

    //#最长可借用时间，超时后视为逾期订单，单位：天
    @Value("${MAX_CAN_BORROW_TIME}")
    private Integer maxCanBorrowTime;
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private StationMapper stationMapper;
    @Autowired
    private SocketService socketService;

    @Autowired
    private AlipayClientFactory alipayClientFactory;
    @Autowired
    private AlipayOrderService alipayOrderService;

    @RequestMapping(value = "/createOrder")
    //sid   设备id
    //cableType 数据线类型
    //session   用户的session，去redis中进行比对查询
    public String createOrder(@RequestParam("sid") String sid, @RequestParam("cable_type") String cableType, @RequestParam("session") String session) {

        AlipayClient alipayClient = alipayClientFactory.newInstance();
        ZhimaMerchantOrderRentCreateRequest request = new ZhimaMerchantOrderRentCreateRequest();
        //回调到商户的url地址
        //商户在组装订单创建https请求时，会附带invoke_return_url参数，当用户完成借用准入及资金处理后，
        // 在借用完成页面会自动回调到商户提供的invoke_return_url地址链接，目前商户链接跳转是通过自动跳转的方式实现。
        String invokeReturnUrl = "http://www.duxinyuan.top/loading.html";
        //下面的代码用来生成外部订单号，就是商户自己的订单号
        String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Random random = new Random();
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }
        //外部订单号，需要唯一，由商户传入，芝麻内部会做幂等控制，格式为：yyyyMMddHHmmss+随机数  示例："2016100100000xxxx\"
        String outOrderNo = date + sb.toString();
        //信用借还的产品码，传入固定值：w1010100000000002858
        String productCode = GlobalConfig.Z_PRODUCT_CODE;
        //物品名称,最长不能超过14个汉字
        String goodsName = "充电宝";
        //租金信息描述 ,长度不超过14个汉字，只用于页面展示给C端用户，除此之外无其他意义。
        String rentInfo = "1小时免费，10元/天";
        /*
        租金单位，租金+租金单位组合才具备实际的租金意义。
        取值定义如下：
        DAY_YUAN:元/天
        HOUR_YUAN:元/小时
        YUAN:元
        YUAN_ONCE: 元/次
         */
        String rentUnit = "DAY_YUAN";
        /*
        租金，租金+租金单位组合才具备实际的租金意义。
        >0.00元，代表有租金
        =0.00元，代表无租金，免费借用
        注：参数传值必须>=0，传入其他值会报错参数非法
         */
        //这里还需要商议
//        FeeStrategy feeStrategy = feeStrategyService.findFeeStrategyByStation(Long.valueOf(sid));
//        String rentAmount = feeStrategy.getFixed().toString();
        String rentAmount = "0.10";
        /*
        押金，金额单位：元。
        注：不允许免押金的用户按此金额支付押金；当物品丢失时，赔偿金额不得高于该金额。
         */
        //根据设备的sid查询店铺信息
        Shop shopInfo = shopMapper.getShopInfoBySid(sid);
        //查询出设备押金
        String depositAmount = shopInfo.getDefaultPay().toString();
        /*
        是否支持当借用用户信用不够（不准入）时，可让用户支付押金借用:
        Y:支持
        N:不支持
        注：支付押金的金额等同于deposit_amount
         */
        String depositState = "Y";

        //物品借用地点的描述，便于用户知道物品是在哪里借的。可为空
        String borrowShopName = shopInfo.getName();
        /*
        租金的结算方式，非必填字段，默认是支付宝租金结算支付
        merchant：表示商户自行结算，信用借还不提供租金支付能力；
        alipay：表示使用支付宝支付功能，给用户提供租金代扣及赔偿金支付能力；
         */
        String rentSettleType = "alipay";

        //商户订单创建的起始借用时间，格式：YYYY-MM-DD HH:MM:SS。如果不传入或者为空，则认为订单创建起始时间为调用此接口时的时间。
        Date borrowDate = new Date();
        String borrowTime = new SimpleDateFormat("YYYY-MM-dd HH:MM:ss").format(borrowDate);
        //下面的代码用于处理到期时间
        //用开始租借的时间加上最长时长
        long l = borrowDate.getTime() + maxCanBorrowTime * 24 * 60 * 60 * 1000;
        //示例：2017-04-30 12:06:31
        //     2017-09-11 13:09:23
        //到期时间，是指最晚归还时间，表示借用用户如果超过此时间还未完结订单（未归还物品或者未支付租金）将会进入逾期状态，
        // 芝麻会给借用用户发送催收提醒。如果此时间不传入或传空，将视为无限期借用
        String expiryTime = new SimpleDateFormat("YYYY-MM-dd HH:MM:ss").format(new Date(l));

        //创建一个未支付订单
        alipayOrderService.createPreOrder(outOrderNo, sid, cableType, session);

        Map<String, Object> bizContentMap = new LinkedHashMap<>();
        bizContentMap.put("invoke_type", "WINDOWS");
        bizContentMap.put("invoke_return_url", invokeReturnUrl);
        bizContentMap.put("out_order_no", outOrderNo);
        bizContentMap.put("product_code", productCode);
        bizContentMap.put("goods_name", goodsName);
        bizContentMap.put("rent_info", rentInfo);
        bizContentMap.put("rent_unit", rentUnit);
        bizContentMap.put("rent_amount", rentAmount);
        bizContentMap.put("deposit_amount", depositAmount);
        bizContentMap.put("deposit_state", depositState);
        bizContentMap.put("borrow_shop_name", borrowShopName);
        bizContentMap.put("rent_settle_type", rentSettleType);
        bizContentMap.put("borrow_time", borrowTime);
        bizContentMap.put("expiry_time", expiryTime);

        request.setBizContent(JsonUtils.writeValueAsString(bizContentMap));
        ZhimaMerchantOrderRentCreateResponse response = null;

        try {
            response = alipayClient.pageExecute(request, "GET"); // 这里一定要用GET模式
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //返回的map
        Map<String,String> returnMap = new LinkedHashMap<>();
        if (response.isSuccess()) {
            System.out.println("调用成功，信用借还订单创建成功");
            String url = response.getBody(); // 从body中获取url
            System.out.println("generateRentUrl url:" + url);
            returnMap.put("msg","success");
            returnMap.put("url",url);
            return JsonUtils.writeValueAsString(returnMap);
        } else {
            returnMap.put("msg","fail");
            System.out.println("调用失败");
            logger.error("调用创建信用借还订单失败，错误代码：" + response.getCode() + "错误信息：" + response.getMsg() +
                    "错误子代码" + response.getSubCode() + "错误子信息：" + response.getSubMsg());
        }
        return null;
    }

}