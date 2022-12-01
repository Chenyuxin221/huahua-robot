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
        group().send("就在刚刚,${user.nickOrUsername}离开了我们")
        if (operator===user){
            group().send("啊这...他是自己退群的啊，那没事了")
        }else{
            if (operator != null) {
                group().send("嗯？他是触犯天条被${operator.nickOrUsername}移出去的")
            }
        }
    }
}