package com.howfun.service;

import com.alibaba.fastjson.JSON;
import com.howfun.service.prefix.KeyPrefix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * redis服务
 */
@Service
public class RedisService {

    @Autowired
    StringRedisTemplate template;

    /**
     * 获取Redis的值
     * @param prefix 类型前缀
     * @param key 键值
     * @param clazz 值对应Class类型
     * @param <T> Redis中存储的值类型
     * @return 查询结果
     */
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
        /**
         * 对key增加前缀，区分对应类型的数据，增加扩展性
         */
        String realKey = prefix.getSimpleClassname() + key;
        String str = template.opsForValue().get(realKey);

        return stringToBean(str, clazz);
    }

    /**
     * 存储对象
     */
    public <T> Boolean set(KeyPrefix prefix, String key, T value) {

        String str = beanToString(value);

        if (str == null || str.length() <= 0) {
            return false;
        }

        String realKey = prefix.getSimpleClassname() + key;
        int seconds = prefix.expireSeconds();//获取过期时间

        if (seconds <= 0) {
            template.opsForValue().set(realKey, str);
        } else {
            template.opsForValue().set(realKey, str, seconds, TimeUnit.SECONDS);
        }

        return true;
    }

    private <T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz == Integer.class) {
            return String.valueOf(value);
        } else if (clazz == long.class || clazz == Long.class) {
            return String.valueOf(value);
        } else if (clazz == String.class) {
            return (String) value;
        } else {
            return JSON.toJSONString(value);
        }

    }

    private <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(str);
        } else if (clazz == String.class) {
            return (T) str;
        } else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }
}
