package com.ycb.wpc.provider.mapper;

import com.ycb.wpc.provider.vo.User;
import com.ycb.wpc.provider.vo.UserInfo;
import com.ycb.wpc.provider.vo.UserInfoVo;
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

    @Insert("Insert INTO ycb_mcs_user(createdBy,createdDate,optlock,openid,platform,usablemoney,deposit,refund,refunded,unsubscribe) " +
            "VALUES(#{createdBy},#{createdDate},#{version},#{openid},#{platform},#{usablemoney},#{deposit},#{refund},#{refunded},0)")
    void insert(User user);

    @Select("SELECT u.id,ui.nickname,u.usablemoney,ui.headimgurl,u.openid FROM ycb_mcs_user u,ycb_mcs_userinfo ui WHERE u.openid=ui.openid AND u.openid = #{openid}")
    UserInfoVo findUserinfo(@Param("openid") String openid);


    @Select("SELECT deposit, usablemoney, refund FROM ycb_mcs_user u WHERE openid = #{openid}")
    User findUserMessageyOpenid(@Param("openid") String openid);

    @Select("SELECT * FROM ycb_mcs_user WHERE openid = #{openid}")
    User findUserinfoByOpenid(@Param("openid") String openid);

    @Update("Update ycb_mcs_user " +
            "SET lastModifiedBy='system', lastModifiedDate=#{date}, optlock=#{version} " +
            "WHERE openid=#{openid} ")
    void update(@Param("version") Integer version, @Param("date") Date date, @Param("openid") String openid);

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

    @Select("Select id, usablemoney, refund, refunded, deposit from ycb_mcs_user WHERE openid = #{openid}")
    User findUserByOpenid(String openid);

    @Select("SELECT openid FROM ycb_mcs_user WHERE platform = 2 AND openid IN(SELECT openid FROM ycb_mcs_userinfo WHERE unionid IN(SELECT unionid FROM ycb_mcs_userinfo WHERE openid = #{openid}))")
    String findSrOpenidByOpenid(String openid);
}
