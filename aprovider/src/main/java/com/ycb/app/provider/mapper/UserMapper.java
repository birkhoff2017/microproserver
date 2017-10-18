package com.ycb.app.provider.mapper;

import com.ycb.app.provider.vo.User;
import com.ycb.app.provider.vo.UserAuth;
import com.ycb.app.provider.vo.UserInfo;
import com.ycb.app.provider.vo.UserInfoVo;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by zhuhui on 17-6-16.
 */
@Mapper
public interface UserMapper {
    @Select("Select optlock from ycb_mcs_user WHERE openid = #{openid}")
    Integer findByOpenid(@Param("openid") String openid);

    @Select("Select optlock from ycb_mcs_user WHERE mobile = #{mobile}")
    Integer findByMobile(@Param("mobile") String mobile);

    @Insert("Insert INTO ycb_mcs_user(createdBy,createdDate,optlock,openid,platform,usablemoney,deposit,refund,refunded,unsubscribe) " +
            "VALUES(#{createdBy},#{createdDate},#{version},#{openid},#{platform},#{usablemoney},#{deposit},#{refund},#{refunded},0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long insert(User user);

    @Select("SELECT u.id,ui.nickname,u.usablemoney,ui.headimgurl,u.deposit,u.refund FROM ycb_mcs_user u,ycb_mcs_userinfo ui WHERE u.openid=ui.openid AND u.openid = #{openid}")
    UserInfoVo findUserinfo(@Param("openid") String openid);

    @Select("SELECT * FROM ycb_mcs_user WHERE openid = #{openid}")
    User findUserinfoByOpenid(@Param("openid") String openid);

    @Update("Update ycb_mcs_user " +
            "SET lastModifiedBy='system', lastModifiedDate=#{date}, optlock=#{version} " +
            "WHERE openid=#{openid} ")
    void update(@Param("version") Integer version, @Param("date") Date date, @Param("openid") String openid);

    @Update("Update ycb_mcs_user " +
            "SET lastModifiedBy='system', lastModifiedDate=#{date}, optlock=#{version} " +
            "WHERE mobile=#{mobile} ")
    void updateByMobile(@Param("version") Integer version, @Param("date") Date date, @Param("mobile") String mobile);

    @Select("Select id,usablemoney from ycb_mcs_user WHERE openid = #{openid}")
    User findUserMoneyByOpenid(String openid);

    @Update("Update ycb_mcs_user SET lastModifiedBy=#{lastModifiedBy},lastModifiedDate=NOW(),usablemoney=usablemoney-#{refund},refund=refund-#{refund},refunded=refunded+#{refunded} WHERE id=#{id}")
    void updateUsablemoneyByUid(User user);

    @Select("Select id, usablemoney from ycb_mcs_user WHERE openid = #{openid}")
    User findUserIdByOpenid(String openid);

    @Update("Update ycb_mcs_user SET lastModifiedBy='SYS:pay',lastModifiedDate=NOW(),optlock =optlock+1,deposit=deposit+#{paid} WHERE id=#{customer}")
    void updateUserDeposit(@Param("paid") BigDecimal paid, @Param("customer") Long customer);

    @Update("Update ycb_mcs_user SET lastModifiedBy='SYS:pay',lastModifiedDate=NOW(),deposit=deposit+#{defaultPay},usablemoney=usablemoney-#{defaultPay} WHERE id=#{id}")
    void updateUserDepositUsable(@Param("defaultPay") BigDecimal defaultPay, @Param("id") Long id);

    @Insert("Insert INTO ycb_mcs_userinfo(createdBy,createdDate,optlock,openid,unionid,nickname,sex,language,city,province,country,headimgurl) " +
            "VALUES(#{createdBy},#{createdDate},#{version},#{openid},#{unionid},#{nickname},#{sex},#{language},#{city},#{province},#{country},#{headimgurl})")
    void insertUserInfo(UserInfo userInfo);

    @Update("Update ycb_mcs_user SET lastModifiedBy=#{lastModifiedBy},lastModifiedDate=NOW(),refund=refund+#{refund} WHERE id=#{id}")
    void updateUserRefund(User user);

    @Select("Select u.* from ycb_mcs_user u,ycb_mcs_user_auth a WHERE a.user_id = u.id AND a.identity_type = 4 AND a.identifier = #{mobile}")
    User findUserByMobile(String mobile);

    @Insert("Insert INTO ycb_mcs_user_auth(createdBy,createdDate,optlock,user_id,identity_type,identifier,credential) " +
            "VALUES(#{createdBy},#{createdDate},#{version},#{userId},#{identityType},#{identifier},#{credential})")
    void insertUserAuth(UserAuth userAuth);

    @Update("Update ycb_mcs_user_auth SET lastModifiedBy='SYS:register',lastModifiedDate=NOW() WHERE identity_type = 0 AND identifier = #{mobile}")
    void updateUserAuth(String mobile);

    @Select("Select * from ycb_mcs_user_auth  WHERE identity_type = 4 AND identifier = #{mobile}")
    UserAuth findUserAuthByMobile(String mobile);
}
