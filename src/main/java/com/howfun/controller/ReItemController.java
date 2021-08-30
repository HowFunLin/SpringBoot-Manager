package com.howfun.controller;

import com.howfun.mapper.ItemMapper;
import com.howfun.mapper.ReItemMapper;
import com.howfun.model.Item;
import com.howfun.model.ReItem;
import com.howfun.model.ResObject;
import com.howfun.util.Constant;
import com.howfun.util.DateUtil;
import com.howfun.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;

/**
 * 回收管理
 */
@Controller
public class ReItemController {

    @Autowired
    private ReItemMapper reItemMapper;

    @Autowired
    private ItemMapper itemMapper;

    // 展示回收的商品列表
    @RequestMapping("/user/recoverManage_{pageCurrent}_{pageSize}_{pageCount}")
    public String itemManage(ReItem reItem, @PathVariable Integer pageCurrent,
                             @PathVariable Integer pageSize,
                             @PathVariable Integer pageCount,
                             Model model) {
        if (pageSize == 0)
            pageSize = 50;
        if (pageCurrent == 0)
            pageCurrent = 1;

        List<ReItem> reItems = reItemMapper.selectAll();

        int rows = reItems.size();

        if (pageCount == 0)
            pageCount = rows % pageSize == 0 ? (rows / pageSize) : (rows / pageSize) + 1;

        reItem.setStart((pageCurrent - 1) * pageSize);
        reItem.setEnd(pageSize);

        // 删除的日期
        for (ReItem r : reItems)
            r.setRecoveredStr(DateUtil.getDateStr(r.getRecovered()));

        model.addAttribute("reItemList", reItems);

        String pageHTML = PageUtil.getPageContent("itemManage_{pageCurrent}_{pageSize}_{pageCount}?", pageCurrent, pageSize, pageCount);

        model.addAttribute("pageHTML", pageHTML);
        model.addAttribute("ReItem", reItem);

        return "item/recoverManage";
    }

    // 恢复
    @ResponseBody
    @PostMapping("/user/reItemEditState")
    public ResObject<Object> reItemEditState(ReItem reItem) {
        ReItem reItem1 = reItemMapper.selectByPrimaryKey(reItem.getId());

        Item item = new Item();

        item.setId(reItem1.getId());
        item.setBarcode(reItem1.getBarcode());
        item.setCid(reItem1.getCid());
        item.setImage(reItem1.getImage());
        item.setPrice(reItem1.getPrice());
        item.setNum(reItem1.getNum());
        item.setSellPoint(reItem1.getSellPoint());
        item.setStatus(reItem1.getStatus());
        item.setTitle(reItem1.getTitle());
        // 恢复订单，重置日期
        item.setCreated(new Date());
        item.setUpdated(new Date());

        itemMapper.insert(item);
        reItemMapper.deleteByPrimaryKey(reItem.getId());

        return new ResObject<>(Constant.Code01, Constant.Msg01, null, null);
    }

    // 彻底删除
    @ResponseBody
    @PostMapping("/user/deleteItemEditState")
    public ResObject<Object> deleteItemEditState(ReItem reItem) {
        reItemMapper.deleteByPrimaryKey(reItem.getId());

        return new ResObject<>(Constant.Code01, Constant.Msg01, null, null);
    }

}
