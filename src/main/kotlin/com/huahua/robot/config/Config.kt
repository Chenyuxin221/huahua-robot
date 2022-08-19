package com.huahua.robot.config

import org.springframework.stereotype.Component

/**
 * ClassName: config
 * @description
 * @author 花云端
 * @date 2022-08-16 20:46
 */
@Component
class Config {
    fun setBoot_Conmmand(str: String) {
        boot_Command = str
    }

    fun setExclude_Groups(arrayList: ArrayList<String>) {
        exclude_groups = arrayList
    }

    fun setBaidubce(b: Baidubce?) {
        if (b != null) {
            baidubce = b
        }
    }

    companion object {
        var boot_Command: String = ""
        var exclude_groups: ArrayList<String> = arrayListOf()
        var baidubce: Baidubce? = null
    }
}



