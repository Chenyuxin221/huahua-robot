package com.huahua.robot.core.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import love.forte.simbot.Bot
import love.forte.simbot.FragileSimbotApi
import love.forte.simbot.ID
import love.forte.simbot.OriginBotManager
import love.forte.simbot.action.SendSupport
import love.forte.simbot.definition.Friend
import love.forte.simbot.definition.Group
import love.forte.simbot.event.Event
import love.forte.simbot.message.Message
import love.forte.simbot.message.Messages
import love.forte.simbot.message.MessagesBuilder
import love.forte.simbot.message.toText
import java.io.File


/**
 * @author wuyou
 */
@Suppress("unused")
object Sender{
    fun send(event: Event, vararg messages: Any) {
        CoroutineScope(Dispatchers.Default).launch {
            if (event is SendSupport) event.send(buildMessage(*messages))
        }
    }

    fun sendGroupMsg(group: Group, vararg messages: Any) {
        CoroutineScope(Dispatchers.Default).launch {
            group.send(buildMessage(*messages))
        }
    }

    fun sendGroupMsg(group: ID, vararg messages: Any) {
        CoroutineScope(Dispatchers.Default).launch {
            getGroup(group)?.send(buildMessage(*messages))
        }
    }

    fun sendGroupMsg(group: String, vararg messages: Any) {
        CoroutineScope(Dispatchers.Default).launch {
            getGroup(group.ID)?.send(buildMessage(*messages))
        }
    }

    fun sendPrivateMsg(friend: Friend, vararg messages: Any) {
        CoroutineScope(Dispatchers.Default).launch {
            friend.send(buildMessage(*messages))
        }
    }

    fun sendPrivateMsg(friend: ID, vararg messages: Any) {
        CoroutineScope(Dispatchers.Default).launch {
            getFriend(friend)?.send(buildMessage(messages))
        }
    }

    fun sendPrivateMsg(friend: String, vararg messages: Any) {
        CoroutineScope(Dispatchers.Default).launch {
            getFriend(friend.ID)?.send(buildMessage(*messages))
        }
    }


    private fun getFriend(friend: ID): Friend? =
        runBlocking { RobotCore.getBot()?.friend(friend) }

    private fun getGroup(group: ID): Group? {
        buildMessage()
        return runBlocking { RobotCore.getBot()?.group(group) }
    }

    private fun buildMessage(vararg messages: Any): Messages =
        MessagesBuilder().apply {
            messages.forEach {
                when (it) {
                    is Message.Element<*> -> {
                        this.append(it)
                    }
                    else -> {
                        this.append(it.toString().toText())
                    }
                }
            }
        }.build()
}