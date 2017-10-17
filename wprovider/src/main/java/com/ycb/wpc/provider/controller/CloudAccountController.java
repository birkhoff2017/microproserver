package com.ycb.wpc.provider.controller;

import com.ycb.wpc.provider.cache.RedisService;
import com.ycb.wpc.provider.constant.GlobalConfig;
import com.ycb.wpc.provider.mapper.UserMapper;
import com.ycb.wpc.provider.service.SaveUserService;
import com.ycb.wpc.provider.utils.HttpRequest;
import com.ycb.wpc.provider.utils.JsonUtils;
import com.ycb.wpc.provider.utils.MD5;
import com.ycb.wpc.provider.vo.UserInfoVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 云账户
 *
 * Created by Bruce on 2017/10/9
 */
@RestController
@RequestMapping("cloudAccount")
public class CloudAccountController {

    @Autowired(required = false)
    private UserMapper userMapper;

    @Autowired
    private SaveUserService saveUserService;

    @Autowired
    private RedisService redisService;

    @Value("${appID}")
    private String appID;

    @Value("${appSecret}")
    private String appSecret;

    private static final Integer expiresIn = 60 * 60 * 24;

    @RequestMapping(value = "/userInfo", method = RequestMethod.POST)
    @ResponseBody
    public String userInfo(@RequestParam("code") String code) {

        Map<String, Object> bacMap = new HashMap<>();

        try {
            String openid = this.redisService.getKeyValue(MD5.getMessageDigest(code.getBytes()));
            if (StringUtils.isBlank(openid)){
                String param = "appid="+appID+"&secret="+appSecret+"&code="+code+"&grant_type=authorization_code ";
                String requestStr = HttpRequest.sendGet(GlobalConfig.WX_AUTH2_URL, param);
                Map<String, Object> requestMap = JsonUtils.readValue(requestStr);
                openid = (String)requestMap.get("openid");
                this.redisService.setKeyValueTimeout(MD5.getMessageDigest(code.getBytes()), openid, Long.valueOf(expiresIn));
            }
            UserInfoVo userInfoVo = this.userMapper.findUserinfo(openid);
            if (null != userInfoVo) {
                Map<String, Object> data = new HashMap<>();
                bacMap.put("code", 0);
                bacMap.put("msg", "成功");
                bacMap.put("data", userInfoVo);
            } else {
                this.saveUserService.saveUser(openid);
                userInfoVo = this.userMapper.findUserinfo(openid);
                Map<String, Object> data = new HashMap<>();
                bacMap.put("code", 0);
                bacMap.put("msg", "成功");
                bacMap.put("data", userInfoVo);
            }
        } catch (Exception e) {
            bacMap.put("data", null);
            bacMap.put("code", 1);
            bacMap.put("msg", "获取用户信息失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }
}
