package com.ycb.wpc.provider.vo;

/**
 * 类名: ComplexButton </br>
 * 包名： com.ycb.wpc.provider.menu.pojo
 * 描述: 根菜单项。 </br>
 * 开发人员： Bruce  </br>
 * 创建时间：  2017-10-7 </br>
 */
public class ComplexButton extends CommonButton {
    private CommonButton[] sub_button;

    private String appid;

    private String pagepath;

    public CommonButton[] getSub_button() {
        return sub_button;
    }

    public void setSub_button(CommonButton[] sub_button) {
        this.sub_button = sub_button;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getPagepath() {
        return pagepath;
    }

    public void setPagepath(String pagepath) {
        this.pagepath = pagepath;
    }
}
