package com.huahua.robot.listener.grouplistener

import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.send
import com.huahua.robot.core.common.then
import com.huahua.robot.core.enums.RobotPermission
import com.huahua.robot.utils.PermissionUtil
import com.huahua.robot.utils.PermissionUtil.Companion.botCompareToAuthor
import com.huahua.robot.utils.PermissionUtil.Companion.botCompareToMember
import io.ktor.util.reflect.*
import love.forte.di.annotation.Beans
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
@Beans
class GroupMangerListener {

    /**
     * 禁言操作
     */

    /**
     * 禁言
     * @receiver GroupMessageEvent
     */
    @RobotListen(
        isBoot = true,
        desc = "禁言服务",
        permission = RobotPermission.ADMINISTRATOR,
        permissionsRequiredByTheRobot = RobotPermission.ADMINISTRATOR
    )
    @Filter(value = "禁", matchType = MatchType.TEXT_STARTS_WITH)
    suspend fun GroupMessageEvent.ban() {
        val msg: String = messageContent.plainText.trim().split("禁")[1]
        if (msg.isEmpty()) {
            return
        }
        try {
            val time = msg.trim().toInt()
            val list: ArrayList<At> = arrayListOf()
            messageContent.messages.forEach{
                if (it is At) {
                    if (!botCompareToMember(group().member(it.target)!!)){
                        send("权限不足，无法对此用户[${it.target}]进行操作")
                        return
                    }
                    list.add(it)
                    group().member(it.target)?.mute(time.minutes)
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
            group().send(e.message!!)
            return
        }
    }

    /**
     * 解除禁言
     * @receiver GroupMessageEvent
     */
    @RobotListen(
        isBoot = true,
        desc = "解除禁言",
        permission = RobotPermission.ADMINISTRATOR,
        permissionsRequiredByTheRobot = RobotPermission.ADMINISTRATOR
    )
    @Filter("解", matchType = MatchType.TEXT_STARTS_WITH)
    suspend fun GroupMessageEvent.unBan() {
        messageContent.messages.forEach {
            if (it is At) {
                if (!botCompareToMember(group().member(it.target)!!)){
                    send("权限不足，无法对此用户[${it.target}]进行操作")
                    return
                }
                val result = group().member(it.target)!!.unmute()
                result.then {
                    send("已解除用户[${it.target}]的禁言")
                }
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
    suspend fun GroupMessageEvent.groupBan() {
        val result = group().mute()
        result.then {
            send("已开启群禁言")
        }
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
    suspend fun GroupMessageEvent.groupUnBan() {
        val result = group().unmute()
        result.then {
            send("已取消群禁言")
        }
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
    suspend fun GroupMessageEvent.kickPerson() =
        messageContent.messages.forEach {
            (it is At).then {
                if (!botCompareToMember(group().member((it as At).target)!!)){
                    send("权限不足，无法对此用户[${it.target}]进行操作")
                    return
                }
                (group().member(it.target) as MiraiMember).kick("芜湖，起飞")
            }
        }

}