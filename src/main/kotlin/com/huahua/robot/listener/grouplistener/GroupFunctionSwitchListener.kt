package com.huahua.robot.listener.grouplistener

import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.core.common.isNull
import com.huahua.robot.core.common.then
import com.huahua.robot.service.SwitchSateService
import com.huahua.robot.utils.Permission
import com.huahua.robot.utils.PermissionUtil.Companion.authorPermission
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.event.GroupMessageEvent
import org.springframework.stereotype.Component

/**
 * ClassName: GroupFunctionSwitchListener
 * @description
 * @author 花云端
 * @date 2022-12-09 17:32
 */
@Component
class GroupFunctionSwitchListener(
    val switchSateService: SwitchSateService,
) {

    @RobotListen("开关设置", isBoot = true)
    @Filter("^(添加|增加|打开|设置){{name}}$", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.openSwitch(@FilterValue("name") name: String) {

        if (author().id.toString() != RobotCore.ADMINISTRATOR.toString() &&
            authorPermission() == Permission.MEMBER
        ) {
            // 没有操作权限则直接返回
            return
        }

        val group = group().id.toString()
        val switch = getSwitch(name)
        switch.isEmpty().then { return }    //不是指定关键词则直接返回
        switchSateService.set(group, switch, true)
        val result = switchSateService.get(group, switch)
        result?.then {
            reply("设置成功，当前状态：${result}")
        }.isNull {
            reply("哎呀，出错啦")
        }
        return
    }

    @RobotListen("开关设置", isBoot = true)
    @Filter("^(取消|关闭){{name}}$", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.closeSwitch(@FilterValue("name") name: String) {

        if (author().id.toString() != RobotCore.ADMINISTRATOR.toString() &&
            authorPermission() == Permission.MEMBER
        ) {
            // 没有操作权限则直接返回
            return
        }

        val group = group().id.toString()
        val switch = getSwitch(name)
        switch.isEmpty().then { return }    //不是指定关键词则直接返回
        switchSateService.set(group, switch, false)
        val result = switchSateService.get(group, switch)
        result?.not()?.then {
            reply("设置成功，当前状态：${result}")
        }.isNull {
            reply("哎呀，出错啦")
        }
    }

    private fun getSwitch(value: String) = when (value.lowercase()) {
        "chatgpt", "openai", "聊天" -> "聊天"
        "抽奖" -> "抽奖"
        "加群自动同意", "自动同意" -> "加群自动同意"
        else -> ""
    }


}