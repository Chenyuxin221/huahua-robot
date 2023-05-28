package com.huahua.robot.listener

import cn.hutool.core.date.DateUtil
import com.huahua.robot.api.mapper.FuncSwitchMapper
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.core.common.logger
import com.huahua.robot.service.SwitchSateService
import com.huahua.robot.utils.HttpUtil
import com.huahua.robot.utils.TimeUtil
import kotlinx.coroutines.runBlocking
import love.forte.simbot.ID
import love.forte.simbot.bot.Bot
import love.forte.simbot.component.mirai.bot.MiraiBot
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
        RobotCore.setBot(bot)
        RobotCore.getBot().contact(RobotCore.ADMINISTRATOR.ID)?.send(getStartupPrompt())
        initRedis() // 将数据库中的数据缓存进redis
        getPsKey(bot)
    }


    /**
     * 获取bot的skey pskey
     * @receiver GroupMessageEvent
     */
    fun getPsKey(bot: Bot) {
        val client = (bot as MiraiBot).originalBot.javaClass.getMethod("getClient").invoke(bot.originalBot)
        val wLoginSigInfo = client.javaClass.getMethod("getWLoginSigInfo").invoke(client)
        val sKey = wLoginSigInfo.javaClass.getDeclaredMethod("getSKey").invoke(wLoginSigInfo)
        val psKeyMap = wLoginSigInfo.javaClass.getDeclaredMethod("getPsKeyMap").invoke(wLoginSigInfo) as HashMap<*, *>
        val map = hashMapOf<String, String>()
        map["code"] = bot.id.toString()
        map["psKey"] = (psKeyMap["vip.qq.com"]?.getFieldValue("data") as ByteArray).decodeToString()
        map["sKey"] = (sKey.getFieldValue("data") as ByteArray).decodeToString()
        RobotCore.BOT_INFORMATION = map
    }

    private fun getStartupPrompt(): String {
        val url = "https://xiaobai.klizi.cn/API/other/wtqh.php"
        val message = try {
            val body = HttpUtil.getBody(url)
            if (body.isEmpty()) throw Exception("空的body")
            "${body}\n\t\t---${TimeUtil.getNowTime()}"
        } catch (e: Exception) {
            val date = DateUtil.date()
            val hours = date.hour(true) // 获取小时
            val timeInterval = if (hours <= 6 || hours >= 23) "凌晨"
            else if (hours in 7..9) "早上"
            else if (hours in 10..13) "中午"
            else if (hours in 14..19) "下午"
            else "晚上"
            "${timeInterval}好啊，现在是${TimeUtil.getNowTime()}"
        }
        return message
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

    fun Any.getFieldValue(fieldName: String): Any {
        return javaClass.getDeclaredField(fieldName).let {
            it.isAccessible = true
            it.get(this)
        }
    }

    fun Any.invoke(methodName: String, vararg args: Any): Any {
        return javaClass.getDeclaredMethod(methodName).let {
            it.isAccessible = true
            if (it.parameterCount > 0) it.invoke(this, args)
            else it.invoke(this)
        }
    }

}