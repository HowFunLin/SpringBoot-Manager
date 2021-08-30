package com.howfun.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.howfun.mapper.DeliveryMapper;
import com.howfun.model.Delivery;
import com.howfun.model.Express;
import com.howfun.util.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * 物流管理
 */
@Controller
public class DeliveryController {

    @Autowired
    private DeliveryMapper deliveryMapper;

    // 记录进入查询页面时的物流公司的ID
    private int id;

    // 显示所有物流公司
    @RequestMapping("user/deliveryManage")
    public String deliveryManage(Model model) {
        List<Delivery> list = deliveryMapper.selectAll();
        model.addAttribute("list", list);

        return "others/deliveryManage";
    }

    // 根据选择的物流公司的ID进入查询页面
    @GetMapping("user/search")
    public String searchGet(Model model, Delivery delivery) {
        id = delivery.getId();
        model.addAttribute("delivery", deliveryMapper.selectByPrimaryKey(id));

        return "others/search";
    }

    // 根据物流公司ID和真实物流单号查询物流信息
    @PostMapping("user/search")
    public String searchPost(Model model, @RequestParam("expressNo") String expressNo) {
        Delivery delivery = deliveryMapper.selectByPrimaryKey(id);

        String code = delivery.getDeliveryCode();

        JSONArray result = getExpress100(code, expressNo);

        List<Express> list = new ArrayList<>();
        for (int j = 0; j < result.size(); j++) {
            JSONObject object = result.getJSONObject(j);
            Express e = new Express();
            e.setId(j + 1);
            e.setContext(object.getString("context"));
            e.setLocation(object.getString("location"));
            e.setTime(object.getString("time"));
            list.add(e);
        }

        model.addAttribute("list", list);
        model.addAttribute("delivery", delivery);

        return "others/search";
    }

    // 查询真实物流信息
    public JSONArray getExpress100(String deliveryCode, String expressNo) {
        StringBuilder url = new StringBuilder("https://m.kuaidi100.com/query?");
        url.append("type=").append(deliveryCode).append("&").append("postid=").append(expressNo);

        String content = HttpRequest.readData(url.toString(), "POST");
        JSONObject responseJson = JSONObject.parseObject(content);
        JSONArray result = responseJson.getJSONArray("data");

        return result;
    }

    // 进入新增物流公司页面
    @GetMapping("user/deliveryEdit")
    public String deliveryEditGet() {
        return "others/deliveryEdit";
    }

    // 新增物流公司 -- 信息提交
    @PostMapping("user/deliveryEdit")
    public String deliveryEditPost(Delivery delivery) {
        deliveryMapper.insert(delivery);

        return "redirect:deliveryManage";
    }

    //根据物流公司中文名去查询其公司编号
    @RequestMapping("user/deliveryDeleteState")
    public String deliveryDeleteStatePost(Delivery delivery) {
        deliveryMapper.deleteByDeliveryName(delivery.getDeliveryName());
        return "redirect:deliveryManage";
    }
}
