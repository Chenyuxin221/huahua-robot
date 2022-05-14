package com.huahua.robot.listener.grouplistener

import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.enums.RobotPermission
import io.ktor.util.reflect.*
import love.forte.simboot.annotation.Filter
import love.forte.simboot.filter.MatchType
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.message.At
import love.forte.simbot.message.Message
import love.forte.simbot.message.buildMessages
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.minutes


/**
 * ClassName: GroupMangerListner
 * @description 群管监听
 * @author 花云端
 * @date 2022-05-11 19:16
 */
@Component
class GroupMangerListener {

    /**
     * 禁言
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "禁言服务", permissionsRequiredByTheRobot = RobotPermission.ADMINISTRATOR)
    @Filter(value = "禁", matchType = MatchType.TEXT_STARTS_WITH)
    suspend fun GroupMessageEvent.ban() {
        val msg: String = messageContent.plainText.trim().split("禁")[1]
        if (msg.isEmpty()) {
            return
        }
        try {
            val time = msg.trim().toInt()
            val list: ArrayList<At> = arrayListOf()
            for (message: Message.Element<*> in messageContent.messages) {
                if (message.instanceOf(At::class)) {
                    list.add(message as At)
                    group().member(message.target)?.mute(time.minutes)
                }
            }
            val message = buildMessages {
                this.at(author().id)
                this.text(" 已将")
                list.forEach {
                    this.append(it)
                    this.text(" ")
                }
                this.text(" 禁言${time}分钟")
            }
            group().send(message)
        } catch (e: Exception) {
            println(e.message)
            group().send("非法的时间参数")
            return
        }
    }
}