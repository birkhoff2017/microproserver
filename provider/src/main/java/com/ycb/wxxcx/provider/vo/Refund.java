package com.ycb.wxxcx.provider.vo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by 杜欣源 on 2017/8/5.
 */
public class Refund extends BaseEntity{

    //用户编号
    private Long uid;

    //订单编号
    private String orderid;

    //提现金额
    private BigDecimal refund;

    //提现状态:1为退款申请、 2为退款完成
    private Integer status;

    //发起时间
    private Long requestTime;

    //提现时间
    private Long refundTime;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public BigDecimal getRefund() {
        return refund;
    }

    public void setRefund(BigDecimal refund) {
        this.refund = refund;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Long requestTime) {
        this.requestTime = requestTime;
    }

    public Long getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(Long refundTime) {
        this.refundTime = refundTime;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }
}
