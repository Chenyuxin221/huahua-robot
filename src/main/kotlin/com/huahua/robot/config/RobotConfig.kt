@file:Suppress("MemberVisibilityCanBePrivate")

package com.huahua.robot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * bot配置项
 * @author 花云端
 * @date 2023-02-10 21:47
 */
@Component
@ConfigurationProperties(prefix = "huahua.config")
class RobotConfig {
    var bootCommand = ""
    var morningPaperGroups: ArrayList<Int>? = null
    var excludeGroups: ArrayList<Int>? = null
    var baidubce: Baidubce? = null
    var gpt: Gpt? = null

    class Baidubce {
        var clientId = ""
        var clientSecret = ""
        override fun toString(): String {
            return "clientID=${clientId};clientSecret=${clientSecret}"
        }
    }

    class Gpt {
        var cfClearance = ""
        var token = ""
        override fun toString(): String {
            return "cfClearance${cfClearance};token=${token}"
        }
    }

    override fun toString(): String {
        return StringBuilder()
            .append("bootCommand=${bootCommand};")
            .append("morningPaperGroups=${morningPaperGroups};")
            .append("excludeGroups=${excludeGroups};")
            .append("baidubce=${baidubce.toString()};")
            .append("Gpt=${gpt.toString()}")
            .toString()
    }
}
