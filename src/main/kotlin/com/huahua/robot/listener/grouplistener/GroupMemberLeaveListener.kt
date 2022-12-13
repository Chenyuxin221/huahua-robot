package com.huahua.robot.listener.grouplistener


import com.huahua.robot.core.annotation.RobotListen
import love.forte.di.annotation.Beans
import love.forte.simbot.component.mirai.event.MiraiMemberLeaveEvent

/**
 * ClassName: GroupMemberLeaveListener  退群监听器
 * @description 退群监听
 * @author 花云端
 * @date 2022-06-11 18:50
 */
@Beans
class GroupMemberLeaveListener {

    /**
     * 新写的退群监听
     * @receiver MiraiMemberLeaveEvent
     */
    @RobotListen(isBoot = true, desc = "退群监听")
    suspend fun MiraiMemberLeaveEvent.leaveGroup() {
        val user = member()
        val operator = operator()

        if (operator===user){
            group().send("就在刚刚,${user.nickOrUsername}离开了我们")
        }else{
            if (operator != null) {
                group().send("${user.nickOrUsername}被${operator.nickOrUsername}挪出去了")
            }
        }
    }
}