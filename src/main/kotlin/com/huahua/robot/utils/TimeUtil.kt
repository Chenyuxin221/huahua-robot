package com.huahua.robot.utils

import com.huahua.robot.core.common.isNull
import com.huahua.robot.core.common.logger
import org.springframework.boot.logging.LogLevel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.*

/**
 * ClassName: TimeUtil
 * @description
 * @author 花云端
 * @date 2022-06-11 18:57
 */
object TimeUtil {

    /**
     * 格式化时间戳
     * @param timeStamp Long 时间戳
     * @param timeUnit TimeUnit 时间戳单位
     * @return String   结果
     * 目前支持的时间戳单位：MILLISECONDS，SECONDS
     */
    fun getStringTime(timeStamp: Long, timeUnit: TimeUnit = MILLISECONDS): String {
        val date = when (timeUnit) {
            MILLISECONDS -> Date(timeStamp)
            SECONDS -> Date(timeStamp * 1000)
            else -> null
        }
        date.isNull {
            logger(LogLevel.ERROR) {
                "不支持的时间格式，仅支持MILLISECONDS和SECONDS"
            }
            return ""
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(date)
    }

    fun getNowTime(): String {
        val date = Date(System.currentTimeMillis())
        return SimpleDateFormat("HH:mm:ss").format(date)
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

    /**
     * 返回时间单位文本描述
     * @receiver TimeUnit   时间单位工具类
     * @return String   文本
     * @see TimeUnit
     */
    fun TimeUnit.name() = when (this) {
        DAYS -> "天"
        HOURS -> "小时"
        MINUTES -> "分钟"
        SECONDS -> "秒"
        MILLISECONDS -> "毫秒"
        MICROSECONDS -> "微秒"
        NANOSECONDS -> "纳秒"
    }
}