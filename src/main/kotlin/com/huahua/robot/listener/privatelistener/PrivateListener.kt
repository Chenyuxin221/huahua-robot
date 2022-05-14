package com.huahua.robot.listener.privatelistener

import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.Listener
import love.forte.simboot.filter.MatchType
import love.forte.simbot.action.replyIfSupport
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.message.Text
import love.forte.simbot.message.toText
import org.springframework.stereotype.Component

@Component
class PrivateListener {

    @Listener
    @Filter(value = "你好", matchType = MatchType.TEXT_EQUALS)
    suspend fun FriendMessageEvent.MyFriendLisnter(){
        val text: Text = "hello".toText()
        friend().send(text)
    }
}