package com.huahua.robot.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import love.forte.simbot.component.mirai.MiraiComponent
import love.forte.simbot.message.Messages

/**
 * ClassName: MessageUtil
 * @description
 * @author 花云端
 * @date 2022-05-07 22:34
 */
class MessageUtil {
    private val json = Json{
        isLenient = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            include(Messages.serializersModule)
            include(MiraiComponent.messageSerializersModule)
        }
    }

    /**
     * messages转json
     * @param messages messages
     * @return String   json字符串
     * @see messages
     */
    fun encodeMessage(messages: Messages):String{
        return json.encodeToString(Messages.serializer,messages)
    }

    /**
     * json转messages
     * @param messageJson String json格式的messages
     * @return Messages messages
     */
    fun decodeMessage(messageJson:String):Messages{
        return json.decodeFromString(Messages.serializer,messageJson)
    }
}