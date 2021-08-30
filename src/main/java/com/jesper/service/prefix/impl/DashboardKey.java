package com.jesper.service.prefix.impl;

public class DashboardKey extends BasePrefix {
    private static final int BOARD_EXPIRE = 60;//默认1分钟

    public static DashboardKey board = new DashboardKey(BOARD_EXPIRE);

    private DashboardKey(int expireSeconds) {
        super(expireSeconds);
    }
}
