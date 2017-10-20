package com.ycb.app.provider.controller;

import com.ycb.app.provider.cache.RedisService;
import com.ycb.app.provider.constant.GlobalConfig;
import com.ycb.app.provider.mapper.UserMapper;
import com.ycb.app.provider.service.SmsService;
import com.ycb.app.provider.utils.JsonUtils;
import com.ycb.app.provider.utils.MD5;
import com.ycb.app.provider.vo.User;
import com.ycb.app.provider.vo.UserAuth;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
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

    @Autowired
    private SmsService smsService;

    @Value("${appID}")
    private String appID;

    @Value("${appSecret}")
    private String appSecret;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public String login(@RequestParam("session") String session) {
        Map<String, Object> bacMap = new HashMap<>();
        try {
            // 判断验证码是否正确
            String mobile = redisService.getKeyValue(session);
            if (StringUtils.isEmpty(mobile)) {
                bacMap.put("data", null);
                bacMap.put("code", 1);
                bacMap.put("msg", "session失效");
            } else {
                // 根据手机号，检索授权表，如果存在，获取主账户，更新并返回session
                User user = this.userMapper.findUserByMobile(mobile);
                if (user == null) {
                    // 如果不存在，则新建主账户，并创建授权表
                    bacMap.put("data", null);
                    bacMap.put("code", 2);
                    bacMap.put("msg", "异常session");
                } else {
                    bacMap.put("data", user);
                    bacMap.put("code", 0);
                    bacMap.put("msg", "成功");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            bacMap.put("data", null);
            bacMap.put("code", 111);
            bacMap.put("msg", "异常失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public String register(@RequestParam("code") String code,
                           @RequestParam("mobile") String mobile) {
        Map<String, Object> bacMap = new HashMap<>();
        try {
            // 判断验证码是否正确
            String rcode = redisService.getKeyValue(GlobalConfig.SMS_CODE_PRE + mobile);
            if (StringUtils.isEmpty(rcode)) {
                bacMap.put("data", null);
                bacMap.put("code", 1);
                bacMap.put("msg", "验证码失效");
            } else if (!rcode.equals(code)) {
                bacMap.put("data", null);
                bacMap.put("code", 2);
                bacMap.put("msg", "验证码不正确");
            } else {
                // 根据手机号，检索授权表，如果存在，获取主账户，更新并返回session
                UserAuth userAuth = this.userMapper.findUserAuthByMobile(mobile);
                if (userAuth == null) {
                    // 如果不存在，则新建主账户，并创建授权表
                    User user = this.userMapper.findUserByMobile(mobile);
                    if (user == null) {
                        user = new User();
                        user.setOpenid(mobile);
                        user.setDeposit(BigDecimal.ZERO);
                        user.setRefund(BigDecimal.ZERO);
                        user.setUsablemoney(BigDecimal.ZERO);
                        user.setRefunded(BigDecimal.ZERO);
                        user.setPlatform(4);
                        user.setCreatedBy("SYS:register");
                        user.setCreatedDate(new Date());
                        this.userMapper.insert(user);
                    }
                    userAuth = new UserAuth();
                    userAuth.setCreatedBy("SYS:register");
                    userAuth.setCreatedDate(new Date());
                    userAuth.setUserId(user.getId());
                    userAuth.setIdentityType("4");
                    userAuth.setIdentifier(mobile);
                    this.userMapper.insertUserAuth(userAuth);
                } else {
                    this.userMapper.updateUserAuth(mobile);
                }
                // 生成第三方session
                String session = MD5.getMessageDigest((mobile + code).getBytes());
                // 设置session过期
                redisService.setKeyValue(session, mobile);
                Map<String, Object> data = new HashMap<>();
                data.put("session", session);
                bacMap.put("data", data);
                bacMap.put("code", 0);
                bacMap.put("msg", "成功");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            bacMap.put("data", null);
            bacMap.put("code", 111);
            bacMap.put("msg", "异常失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }

    @RequestMapping(value = "/getSms", method = RequestMethod.POST)
    @ResponseBody
    public String getSms(@RequestParam("mobile") String mobile) {
        Map<String, Object> bacMap = new HashMap<>();
        try {
            String code = RandomStringUtils.random(4, "0123456789");
            boolean sendResult = smsService.senSms(mobile, "【云充吧】" + code + "(云充吧手机第三方授权验证码，如非您本人操作，请乎略本短信。)");
            if (sendResult) {
                redisService.setKeyValueTimeout(GlobalConfig.SMS_CODE_PRE + mobile, code, 90l);
                bacMap.put("data", null);
                bacMap.put("code", 0);
                bacMap.put("msg", "获取验证码成功");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            bacMap.put("data", null);
            bacMap.put("code", 1);
            bacMap.put("msg", "获取验证码失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }

    @RequestMapping(value = "/loginOut", method = RequestMethod.POST)
    @ResponseBody
    public String loginOut(@RequestParam("session") String session) {
        Map<String, Object> bacMap = new HashMap<>();
        try {
            redisService.deleteKey(session);
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