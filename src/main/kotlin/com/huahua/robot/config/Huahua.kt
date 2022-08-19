package com.huahua.robot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * ClassName: Huahua
 * @description
 * @author 花云端
 * @date 2022-08-16 21:53
 */
@ConfigurationProperties(prefix = "huahua")
@Configuration
class Huahua {
    fun setConfig(c: Config) {
        config = c
    }

    fun setAccount(a: Account) {
        account = a
    }

    companion object {
        var config: Config = Config()
        var account: Account = Account()
    }
}