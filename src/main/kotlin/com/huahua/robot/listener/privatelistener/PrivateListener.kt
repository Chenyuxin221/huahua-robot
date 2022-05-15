package com.huahua.robot.listener.privatelistener

import io.ktor.util.reflect.*
import love.forte.simboot.annotation.Listener
import love.forte.simbot.LoggerFactory
import love.forte.simbot.action.replyIfSupport
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.message.Image
import love.forte.simbot.message.Message
import org.springframework.stereotype.Component
import kotlin.reflect.jvm.jvmName

@Component
class PrivateListener {
    val log = LoggerFactory.getLogger(PrivateListener::class.jvmName)
    @Listener
    suspend fun FriendMessageEvent.sendImageUrl(event: FriendMessageEvent){
        for (message:Message.Element<*> in messageContent.messages) {
            if (message.instanceOf(Image::class)){
                val resource = (message as Image).resource()
                event.replyIfSupport("图片直链：\n${resource.name}")
                log.info("用户ID：${friend().id}\t图片地址:${resource.name}")
            }
        }
    }
}