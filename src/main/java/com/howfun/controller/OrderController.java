package com.howfun.controller;


import com.howfun.mapper.OrderItemMapper;
import com.howfun.mapper.OrderMapper;
import com.howfun.model.Order;
import com.howfun.model.OrderItem;
import com.howfun.util.DateUtil;
import com.howfun.util.ExcelUtil;
import com.howfun.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 订单管理
 */
@Controller
public class OrderController {

    List<Order> orderList;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    // 展示主页面和接收搜索请求
    @RequestMapping("/user/orderManage_{pageCurrent}_{pageSize}_{pageCount}")
    public String orderManage(Order order,
                              @PathVariable Integer pageCurrent,
                              @PathVariable Integer pageSize,
                              @PathVariable Integer pageCount,
                              Model model) {
        if (pageSize == 0)
            pageSize = 50;
        if (pageCurrent == 0)
            pageCurrent = 1;

        order.setMinOrderTime(DateUtil.strToDate(order.getMinOrderTimeStr()));
        order.setMaxOrderTime(DateUtil.strToDate(order.getMaxOrderTimeStr()));

        int rows = orderMapper.list(order).size();

        if (pageCount == 0)
            pageCount = rows % pageSize == 0 ? (rows / pageSize) : (rows / pageSize) + 1;

        order.setStart((pageCurrent - 1) * pageSize);
        order.setEnd(pageSize);

        orderList = orderMapper.list(order);

        for (Order order1 : orderList) {
            String orderId = order1.getOrderId();

            if (orderItemMapper.selectByPrimaryOrderKey(orderId) != null) {
                // 根据订单ID获取对应订单的商品信息
                OrderItem orderItem = orderItemMapper.selectByPrimaryOrderKey(orderId);

                order1.setItemTitle(orderItem.getTitle());
                order1.setTotalFee(orderItem.getTotalFee());
                order1.setNum(orderItem.getNum());

                // 设置订单实体的其他信息
                order1.setStatusStr(getStatusStrById(order1.getStatus()));
                order1.setDateStr1(DateUtil.getDateStr(order1.getCreateTime()));
                order1.setPaymentTypeStr(getPaymentTypeById(order1.getPaymentType()));
            }
        }

        model.addAttribute("orderList", orderList);

        String pageHTML = PageUtil.getPageContent("itemManage_{pageCurrent}_{pageSize}_{pageCount}?title=" + order.getItemTitle() + "&orderId=" + order.getOrderId() + "&minOrderTimeStr=" + order.getMinOrderTimeStr() + "&maxOrderTimeStr=" + order.getMaxOrderTimeStr() + "&status=" + order.getStatus(), pageCurrent, pageSize, pageCount);

        model.addAttribute("pageHTML", pageHTML);
        model.addAttribute("order", order);

        return "order/orderManage";
    }

    //导出Excel
    @RequestMapping("/user/download2")
    public void downloadOrder(HttpServletResponse response) throws IOException {
        LinkedHashMap<String, String> fieldMap = new LinkedHashMap<>();
        fieldMap.put("orderId", "订单id");
        fieldMap.put("payment", "实付金额");
        fieldMap.put("paymentType", "支付类型，1、在线支付，2、货到付款");
        fieldMap.put("postFee", "邮费");
        fieldMap.put("status", "状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭");
        fieldMap.put("createTime", "订单创建时间");
        fieldMap.put("updateTime", "订单更新时间");
        fieldMap.put("paymentTime", "付款时间");
        fieldMap.put("consignTime", "发货时间");
        fieldMap.put("endTime", "交易完成时间");
        fieldMap.put("closeTime", "交易关闭时间");
        fieldMap.put("shippingName", "物流名称");
        fieldMap.put("shippingCode", "物流单号");
        fieldMap.put("userId", "用户id");
        fieldMap.put("buyerMessage", "买家留言");
        fieldMap.put("buyerNick", "买家昵称");
        fieldMap.put("buyerRate", "买家是否已经评价");

        String sheetName = "订单报表";

        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition", "attachment;filename=OrderManage.xls");//默认Excel名称
        response.flushBuffer();

        OutputStream fos = response.getOutputStream();

        try {
            ExcelUtil.listToExcel(orderList, fieldMap, sheetName, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 查看详情
    @GetMapping("/user/orderDetails")
    public String orderDetailsGet(Model model, Order order) {
        String orderId = order.getOrderId();
        Order order1 = orderMapper.selectByPrimaryKey(orderId);

        if (orderItemMapper.selectByPrimaryOrderKey(orderId) != null) {
            OrderItem orderItem = orderItemMapper.selectByPrimaryOrderKey(orderId);

            order1.setItemTitle(orderItem.getTitle());
            order1.setTotalFee(orderItem.getTotalFee());
            order1.setNum(orderItem.getNum());
            order1.setStatusStr(getStatusStrById(order1.getStatus()));
            order1.setItemId(orderItem.getItemId());

            order1.setBuyerRateStr(getBuyerRateStrById(order1.getBuyerRate()));
            order1.setDateStr1(DateUtil.getDateStr(order1.getCreateTime()));
            order1.setDateStr2(DateUtil.getDateStr(order1.getUpdateTime()));
            order1.setDateStr3(DateUtil.getDateStr(order1.getPaymentTime()));
            order1.setDateStr4(DateUtil.getDateStr(order1.getConsignTime()));
            order1.setDateStr5(DateUtil.getDateStr(order1.getEndTime()));
            order1.setPaymentTypeStr(getPaymentTypeById(order1.getPaymentType()));

            model.addAttribute("order", order1);
        }
        return "order/orderDetails";
    }

    // 退款管理和订单搜索
    @RequestMapping("/user/orderRefund_{pageCurrent}_{pageSize}_{pageCount}")
    public String RefundManage(Order order,
                               @PathVariable Integer pageCurrent,
                               @PathVariable Integer pageSize,
                               @PathVariable Integer pageCount,
                               Model model) {
        if (pageSize == 0)
            pageSize = 50;
        if (pageCurrent == 0)
            pageCurrent = 1;

        int rows = orderMapper.list(order).size();
        if (pageCount == 0)
            pageCount = rows % pageSize == 0 ? (rows / pageSize) : (rows / pageSize) + 1;

        order.setStart((pageCurrent - 1) * pageSize);
        order.setEnd(pageSize);

        List<Order> orderList = orderMapper.listRefund(order);

        for (Order order1 : orderList) {
            String orderId = order1.getOrderId();
            if (orderItemMapper.selectByPrimaryOrderKey(orderId) != null) {
                OrderItem orderItem = orderItemMapper.selectByPrimaryOrderKey(orderId);

                order1.setItemTitle(orderItem.getTitle());
                order1.setTotalFee(orderItem.getTotalFee());
                order1.setNum(orderItem.getNum());

                order1.setStatusStr(getStatusStrById(order1.getStatus()));
                order1.setDateStr1(DateUtil.getDateStr(order1.getCreateTime()));
                order1.setPaymentTypeStr(getPaymentTypeById(order1.getPaymentType()));
                order1.setRefundStatusStr(getRefundStatusStr(order1.getRefundStatus()));
            }
        }

        model.addAttribute("orderList", orderList);

        String pageHTML = PageUtil.getPageContent("orderRefund_{pageCurrent}_{pageSize}_{pageCount}?refundStatus=" + order.getRefundStatus(), pageCurrent, pageSize, pageCount);

        model.addAttribute("pageHTML", pageHTML);
        model.addAttribute("order", order);

        return "order/orderRefund";
    }

    // 审核
    @GetMapping("/user/orderCheck")
    public String orderCheckGet(Model model, Order order) {
        String orderId = order.getOrderId();
        Order order1 = orderMapper.selectByPrimaryKey(orderId);

        if (orderItemMapper.selectByPrimaryOrderKey(orderId) != null) {
            OrderItem orderItem = orderItemMapper.selectByPrimaryOrderKey(orderId);

            order1.setItemTitle(orderItem.getTitle());
            order1.setTotalFee(orderItem.getTotalFee());
            order1.setNum(orderItem.getNum());
            order1.setStatusStr(getStatusStrById(order1.getStatus()));
            order1.setItemId(orderItem.getItemId());

            order1.setBuyerRateStr(getBuyerRateStrById(order1.getBuyerRate()));
            order1.setDateStr1(DateUtil.getDateStr(order1.getCreateTime()));
            order1.setDateStr3(DateUtil.getDateStr(order1.getPaymentTime()));
            order1.setPaymentTypeStr(getPaymentTypeById(order1.getPaymentType()));
            order1.setRefundStatus(null);

            model.addAttribute("order", order1);
        }
        return "order/orderCheck";
    }

    @PostMapping("/user/orderCheck")
    public String orderCheckPost(Order order) {
        // 无指定默认为0
        if (order.getRefundStatus() == null) {
            order.setRefundStatus(0);
        }

        // 更新退货情况
        if (order.getOrderId() != null) {
            orderMapper.updateByPrimaryKey(order);
        }

        return "redirect:orderRefund_0_0_0";
    }

    /**
     * 获取退货情况
     *
     * @param i 对应状态的整数表示
     * @return 退款状态
     */
    private String getRefundStatusStr(int i) {
        switch (i) {
            case 1:
                return "申请退款";
            case 2:
                return "退款失败";
            case 3:
                return "退款成功";
        }

        return null;
    }

    /**
     * 获取评价情况
     *
     * @param i 对应状态的整数表示
     * @return 是否已评价
     */
    private String getBuyerRateStrById(int i) {
        switch (i) {
            case 0:
                return "否";
            case 1:
                return "是";
        }

        return null;
    }

    /**
     * 获取订单情况
     *
     * @param i 对应状态的整数表示
     * @return 退款状态
     */
    private String getStatusStrById(int i) {
        switch (i) {
            case 1:
                return "未付款";
            case 2:
                return "已付款";
            case 3:
                return "未发货";
            case 4:
                return "已发货";
            case 5:
                return "交易成功";
            case 6:
                return "交易关闭";
        }

        return null;
    }

    /**
     * 获取支付情况
     *
     * @param i 对应状态的整数表示
     * @return 支付情况
     */
    private String getPaymentTypeById(int i) {
        switch (i) {
            case 1:
                return "在线支付";
            case 2:
                return "货到付款";
        }

        return null;
    }
}
