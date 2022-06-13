package com.huahua.robot.listener.grouplistener

import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.enums.RobotPermission
import io.ktor.util.reflect.*
import love.forte.simboot.annotation.Filter
import love.forte.simboot.filter.MatchType
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.message.At
import love.forte.simbot.message.Message
import love.forte.simbot.message.buildMessages
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.minutes


/**
 * ClassName: GroupMangerListener
 * @description 群管监听
 * @author 花云端
 * @date 2022-05-11 19:16
 */
@Component
class GroupMangerListener {

    /**
     * 禁言操作
     */

    /**
     * 禁言
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true,
        desc = "禁言服务",
        permission = RobotPermission.ADMINISTRATOR,
        permissionsRequiredByTheRobot = RobotPermission.ADMINISTRATOR)
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
                if (message is At) {
                    list.add(message)
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

    /**
     * 解除禁言
     * @receiver GroupMessageEvent
     * @param event GroupMessageEvent
     */
    @RobotListen(isBoot = true,
        desc = "解除禁言",
        permission = RobotPermission.ADMINISTRATOR,
        permissionsRequiredByTheRobot = RobotPermission.ADMINISTRATOR)
    @Filter("解", matchType = MatchType.TEXT_STARTS_WITH)
    suspend fun GroupMessageEvent.unBan() {
        messageContent.messages.forEach{
            if (it is At) {
                group().member(it.target)?.unmute()
            }
        }
    }

    /**
     * 群禁言
     * @receiver GroupMessageEvent
     */
    @RobotListen(
        isBoot = true,
        desc = "群禁言",
        permission = RobotPermission.ADMINISTRATOR,
        permissionsRequiredByTheRobot = RobotPermission.ADMINISTRATOR
    )
    @Filter("开全体禁言")
    suspend fun GroupMessageEvent.groupBan(){
        group().mute()
    }

    /**
     * 群解禁
     * @receiver GroupMessageEvent
     */
    @RobotListen(
        isBoot = true,
        desc = "群解禁",
        permission = RobotPermission.ADMINISTRATOR,
        permissionsRequiredByTheRobot = RobotPermission.ADMINISTRATOR
    )
    @Filter("关全体禁言")
    suspend fun GroupMessageEvent.groupUnBan(){
        group().unmute()
    }

    /**
     * 踢人操作
     * -------------尚未测试
     * @receiver GroupMessageEvent
     */
    @RobotListen(
        isBoot = true,
        desc = "踢人操作",
        permission = RobotPermission.ADMINISTRATOR,
        permissionsRequiredByTheRobot = RobotPermission.ADMINISTRATOR
    )
    @Filter("踢", matchType = MatchType.TEXT_STARTS_WITH)
    suspend fun GroupMessageEvent.kickPerson(){
        for (message: Message.Element<*> in messageContent.messages) {
            if (message is At) {
                (group().member(message.target) as MiraiMember).kick("芜湖，起飞")
            }
        }
    }
}