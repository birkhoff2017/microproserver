package com.ycb.app.provider.controller;

import com.ycb.app.provider.cache.RedisService;
import com.ycb.app.provider.constant.GlobalConfig;
import com.ycb.app.provider.mapper.UserMapper;
import com.ycb.app.provider.utils.HttpRequest;
import com.ycb.app.provider.utils.JsonUtils;
import com.ycb.app.provider.utils.MD5;
import com.ycb.app.provider.vo.User;
import com.ycb.app.provider.vo.UserInfo;
import com.ycb.app.provider.vo.UserInfoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhuhui on 17-6-16.
 */
@RestController
@RequestMapping("user")
public class UserController {

    public static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired(required = false)
    private UserMapper userMapper;

    @Autowired
    private RedisService redisService;

    @Value("${appID}")
    private String appID;

    @Value("${appSecret}")
    private String appSecret;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public String login(@RequestParam("code") String code,
                        @RequestParam("encryptedData") String encryptedData,
                        @RequestParam("iv") String iv) {
        // 通过微信接口,根据code获取sessionkey和openid
        String param = "appid=" + appID + "&secret=" + appSecret + "&js_code=" + code + "&grant_type=authorization_code";
        Map<String, Object> bacMap = new HashMap<>();
        try {
            String getLoginInfo = HttpRequest.sendGet(GlobalConfig.WX_OPENID_URL, param);
            Map<String, Object> loginInfoMap = JsonUtils.readValue(getLoginInfo);
            if (loginInfoMap.containsKey("errcode")) {
                if (loginInfoMap.get("errcode").equals(40163)) {
                    bacMap.put("data", null);
                    bacMap.put("code", 3);
                    bacMap.put("msg", "登录code不合法");
                } else if (loginInfoMap.get("errcode").equals(40029)) {
                    bacMap.put("data", null);
                    bacMap.put("code", 3);
                    bacMap.put("msg", "登录code不合法");
                } else {
                    bacMap.put("data", null);
                    bacMap.put("code", 3);
                    bacMap.put("msg", "登录code不合法");
                }
            } else {
                String sessinKey = (String) loginInfoMap.get("session_key");
                String openid = (String) loginInfoMap.get("openid");
                Long expiresIn = Long.valueOf(loginInfoMap.get("expires_in").toString());
                // 根据openid检索数据库，不存在新建用户
                Integer optlock = this.userMapper.findByOpenid(openid);
                if (optlock == null) {
                    User user = new User();
                    user.setOpenid(openid);
                    user.setDeposit(BigDecimal.ZERO);
                    user.setRefund(BigDecimal.ZERO);
                    user.setUsablemoney(BigDecimal.ZERO);
                    user.setRefunded(BigDecimal.ZERO);
                    user.setPlatform(3);
                    user.setCreatedBy("SYS:login");
                    user.setCreatedDate(new Date());
                    this.userMapper.insert(user);
                    Map<String, Object> userInfoMap = HttpRequest.getUserInfo(encryptedData, sessinKey, iv);
                    UserInfo userInfo = new UserInfo();
                    userInfo.setOpenid(openid);
                    userInfo.setNickname((String) userInfoMap.get("nickName"));
                    userInfo.setSex((Integer) userInfoMap.get("gender"));
                    userInfo.setLanguage((String) userInfoMap.get("language"));
                    userInfo.setCity((String) userInfoMap.get("city"));
                    userInfo.setProvince((String) userInfoMap.get("province"));
                    userInfo.setCountry((String) userInfoMap.get("country"));
                    userInfo.setHeadimgurl((String) userInfoMap.get("avatarUrl"));
                    userInfo.setUnionid((String) userInfoMap.get("unionId"));
                    userInfo.setCreatedBy("SYS:login");
                    userInfo.setCreatedDate(new Date());
                    this.userMapper.insertUserInfo(userInfo);
                } else {
                    optlock++;
                    this.userMapper.update(optlock, new Date(), openid);
                }
                // 生成第三方session
                String session = MD5.getMessageDigest((sessinKey + encryptedData + iv).getBytes());
                // 设置session过期
                redisService.setKeyValueTimeout(session, openid, expiresIn);
                Map<String, Object> data = new HashMap<>();
                data.put("session", session);
                bacMap.put("data", data);
                bacMap.put("code", 0);
                bacMap.put("msg", "成功");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            bacMap.put("data", null);
            bacMap.put("code", 1);
            bacMap.put("msg", "失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }

    // 获取用户基本信息 用户头像，用户昵称，用户账户余额 (用户中心)
    @RequestMapping(value = "/userInfo", method = RequestMethod.POST)
    public String query(@RequestParam("session") String session) {
        Map<String, Object> bacMap = new HashMap<>();
        try {
            String openid = redisService.getKeyValue(session);
            UserInfoVo userInfoVo = this.userMapper.findUserinfo(openid);
            if (null != userInfoVo) {
                Map<String, Object> data = new HashMap<>();
                bacMap.put("code", 0);
                bacMap.put("msg", "成功");
                data.put("user_info", userInfoVo);
                bacMap.put("data", data);
            } else {
                bacMap.put("data", null);
                bacMap.put("code", 2);
                bacMap.put("msg", "session有误");
            }
        } catch (Exception e) {
            bacMap.put("data", null);
            bacMap.put("code", 1);
            bacMap.put("msg", "获取用户信息失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }


    @RequestMapping(value = "/getSms", method = RequestMethod.POST)
    @ResponseBody
    public String getSms(@RequestParam("mobile") String mobile) {
        Map<String, Object> bacMap = new HashMap<>();
        try {
            //初始化clnt,使用单例方式
//            YunpianClient clnt = new YunpianClient("bf1c0728f2d9fd9db23ee6439848efe6").init();
//            String code = RandomStringUtils.random(4, "0123456789");
//            //发送短信API
//            Map<String, String> param = clnt.newParam(2);
//            param.put(YunpianClient.MOBILE, mobile);
//            param.put(YunpianClient.TEXT, "【云充吧】您的验证码是" + code);
//            Result<SmsSingleSend> r = clnt.sms().single_send(param);
//            //获取返回结果，返回码:r.getCode(),返回码描述:r.getMsg(),API结果:r.getData(),其他说明:r.getDetail(),调用异常:r.getThrowable()
//            //账户:clnt.user().* 签名:clnt.sign().* 模版:clnt.tpl().* 短信:clnt.sms().* 语音:clnt.voice().* 流量:clnt.flow().* 隐私通话:clnt.call().*
//            clnt.close();
//            if (r.getCode().equals(0)) {
//                redisService.setKeyValueTimeout("CACHE" + mobile, code, 90L);
//                bacMap.put("data", code);
//                bacMap.put("code", 0);
//                bacMap.put("msg", "成功");
//            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            bacMap.put("data", null);
            bacMap.put("code", 1);
            bacMap.put("msg", "失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }

    @RequestMapping(value = "/checkLogin", method = RequestMethod.POST)
    @ResponseBody
    public String login(@RequestParam("mobile") String mobile,
                        @RequestParam("code") String code) {
        Map<String, Object> bacMap = new HashMap<>();
        try {
            String cacheCode = redisService.getKeyValue("CACHE" + mobile);
            // 失效、不等的情况下返回验证失败
            if (code.equals(cacheCode)) {
                // 根据openid检索数据库，不存在新建用户
                Integer optlock = this.userMapper.findByMobile(mobile);
                if (optlock == null) {
                    User user = new User();
                    user.setMobile(mobile);
                    user.setDeposit(BigDecimal.ZERO);
                    user.setRefund(BigDecimal.ZERO);
                    user.setUsablemoney(BigDecimal.ZERO);
                    user.setRefunded(BigDecimal.ZERO);
                    user.setPlatform(4);//app
                    user.setCreatedBy("SYS:login");
                    user.setCreatedDate(new Date());
                    this.userMapper.insert(user);
                } else {
                    optlock++;
                    this.userMapper.updateByMobile(optlock, new Date(), mobile);
                }
                bacMap.put("data", null);
                bacMap.put("code", 0);
                bacMap.put("msg", "验证通过");
            } else {
                bacMap.put("data", null);
                bacMap.put("code", 2);
                bacMap.put("msg", "验证码失效、或输入有误");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            bacMap.put("data", null);
            bacMap.put("code", 1);
            bacMap.put("msg", "失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }

    @RequestMapping(value = "/loginOut", method = RequestMethod.POST)
    @ResponseBody
    public String loginOut(@RequestParam("mobile") String mobile) {
        Map<String, Object> bacMap = new HashMap<>();
        try {
            redisService.deleteKey("CACHE" + mobile);
            bacMap.put("data", null);
            bacMap.put("code", 0);
            bacMap.put("msg", "成功");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            bacMap.put("data", null);
            bacMap.put("code", 1);
            bacMap.put("msg", "失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }
}
