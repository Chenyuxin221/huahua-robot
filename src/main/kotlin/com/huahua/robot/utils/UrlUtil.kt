package com.huahua.robot.utils

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * ClassName: UrlUtil
 * @description URL编码解码工具类
 * @author 花云端
 * @date 2022-02-20 20:48
 */
object UrlUtil {
    private val encode = "GBK"

    /**
     * 对URL编码后的中文字符串进行解吗
     * @param str String    URL编码后的字符串
     * @return String   进行解码后的字符串
     */
    fun decode(str: String): String {
        var result = "" //解码后的字符串
        try {   //对字符串进行解码
            result = URLDecoder.decode(str, encode) //解码
        } catch (e: UnsupportedEncodingException) { //解码失败
            e.printStackTrace() //打印异常信息
        }
        return result   //返回解码后的字符串
    }

    /**
     * 对中文字符串进行URL编码
     * @param str String 需要进行编码的中文字符串
     * @return String   编码后的字符串
     */
    fun encode(str: String): String {   //对字符串进行编码
        var result = "" //编码后的字符串
        try {   //尝试
            result = URLEncoder.encode(str, encode) //编码
        } catch (e: UnsupportedEncodingException) { //编码失败
            e.printStackTrace() //打印异常信息
        }
        return result   //返回编码后的字符串
    }
}