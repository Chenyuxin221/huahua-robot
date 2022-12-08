package com.huahua.robot.listener.grouplistener

import com.alibaba.fastjson2.JSON
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.core.common.send
import com.huahua.robot.core.common.then
import com.huahua.robot.core.enums.RobotPermission
import com.huahua.robot.utils.HttpUtil
import com.huahua.robot.utils.Permission
import com.huahua.robot.utils.PermissionUtil.Companion.authorPermission
import com.huahua.robot.utils.PermissionUtil.Companion.botCompareToAuthor
import com.huahua.robot.utils.PermissionUtil.Companion.botCompareToMember
import com.huahua.robot.utils.PermissionUtil.Companion.botPermission
import love.forte.di.annotation.Beans
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.ID
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.event.MiraiGroupMessageEvent
import love.forte.simbot.component.mirai.message.MiraiQuoteReply
import love.forte.simbot.component.mirai.message.SimbotOriginalMiraiMessage
import love.forte.simbot.definition.Group
import love.forte.simbot.definition.Member
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.message.At
import love.forte.simbot.message.buildMessages
import love.forte.simbot.message.plus
import love.forte.simbot.message.toText
import love.forte.simbot.tryToLong
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.QuoteReply
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
            messageContent.messages.forEach {
                if (it is At) {
                    if (!getBotManagerPermission(group(), author()) &&  //成员没有机器人管理权限
                        authorPermission() < Permission.ADMINISTRATORS  //成员群权限小于管理员
                    ) {
                        send("你的权限不足，无法进行此操作")
                        return
                    }
                    if (!botCompareToMember(group().member(it.target)!!)) {
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
        permissionsRequiredByTheRobot = RobotPermission.ADMINISTRATOR
    )
    @Filter("解", matchType = MatchType.TEXT_STARTS_WITH)
    suspend fun GroupMessageEvent.unBan() {

        messageContent.messages.forEach {
            if (it is At) {
                if (!getBotManagerPermission(group(), author()) &&  //成员没有机器人管理权限
                    authorPermission() < Permission.ADMINISTRATORS  //成员群权限小于管理员
                ) {
                    send("你的权限不足，无法进行此操作")
                    return
                }
                if (!botCompareToMember(group().member(it.target)!!)) {
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
        permissionsRequiredByTheRobot = RobotPermission.ADMINISTRATOR
    )
    @Filter("开全体禁言")
    suspend fun GroupMessageEvent.groupBan() {
        if (!getBotManagerPermission(group(), author()) &&  //成员没有机器人管理权限
            authorPermission() < Permission.ADMINISTRATORS  //成员群权限小于管理员
        ) {
            send("你的权限不足，无法进行此操作")
            return
        }

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
        permissionsRequiredByTheRobot = RobotPermission.ADMINISTRATOR
    )
    @Filter("关全体禁言")
    suspend fun GroupMessageEvent.groupUnBan() {
        if (!getBotManagerPermission(group(), author()) &&  //成员没有机器人管理权限
            authorPermission() < Permission.ADMINISTRATORS  //成员群权限小于管理员
        ) {
            send("你的权限不足，无法进行此操作")
            return
        }
        val result = group().unmute()
        result.then {
            send("已取消群禁言")
        }
    }

    @RobotListen(
        isBoot = true,
        desc = "撤回操作",
        permissionsRequiredByTheRobot = RobotPermission.ADMINISTRATOR
    )
    @Filter("撤回", matchType = MatchType.TEXT_STARTS_WITH)
    suspend fun GroupMessageEvent.messageRecall() {
        val messages = messageContent.messages
        val originMiraiQuoteReply = messages[MiraiQuoteReply].firstOrNull()?.originalMiraiMessage
            ?: messages.firstNotNullOf { element ->
                (element as? SimbotOriginalMiraiMessage)?.originalMiraiMessage as? QuoteReply
            }
        try {
            if (!getBotManagerPermission(group(), author()) &&  //成员没有机器人管理权限
                authorPermission() < Permission.ADMINISTRATORS && //成员群权限小于管理员
                originMiraiQuoteReply.source.fromId.ID != author().id  //成员不是撤回自己的消息
            ) {
                return
            }
            originMiraiQuoteReply.source.recall()   //撤回原消息
            if (botCompareToAuthor()) {     //将bot权限和发送人比较
                messageContent.delete()     //如果bot权限大于发送人则撤回此条消息
            }
            val msg =
                "「${author().nickOrUsername}」 通过bot撤回了「${group().member(originMiraiQuoteReply.source.fromId.ID)!!.nickOrUsername}」的一条消息"
            send(msg)
        } catch (e: PermissionDeniedException) {
            send("我无权操作此消息")
        } catch (e: Exception) {
            send("撤回失败，无法撤回此消息：${e.message}")
        }
    }


    /**
     * 踢人操作
     * @receiver GroupMessageEvent
     */
    @RobotListen(
        isBoot = true,
        desc = "踢人操作",
        permissionsRequiredByTheRobot = RobotPermission.ADMINISTRATOR
    )
    @Filter("踢", matchType = MatchType.TEXT_STARTS_WITH)
    suspend fun GroupMessageEvent.kickPerson() {
        messageContent.messages.forEach {
            (it is At).then {
                if (!getBotManagerPermission(group(), author()) &&  //成员没有机器人管理权限
                    authorPermission() < Permission.ADMINISTRATORS  //成员群权限小于管理员
                ) {
                    send("你的权限不足，无法进行此操作")
                    return
                }
                if (!botCompareToMember(group().member((it as At).target)!!)) {
                    send("权限不足，无法对此用户[${it.target}]进行操作")
                    return
                }
                (group().member(it.target) as MiraiMember).kick("芜湖，起飞")
            }
        }
    }

    /**
     * 给予成员群头衔 bot必须为群主
     * @receiver GroupMessageEvent
     */
    @RobotListen(
        isBoot = true,
        desc = "特殊头衔设置",
        permission = RobotPermission.ADMINISTRATOR,
        permissionsRequiredByTheRobot = RobotPermission.OWNER
    )
    @Filter("给头衔", matchType = MatchType.TEXT_STARTS_WITH)
    suspend fun GroupMessageEvent.setTitle() =
        messageContent.messages.forEach {
            if (it is At) {
                (botPermission() != Permission.OWNER).then {
                    send("头衔设置失败，bot权限不足")
                    return@forEach
                }
                val event = this as MiraiGroupMessageEvent
                val member = event.originalEvent.group[it.target.tryToLong()]!!
                val title = messageContent.plainText.split("给头衔")[1].trim()
                member.specialTitle = title
                send(At(author().id) + " 成功给予用户[${group().member(it.target)!!.nickOrUsername}]头衔：${title}".toText())

            }
        }

    @RobotListen("增删机器人管理权限", isBoot = true)
    @Filter("{{value}}权限", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.serSuperManager(@FilterValue("value") value: String) {
        if (author().id != RobotCore.ADMINISTRATOR.ID) {
            reply(" 操作失败，你的权限不足")
            return
        }
        messageContent.messages.forEach {
            if (it is At) {
                val url = "http://127.0.0.1:8080/manager/${
                    when (value) {
                        "给", "设置", "添加", "增加" -> "add"
                        "取消", "删除", "移除" -> "delete"
                        else -> return
                    }
                }?groupId=${group().id}&userId=${it.target}"
                val body = JSON.parseObject(HttpUtil.getBody(url))
                when (body.getIntValue("code")) {
                    200 -> {
                        reply("操作成功")
                    }

                    else -> {
                        reply("操作失败：${body.getString("msg")}")
                    }
                }
            }
        }
    }

    @RobotListen("增删管理", isBoot = true, permissionsRequiredByTheRobot = RobotPermission.OWNER)
    @Filter("{{value}}管理", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.setManager(@FilterValue("value") value: String) {
        messageContent.messages.forEach {
            if (it is At) {
                if (author().id != RobotCore.ADMINISTRATOR.ID) {
                    reply(" 权限设置失败，你的权限不足")
                    return
                }
                val event = this as MiraiGroupMessageEvent
                val member = event.originalEvent.group[it.target.tryToLong()]!!
                when (value.trim()) {
                    "给", "设置", "添加", "增加" -> {
                        if (group().member(it.target)!!.isAdmin()) {
                            send("用户[${it.target}]已经是管理员了")
                            return@forEach
                        }
                        member.modifyAdmin(true)
                        send(At(author().id) + " 成功给予用户[${group().member(it.target)!!.nickOrUsername}]管理员权限".toText())
                    }

                    "取消", "删除", "移除" -> {
                        if (!group().member(it.target)!!.isAdmin()) {
                            send("用户[${it.target}]没有管理员权限")
                            return@forEach
                        }
                        member.modifyAdmin(false)
                        send(At(author().id) + " 成功${value.trim()}用户[${group().member(it.target)!!.nickOrUsername}]管理员权限".toText())
                    }
                }
            }
        }
    }

    @RobotListen(desc = "权限测试", isBoot = true)
    @Filter("我的权限")
    suspend fun GroupMessageEvent.doIHavePermission() {
        if (author().id == RobotCore.ADMINISTRATOR.ID) {
            reply("你拥有机器人的最高权限")
            return
        }
        if (getBotManagerPermission(group(), author())) {
            reply("你拥有机器人的部分操作权限")
            return
        }
        if (authorPermission() == Permission.ADMINISTRATORS) {
            reply("你拥有基于管理员的部分权限")
            return
        } else if (authorPermission() == Permission.OWNER) {
            reply("这个群你说了算")
            return
        }
        send("你没有操作权限")
    }
}

fun getBotManagerPermission(group: Group, user: Member): Boolean = getBotManagerPermission(group.id, user.id)
fun getBotManagerPermission(group: ID, user: ID): Boolean =
    getBotManagerPermission(group.toString(), user.toString())

fun getBotManagerPermission(group: String, user: String): Boolean {
    val url = "http://127.0.0.1:8080/manager/query?groupId=${group}&userId=$user"
    val body = HttpUtil.get(url).response
    try {
        if (JSON.parseObject(body).getIntValue("code") == 200) {
            return true
        }
        return false
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }

}