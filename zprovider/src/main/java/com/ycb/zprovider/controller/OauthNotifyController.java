package com.ycb.zprovider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.ycb.zprovider.cache.RedisService;
import com.ycb.zprovider.constant.GlobalConfig;
import com.ycb.zprovider.mapper.UserMapper;
import com.ycb.zprovider.utils.MD5;
import com.ycb.zprovider.vo.AlipayClientFactory;
import com.ycb.zprovider.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by duxinyuan on 17-6-19.
 */
@Controller
@RequestMapping("/oauth")
public class OauthNotifyController {

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AlipayClientFactory alipayClientFactory;

    @Value("${APPID}")
    private String appId;

    // 支付宝授权回调页面
    @RequestMapping(value = "/oauthNotify", method = RequestMethod.GET)
    public void oauthNotify(HttpServletRequest request, HttpServletResponse response) throws Exception{

        String stationId = request.getParameter("state");
        String auth_code = request.getParameter("auth_code");
        //获取token
        AlipayClient alipayClient = alipayClientFactory.newInstance();
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

            //判断stationId是否为空 如果空 说明是用户中心里面的授权，不为空则是扫码租借时的授权
            if (StringUtils.isEmpty(stationId)){
                //跳转到用户中心
                response.sendRedirect("http://www.duxinyuan.top/userInfo.html?session=" + session);
            }else {
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
                    response.sendRedirect(url);
                }else {
                    optlock++;
                    this.userMapper.update(optlock, new Date(), alipayUserId);
                    response.sendRedirect("http://www.duxinyuan.top/borrow.html?sid=" + stationId + "&session=" + session);
                }
            }
        } catch (AlipayApiException e) {
            //处理异常
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/userInfoOauth", method = RequestMethod.GET)
    public void userInfoOauth(HttpServletRequest request, HttpServletResponse response) {
        String url = GlobalConfig.ZFB_SEND_AUTH_URL +
                "app_id=" + appId +
                "&scope=auth_base" +
                "&redirect_uri=http%3a%2f%2fwww.duxinyuan.top%2foauth%2foauthNotify";
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
