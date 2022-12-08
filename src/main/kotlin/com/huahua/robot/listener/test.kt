package com.huahua.robot.listener

import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.utils.TimeUtil
import love.forte.simbot.ID
import love.forte.simbot.event.internal.BotStartedEvent
import org.springframework.stereotype.Component

@Component
class test {


    @RobotListen("启动事件")
    suspend fun BotStartedEvent.start() {
        bot.contact(RobotCore.ADMINISTRATOR.ID)?.send("${TimeUtil.getNowTime()}: 我好了")
    }

}