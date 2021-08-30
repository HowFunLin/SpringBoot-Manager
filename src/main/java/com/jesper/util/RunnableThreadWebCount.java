package com.jesper.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 网站访问人数计数器
 */
public class RunnableThreadWebCount{
    private static AtomicInteger visit = new AtomicInteger(0);

    public  static int addCount(){
        Integer count = visit.incrementAndGet();

        System.out.println("网站访问人数：" + count);
        return count;
    }
}
