package com.huahua.robot.listener

import com.huahua.robot.api.mapper.FuncSwitchMapper
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.core.common.logger
import com.huahua.robot.service.SwitchSateService
import com.huahua.robot.utils.TimeUtil
import kotlinx.coroutines.runBlocking
import love.forte.simbot.ID
import love.forte.simbot.event.internal.BotStartedEvent
import org.springframework.boot.logging.LogLevel
import org.springframework.stereotype.Component

@Component
class test(
    val switchSateService: SwitchSateService,
    val mapper: FuncSwitchMapper,
) {

    @RobotListen("启动事件")
    suspend fun BotStartedEvent.start() {
        bot.contact(RobotCore.ADMINISTRATOR.ID)?.send("${TimeUtil.getNowTime()}: 我好了")
        initRedis()
    }

    fun unregisterEvent() {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                runBlocking {
                    RobotCore.getBot().cancel()
                }
                super.run()
            }
        })
    }


    //    @PostConstruct
    fun initRedis() {

        val resul = mapper.selectList(null)
        if (resul.isNullOrEmpty()) {
            logger(LogLevel.ERROR) {
                "哎呀，出错啦，没有获取到数据呢"
            }
            return
        }
        resul.forEach {
            val groupId = it.groupId
            val func = it.func
            val state = it.state
            switchSateService.set(groupId, func, state)
        }
    }

}