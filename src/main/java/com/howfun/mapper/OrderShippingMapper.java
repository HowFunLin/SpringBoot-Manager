package com.howfun.mapper;

import com.howfun.model.OrderShipping;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface OrderShippingMapper {

    int deleteByPrimaryKey(String orderId);

    int insert(OrderShipping record);

    OrderShipping selectByPrimaryKey(String orderId);

    List<OrderShipping> selectAll();

    int updateByPrimaryKey(OrderShipping record);
}