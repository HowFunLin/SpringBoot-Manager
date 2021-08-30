package com.howfun.controller;

import com.howfun.mapper.ImageMapper;
import com.howfun.mapper.ItemCategoryMapper;
import com.howfun.mapper.ItemMapper;
import com.howfun.mapper.ReItemMapper;
import com.howfun.model.*;
import com.howfun.util.Constant;
import com.howfun.util.DateUtil;
import com.howfun.util.ExcelUtil;
import com.howfun.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;


/**
 * 商品管理
 */
@Controller
public class ItemController {

    public static final String ROOT = "src/main/resources/static/img/item/";
    private final ResourceLoader resourceLoader;

    /**
     * 记录商品列表
     */
    private List<Item> itemList;

    /**
     * 缓存图片文件
     */
    private File getFile = null;
    /**
     * 缓存图片名
     */
    private String imageName = null;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private ItemCategoryMapper itemCategoryMapper;
    @Autowired
    private ReItemMapper reItemMapper;
    @Autowired
    private ImageMapper imageMapper;

    @Autowired
    public ItemController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    // 类似商品分类页面的实现形式，默认的展示和搜索
    @RequestMapping("/user/itemManage_{pageCurrent}_{pageSize}_{pageCount}")
    public String itemManage(Item item,
                             @PathVariable Integer pageCurrent,
                             @PathVariable Integer pageSize,
                             @PathVariable Integer pageCount,
                             Model model) {
        if (pageSize == 0)
            pageSize = 50;
        if (pageCurrent == 0)
            pageCurrent = 1;

        int rows = itemMapper.count(item);

        if (pageCount == 0)
            pageCount = rows % pageSize == 0 ? (rows / pageSize) : (rows / pageSize) + 1;

        item.setStart((pageCurrent - 1) * pageSize);
        item.setEnd(pageSize);

        // 根据Item的属性查询页面展示的内容
        itemList = itemMapper.list(item);

        for (Item i : itemList)
            i.setUpdatedStr(DateUtil.getDateStr(i.getUpdated()));

        ItemCategory itemCategory = new ItemCategory();

        // 查询所有商品类型
        itemCategory.setStart(0);
        itemCategory.setEnd(Integer.MAX_VALUE);

        List<ItemCategory> itemCategoryList = itemCategoryMapper.list(itemCategory);

        Integer minPrice = item.getMinPrice();
        Integer maxPrice = item.getMaxPrice();

        model.addAttribute("itemCategoryList", itemCategoryList);
        model.addAttribute("itemList", itemList);

        String pageHTML = PageUtil.getPageContent("itemManage_{pageCurrent}_{pageSize}_{pageCount}?title=" + item.getTitle() + "&cid=" + item.getCid() + "&minPrice=" + minPrice + "&maxPrice=" + maxPrice, pageCurrent, pageSize, pageCount);

        model.addAttribute("pageHTML", pageHTML);
        model.addAttribute("item", item);

        return "item/itemManage";
    }

    // 导出Excel
    @RequestMapping("/user/download1")
    public void postItemExcel(HttpServletResponse response) throws IOException {
        //导出Excel
        LinkedHashMap<String, String> fieldMap = new LinkedHashMap<>();
        fieldMap.put("id", "商品id");
        fieldMap.put("title", "商品标题");
        fieldMap.put("sellPoint", "商品卖点");
        fieldMap.put("price", "商品价格");
        fieldMap.put("num", "库存数量");
        fieldMap.put("image", "商品图片");
        fieldMap.put("cid", "所属类目，叶子类目");
        fieldMap.put("status", "商品状态，1-正常，2-下架，3-删除");
        fieldMap.put("created", "创建时间");
        fieldMap.put("updated", "更新时间");

        String sheetName = "商品管理报表";

        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition", "attachment;filename=ItemManage.xls"); // 默认Excel名称
        response.flushBuffer();

        OutputStream fos = response.getOutputStream();

        try {
            ExcelUtil.listToExcel(itemList, fieldMap, sheetName, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 进入修改商品或新增商品页
    @GetMapping("/user/itemEdit")
    public String itemEditGet(Model model, Item item) {
        ItemCategory itemCategory = new ItemCategory();

        itemCategory.setStart(0);
        itemCategory.setEnd(Integer.MAX_VALUE);

        List<ItemCategory> itemCategoryList = itemCategoryMapper.list(itemCategory);

        model.addAttribute("itemCategoryList", itemCategoryList);

        if (item.getId() != 0) {
            Item item1 = itemMapper.findById(item);
            int id = item.getId();
            Image image = imageMapper.selectById(id);

            if (image != null) {
                StringBuilder sb = new StringBuilder(ROOT);
                imageName = image.getName();
                sb.append(imageName);

                try {
                    getFile = new File(sb.toString());
                    FileOutputStream outputStream = new FileOutputStream(getFile);
                    byte[] data = image.getImage();
                    InputStream in = new ByteArrayInputStream(data);

                    int len;
                    byte[] buf = new byte[1024];
                    while ((len = in.read(buf, 0, 1024)) != -1) {
                        outputStream.write(buf, 0, len);
                    }

                    in.close();
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                item1.setImage(imageName);
            }

            // 传递item的image属性，让页面获取对应图片
            model.addAttribute("item", item1);
        }

        return "item/itemEdit";
    }

    // 将修改或新增的商品入库，返回首页
    @PostMapping("/user/itemEdit")
    public String itemEditPost(@RequestParam("file") MultipartFile file, Item item) {
        Date date = new Date();

        item.setCreated(date);
        item.setUpdated(date);
        item.setBarcode("");
        item.setImage("");

        int randomNum = 0;
        if (file.isEmpty()) {
            System.out.println("图片未上传");
        } else {
            try {
                String filename = file.getOriginalFilename();
                Path path = Paths.get(ROOT, filename);
                File tempFile = new File(path.toString());
                if (!tempFile.exists()) {
                    Files.copy(file.getInputStream(), path);
                }

                int id;
                if (item.getId() != 0) {
                    id = item.getId();
                } else {
                    Random random = new Random();
                    randomNum = (int) (random.nextDouble() * (99999 - 10000 + 1)) + 1000;// 获取5位随机数
                    id = randomNum;
                }

                InputStream is = file.getInputStream();

                Image image = new Image();
                image.setId(id);
                image.setName(filename);

                byte[] pic = new byte[(int) file.getSize()];
                is.read(pic);
                image.setImage(pic);

                imageMapper.insert(image);

                tempFile.delete();
                getFile.delete();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            System.out.println("get File by Id Success");
        }

        if (item.getId() != 0) {
            itemMapper.update(item);
        } else {
            item.setId(randomNum);
            itemMapper.insert(item);
        }

        return "redirect:itemManage_0_0_0";
    }

    // 访问图片接口
    @GetMapping(value = "/{filename:.+}")
    @ResponseBody
    public ResponseEntity<?> getFile() {
        try {
            return ResponseEntity.ok(resourceLoader.getResource("file:" + Paths.get(ROOT, imageName).toString()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 删除商品
    @ResponseBody
    @PostMapping("/user/itemEditState")
    public ResObject<Object> itemEditState(Item item1) {
        Item item = itemMapper.findById(item1);

        // 删除的商品加入回收站
        ReItem reItem = new ReItem();
        reItem.setId(item.getId());
        reItem.setBarcode(item.getBarcode());
        reItem.setCid(item.getCid());
        reItem.setImage(item.getImage());
        reItem.setPrice(item.getPrice());
        reItem.setNum(item.getNum());
        reItem.setSellPoint(item.getSellPoint());
        reItem.setStatus(item.getStatus());
        reItem.setTitle(item.getTitle());
        reItem.setRecovered(new Date());
        reItemMapper.insert(reItem);

        itemMapper.delete(item1);
        ResObject<Object> object = new ResObject<>(Constant.Code01, Constant.Msg01, null, null);

        return object;
    }
}
