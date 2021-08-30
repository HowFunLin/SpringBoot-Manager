package com.howfun.mapper;

import com.howfun.model.Image;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImageMapper {
    /**
     * 插入图片
     *
     * @param image 图片对象
     */
    void insert(Image image);

    /**
     * 通过商品名查询图片
     * @param id 商品ID
     * @return 图片对象
     */
    Image selectById(int id);
}
