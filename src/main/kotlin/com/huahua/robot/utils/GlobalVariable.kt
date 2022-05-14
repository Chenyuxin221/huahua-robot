package com.huahua.robot.utils

import love.forte.simbot.Bot
import love.forte.simbot.ID

@SuppressWarnings("unused")
class GlobalVariable {


    /**
     * 百度翻译
     */
    val App_ID: String = "20210225000707511"
    val SECURITY_KEY: String = "vef71g0p0iTyRtM5bLIh"

    /**
     * 全局bot
     */
    var BOT:Bot? = null

    /**
     * 机器人主人
     */
    val MASTER = "189950046".ID

    /**
     * 主机器人
     */
    val BOTID = "1849950085".ID

    /**
     * 写真列表
     */
    var PhotoList = arrayListOf<String>()

    /**
     * 管理员列表
     */
    val ADMINISTRATOR: List<String>? = ArrayList()



}