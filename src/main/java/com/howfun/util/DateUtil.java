package com.howfun.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期和字符串转换的工具类
 */
public class DateUtil {
    /**
     * Date --> String
     *
     * @param date 日期
     * @return yyyy-MM-dd
     */
    public static String getDateStr(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(date);
        return dateStr;
    }

    /**
     * String --> Date
     *
     * @param date 日期
     * @return yyyy-MM-dd
     */
    public static Date strToDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date result = null;
        try {
            result = sdf.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
