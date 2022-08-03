@file:Suppress("unused")

package com.huahua.robot.utils

import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.core.common.isNull
import com.huahua.robot.utils.FileUtil.getTempImage
import com.huahua.robot.utils.FileUtil.url
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import love.forte.simbot.LoggerFactory
import love.forte.simbot.component.mirai.MiraiComponent
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.component.mirai.event.MiraiGroupMessageEvent
import love.forte.simbot.component.mirai.message.MiraiMusicShare
import love.forte.simbot.component.mirai.message.asSimbotMessage
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.event.MessageEvent
import love.forte.simbot.message.*
import love.forte.simbot.message.At
import love.forte.simbot.message.Image.Key.toImage
import love.forte.simbot.message.Message
import love.forte.simbot.resources.Resource.Companion.toResource
import love.forte.simbot.tryToLong
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.PlainText
import java.io.File
import java.net.URL
import java.nio.file.Path

/**
 * ClassName: MessageUtil
 * @description
 * @author 花云端
 * @date 2022-05-07 22:34
 */
class MessageUtil {

    private val log = LoggerFactory.getLogger(MessageUtil::class)
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
    fun encodeMessage(messages: Messages) = json.encodeToString(Messages.serializer, messages)


    /**
     * json转messages
     * @param messageJson String json格式的messages
     * @return Messages messages
     */
    fun decodeMessage(messageJson: String) = json.decodeFromString(Messages.serializer, messageJson)


    companion object {
        fun getAtList(messageContent: ReceivedMessageContent) =
            messageContent.messages.filter { it.key == At.Key }.map { (it as At).target }


        private fun getImageMsg(url: String) = runBlocking {
            url.url().toResource().toImage()
        }


        private fun getImageMsg(file: File) = runBlocking {
            file.toResource().toImage()
        }

        fun String.getImageMessage() = getImageMsg(this)

        fun File.getImageMessage() = getImageMsg(this)

        fun URL.getImageMessage() = runBlocking {
            this@getImageMessage.toResource().toImage()
        }

        fun Path.getImageMessage() = runBlocking {
            this@getImageMessage.toResource().toImage()
        }

        suspend fun String.getTempImageMessage(array: ByteArray): love.forte.simbot.message.Image<*>? {
            val file = this.getTempImage(array)
            file.isNull { return null }
            return file!!.toResource().toImage()
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
/**
 * 图集转发消息
 * @receiver GroupMessageEvent  simbot的群事件监听
 * @param list ArrayList<String>?   图片列表
 * @param userId Long   用户Id
 * @return Message.Element<*>? simbot的消息
 */
suspend fun GroupMessageEvent.getForwardImageMessages(list:ArrayList<String>?,userId:Long): Message.Element<*>? {
    val log = LoggerFactory.getLogger(MessageUtil::class)
    if (list.isNullOrEmpty()){
        log.error("图片列表为空")
        return null
    }
    val forward:ForwardMessage ?
    val miraiBot = bot as MiraiBot
        forward = buildForwardMessage((this as MiraiGroupMessageEvent).originalEvent.group){
            list.forEach{
                val img = Image(miraiBot.originalBot.asFriend.uploadImage(File(it)).imageId)
                add(RobotCore.BOTID.tryToLong(), Mirai.queryProfile(bot.originalBot,userId).nickname, img)
            }
        }
    return forward.asSimbotMessage()
}