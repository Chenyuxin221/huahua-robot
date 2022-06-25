@file:Suppress("unused")

package com.huahua.robot.utils

import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.core.common.isNull
import com.huahua.robot.utils.FileUtil.getTempImage
import com.huahua.robot.utils.FileUtil.url
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import love.forte.simbot.ID
import love.forte.simbot.component.mirai.MiraiComponent
import love.forte.simbot.component.mirai.message.MiraiMusicShare
import love.forte.simbot.event.MessageEvent
import love.forte.simbot.message.*
import love.forte.simbot.resources.FileResource
import love.forte.simbot.resources.Resource.Companion.toResource
import net.mamoe.mirai.message.data.MusicKind
import java.io.File
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * ClassName: MessageUtil
 * @description
 * @author 花云端
 * @date 2022-05-07 22:34
 */
class MessageUtil {
    private val json = Json {
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
    fun encodeMessage(messages: Messages): String {
        return json.encodeToString(Messages.serializer, messages)
    }

    /**
     * json转messages
     * @param messageJson String json格式的messages
     * @return Messages messages
     */
    fun decodeMessage(messageJson: String): Messages {
        return json.decodeFromString(Messages.serializer, messageJson)
    }

    companion object {
        fun getAtList(messageContent: ReceivedMessageContent): List<ID> {
            return messageContent.messages.filter { it.key == At.Key }.map { (it as At).target }
        }

        private fun getImageMsg(url: String): Message {
            return runBlocking { RobotCore.getBot()!!.uploadImage(url.url().toResource()) }
        }


        private fun getImageMsg(file: File): Message {
            return runBlocking {
                RobotCore.getBot()!!.uploadImage(FileResource(file))
            }
        }

        fun String.getImageMessage() = getImageMsg(this)

        fun File.getImageMessage() = getImageMsg(this)

        fun URL.getImageMessage() = runBlocking { RobotCore.getBot()!!.uploadImage(this@getImageMessage.toResource()) }

        fun Path.getImageMessage() = runBlocking { RobotCore.getBot()!!.uploadImage(this@getImageMessage.toResource()) }

        suspend fun String.getTempImageMessage(array: ByteArray): Image<*>? {
            val file = this.getTempImage(array)
            file.isNull { return null }
            return RobotCore.getBot()!!.uploadImage(file!!.toResource())
        }

        /**
         * 获取音乐分享消息
         * @param kind MusicKind    音乐类型
         * @param song String    歌曲名
         * @param singer String   歌手名
         * @param jumpUrl String    跳转链接
         * @param picture String    封面图片路径
         * @param musicUrl String   音乐链接
         * @param brief String  简介
         * @return MiraiMusicShare  音乐分享消息
         */
        private fun getMusicShare(
            kind: MusicKind,
            song: String,
            singer: String,
            jumpUrl: String,
            picture: String,
            musicUrl: String,
            brief: String,
        ) = MiraiMusicShare(
            kind,
            song,
            singer,
            jumpUrl,
            picture,
            musicUrl,
            brief
        )

        /**
         *  获取QQ音乐分享卡片消息
         * @param song String   歌曲名
         * @param singer String 歌手名
         * @param jumpUrl String    跳转链接
         * @param picture String    封面图片
         * @param musicUrl String   音乐链接
         * @param brief String  简介
         * @return Message   QQ音乐分享卡片消息
         */
        fun getQQMusicShare(
            song: String,
            singer: String,
            jumpUrl: String,
            picture: String,
            musicUrl: String,
            brief: String = "[分享]${"$song-$singer"}",
        ): Message = getMusicShare(
            kind = MusicKind.QQMusic,
            song = song,
            singer = singer,
            jumpUrl = jumpUrl,
            picture = picture,
            musicUrl = musicUrl,
            brief = brief
        )

        /**
         *  获取QQ音乐分享卡片消息
         * @param song String   歌曲名
         * @param singer String 歌手名
         * @param jumpUrl String    跳转链接
         * @param picture String    封面图片
         * @param musicUrl String   音乐链接
         * @param brief String  简介
         * @return Message   QQ音乐分享卡片消息
         */
        fun MessageEvent.getQQMusicShare(
            song: String,
            singer: String,
            jumpUrl: String,
            picture: String,
            musicUrl: String,
            brief: String = "[分享]${"$song-$singer"}",
        ): Message = getMusicShare(
            kind = MusicKind.QQMusic,
            song = song,
            singer = singer,
            jumpUrl = jumpUrl,
            picture = picture,
            musicUrl = musicUrl,
            brief = brief
        )

        /**
         * 获取网易云音乐分享卡片消息
         * @param song String   歌曲名
         * @param singer String     歌手名
         * @param jumpUrl String    跳转链接
         * @param picture String    封面图片
         * @param musicUrl String   音乐链接
         * @param brief String  简介
         * @return Message  网易云分享卡片消息
         */
        fun getNeteaseCloudMusicShare(
            song: String,
            singer: String,
            jumpUrl: String,
            picture: String,
            musicUrl: String,
            brief: String = "[分享]${song}-${singer}",
        ): Message = MiraiMusicShare(
            MusicKind.NeteaseCloudMusic,
            song,
            singer,
            jumpUrl,
            picture,
            musicUrl,
            brief
        )

        /**
         * 获取网易云音乐分享卡片消息
         * @param song String   歌曲名
         * @param singer String     歌手名
         * @param jumpUrl String    跳转链接
         * @param picture String    封面图片
         * @param musicUrl String   音乐链接
         * @param brief String  简介
         * @return Message  网易云分享卡片消息
         */
        fun MessageEvent.getNeteaseCloudMusicShare(
            song: String,
            singer: String,
            jumpUrl: String,
            picture: String,
            musicUrl: String,
            brief: String = "[分享]${song}-${singer}",
        ): Message = MiraiMusicShare(
            MusicKind.NeteaseCloudMusic,
            song,
            singer,
            jumpUrl,
            picture,
            musicUrl,
            brief
        )

        /**
         * 获取酷狗音乐分享卡片消息
         * @param song String   歌曲名
         * @param singer String    歌手名
         * @param jumpUrl String    跳转链接
         * @param picture String    封面图片
         * @param musicUrl String   音乐链接
         * @param brief String  简介
         */
        fun getKugouMusicShare(
            song: String,
            singer: String,
            jumpUrl: String,
            picture: String,
            musicUrl: String,
            brief: String = "[分享]${song}-${singer}",
        ): Message = getMusicShare(
            kind = MusicKind.KugouMusic,
            song = song,
            singer = singer,
            jumpUrl = jumpUrl,
            picture = picture,
            musicUrl = musicUrl,
            brief = brief
        )

        /**
         * 获取酷狗音乐分享卡片消息
         * @param song String   歌曲名
         * @param singer String    歌手名
         * @param jumpUrl String    跳转链接
         * @param picture String    封面图片
         * @param musicUrl String   音乐链接
         * @param brief String  简介
         */
        fun MessageEvent.getKugouMusicShare(
            song: String,
            singer: String,
            jumpUrl: String,
            picture: String,
            musicUrl: String,
            brief: String = "[分享]${song}-${singer}",
        ): Message = getMusicShare(
            kind = MusicKind.KugouMusic,
            song = song,
            singer = singer,
            jumpUrl = jumpUrl,
            picture = picture,
            musicUrl = musicUrl,
            brief = brief
        )
    }
}