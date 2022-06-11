package com.huahua.robot.listener.grouplistener

import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.Sender
import love.forte.simbot.component.mirai.event.MiraiMemberJoinEvent
import love.forte.simbot.component.mirai.event.MiraiMemberJoinRequestEvent
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
     * 加群监听
     * @description 加群监听
     * @receiver MiraiMemberJoinEvent   入群事件
     */
    @RobotListen(isBoot = true)
    suspend fun MiraiMemberJoinEvent.joinGroup() {
        val group = group() //所在群
        val member = member()   //加群人
        group().send("呐呐，欢迎 ${member.nickname} 加入群聊,请查看群公告")
        group().send("你大概是本群的第${group.currentMember}个人")
        group().send("别忘了给我点点小星星哦\nhttps://github.com/Chenyuxin221/huahua-robot")
        group().send("最后最后，有需要的话可以发送\".h|.help\"查看帮助哦")
    }

    /**
     * 加群监听
     * @receiver MiraiMemberJoinRequestEvent    加群请求事件
     */
    @RobotListen(isBoot = true)
    suspend fun MiraiMemberJoinRequestEvent.joinGroup() {
        val text = message  //加群请求消息
        val group = group() //所在群
        val member = user() //加群人
        Sender.sendGroupMsg(group.id.toString(), "入群申请：\n申请人：${member.nickOrUsername}\n申请原因：${text}\n请管理员前往处理")
    }
}