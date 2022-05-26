package com.huahua.robot.music

import com.google.gson.Gson
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.core.common.Sender
import com.huahua.robot.core.enums.RobotPermission
import com.huahua.robot.music.entity.neteasemusic.NeteaseMusic
import com.huahua.robot.music.entity.qqmusic.QQMusic
import com.huahua.robot.music.util.Cookie
import com.huahua.robot.utils.GlobalVariable
import com.huahua.robot.utils.HttpUtil
import com.huahua.robot.utils.MessageUtil
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.ExperimentalSimbotApi
import love.forte.simbot.ID
import love.forte.simbot.LoggerFactory
import love.forte.simbot.event.ContinuousSessionContext
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.event.MessageEvent
import net.mamoe.mirai.message.data.MusicKind
import org.springframework.stereotype.Component
import kotlin.reflect.jvm.jvmName

/**
 * ClassName: MusicListener
 * @description
 * @author 花云端
 * @date 2022-05-18 16:43
 */
@Component
class MusicListener {

    private val log = LoggerFactory.getLogger(MusicListener::class.jvmName)
    private val listTip = "---------------------\n" +
            "请输入序号or播放+序号进行播放\n" +
            "或者输入下载+序号获取下载链接\n" +
            "---------------------\n"
    private val downloadTip = "下载地址（复制到浏览器下载）：\n"

    @RobotListen(desc = "登录", isBoot = true, permission = RobotPermission.ADMINISTRATOR)
    @Filter("登录")
    suspend fun GroupMessageEvent.login() {
        userLogin()
    }

    @OptIn(ExperimentalSimbotApi::class)
    @RobotListen(desc = "点歌", isBoot = true)
    @Filter("点歌{{name}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.music(
        @FilterValue("name") name: String,
        session: ContinuousSessionContext,
    ){
        if (GlobalVariable.SKey.isEmpty()) {
            group().send("正在登录中~请稍后")
            userLogin()
        }
        val qqMusicState = qqMusic(GlobalVariable.MusicJump, name, this, session) //是否有匹配歌曲
        if (!qqMusicState) {
            group().send("QQ音乐未搜索到结果，正在为你跳转至网易云")
            neteaseMusic(name, this, session)
        }
    }

    @OptIn(ExperimentalSimbotApi::class)
    @RobotListen(desc = "指定点歌", isBoot = true)
    @Filter("{{channel}}点歌{{name}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.music(
        @FilterValue("channel") channel: String,
        @FilterValue("name") name: String,
        session: ContinuousSessionContext,
    ) {
        //判断channel是否为空
        if(channel.isNotEmpty()){
            //指定点歌
            when(channel){
                "网易云","网抑云","网易" -> neteaseMusic(name,this,session)
                "QQ","qq","qq音乐","QQ音乐" -> {
                    if (GlobalVariable.SKey.isEmpty()) {
                        group().send("正在登录中~请稍后")
                        userLogin()
                    }
                    qqMusic(false, name, this, session)
                }
                else -> return
            }
        }

    }

    /**
     * 是否需要跳转
     * @receiver GroupMessageEvent   群聊事件
     * @param state String   状态
     */
    @RobotListen(desc = "是否需要跳转", isBoot = true)
    @Filter("{{state}}自动跳转", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.setMusic(@FilterValue("state") state: String) {
        if (state == "开启" || state == "设置") {
            GlobalVariable.MusicJump = true
            group().send("自动跳转已开启")
            return
        }
        if (state == "关闭" || state == "取消") {
            GlobalVariable.MusicJump = false
            group().send("自动跳转已关闭")
        }
    }


    /**
     * qq音乐功能
     * @param isJump Boolean    是否判断歌曲列表包含name
     * @param name String   搜索的名字
     * @param event GroupMessageEvent   群聊时间
     * @param session ContinuousSessionContext  持续会话
     * @return Boolean
     * 不需要自动跳转就没用，开启自动跳转后判断列表是否包含 name ,没有则返回false
     */
    @OptIn(ExperimentalSimbotApi::class)
    private suspend fun qqMusic(
        listIsContainsName: Boolean,
        name: String,
        event: GroupMessageEvent,
        session: ContinuousSessionContext,
    ): Boolean {
        val group = event.group()
        var url =
            "https://api.klizi.cn/API/music/vipqqyy.php?msg=${name}&uin=1849950046&skey=${GlobalVariable.SKey}"
        val musicList = HttpUtil.getBody(url)
        if (listIsContainsName) {   // 判断是否需要跳转
            if (!musicList.contains(name.split(" ")[0])) {    // 判断列表是否包含歌名
                return false    // 无结果返回false
            }
        }
        group.send("${musicList}${listTip}")
        event.getNum(session)?.let {
            var music = Gson().fromJson(HttpUtil.getBody("${url}&n=${it.index}"), QQMusic::class.java).data
            if (music.music.isEmpty()) {
                group.send("skey失效，请重新登录")
                userLogin()
                url =
                    "https://api.klizi.cn/API/music/vipqqyy.php?msg=${name}&uin=1849950046&skey=${GlobalVariable.SKey}"
                music = Gson().fromJson(HttpUtil.getBody("${url}&n=${it.index}"), QQMusic::class.java).data
            }
            when (it.state) {
                1 -> {
                    val share = MessageUtil.getMusicShare(music, MusicKind.QQMusic)
                    group.send(share)
                }
                0 -> group.send("${downloadTip}${music.music}")
                else -> group.send("哎呀，出错啦")
            }
        }

        return true
    }

    /**
     * 网易云功能
     * @param name String   名字
     * @param event GroupMessageEvent   群聊事件
     * @param session ContinuousSessionContext  持续会话
     * @return Boolean  没用
     */
    @OptIn(ExperimentalSimbotApi::class)
    private suspend fun neteaseMusic(
        name: String,
        event: GroupMessageEvent,
        session: ContinuousSessionContext,
    ): Boolean {
        val group = event.group()
        val url = "https://api.klizi.cn/API/music/netease.php?msg=$name"
        val response = HttpUtil.getBody(url)
        val musicList = response.substring(0, response.length - 12).trim()
        group.send("${musicList}\n${listTip}")
        event.getNum(session)?.let {
            println(it.index)
            val musicUrl = "${url}&n=${it.index}"
            val music = Gson().fromJson(HttpUtil.getBody(musicUrl), NeteaseMusic::class.java)
            when (it.state) {
                1 -> {
                    val share = MessageUtil.getMusicShare(music)
                    group.send(share)
                }
                0 -> group.send("${downloadTip}$music.url")
                else -> group.send("哎呀，出错啦~")
            }

        }
        return true
    }


    @OptIn(ExperimentalSimbotApi::class)
    private suspend fun GroupMessageEvent.getNum(session: ContinuousSessionContext): State? =
        getId(this)?.let { id ->
            session.waitingForOnMessage(id = id.ID, timeout = 60000L, this) { event, _, provider ->
                getId(event)?.let {
                    val text = event.messageContent.plainText
                    if (author().id == event.author().id && group().id == event.group().id) {   //判断是否是同一个人的消息
                        val num = Regex("""^(?:下载|播放)?\s*(\d*)$""").find(text)?.groups?.get(1)?.value
                        num?.let {
                            provider.push(
                                when {
                                    text.startsWith("下载") -> State(0, num.toInt())
                                    else -> State(1, num.toInt())
                                }
                            )
                        }
                    }
                }
            }
        }


    private suspend fun getId(event: MessageEvent): String? {
        return when (event) {
            is GroupMessageEvent -> "music${event.group().id}${event.author().id}"
            is FriendMessageEvent -> "music${event.friend().id}"
            else -> null
        }
    }


    private fun userLogin() {
        val loginState = Cookie().loginState
        if (loginState.cookies["skey"] != null) {
            Sender.sendPrivateMsg(RobotCore.ADMINISTRATOR[0], "登录成功~")
            GlobalVariable.SKey = loginState.cookies["skey"]!!
            log.info("获取Skey成功 Skey:${GlobalVariable.SKey}")
        }
    }
}

/**
 * 点歌状态
 * @property state Int 0是下载，1是播放
 * @property index Int  索引
 * @constructor
 */
data class State(val state: Int, val index: Int)