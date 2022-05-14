package com.huahua.robot.core.listener

import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.enums.RobotPermission
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.Listener
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.event.GroupMessageEvent
import org.springframework.stereotype.Component

/**
 * @author wuyou
 */
@Component
class TestListener {
    @RobotListen(desc = "测试私聊监听器")
    @Filter("1")
    suspend fun FriendMessageEvent.test() {
        println(friend().send("执行了监听器test"))
    }

    @RobotListen(permission = RobotPermission.ADMINISTRATOR, noPermissionTip = "111", desc = "测试群聊监听器", isBoot = true)
    @Filter("1")
    suspend fun GroupMessageEvent.groupTest() {
        println(group().send("执行了监听器groupTest"))
    }

    /**
     * 原生监听器, 不知道为什么不能用了...如果找到了原因记得跟我说一声
     */
    @Listener
    suspend fun GroupMessageEvent.groupTest2() {
        println("groupTest")
    }
}