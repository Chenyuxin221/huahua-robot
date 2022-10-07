package com.huahua.robot.utils

import com.huahua.robot.core.common.then
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination
import java.util.*

/**
 * ClassName: ChineseToPinyinUtils
 * @description 汉字转拼音工具类
 * @author 花云端
 * @date 2022-09-22 17:28
 */
object ChineseToPinyinUtils {

    fun toPinYin(str: String) = toPinYin(str, "", Type.LOWERCASE)

    fun toPinYin(str: String, separator: String) = toPinYin(str, separator, Type.LOWERCASE)

    fun toPinYin(str: String, type: Type) = toPinYin(str, "", type)


    /**
     * 将str转换成拼音，如果不是汉字或没有对应的拼音，则不做处理
     * 例如：抽奖 转换成 choujiang
     * @param str String    需要转换的汉字
     * @param separator String  转换结果的分割符
     * @param type Type 转换的拼音格式
     * @return String
     */
    private fun toPinYin(str: String?, separator: String, type: Type): String? {
        str.isNullOrEmpty().then { return "" }
        try {
            val format = HanyuPinyinOutputFormat()
            if (type == Type.UPPERCASE) {
                format.caseType = HanyuPinyinCaseType.UPPERCASE
            } else {
                format.caseType = HanyuPinyinCaseType.LOWERCASE
            }
            var py = ""
            var temp: String
            var t: Array<String>
            for (i in 0 until str!!.length) {
                val c = str[i]
                if (str.codePointAt(i) <= 128) {
                    py += c + separator
                } else {
                    t = PinyinHelper.toHanyuPinyinStringArray(c, format)
                    if (t == null) {
                        py += c
                    } else {
                        temp = t[0]
                        if (type == Type.FIRSTUPPER) {
                            temp = t[0].uppercase(Locale.getDefault())[0] + temp.substring(1)
                        }
                        temp = temp.substring(0, temp.length - 1)
                        py += temp + (if (i == str.length - 1) {
                            ""
                        } else {
                            separator
                        })
                    }
                }
            }
            return py.trim()
        } catch (e: BadHanyuPinyinOutputFormatCombination) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 方式
     */
    enum class Type {
        /**
         * 全大写
         */
        UPPERCASE,

        /**
         * 全小写
         */
        LOWERCASE,

        /**
         * 首字母大写
         */
        FIRSTUPPER
    }
}