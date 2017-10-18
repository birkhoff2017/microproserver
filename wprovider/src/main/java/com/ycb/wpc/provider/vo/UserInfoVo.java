package com.ycb.wpc.provider.vo;

import java.math.BigDecimal;

/**
 * Created by Bruce on 17-10-10. 用户中心接口实体类
 */
public class UserInfoVo {

    //用户ID
    private Long id;

    //用户昵称
    private String nickname;

    //账户余额
    private BigDecimal usablemoney;

    //头像地址
    private String headimgurl;

    //用户openid
    private String openid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public BigDecimal getUsablemoney() {
        return usablemoney;
    }

    public void setUsablemoney(BigDecimal usablemoney) {
        this.usablemoney = usablemoney;
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public void setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }
}
