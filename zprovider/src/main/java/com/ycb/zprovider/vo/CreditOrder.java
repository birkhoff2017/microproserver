package com.ycb.zprovider.vo;

/**
 * 信用借还订单的实体类
 */
public class CreditOrder {

    /**
     * 是否准入:
     * Y:准入
     * N:不准入
     */
    private String admitState;

    /**
     * 资金流水号
     */
    private String alipayFundOrderNo;

    /**
     * 借用时间
     */
    private String borrowTime;

    /**
     * 物品名称,最长不能超过14个汉字
     */
    private String goodsName;

    /**
     * 芝麻信用借还订单号
     */
    private String orderNo;

    /**
     * 支付金额
     */
    private String payAmount;

    /**
     * 支付金额类型
     * RENT:租金
     * DAMAGE:赔偿金
     */
    private String payAmountType;

    /**
     * 支付状态
     * PAY_INIT:待支付
     * PAY_SUCCESS:支付成功
     * PAY_FAILED:支付失败
     * PAY_INPROGRESS:支付中
     */
    private String payStatus;

    /**
     * 支付时间
     */
    private String payTime;

    /**
     * 归还时间
     */
    private String restoreTime;

    /**
     * 订单状态:
     * borrow:借出
     * restore:归还
     * cancel:撤销
     */
    private String useState;

    /**
     * 支付宝userId
     */
    private String userId;

    public String getAdmitState() {
        return admitState;
    }

    public void setAdmitState(String admitState) {
        this.admitState = admitState;
    }

    public String getAlipayFundOrderNo() {
        return alipayFundOrderNo;
    }

    public void setAlipayFundOrderNo(String alipayFundOrderNo) {
        this.alipayFundOrderNo = alipayFundOrderNo;
    }

    public String getBorrowTime() {
        return borrowTime;
    }

    public void setBorrowTime(String borrowTime) {
        this.borrowTime = borrowTime;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(String payAmount) {
        this.payAmount = payAmount;
    }

    public String getPayAmountType() {
        return payAmountType;
    }

    public void setPayAmountType(String payAmountType) {
        this.payAmountType = payAmountType;
    }

    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getRestoreTime() {
        return restoreTime;
    }

    public void setRestoreTime(String restoreTime) {
        this.restoreTime = restoreTime;
    }

    public String getUseState() {
        return useState;
    }

    public void setUseState(String useState) {
        this.useState = useState;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}