package com.ycb.zprovider.controller;

import com.ycb.zprovider.constant.GlobalConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by duxinyuan on 2017/9/18.
 */
@RestController
@RequestMapping("q")
public class IndexController {

    @Value("${APPID}")
    private String appId;

    // 扫码入口
    @RequestMapping(value = "{stationId}", method = RequestMethod.GET)
    public void scanEntry(@PathVariable("stationId") String stationId, HttpServletRequest request, HttpServletResponse response) throws IOException {
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
                    "&redirect_uri=http%3a%2f%2fwww.duxinyuan.top%2foauth%2foauthNotify" +
                    "&state="+stationId;  //设备ID
            response.sendRedirect(url);
        }
    }
}
