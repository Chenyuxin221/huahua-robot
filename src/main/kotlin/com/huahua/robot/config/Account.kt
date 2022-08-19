package com.huahua.robot.config

import org.springframework.stereotype.Component

/**
 * ClassName: Account
 * @description
 * @author 花云端
 * @date 2022-08-16 21:59
 */
@Component
class Account {
    fun setAdmin(str: String) {
        admin = str
    }

    fun setBot(str: String) {
        bot = str
    }

    companion object {
        var admin: String = ""
        var bot: String = ""
    }
}