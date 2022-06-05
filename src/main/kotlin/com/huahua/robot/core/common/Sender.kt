@file:Suppress("MemberVisibilityCanBePrivate", "unused")
package com.huahua.robot.core.common

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import love.forte.simboot.annotation.Listener
import love.forte.simbot.ID
import love.forte.simbot.action.SendSupport
import love.forte.simbot.action.sendIfSupport
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.event.MiraiMessagePostSendEvent
import love.forte.simbot.definition.Friend
import love.forte.simbot.definition.Group
import love.forte.simbot.event.*
import love.forte.simbot.message.*
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit


/**
 * @author wuyou
 */
@Suppress("unused")
@Component
class Sender {

    @Suppress("OPT_IN_USAGE", "UNCHECKED_CAST")
    @Listener
    suspend fun MiraiMessagePostSendEvent<*, *>.postSendEvent(session: ContinuousSessionContext) {
        delay(100) // 加个小延时避免获取到空值
        waitMap[originalEvent.receipt.hashCode()]?.let {
            val time = it["time"].toString().toLong()
            if (time == 0L) return
            withTimeoutOrNull(time) {
                val eventMatcher = it["eventMatcher"] as EventMatcher<MessageEvent>
                val channel = it["channel"] as Channel<MessageContent>
                session.waitingForNextMessage(originalEvent.receipt.hashCode().ID, EventMatcher { event ->
                    return@EventMatcher eventMatcher.run { invoke(event) } && event.getId() == it["id"]
                }).let { msg ->
                    channel.send(msg)
                }
            }.let { result ->
                if (result == null) {
                    logger { "${it["id"]}等待超时" }
                }
            }
        }
    }


    companion object {
        suspend fun sendAndWait(
            event: MessageEvent,
            messages: Any,
            separator: String = "",
            timeout: Long = 0,
            timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
            eventMatcher: EventMatcher<MessageEvent> = EventMatcher,
        ): MessageContent? {
            val messageReceipt = when (event) {
                is SendSupport -> event.send(buildMessage(messages, separator))
                else -> event.source().sendIfSupport(buildMessage(messages, separator))
            }
            val time = timeUnit.toMillis(timeout)
            if (time > 0 && messageReceipt?.isSuccess == true && messageReceipt is SimbotMiraiMessageReceipt<*>) {
                val channel = Channel<MessageContent>()
                waitMap[messageReceipt.receipt.hashCode()] = mutableMapOf(
                    "time" to time,
                    "eventMatcher" to eventMatcher,
                    "id" to event.getId(),
                    "channel" to channel
                )
                return withTimeoutOrNull(time) {
                    channel.receive()
                }.also {
                    waitMap.remove(messageReceipt.receipt.hashCode())
                }
            }
            return null
        }

        suspend fun send(
            event: Event,
            messages: Any,
            separator: String = "",
        ): MessageReceipt? = when (event) {
            is SendSupport -> event.send(buildMessage(messages, separator))
            is MessageEvent -> event.source().sendIfSupport(buildMessage(messages, separator))
            else -> null
        }

        fun sendGroupMsg(
            group: Group?,
            messages: Any,
            separator: String = "",
        ) = CoroutineScope(Dispatchers.Default).launch {
            group?.send(buildMessage(messages, separator))
        }

        fun sendGroupMsg(
            group: ID,
            messages: Any,
            separator: String = "",
        ) = sendGroupMsg(getGroup(group), messages, separator)

        fun sendGroupMsg(
            group: String,
            messages: Any,
            separator: String = "",
        ) = sendGroupMsg(getGroup(group.ID), messages, separator)


        fun sendPrivateMsg(
            friend: Friend?,
            messages: Any,
            separator: String = "",
        ) = CoroutineScope(Dispatchers.Default).launch {
            friend?.send(buildMessage(messages, separator))
        }

        fun sendPrivateMsg(
            friend: ID,
            messages: Any,
            separator: String = "",
        ) = sendPrivateMsg(getFriend(friend), messages, separator)

        fun sendPrivateMsg(
            friend: String,
            messages: Any,
            separator: String = "",
        ) = sendPrivateMsg(getFriend(friend.ID), messages, separator)

        private fun getFriend(friend: ID): Friend? = runBlocking { RobotCore.getBot()?.friend(friend) }

        private fun getGroup(group: ID): Group? = runBlocking { RobotCore.getBot()?.group(group) }

        fun buildMessage(
            messages: Any,
            separator: String = "",
        ): Messages = MessagesBuilder().apply {
            when (messages) {
                is Array<*> -> {
                    messages.forEachIndexed { index, it ->
                        append(it.toString())
                        if (index != messages.size - 1) {
                            append(separator)
                        }
                    }
                }
                is Iterable<*> -> {
                    messages.forEachIndexed { index, it ->
                        append(it.toString())
                        if (index != messages.count() - 1) {
                            append(separator)
                        }
                    }
                }
                is Message.Element<*> -> append(messages)
                else -> append(messages.toString())
            }
        }.build()

    }
}

val waitMap: MutableMap<Int, MutableMap<String, Any>> = HashMap()
suspend fun MessageEvent.sendAndWait(
    messages: Any,
    timeout: Long = 0,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    regex: Regex,
): MessageContent? = sendAndWait(messages, "", timeout, timeUnit) {
    regex.matches(it.messageContent.plainText)
}

suspend fun MessageEvent.sendAndWait(
    messages: Any,
    separator: String = "",
    timeout: Long = 0,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    regex: Regex,
): MessageContent? = sendAndWait(messages, separator, timeout, timeUnit) {
    regex.matches(it.messageContent.plainText)
}

suspend fun MessageEvent.sendAndWait(
    messages: Any,
    timeout: Long = 0,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    eventMatcher: EventMatcher<MessageEvent> = EventMatcher,
): MessageContent? = sendAndWait(messages, "", timeout, timeUnit, eventMatcher)

suspend fun MessageEvent.sendAndWait(
    messages: Any,
    separator: String = "",
    timeout: Long = 0,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    eventMatcher: EventMatcher<MessageEvent> = EventMatcher,
): MessageContent? = Sender.sendAndWait(this, messages, separator, timeout, timeUnit, eventMatcher)

fun MessageEvent.send(messages: Any, separator: String = "") = CoroutineScope(Dispatchers.Default).launch {
    Sender.send(this@send, messages, separator)
}

suspend fun MessageEvent.getId(): String = when (this) {
    is GroupMessageEvent -> "message-${group().id}-${author().id}"
    is FriendMessageEvent -> "message-${friend().id}"
    else -> ""
}