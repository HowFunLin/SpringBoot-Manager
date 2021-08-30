package com.jesper.service.prefix.impl;

public class PieChartKey extends BasePrefix {
    private static final int CHART_EXPIRE = 60 * 60 * 24;//默认1天

    public static PieChartKey chart = new PieChartKey(CHART_EXPIRE);

    private PieChartKey(int expireSeconds) {
        super(expireSeconds);
    }
}
