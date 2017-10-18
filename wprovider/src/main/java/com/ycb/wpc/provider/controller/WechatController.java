package com.ycb.wpc.provider.controller;

import com.ycb.wpc.provider.mapper.UserMapper;
import com.ycb.wpc.provider.service.MenuService;
import com.ycb.wpc.provider.service.SaveUserService;
import com.ycb.wpc.provider.utils.WeixinUtil;
import com.ycb.wpc.provider.utils.XmlUtil;
import com.ycb.wpc.provider.vo.Menu;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信开发
 * <p>
 * Created by Bruce on 2017/9/28.
 */
@RestController
@RequestMapping("wechat")
public class WechatController {
    public static final Logger logger = LoggerFactory.getLogger(WechatController.class);

    @Autowired(required = false)
    private UserMapper userMapper;

    @Value("${appID}")
    private String appID;

    @Value("${token}")
    private String token;

    @Value("${appSecret}")
    private String appSecret;

    @Autowired
    private SaveUserService saveUserService;

    @Autowired
    private MenuService menuService;

    private static final String subscribe = "subscribe";

    /**
     * 验证微信服务器
     *
     * @param signature
     * @param timestamp
     * @param nonce
     * @param echostr
     * @return
     */
    @RequestMapping(value = "/wechat", method = RequestMethod.GET)
    @ResponseBody
    public String query(@RequestParam("signature") String signature, @RequestParam("timestamp") String timestamp,
                        @RequestParam("nonce") String nonce, @RequestParam("echostr") String echostr) {
        logger.info("signature：" + signature + "\ntimestamp：" + timestamp + "\nnonce：" + nonce + "\nechostr：" + echostr);
        if (WeixinUtil.validate(signature, timestamp, nonce)) {
            logger.info("验证token成功：signature");
            return echostr;
        }
        logger.error("验证tokn失败：signature");
        return null;
    }

    /**
     * 接收来自微信发来的消息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/wechat", method = RequestMethod.POST)
    @ResponseBody
    public void saveUser(HttpServletRequest request, HttpServletResponse response,
                         @RequestParam("timestamp") String timestamp) throws Throwable {
        Map<String, Object> bacMap = new HashMap<>();
        PrintWriter out = response.getWriter();
        try {
            String requsetStr = IOUtils.toString(request.getInputStream());

            Map<String, Object> requestMap = XmlUtil.doXMLParse(requsetStr);

            // 发送方帐号（open_id）
            String openid = (String) requestMap.get("FromUserName");

            //回复的消息内容
            String event = (String) requestMap.get("Event");

            //当用户关注时，则保存用户信息并回复
            if (StringUtils.equals(event, subscribe)) {
                this.saveUserService.saveUser(openid);
            }
            // 将请求、响应的编码均设置为UTF-8（防止中文乱码）
            request.setCharacterEncoding("UTF-8");
            //response.setCharacterEncoding("UTF-8");

            // 调用核心业务类接收消息、处理消息
            String respMessage = WeixinUtil.automatismRestoreEntity(requestMap);
            logger.info("响应的消息为：" + respMessage);
            // 响应消息
            String s = new String(respMessage.getBytes("UTF-8"), "iso-8859-1");
            out.print(s);
            out.flush();
            out.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        out.print("success");
    }

    @RequestMapping(value = "/createMenu", method = RequestMethod.GET)
    @ResponseBody
    public void createMenu() {
        try {
            this.menuService.createMenu();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
