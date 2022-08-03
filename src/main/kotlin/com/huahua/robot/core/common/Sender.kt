@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.huahua.robot.core.common

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import love.forte.di.annotation.Beans
import love.forte.simboot.annotation.Listener
import love.forte.simbot.ID
import love.forte.simbot.action.SendSupport
import love.forte.simbot.action.sendIfSupport
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.event.MiraiMessagePostSendEvent
import love.forte.simbot.definition.Contact
import love.forte.simbot.definition.Friend
import love.forte.simbot.definition.Group
import love.forte.simbot.event.*
import love.forte.simbot.message.*
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit



/**
 * @author wuyou，花云端
 */
@Beans
class Sender {
    /**
     * 发送消息后的事件,用于获取上下文后监听下一条消息
     */
    @Suppress("OPT_IN_USAGE", "UNCHECKED_CAST")
    @Listener
    suspend fun MiraiMessagePostSendEvent<*, *>.postSendEvent(session: ContinuousSessionContext) {
        delay(100) // 加个小延时避免获取到空值
        waitMap[originalEvent.receipt.hashCode()]?.let {
            val time = it["time"].toString().toLong()
            if (time == 0L) return
            withTimeoutOrNull(time) {
                val eventMatcher = it["eventMatcher"] as ContinuousSessionEventMatcher<MessageEvent>
                val channel = it["channel"] as Channel<MessageContent>
                session.waitingForNextMessage(originalEvent.receipt.hashCode().toString(), ContinuousSessionEventMatcher { event ->
                    return@ContinuousSessionEventMatcher eventMatcher.run { invoke(event) } && event.getId() == it["id"]
                }).let { msg ->
                    channel.send(msg)
                }
            }.isNull {
                logger { "${it["id"]}等待超时" }
            }
        }
    }


    companion object {
        /**
         * 发送并等待发送者的下一条消息的具体实现
         * @param event 原消息对象
         * @param messages 要发送的消息
         * @param separator [messages]是数组或列表时的消息分隔符
         * @param timeout 超时时间,单位[timeUnit]
         * @param timeUnit 超时时间单位
         * @param eventMatcher 收到消息时的匹配方法,只返回匹配通过时的消息
         */
        suspend fun sendAndWait(
            event: MessageEvent,
            messages: Any,
            separator: String = "",
            timeout: Long = 0,
            timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
            eventMatcher: ContinuousSessionEventMatcher<MessageEvent> = ContinuousSessionEventMatcher,
        ): MessageContent? {
            val messageReceipt = when (event) {
                is SendSupport -> event.send(buildMessage(messages, separator))
                else -> event.source().sendIfSupport(buildMessage(messages, separator))
            }
            val coroutineScope = CoroutineScope(Dispatchers.Default)
            coroutineScope.launch {
                delay(timeUnit.toMillis(timeout))
                messageReceipt?.delete()
            }
            return wait(messageReceipt, event.getId(), timeout, timeUnit, eventMatcher)
        }

        /**
         * 发送群聊消息并等待发送者的下一条消息
         * @param group 发送的群
         * @param qq 接收消息的用户QQ号
         * @param messages 要发送的消息
         * @param separator [messages]是数组或列表时的消息分隔符
         * @param timeout 超时时间,单位[timeUnit]
         * @param timeUnit 超时时间单位
         * @param eventMatcher 收到消息时的匹配方法,只返回匹配通过时的消息
         */
        suspend fun sendGroupAndWait(
            group: String,
            qq: String,
            messages: Any,
            separator: String = "",
            timeout: Long = 0,
            timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
            eventMatcher: ContinuousSessionEventMatcher<MessageEvent> = ContinuousSessionEventMatcher,
        ): MessageContent? = wait(
            sendGroupMsg(group, messages, separator), "message-${group}-${qq}", timeout, timeUnit, eventMatcher
        )

        /**
         * 发送私聊消息并等待发送者的下一条消息
         * @param qq 接收消息的用户QQ号
         * @param messages 要发送的消息
         * @param separator [messages]是数组或列表时的消息分隔符
         * @param timeout 超时时间,单位[timeUnit]
         * @param timeUnit 超时时间单位
         * @param eventMatcher 收到消息时的匹配方法,只返回匹配通过时的消息
         */
        suspend fun sendPrivateAndWait(
            qq: String,
            messages: Any,
            separator: String = "",
            timeout: Long = 0,
            timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
            eventMatcher: ContinuousSessionEventMatcher<MessageEvent> = ContinuousSessionEventMatcher,
        ): MessageContent? = wait(
            sendPrivateMsg(qq, messages, separator), "message-$qq", timeout, timeUnit, eventMatcher
        )

        /**
         * 等待下一条消息具体实现
         * @param messageReceipt 发送消息的消息回执
         * @param id 校验id
         * @param timeout 超时时间,单位[timeUnit]
         * @param timeUnit 超时时间单位
         * @param eventMatcher 收到消息时的匹配方法,只返回匹配通过时的消息
         */
        private suspend fun wait(
            messageReceipt: MessageReceipt?,
            id: String,
            timeout: Long = 0,
            timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
            eventMatcher: ContinuousSessionEventMatcher<MessageEvent> = ContinuousSessionEventMatcher,
        ): MessageContent? {
            val time = timeUnit.toMillis(timeout)
            if (time > 0 && messageReceipt?.isSuccess == true && messageReceipt is SimbotMiraiMessageReceipt<*>) {
                val channel = Channel<MessageContent>()
                waitMap[messageReceipt.receipt.hashCode()] = mutableMapOf(
                    "time" to time, "eventMatcher" to eventMatcher, "id" to id, "channel" to channel
                )
                return withTimeoutOrNull(time) {
                    channel.receive()
                }.also {
                    waitMap.remove(messageReceipt.receipt.hashCode())
                }
            }
            return null
        }

        /**
         * 发送消息
         * @param event 消息对象
         * @param messages 要发送的消息
         * @param separator [messages]是数组或列表时的消息分隔符
         */
        suspend fun send(
            event: Event,
            messages: Any,
            separator: String = "",
        ): MessageReceipt? {
            if (messages.toString().isEmpty()) return null
            return when (event) {
                is SendSupport -> event.send(buildMessage(messages, separator))
                is MessageEvent -> event.source().sendIfSupport(buildMessage(messages, separator))
                else -> null
            }
        }

        /**
         * 发送群消息
         * @param group [Group]对象
         * @param messages 要发送的消息
         * @param separator [messages]是数组或列表时的消息分隔符
         */
        fun sendGroupMsg(
            group: Group?,
            messages: Any,
            separator: String = "",
        ): MessageReceipt? = runBlocking { return@runBlocking group?.send(buildMessage(messages, separator)) }

        /**
         * 发送群消息
         * @param group 群号.ID
         * @param messages 要发送的消息
         * @param separator [messages]是数组或列表时的消息分隔符
         */
        private fun sendGroupMsg(
            group: ID,
            messages: Any,
            separator: String = "",
        ) = sendGroupMsg(getGroup(group), messages, separator)

        /**
         * 发送群消息
         * @param group 群号
         * @param messages 要发送的消息
         * @param separator [messages]是数组或列表时的消息分隔符
         */
        fun sendGroupMsg(
            group: String,
            messages: Any,
            separator: String = "",
        ) = sendGroupMsg(getGroup(group.ID), messages, separator)


        /**
         * 发送私聊消息
         * @param friend [Friend]对象
         * @param messages 要发送的消息
         * @param separator [messages]是数组或列表时的消息分隔符
         */
        private fun sendPrivateMsg(
            friend: Contact?,
            messages: Any,
            separator: String = "",
        ) = runBlocking { return@runBlocking friend?.send(buildMessage(messages, separator)) }

        /**
         * 发送私聊消息
         * @param friend QQ号.ID
         * @param messages 要发送的消息
         * @param separator [messages]是数组或列表时的消息分隔符
         */
        private fun sendPrivateMsg(
            friend: ID,
            messages: Any,
            separator: String = "",
        ) = sendPrivateMsg(getFriend(friend), messages, separator)

        /**
         * 发送私聊消息
         * @param friend QQ号
         * @param messages 要发送的消息
         * @param separator [messages]是数组或列表时的消息分隔符
         */
        fun sendPrivateMsg(
            friend: String,
            messages: Any,
            separator: String = "",
        ) = sendPrivateMsg(getFriend(friend.ID), messages, separator)

        /**
         * 获取好友对象
         * @param friend QQ号.ID
         */
        private fun getFriend(friend: ID): Contact? = runBlocking { RobotCore.getBot().contact(friend) }

        /**
         * 获取群聊对象
         * @param group 群号.ID
         */
        private fun getGroup(group: ID): Group? = runBlocking { RobotCore.getBot().group(group) }

        /**
         * 根据[messages]和[separator]构建一条消息
         * @param messages 要构建的消息
         * @param separator [messages]是数组或列表时的消息分隔符
         */
        private fun buildMessage(
            messages: Any,
            separator: String = "",
        ): Messages = MessagesBuilder().apply {
            when (messages) {
                is Array<*> -> {
                    messages.forEachIndexed { index, it ->
                        when (it) {
                            is Message.Element<*> -> append(it)
                            else -> append(it.toString())
                        }
                        if (index != messages.size - 1) {
                            append(separator)
                        }
                    }
                }
                is Iterable<*> -> {
                    messages.forEachIndexed { index, it ->
                        when (it) {
                            is Message.Element<*> -> append(it)
                            else -> append(it.toString())
                        }
                        if (index != messages.count() - 1) {
                            append(separator)
                        }
                    }
                }
                is Message.Element<*> -> append(messages)
                else -> append(messages.toString())
            }
        }.build()

        /**
         * 给管理员发送消息
         * @param messages Any  要发送的消息
         * @return MessageReceipt?  发送结果
         */
        fun sendAdminMsg(messages: Any) = runBlocking {
            return@runBlocking sendPrivateMsg(RobotCore.ADMINISTRATOR, messages)
        }
    }
}

/**
 * 等待消息的缓存map
 */
val waitMap: MutableMap<Int, MutableMap<String, Any>> = HashMap()

/**
 * 发送并等待发送者的下一条消息
 * @see Sender.sendAndWait
 * @param messages 要发送的消息
 * @param timeout 超时时间,单位[timeUnit]
 * @param timeUnit 超时时间单位
 * @param regex 消息的正则表达式
 */
suspend fun MessageEvent.sendAndWait(
    messages: Any,
    timeout: Long = 0,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    regex: Regex,
): MessageContent? = sendAndWait(messages, "", timeout, timeUnit) {
    regex.matches(it.messageContent.plainText)
}

/**
 * 发送并等待发送者的下一条消息
 * @see Sender.sendAndWait
 * @param messages 要发送的消息
 * @param separator [messages]是数组或列表时的消息分隔符
 * @param timeout 超时时间,单位[timeUnit]
 * @param timeUnit 超时时间单位
 * @param regex 消息的正则表达式
 */
suspend fun MessageEvent.sendAndWait(
    messages: Any,
    separator: String = "",
    timeout: Long = 0,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    regex: Regex,
): MessageContent? = sendAndWait(messages, separator, timeout, timeUnit) {
    regex.matches(it.messageContent.plainText)
}

/**
 * 发送并等待发送者的下一条消息
 * @see Sender.sendAndWait
 * @param messages 要发送的消息
 * @param timeout 超时时间,单位[timeUnit]
 * @param timeUnit 超时时间单位
 * @param eventMatcher 收到消息时的匹配方法,只返回匹配通过时的消息
 */
suspend fun MessageEvent.sendAndWait(
    messages: Any,
    timeout: Long = 0,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    eventMatcher: ContinuousSessionEventMatcher<MessageEvent> = ContinuousSessionEventMatcher,
): MessageContent? = sendAndWait(messages, "", timeout, timeUnit, eventMatcher)

/**
 * 发送并等待发送者的下一条消息
 * @see Sender.sendAndWait
 * @param messages 要发送的消息
 * @param separator [messages]是数组或列表时的消息分隔符
 * @param timeout 超时时间,单位[timeUnit]
 * @param timeUnit 超时时间单位
 * @param eventMatcher 收到消息时的匹配方法,只返回匹配通过时的消息
 */
suspend fun MessageEvent.sendAndWait(
    messages: Any,
    separator: String = "",
    timeout: Long = 0,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    eventMatcher: ContinuousSessionEventMatcher<MessageEvent> = ContinuousSessionEventMatcher,
): MessageContent? = Sender.sendAndWait(this, messages, separator, timeout, timeUnit, eventMatcher)

/**
 * 发送消息
 * @see Sender.send
 * @param messages 要发送的消息
 * @param separator [messages]是数组或列表时的消息分隔符
 */
fun MessageEvent.send(messages: Any, separator: String = "") = runBlocking {
    return@runBlocking Sender.send(this@send, messages, separator)
}

/**
 * 获取消息id,用于等待下一条消息的标识
 */
suspend fun MessageEvent.getId(): String = when (this) {
    is GroupMessageEvent -> "message-${group().id}-${author().id}"
    is FriendMessageEvent -> "message-${friend().id}"
    else -> ""
}