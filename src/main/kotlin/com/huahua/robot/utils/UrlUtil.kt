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
class UrlUtil {
    private val ENCODE = "GBK"

    /**
     * 对URL编码后的中文字符串进行解吗
     * @param str String    URL编码后的字符串
     * @return String   进行解码后的字符串
     */
    fun decode(str: String): String {
        var result = ""
        try {
            result = URLDecoder.decode(str, ENCODE)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * 对中文字符串进行URL编码
     * @param str String 需要进行编码的中文字符串
     * @return String   编码后的字符串
     */
    fun encode(str: String): String {
        var result = ""
        try {
            result = URLEncoder.encode(str, ENCODE)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return result
    }
}