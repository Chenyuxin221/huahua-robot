package com.huahua.robot.listener.privatelistener

import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.RobotCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.Listener
import love.forte.simboot.filter.MatchType
import love.forte.simbot.LoggerFactory
import love.forte.simbot.action.replyIfSupport
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.message.Image
import org.springframework.stereotype.Component
import kotlin.reflect.jvm.jvmName
import kotlin.system.exitProcess

@Component
class PrivateListener {
    val log = LoggerFactory.getLogger(PrivateListener::class.jvmName)
    @Listener
    suspend fun FriendMessageEvent.sendImageUrl(){
        messageContent.messages.forEach{
            if (it is Image){
                val resource = it.resource()
                replyIfSupport("图片直链：\n${resource.name}")
                log.info("用户ID：${friend().id}\t图片地址:${resource.name}")
            }
        }
    }
}