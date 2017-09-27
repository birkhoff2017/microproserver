package com.ycb.zprovider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.ycb.zprovider.cache.RedisService;
import com.ycb.zprovider.constant.GlobalConfig;
import com.ycb.zprovider.mapper.UserMapper;
import com.ycb.zprovider.utils.MD5;
import com.ycb.zprovider.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by duxinyuan on 2017/9/18.
 */
@RestController
@RequestMapping("q")
public class IndexController {

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserMapper userMapper;

    //初始化alipayClient用到的参数:支付宝网关
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

    // 通用跳转页面
    @RequestMapping(value = "{stationId}", method = RequestMethod.GET)
    @ResponseBody
    public void toPage(@PathVariable("stationId") String stationId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String agent = request.getHeader("User-Agent").toLowerCase();
        System.out.println("响应头的类型：" + agent);
        if (agent.indexOf("micromessenger") > 0) {
            System.out.println("微信支付");
            response.setContentType("text/html;charset=UTF-8");
            response.sendRedirect("https://m.dev.yunchongba.com/q/"+stationId);
        } else if (agent.indexOf("alipayclient") > 0) {
            System.out.println("阿里支付");
            String url = GlobalConfig.ZFB_SEND_AUTH_URL +
                    "app_id=" + appId +
                    "&scope=auth_base" +
                    "&redirect_uri=http%3a%2f%2fwww.duxinyuan.top%2fq%2flogin" +
                    "&state="+stationId;
            response.sendRedirect(url);
        }
    }

    // 支付宝授权回调页面
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public void query(HttpServletRequest request, HttpServletResponse response) throws Exception{

        String app_id = request.getParameter("app_id");
        String scope = request.getParameter("scope");
        String stationId = request.getParameter("state");
        String auth_code = request.getParameter("auth_code");
        //获取token
        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL, appId, privateKey, format, charset, alipayPublicKey, signType);
        AlipaySystemOauthTokenRequest alipayRequest = new AlipaySystemOauthTokenRequest();
        alipayRequest.setCode(auth_code);
        alipayRequest.setGrantType("authorization_code");
        try {
            AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient.execute(alipayRequest);
            String alipayUserId = oauthTokenResponse.getAlipayUserId();
            String accessToken = oauthTokenResponse.getAccessToken();
            // 生成第三方session
            String session = MD5.getMessageDigest(alipayUserId.getBytes());
            // 设置session过期
            redisService.setKeyValueTimeout(session, alipayUserId, 7200);

            Integer optlock = this.userMapper.findByOpenid(alipayUserId);
            if (optlock == null) {
                User user = new User();
                user.setOpenid(alipayUserId);
                user.setDeposit(BigDecimal.ZERO);
                user.setRefund(BigDecimal.ZERO);
                user.setUsablemoney(BigDecimal.ZERO);
                user.setRefunded(BigDecimal.ZERO);
                user.setPlatform(1);//支付宝
                user.setCreatedBy("SYS:login");
                user.setCreatedDate(new Date());
                this.userMapper.insert(user);

                //让用户关注
                String url = "http://p.alipay.com/P/RuMIvyjz";
                try {
                    response.sendRedirect(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }else {
                optlock++;
                this.userMapper.update(optlock, new Date(), alipayUserId);
                response.sendRedirect("http://www.duxinyuan.top/index.html?sid=" + stationId + "&session=" + session);
            }
        } catch (AlipayApiException e) {
            //处理异常
            e.printStackTrace();
        }
    }
}
