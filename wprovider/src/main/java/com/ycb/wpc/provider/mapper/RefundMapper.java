package com.ycb.wpc.provider.mapper;

import com.ycb.wpc.provider.vo.Refund;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Created by duxinyuan on 2017/8/5.
 */

@Mapper
public interface RefundMapper {

    @Select("SELECT id,refund,status,UNIX_TIMESTAMP(request_time) AS requestTime ,UNIX_TIMESTAMP(refund_time) AS refundTime\n" +
            "            FROM ycb_mcs_refund_log WHERE uid = #{uid} ORDER BY id DESC LIMIT 0,20")
    List<Refund> findRefunds(Long uid);

    @Insert("INSERT INTO ycb_mcs_refund_log(createdBy,createdDate,optlock,refund,request_time,orderid,status,uid) " +
            "VALUES(#{createdBy},NOW(),#{version},#{refund},NOW(),#{orderid},#{status},#{uid})")
    Integer insertRefund(Refund refund);

    @Select("SELECT MAX(id) id FROM ycb_mcs_refund_log WHERE uid = #{uid}")
    Refund findRefundIdByUid(Long uid);

    @Update("UPDATE ycb_mcs_refund_log SET lastModifiedBy=#{lastModifiedBy},lastModifiedDate=NOW(),refund_time=NOW(),status=#{status} WHERE id=#{id}")
    void updateStatus(Refund refund);

    @Update("UPDATE ycb_mcs_refund_log SET lastModifiedBy=#{lastModifiedBy},lastModifiedDate=NOW(),refunded=#{refund} WHERE id=#{id}")
    void updateRefunded(Refund refund);

    @Update("UPDATE ycb_mcs_refund_log SET lastModifiedBy=#{lastModifiedBy},lastModifiedDate=NOW(),detail=#{detail} WHERE id=#{id}")
    void updateRefundDetail(Refund refund);

    @Select("SELECT r.id,r.refund,r.refund_time refundTime,r.request_time requestTime FROM ycb_mcs_refund_log r WHERE id=#{id}")
    Refund findRefundByRefundId(Long id);
}
