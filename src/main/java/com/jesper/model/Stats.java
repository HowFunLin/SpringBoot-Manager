package com.jesper.model;

import lombok.Data;

/**
 * 统计类
 */
@Data
public class Stats {

    /**
     * 月收入
     */
    private long mIncome;
    /**
     * 月收入环比
     */
    private String incomePer;

    /**
     * 月订单数
     */

    private int mOrderNum;
    /**
     * 月订单数环比
     */
    private String orderNumPer;

    /**
     * 月退单
     */
    private int mOrderRefund;
    /**
     * 月退单
     */
    private String mOrderRefundPer;

    /**
     * 访问量
     */
    private int pv;
}
