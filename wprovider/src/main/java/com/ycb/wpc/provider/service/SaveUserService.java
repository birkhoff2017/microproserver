package com.ycb.wpc.provider.service;

import com.ycb.wpc.provider.constant.GlobalConfig;
import com.ycb.wpc.provider.mapper.UserMapper;
import com.ycb.wpc.provider.utils.HttpRequest;
import com.ycb.wpc.provider.utils.JsonUtils;
import com.ycb.wpc.provider.utils.WeixinUtil;
import com.ycb.wpc.provider.vo.User;
import com.ycb.wpc.provider.vo.UserInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * Created by Bruce on 17-10-10.
 */
@Service
public class SaveUserService {

    public static final Logger logger = LoggerFactory.getLogger(SaveUserService.class);

    @Autowired(required = false)
    private UserMapper userMapper;

    @Autowired()
    private MessageService messageService;

    @Value("${appID}")
    private String appID;
    @Value("${appSecret}")
    private String appSecret;

    public void saveUser(String openid) throws Exception {
        try {
            //获取accessToken
            String accessToken = this.messageService.getAccessToken(appID, appSecret);

            String param = "access_token=" + accessToken + "&openid=" + openid;
            String getUserMsg = HttpRequest.sendGet(GlobalConfig.WX_USERINFO_URL, param);
            Map<String, Object> userInfoMap = JsonUtils.readValue(getUserMsg);
            Integer optlock = this.userMapper.findByOpenid(openid);
            if (optlock == null && StringUtils.equals(userInfoMap.get("subscribe").toString(), "1")) {
                User user = new User();
                user.setOpenid(openid);
                user.setDeposit(BigDecimal.ZERO);
                user.setRefund(BigDecimal.ZERO);
                user.setUsablemoney(BigDecimal.ZERO);
                user.setRefunded(BigDecimal.ZERO);
                user.setPlatform(0);//微信公众号
                user.setCreatedBy("User:subscribe");
                user.setCreatedDate(new Date());
                this.userMapper.insert(user);
                UserInfo userInfo = new UserInfo();
                userInfo.setOpenid(openid);
                userInfo.setNickname((String) userInfoMap.get("nickname"));
                userInfo.setSex((Integer) userInfoMap.get("sex"));
                userInfo.setLanguage((String) userInfoMap.get("language"));
                userInfo.setCity((String) userInfoMap.get("city"));
                userInfo.setProvince((String) userInfoMap.get("province"));
                userInfo.setCountry((String) userInfoMap.get("country"));
                userInfo.setHeadimgurl((String) userInfoMap.get("headimgurl"));
                userInfo.setUnionid((String) userInfoMap.get("unionid"));
                userInfo.setCreatedBy("User:subscribe");
                userInfo.setCreatedDate(new Date());
                this.userMapper.insertUserInfo(userInfo);
            } else {
                optlock++;
                this.userMapper.update(optlock, new Date(), openid);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }
}
