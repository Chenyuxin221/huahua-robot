package com.huahua.robot.utils

import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.music.MusicInfo
import com.huahua.robot.music.entity.music.Data
import com.huahua.robot.music.entity.music.Music
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import love.forte.simbot.ID
import love.forte.simbot.component.mirai.MiraiComponent
import love.forte.simbot.component.mirai.message.MiraiMusicShare
import love.forte.simbot.message.At
import love.forte.simbot.message.Message
import love.forte.simbot.message.Messages
import love.forte.simbot.message.ReceivedMessageContent
import love.forte.simbot.resources.FileResource
import love.forte.simbot.resources.Resource.Companion.toResource
import net.mamoe.mirai.message.data.MusicKind
import java.io.File
import kotlin.io.path.Path

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

    companion object{
        fun getAtList(messageContent: ReceivedMessageContent): List<ID> {
            return messageContent.messages.filter { it.key == At.Key }.map { (it as At).target }
        }

        fun getImageMessage(path: String): Message {
            return runBlocking { RobotCore.getBot()!!.uploadImage(Path(path).toResource()) }
        }
        fun getImageMessage(file:File):Message{
            return runBlocking {
                RobotCore.getBot()!!.uploadImage(FileResource(file)) }
        }

        fun getMusicShare(musicInfo: MusicInfo): Message {
            return MiraiMusicShare(
                musicInfo.type,
                musicInfo.title,
                musicInfo.artist,
                musicInfo.jumpUrl,
                musicInfo.previewUrl,
                musicInfo.musicUrl,
                "[分享]${musicInfo.title}"
            )
        }
        fun getMusicShare(music:Music,type:MusicKind):Message{
            return MiraiMusicShare(
                type,
                music.data.song+"-"+music.data.singer,
                music.data.singer,
                music.data.url,
                music.data.picture,
                music.data.music,
            "[分享]${music.data.song+"-"+music.data.singer}"
            )
        }
        fun getMusicShare(music:Data,type:MusicKind):Message{
            return MiraiMusicShare(
                type,
                music.song+"-"+music.singer,
                music.singer,
                music.url,
                music.picture,
                music.music,
                "[分享]${music.song+"-"+music.singer}"
            )
        }
    }
}