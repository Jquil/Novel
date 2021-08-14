package com.wong.novel.util;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    private static final String TAG = "DateUtil";

    private static Calendar mCalendar = Calendar.getInstance();

    public static String getDate(){
        SimpleDateFormat sdf;
        Date date;
        try {
            sdf  = new SimpleDateFormat("yyyy-MM-dd");
            date = new Date(System.currentTimeMillis());
            return sdf.format(date);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    // 判断今天是星期几
    public static boolean getWhatDay(int flag){
        /* 这里的星期主要按照西方的来：
            1 => 星期日
            2 => 星期一
            3 => 星期二
            4 => 星期三
            5 => 星期四
            6 => 星期五
            7 => 星期六
            ::所以，如果想要判断星期一，flag => 2
        */
        int day = mCalendar.get(Calendar.DAY_OF_WEEK);
        if (day == flag){
            return true;
        }
        return false;
    }
}
