package com.huahua.robot.listener.grouplistener

import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.utils.GlobalVariable
import love.forte.simbot.event.GroupRequestEvent
import love.forte.simbot.event.JoinRequestEvent
import org.springframework.stereotype.Component

/**
 * ClassName: JoinGroupListner
 * @description 加群监听
 * @author 花云端
 * @date 2022-05-10 17:26
 */
@Component
class JoinGroupListener {

    /**
     * 没测试过 或许能用？
     * @receiver JoinGroupListener
     * @param group GroupRequestEvent
     * @param event JoinRequestEvent
     */
    @RobotListen(isBoot = true, desc = "加群监听")
    suspend fun JoinGroupListener.joinGroup(group: GroupRequestEvent,event:JoinRequestEvent) {
        val applicant = group.user()   //申请人
        val name = applicant.username   //申请人名字
        val id = applicant.id   //申请人id
        val message = group.message ?: "Ta什么也没填" //申请信息
        val type = when(group.type.ordinal){
            0 -> "主动申请"
            1 -> "被邀请"
            else -> "emmm"
        }
        val sb = StringBuilder("\t入群申请\n")
            .append("申请人：${name}(${id})\n")
            .append("申请理由：${message}\n")
            .append("申请类型：$type")
        if(group.type.ordinal==1){
            sb.append("\n邀请人：${event.message}")
        }
        GlobalVariable.BOT?.group(group.id)?.send(sb.toString())
    }
}