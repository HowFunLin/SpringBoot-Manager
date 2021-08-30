package com.jesper.service.prefix.impl;

import com.jesper.service.prefix.KeyPrefix;

public abstract class BasePrefix implements KeyPrefix {

    private int expireSeconds;

    public BasePrefix(){
        this(0);//默认0代表永不过期
    }

    public BasePrefix(int expireSeconds){
        this.expireSeconds = expireSeconds;
    }

    @Override
    public int expireSeconds() {
        return expireSeconds;
    }

    @Override
    public String getSimpleClassname() {
        String className = getClass().getSimpleName();//拿到参数类类名
        return className + ":";
    }
}
