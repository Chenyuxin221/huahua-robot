package com.huahua.robot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * 账号配置类
 * @author 花云端
 * @date 2023-02-10 21:51
 */
@Component
@ConfigurationProperties(prefix = "huahua.account")
class AccountConfig {
    var adminId = -1
    var botId = -1
    override fun toString(): String {
        return "adminID=${adminId};botId=${botId}"
    }
}