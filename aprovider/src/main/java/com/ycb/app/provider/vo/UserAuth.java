package com.ycb.app.provider.vo;

/**
 * Created by zhuhui on 17-10-10.
 */
public class UserAuth extends BaseEntity {

    //@MetaData("用户表")
    private Long userId;

    //@MetaData("账户类型")
    private String identityType;

    //@MetaData("账户值")
    private String identifier;

    //@MetaData("凭据")
    private String credential;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }
}
