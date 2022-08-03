package com.huahua.robot.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * ClassName: TimeUtil
 * @description
 * @author 花云端
 * @date 2022-06-11 18:57
 */
object TimeUtil {

    /**
     * @description 时间戳格式化
     * @receiver Long   时间戳
     * @return String   格式化时间
     */
    fun getStringTime(timeStamp: Long): String {
        val date = Date(timeStamp)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(date)
    }

    fun millisecondFormat(ms: Long): String {
        val ss = 1000
        val mi = ss * 60
        val hh = mi * 60
        val dd = hh * 24

        val day = ms / dd
        val hour = (ms - day * dd) / hh
        val minute = (ms - day * dd - hour * hh) / mi
        val second = (ms - day * dd - hour * hh - minute * mi) / ss

        val sb = StringBuilder()
        if (day > 0) {
            sb.append("${day}天")
        }
        if (hour > 0) {
            sb.append("${hour}小时")
        }
        if (minute > 0) {
            sb.append("${minute}分钟")
        }
        if (second > 0) {
            sb.append("${second}秒")
        }
        return sb.toString()
    }
}