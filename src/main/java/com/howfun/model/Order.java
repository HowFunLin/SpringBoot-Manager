package com.howfun.model;

import lombok.Data;

import java.util.Date;

/**
 * 订单实体
 */
@Data
public class Order extends BaseObject {
    private String orderId;
    private String itemId;
    private String payment;
    private String refundStatusStr;
    private Integer paymentType;
    private String postFee;
    private String refundReason;
    private Integer refundStatus;
    private int isRefund;
    private Integer status;
    private String itemTitle;
    private Long totalFee;
    private int num;
    private String statusStr;
    private Date createTime;
    private Date updateTime;
    private Date paymentTime;
    private Date consignTime;
    private Date endTime;
    private Date closeTime;
    private String shippingName;
    private String shippingCode;
    private String minOrderTimeStr;
    private String maxOrderTimeStr;
    private Date minOrderTime;
    private Date maxOrderTime;
    private Long userId;
    /**
     * 支付方式
     */
    private String paymentTypeStr;
    private String buyerMessage;
    private String buyerNick;
    private String buyerRateStr;
    /**
     * 创建时间
     */
    private String dateStr1;
    /**
     * 更新时间
     */
    private String dateStr2;
    /**
     * 支付时间
     */
    private String dateStr3;
    /**
     * 确认时间
     */
    private String dateStr4;
    /**
     * 完成 / 结束时间
     */
    private String dateStr5;
    private Integer buyerRate;
}