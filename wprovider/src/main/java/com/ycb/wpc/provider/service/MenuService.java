package com.ycb.wpc.provider.service;

import com.ycb.wpc.provider.constant.WechatMenuType;
import com.ycb.wpc.provider.utils.WeixinUtil;
import com.ycb.wpc.provider.vo.Button;
import com.ycb.wpc.provider.vo.CommonButton;
import com.ycb.wpc.provider.vo.ComplexButton;
import com.ycb.wpc.provider.vo.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by Bruce on 17-10-17.
 */
@Service
public class MenuService {
    private static final Logger log = LoggerFactory.getLogger(MenuService.class);

    @Autowired
    private MessageService messageService;

    // 第三方用户唯一凭证
    @Value("${appID}")
    private String appId;
    // 第三方用户唯一凭证密钥
    @Value("${appSecret}")
    private String appSecret;
    // 回调地址
    @Value("${redirect_uri}")
    private String redirect_uri;
    // 请求地址
    @Value("${request_uri}")
    private String request_uri;

    public void createMenu() {

        // 调用接口获取access_token
        String accessToken = messageService.getAccessToken(appId, appSecret);

        if (null != accessToken) {
            // 调用接口创建菜单
            int result = WeixinUtil.createMenu(getMenu(), accessToken);

            // 判断菜单创建结果
            if (0 == result)
                log.info("菜单创建成功！");
            else
                log.info("菜单创建失败，错误码：" + result);
        }
    }

    /**
     * 组装菜单数据
     *
     * @return
     */
    private Menu getMenu() {

        CommonButton btn31 = new CommonButton();
        btn31.setName("云账户");
        btn31.setType(WechatMenuType.view.toString());
        String url31 = request_uri + "?appid=" + appId + "&redirect_uri=" + redirect_uri + "/user.html&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
        btn31.setUrl(url31);

        CommonButton btn32 = new CommonButton();
        btn32.setName("招商政策");
        btn32.setType(WechatMenuType.view.toString());
        String url32 = "http://mp.weixin.qq.com/s/jLtnnzrrVnUSQISTRHEXKA";
        btn32.setUrl(url32);

        CommonButton btn33 = new CommonButton();
        btn33.setName("联系合作");
        btn33.setType(WechatMenuType.view.toString());
        String url33 = "http://mp.weixin.qq.com/s/jLtnnzrrVnUSQISTRHEXKA";
        btn33.setUrl(url33);


        /**
         * 微信：  mainBtn1,mainBtn2,mainBtn3底部的三个一级菜单。
         */

        ComplexButton mainBtn1 = new ComplexButton();
        mainBtn1.setName("借充电宝");
        mainBtn1.setType(WechatMenuType.scancode_push.toString());
        mainBtn1.setKey("1");
        ComplexButton mainBtn2 = new ComplexButton();
        //跳转小程序
        //测试号不能用，必须用企业的在后台先关联 ，再通过接口 发布菜单
        mainBtn2.setName("附近网点");
        mainBtn2.setType(WechatMenuType.miniprogram.toString());
        mainBtn2.setUrl("https//x.yunchongba.com");
        mainBtn2.setAppid("wxb2fe17882a52afe9");
        mainBtn2.setPagepath("pages/index/index");


        ComplexButton mainBtn3 = new ComplexButton();
        mainBtn3.setName("用户中心");
        mainBtn3.setSub_button(new CommonButton[]{btn31, btn32, btn33});


        /**
         * 封装整个菜单
         */
        Menu menu = new Menu();
        menu.setButton(new Button[]{mainBtn1, mainBtn2, mainBtn3});
        return menu;
    }
}
