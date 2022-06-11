package com.huahua.robot.listener.grouplistener


import com.huahua.robot.core.annotation.RobotListen
import love.forte.simbot.component.mirai.event.MiraiMemberLeaveEvent
import org.springframework.stereotype.Component

/**
 * ClassName: GroupMemberLeaveListener  退群监听器
 * @description 退群监听
 * @author 花云端
 * @date 2022-06-11 18:50
 */
@Component
class GroupMemberLeaveListener {

    /**
     * 新写的退群监听
     * 没测试过，不知道能不能用 或许能把？？？
     * @receiver MiraiMemberLeaveEvent
     */
    @RobotListen(isBoot = true)
    suspend fun MiraiMemberLeaveEvent.leaveGroup() {
        val user = member()
        val operator = operator()
        group().send("就在刚刚,${user.nickOrUsername}离开了我们")
        if (operator===user){
            group().send("他是自己退群的呢，走的时候很安详")
        }else{
            group().send("嗯？他是被${operator.nickOrUsername}踢出群的呢，走的时候很不甘心")
        }
    }
}