package com.huahua.robot.config

import org.springframework.stereotype.Component

/**
 * ClassName: Baidubce
 * @description
 * @author 花云端
 * @date 2022-08-16 22:02
 */
@Component
class Baidubce {
    fun setClient_id(string: String) {
        client_id = string
    }

    fun setClient_secret(string: String) {
        client_secret = string
    }

    companion object {
        var client_id: String = ""
        var client_secret: String = ""
    }
}