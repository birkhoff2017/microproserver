package com.ycb.wpc.provider.vo;

/**
 * 类名: CommonButton </br>
 * 包名： com.ycb.wpc.provider.menu.pojo
 * 描述: 子菜单项 :没有子菜单的菜单项，有可能是二级菜单项，也有可能是不含二级菜单的一级菜单。 </br>
 * 开发人员： Bruce  </br>
 * 创建时间：  2017-10-7 </br>
 */
public class CommonButton extends Button {

    private String type;

    private String key;

    private String url;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}