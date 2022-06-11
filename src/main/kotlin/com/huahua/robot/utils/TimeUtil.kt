package com.huahua.robot.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * ClassName: TimeUtil
 * @description
 * @author 花云端
 * @date 2022-06-11 18:57
 */
class TimeUtil {

}

/**
 * @description 获取当前时间
 * @receiver Long
 * @return String
 */
fun Long.toStringTime():String{
    val date = Date(this)
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    return sdf.format(date)
}