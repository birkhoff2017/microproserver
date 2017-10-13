package com.ycb.zprovider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.ZhimaMerchantOrderRentCreateRequest;
import com.alipay.api.response.ZhimaMerchantOrderRentCreateResponse;
import com.ycb.zprovider.constant.GlobalConfig;
import com.ycb.zprovider.mapper.ShopMapper;
import com.ycb.zprovider.service.AlipayOrderService;
import com.ycb.zprovider.service.FeeStrategyService;
import com.ycb.zprovider.utils.JsonUtils;
import com.ycb.zprovider.vo.AlipayClientFactory;
import com.ycb.zprovider.vo.FeeStrategy;
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

    //最长可借用时间，超时后视为逾期订单，单位：天
    @Value("${MAX_CAN_BORROW_TIME}")
    private Integer maxCanBorrowTime;
    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private FeeStrategyService feeStrategyService;

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
        // 在借用完成页面会自动回调到商户提供的invoke_return_url地址链接，目前商户链接跳转是通过自动跳转的方式实现。
        String invokeReturnUrl = GlobalConfig.Z_GATEWAY_URL + "loading.html";
        //下面的代码用来生成外部订单号，就是商户自己的订单号
        String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Random random = new Random();
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }
        //外部订单号，需要唯一，由商户传入，芝麻内部会做幂等控制，格式为：yyyyMMddHHmmss+随机数
        String outOrderNo = date + sb.toString();
        //信用借还的产品码，传入固定值：w1010100000000002858
        String productCode = GlobalConfig.Z_PRODUCT_CODE;
        //物品名称,最长不能超过14个汉字
        String goodsName = "云充吧-充电宝";
        //租金信息描述 ,长度不超过14个汉字，只用于页面展示给C端用户，除此之外无其他意义。例如：1小时免费，10元/天
        FeeStrategy feeStrategy = feeStrategyService.findFeeStrategyByStation(Long.valueOf(sid));
        String rentInfo = feeStrategyService.creditDescFeeStrategy(feeStrategy);
        /*
        租金单位，租金+租金单位组合才具备实际的租金意义。
        取值定义如下：
        HOUR_YUAN:元/小时
         */
        String rentUnit = "HOUR_YUAN";
        /*
        租金，租金+租金单位组合才具备实际的租金意义。
        >0.00元，代表有租金
        =0.00元，代表无租金，免费借用
        注：参数传值必须>=0，传入其他值会报错参数非法
         */
        String rentAmount = feeStrategy.getFee().toString();
        /*
        押金，金额单位：元。
         */
        //根据设备的sid查询店铺信息
        Shop shopInfo = shopMapper.getShopInfoBySid(sid);
        //查询出设备押金
        String depositAmount = shopInfo.getDefaultPay().toString();
        /*
        是否支持当借用用户信用不够（不准入）时，可让用户支付押金借用:
        Y:支持
        N:不支持
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
        String borrowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(borrowDate);
        //下面的代码用于处理到期时间
        //用开始租借的时间加上最长时长
        long l = borrowDate.getTime() + maxCanBorrowTime * 24 * 60 * 60 * 1000;
        //到期时间，是指最晚归还时间，表示借用用户如果超过此时间还未完结订单（未归还物品或者未支付租金）将会进入逾期状态，
        // 芝麻会给借用用户发送催收提醒。如果此时间不传入或传空，将视为无限期借用
        String expiryTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(l));

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
            logger.error(e.getMessage());
        }
        //返回的map
        Map<String, String> returnMap = new LinkedHashMap<>();
        if (null != response && response.isSuccess()) {
            String url = response.getBody(); // 从body中获取url
            returnMap.put("msg", "success");
            returnMap.put("url", url);
            logger.info("创建信用借还订单成功" + response.getBody());
            return JsonUtils.writeValueAsString(returnMap);
        } else {
            returnMap.put("msg", "fail");
            if (response != null) {
                logger.error("调用创建信用借还订单失败，错误代码：" + response.getCode() + "错误信息：" + response.getMsg() +
                        "错误子代码" + response.getSubCode() + "错误子信息：" + response.getSubMsg());
            }
        }
        return null;
    }
}