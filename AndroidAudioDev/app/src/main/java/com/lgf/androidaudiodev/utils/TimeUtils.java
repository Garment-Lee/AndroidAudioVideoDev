package com.lgf.androidaudiodev.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.SimpleFormatter;

/**
 * Created by garment on 2018/10/26.
 */

public class TimeUtils {

    private SimpleFormatter mSimpleFormatter = new SimpleFormatter();

    /**
     * 获取当前时间字符串（年月日）,返回的字符串如下：<br>
     *     20181026
     *
     * @return
     */
    public static String getCurrentTimeStr(){
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(calendar.get(Calendar.YEAR))
                .append(month < 10 ? "0" + month : month + "")
                .append(calendar.get(Calendar.DATE));
        return stringBuilder.toString();
    }

    /**
     * 时间戳转换为格式化的时间
     * long 	时间戳，单位是ms(毫秒)
     * format 	时间格式 (ex：yyyy/MM/dd hh:mm:ss)
     */
    public static String getFormatTimeStr(long time, String format) {
        String convertTime = "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date date = new Date(time);
        convertTime = simpleDateFormat.format(date);
        return convertTime;
    }
}
