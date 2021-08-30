package com.howfun.controller;

import com.howfun.mapper.ItemCategoryMapper;
import com.howfun.model.ItemCategory;
import com.howfun.model.ResObject;
import com.howfun.util.Constant;
import com.howfun.util.DateUtil;
import com.howfun.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 商品管理：商品分类
 */
@Controller
public class ItemCategoryController {

    @Autowired
    private ItemCategoryMapper itemCategoryMapper;

    // 展示所有分类和搜索
    @RequestMapping("/user/itemCategoryManage_{pageCurrent}_{pageSize}_{pageCount}")
    public String itemCategoryManage(ItemCategory itemCategory,
                                     @PathVariable Integer pageCurrent, // 当前页
                                     @PathVariable Integer pageSize, // 页面大小
                                     @PathVariable Integer pageCount, // 页数
                                     Model model) {
        // 默认页大小 20
        if (pageSize == 0)
            pageSize = 20;

        // 默认页 首页
        if (pageCurrent == 0)
            pageCurrent = 1;

        int rows = itemCategoryMapper.count(itemCategory);

        // 计算得出页数
        if (pageCount == 0)
            pageCount = rows % pageSize == 0 ? (rows / pageSize) : (rows / pageSize) + 1;

        // 页面查询结果的起始和结束
        itemCategory.setStart((pageCurrent - 1) * pageSize);
        itemCategory.setEnd(pageSize);

        List<ItemCategory> list = itemCategoryMapper.list(itemCategory);

        // 设置字符串形式的添加时间和更新时间
        for (ItemCategory i : list) {
            i.setCreatedStr(DateUtil.getDateStr(i.getCreated()));
            i.setUpdatedStr(DateUtil.getDateStr(i.getUpdated()));
        }

        model.addAttribute("list", list);

        // 拼接右下方的分页栏
        String pageHTML = PageUtil.getPageContent("itemCategoryManage_{pageCurrent}_{pageSize}_{pageCount}?name=" + itemCategory.getName(), pageCurrent, pageSize, pageCount);

        model.addAttribute("pageHTML", pageHTML);
        model.addAttribute("itemCategory", itemCategory);

        return "item/itemCategoryManage";
    }

    // 跳转页面，修改分类列表中的项
    @GetMapping("/user/itemCategoryEdit")
    public String itemCategoryEditGet(Model model, ItemCategory itemCategory) {
        if (itemCategory.getId() != 0)
            model.addAttribute("itemCategory", itemCategoryMapper.findById(itemCategory));

        return "item/itemCategoryEdit";
    }

    // 跳转页面，新增分类
    @PostMapping("/user/itemCategoryEdit")
    public String newsCategoryEditPost(ItemCategory itemCategory) {
        Date date = new Date();
        Random random = new Random();

        // 获取3位随机数
        int id = (int) (random.nextDouble() * (999 - 100 + 1)) + 10;

        itemCategory.setCreated(date);
        itemCategory.setUpdated(date);

        List<ItemCategory> list = itemCategoryMapper.list1();
        String name = itemCategory.getName();

        // 如果存在相同名称的分类，直接返回展示页
        for (ItemCategory i : list)
            if (i.getName().equals(name))
                return "redirect:itemCategoryManage_0_0_0";

        // 如果ID不为0，则为修改分类内容；否则才是新增分类
        if (itemCategory.getId() != 0) {
            itemCategoryMapper.update(itemCategory);
        } else {
            itemCategory.setId(id);
            itemCategoryMapper.insert(itemCategory);
        }

        return "redirect:itemCategoryManage_0_0_0";
    }

    // 删除分类，接收AJAX请求并返回
    @ResponseBody
    @PostMapping("/user/itemCategoryEditState")
    public ResObject<Object> itemCategoryEditState(ItemCategory itemCategory) {
        itemCategoryMapper.delete(itemCategory);
        ResObject<Object> object = new ResObject<>(Constant.Code01, Constant.Msg01, null, null);

        return object;
    }
}
