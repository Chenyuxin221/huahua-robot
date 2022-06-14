package com.huahua.robot.utils

import love.forte.simbot.Bot
import love.forte.simbot.ID

@SuppressWarnings("unused")
object GlobalVariable {

    /**
     * 全局bot
     */
    var BOT: Bot? = null

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

    var SKey = ""

    var MusicJump = true

}