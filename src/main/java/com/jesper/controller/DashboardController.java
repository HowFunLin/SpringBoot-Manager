package com.jesper.controller;


import com.jesper.mapper.OrderMapper;
import com.jesper.model.Order;
import com.jesper.model.Stats;
import com.jesper.service.RedisService;
import com.jesper.service.prefix.impl.DashboardKey;
import com.jesper.service.prefix.impl.PieChartKey;
import com.jesper.util.RunnableThreadWebCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisService redisService;

    // 仪表盘数据显示
    @GetMapping("/user/dashboard")
    public String dashboard(Model model, Stats stats) {

        Long curIncome, preIncome;
        Integer curOrderNum, preOrderNum, curRefundOrder, lastRefundOrder, dailyOrderNum, dailyOrderIncome;

        //全部加缓存

        //当前月收入
        curIncome = redisService.get(DashboardKey.board, "curIncome", Long.class);
        if (curIncome == null) {
            curIncome = orderMapper.selectCurPayment();
            curIncome = curIncome == null ? 0L : curIncome;
            redisService.set(DashboardKey.board, "curIncome", curIncome);
        }

        // 上个月收入
        preIncome = redisService.get(DashboardKey.board, "preIncome", Long.class);
        if (preIncome == null) {
            preIncome = orderMapper.selectLastPayment();
            preIncome = preIncome == null ? 0L : preIncome;
            redisService.set(DashboardKey.board, "preIncome", preIncome);
        }

        // 当前月订单
        curOrderNum = redisService.get(DashboardKey.board, "curOrderNum", Integer.class);
        if (curOrderNum == null) {
            curOrderNum = orderMapper.selectCurOrderNum();
            curOrderNum = curOrderNum == null ? 0 : curOrderNum;
            redisService.set(DashboardKey.board, "curOrderNum", curOrderNum);
        }

        // 上个月订单
        preOrderNum = redisService.get(DashboardKey.board, "preOrderNum", Integer.class);
        if (preOrderNum == null) {
            preOrderNum = orderMapper.selectLastOrderNum();
            preOrderNum = preOrderNum == null ? 0 : preOrderNum;
            redisService.set(DashboardKey.board, "preOrderNum", preOrderNum);
        }

        // 当前月退单
        curRefundOrder = redisService.get(DashboardKey.board, "preOrderNum", Integer.class);
        if (curRefundOrder == null) {
            curRefundOrder = orderMapper.selectCurRefundOrder();
            curRefundOrder = curRefundOrder == null ? 0 : curRefundOrder;
            redisService.set(DashboardKey.board, "curRefundOrder", curRefundOrder);
        }

        // 当前月退单
        lastRefundOrder = redisService.get(DashboardKey.board, "lastRefundOrder", Integer.class);
        if (lastRefundOrder == null) {
            lastRefundOrder = orderMapper.selectLastRefundOrder();
            lastRefundOrder = lastRefundOrder == null ? 0 : lastRefundOrder;
            redisService.set(DashboardKey.board, "lastRefundOrder", lastRefundOrder);
        }

        int count = RunnableThreadWebCount.addCount();

        stats.setMIncome(curIncome);//月收入
        stats.setMOrderNum(curOrderNum);//月订单数
        stats.setPv(count);//访问量
        stats.setMOrderRefund(curRefundOrder);
        stats.setOrderNumPer(getPer(curOrderNum, preOrderNum));//月订单数环比
        stats.setIncomePer(getPer(curIncome, preIncome));//月收入环比
        stats.setMOrderRefundPer(getPer(curRefundOrder, lastRefundOrder));//月退单数环比

        model.addAttribute("dashboard", stats);

        List<Integer> monthlyOrder = new ArrayList<>();
        List<Integer> monthlyIncome = new ArrayList<>();

        Date now = new Date();

        //获取一个月前的日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DATE, -31);

        Order order = new Order();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 30; i++) {
            calendar.add(Calendar.DATE, 1);
            order.setCreateTime(calendar.getTime());

            int month = calendar.get(Calendar.MONTH) + 1;
            builder.append(month < 10 ? "0" + month : month);
            int date = calendar.get(Calendar.DATE);
            builder.append(date < 10 ? "0" + date : date);
            String key = builder.toString();
            builder.delete(0, builder.length());

            //每天的订单数
            dailyOrderNum = redisService.get(PieChartKey.chart, "dailyOrderNum:" + key, Integer.class);
            if (dailyOrderNum == null) {
                dailyOrderNum = orderMapper.selectDayOrderNum(order);
                dailyOrderNum = dailyOrderNum == null ? 0 : dailyOrderNum;
                redisService.set(PieChartKey.chart, "dailyOrderNum:" + key, dailyOrderNum);
            }

            //每天的收入
            dailyOrderIncome = redisService.get(PieChartKey.chart, "dailyOrderIncome:" + key, Integer.class);
            if (dailyOrderIncome == null) {
                dailyOrderIncome = orderMapper.selectDayOrderSum(order);
                dailyOrderIncome = dailyOrderIncome == null ? 0 : dailyOrderIncome;
                redisService.set(PieChartKey.chart, "dailyOrderIncome:" + key, dailyOrderIncome);
            }

            monthlyOrder.add(dailyOrderNum);
            monthlyIncome.add(dailyOrderIncome);
        }

        model.addAttribute("data2", monthlyOrder);
        model.addAttribute("data3", monthlyIncome);

        return "dashboard";
    }

    /**
     * 计算数据相对上个月的环比
     *
     * @param a 当前月数据
     * @param b 上个月数据
     * @return 字符串形式的百分比
     */
    private String getPer(long a, long b) {
        StringBuilder orderNumPer = new StringBuilder();
        String s;

        if (b == 0d)
            s = "NaN";
        else {
            double diff = a - b;
            double d = diff / b * 100;
            s = String.format("%.2f", d);
        }

        orderNumPer.append(s).append("%");
        return orderNumPer.toString();
    }
}