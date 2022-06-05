package com.huahua.robot.music

import com.google.gson.Gson
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.core.common.Sender.Companion.sendPrivateMsg
import com.huahua.robot.core.common.send
import com.huahua.robot.core.common.sendAndWait
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
import love.forte.simbot.LoggerFactory
import love.forte.simbot.event.ContinuousSessionContext
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.event.MessageEvent
import net.mamoe.mirai.message.data.MusicKind
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import kotlin.reflect.jvm.jvmName

/**
 * ClassName: MusicListener
 * @description
 * @author 花云端
 * @date 2022-05-18 16:43
 */
@Component
class MusicListener {

    private val log = LoggerFactory.getLogger(MusicListener::class.jvmName) // 获取日志记录器
    private val listTip = "---------------------\n" +
            "请输入序号or播放+序号进行播放\n" +
            "或者输入下载+序号获取下载链接\n" +
            "---------------------\n"   // 列表提示
    private val downloadTip = "下载地址（复制到浏览器下载）：\n"   // 下载提示

    @RobotListen(desc = "登录", isBoot = true, permission = RobotPermission.ADMINISTRATOR)
    @Filter("登录")
    suspend fun GroupMessageEvent.login() {
        userLogin() // 登录
    }

    @OptIn(ExperimentalSimbotApi::class)
    @RobotListen(desc = "点歌", isBoot = true)
    @Filter("点歌{{name}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.music(
        @FilterValue("name") name: String,  // 歌曲名称
        session: ContinuousSessionContext,  // 可以获取到上一次的消息
    ) {
        if (GlobalVariable.SKey.isEmpty()) {    // 如果没有登录，则先登录
            group().send("正在登录中~请稍后")   // 发送消息
            userLogin() // 登录
        }
        val qqMusicState = qqMusic(GlobalVariable.MusicJump, name, this) //是否有匹配歌曲
        if (!qqMusicState) {    // 如果没有匹配歌曲
            group().send("QQ音乐未搜索到结果，正在为你跳转至网易云")   // 发送消息
            neteaseMusic(name, this)    // 网易云音乐
        }
    }


    @OptIn(ExperimentalSimbotApi::class)
    @RobotListen(desc = "指定点歌", isBoot = true)
    @Filter("{{channel}}点歌{{name}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.music(
        @FilterValue("channel") channel: String,    // 渠道
        @FilterValue("name") name: String,  // 歌曲名称
        session: ContinuousSessionContext,  // 可以获取到上一次的消息
    ) {
        //判断channel是否为空
        if (channel.isNotEmpty()) { //如果不为空
            //指定点歌
            when (channel) {
                "网易云", "网抑云", "网易" -> neteaseMusic(name, this)  // 网易云音乐
                "QQ", "qq", "qq音乐", "QQ音乐" -> { // QQ音乐
                    if (GlobalVariable.SKey.isEmpty()) {    // 如果没有登录，则先登录
                        group().send("正在登录中~请稍后")   // 发送消息
                        userLogin() // 登录
                    }
                    qqMusic(false, name, this)  // QQ音乐
                }
                else -> return  // 如果没有匹配到渠道，则返回
            }
        }

    }

    /**
     * 是否需要跳转
     * @receiver GroupMessageEvent   群聊事件
     * @param state String   状态
     */
    @RobotListen(desc = "是否需要跳转", isBoot = true, permission = RobotPermission.ADMINISTRATOR)
    @Filter("{{state}}自动跳转", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.setMusic(@FilterValue("state") state: String) {
        if (state == "开启" || state == "设置") {   // 如果开启跳转
            GlobalVariable.MusicJump = true // 设置跳转为开启
            group().send("自动跳转已开启")  // 发送消息
            return  // 返回
        }
        if (state == "关闭" || state == "取消") {   // 如果关闭跳转
            GlobalVariable.MusicJump = false    // 设置跳转为关闭
            group().send("自动跳转已关闭") // 发送消息
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
    private suspend fun MessageEvent.qqMusic(
        listIsContainsName: Boolean,
        name: String,
        event: GroupMessageEvent,
    ): Boolean {
        val group = event.group()   // 获取群聊
        var url =
            "https://api.klizi.cn/API/music/vipqqyy.php?msg=${name}&uin=1849950046&skey=${GlobalVariable.SKey}"
        val musicList = HttpUtil.getBody(url)   // 获取歌曲列表
        if (listIsContainsName) {   // 判断是否需要跳转
            if (!musicList.contains(name.split(" ")[0])) {    // 判断列表是否包含歌名
                return false    // 无结果返回false
            }
        }
        if (musicList.isEmpty()) { // 如果歌曲列表为空
            send("QQ音乐搜索失败") // 发送消息
        }
        val pattern = """^(?:下载|播放)?\s*(\d*)$"""    // 正则表达式
        val text = sendAndWait("${musicList}${listTip}", 30, TimeUnit.SECONDS, Regex(pattern))?.plainText   // 发送消息并等待回复
        text?.let { // 如果不为空
            Regex(pattern).find(text)?.groups?.get(1)?.value?.let { // 如果不为空
                var music = Gson().fromJson(HttpUtil.getBody("${url}&n=${it.toInt()}"), QQMusic::class.java).data   // 获取歌曲
                if (music.music.isEmpty()) {    // 如果歌曲为空
                    group.send("skey失效，请重新登录")  // 发送消息
                    userLogin() // 重新登录
                    url =
                        "https://api.klizi.cn/API/music/vipqqyy.php?msg=${name}&uin=1849950046&skey=${GlobalVariable.SKey}" // 重新获取url
                    music = HttpUtil.getJsonClassFromUrl("${url}&n=${it.toInt()}", QQMusic::class.java).data    // 获取歌曲
                }
                when {
                    text.startsWith("下载") -> send("${downloadTip}${music.music}")   // 发送下载提示
                    else -> {   // 如果不是下载
                        val share = MessageUtil.getMusicShare(music, MusicKind.QQMusic) // 获取分享卡片
                        send(share) // 发送卡片
                    }

                }
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
    private suspend fun MessageEvent.neteaseMusic(
        name: String,
        event: GroupMessageEvent,
    ): Boolean {
        val group = event.group()
        val url = "https://api.klizi.cn/API/music/netease.php?msg=$name"
        val response = HttpUtil.getBody(url)    // 获取歌曲列表
        val musicList = response.substring(0, response.length - 12).trim()  // 获取歌曲列表
        if (musicList.isEmpty()||musicList==" ") {   // 如果歌曲列表为空
            group.send("没有找到相关歌曲") // 发送消息
        }
        val pattern = """^(?:下载|播放)?\s*(\d*)$"""    // 正则表达式
        val text = sendAndWait("${musicList}${listTip}", 30, TimeUnit.SECONDS, Regex(pattern))?.plainText   // 发送消息并等待回复
        text?.let {
            Regex(pattern).find(text)?.groups?.get(1)?.value?.let { // 如果不为空
                val music = HttpUtil.getJsonClassFromUrl("${url}&n=${it.toInt()}", NeteaseMusic::class.java)    // 获取歌曲
                when {
                    text.startsWith("下载") -> send("${downloadTip}${music.url}")  // 发送下载提示
                    else -> {   // 如果不是下载
                        val share = MessageUtil.getMusicShare(music) // 获取分享卡片
                        send(share) // 发送卡片
                    }

                }
            }
        }
        return true // 暂时没用
    }

    /**
     * 登录
     */
    private fun userLogin() {
        val loginState = Cookie().loginState    // 获取登录状态
        if (loginState.cookies["skey"] != null) {   // 如果有skey
            sendPrivateMsg(RobotCore.ADMINISTRATOR[0], "登录成功~") // 发送消息
            GlobalVariable.SKey = loginState.cookies["skey"]!!      // 设置skey
            log.info("获取Skey成功 Skey:${GlobalVariable.SKey}")    // 打印日志
        }
    }
}