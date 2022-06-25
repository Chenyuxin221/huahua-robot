package com.huahua.robot.music

import com.google.gson.Gson
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.core.common.isNull
import com.huahua.robot.core.common.send
import com.huahua.robot.core.common.sendAndWait
import com.huahua.robot.core.enums.RobotPermission
import com.huahua.robot.music.entity.kugoumusic.list.MusicList
import com.huahua.robot.music.entity.kugoumusic.music.KugouMusic
import com.huahua.robot.music.entity.neteasemusic.NeteaseMusic
import com.huahua.robot.music.entity.qqmusic.QQMusic
import com.huahua.robot.music.util.Cookie
import com.huahua.robot.utils.HttpUtil
import com.huahua.robot.utils.MessageUtil.Companion.getKugouMusicShare
import com.huahua.robot.utils.MessageUtil.Companion.getNeteaseCloudMusicShare
import com.huahua.robot.utils.MessageUtil.Companion.getQQMusicShare
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.LoggerFactory
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.event.MessageEvent
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
    private val listTip = "\n---------------------\n" +
            "请输入序号or播放+序号进行播放\n" +
            "或者输入下载+序号获取下载链接\n" +
            "---------------------\n"   // 列表提示
    private val downloadTip = "下载地址（复制到浏览器下载）：\n"   // 下载提示
    private val uin = RobotCore.ADMINISTRATOR

    /**
     *  登录QQ音乐
     * @receiver GroupMessageEvent  事件
     */
    @RobotListen(desc = "登录", isBoot = true, permission = RobotPermission.ADMINISTRATOR)
    @Filter("登录")
    suspend fun GroupMessageEvent.login() {
        userLogin() // 登录
    }

    /**
     * 通用点歌
     * @receiver MessageEvent   事件
     * @param name String   名称
     */
    @RobotListen(desc = "点歌", isBoot = true)
    @Filter("点歌{{name}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.music(
        @FilterValue("name") name: String,  // 歌曲名称
    ) {
        val qqMusicState = qqMusic(RobotCore.MusicJump, name) //是否有匹配歌曲
        if (!qqMusicState) {    // 如果没有匹配歌曲
            send("QQ音乐未搜索到结果，正在为你跳转至网易云")   // 发送消息
            neteaseMusic(name)    // 网易云音乐
        }
    }

    /**
     *  指定点歌
     * @receiver MessageEvent   事件
     * @param kind String    音乐类型
     * @param name String    歌曲名称
     */
    @RobotListen(desc = "指定点歌", isBoot = true)
    @Filter("{{kind}}点歌{{name}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.music(
        @FilterValue("kind") kind: String,    // 渠道
        @FilterValue("name") name: String,  // 歌曲名称
    ) {
        //判断channel是否为空
        if (kind.isNotEmpty()) { //如果不为空
            //指定点歌
            when (kind) {
                "网易云", "网抑云", "网易" -> neteaseMusic(name)  // 网易云音乐
                "QQ", "qq", "qq音乐", "QQ音乐" -> { // QQ音乐
                    if (RobotCore.Skey.isEmpty()) {    // 如果没有登录，则先登录
                        send("正在登录中~请稍后")   // 发送消息
                        userLogin() // 登录
                    }
                    qqMusic(false, name)  // QQ音乐
                }
                "酷狗","kugou","kg","KG" -> kugouMusic(name)
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
            RobotCore.MusicJump = true // 设置跳转为开启
            send("自动跳转已开启")  // 发送消息
            return  // 返回
        }
        if (state == "关闭" || state == "取消") {   // 如果关闭跳转
            RobotCore.MusicJump = false    // 设置跳转为关闭
            send("自动跳转已关闭") // 发送消息
        }
    }


    /**
     *  QQ音乐点歌
     * @receiver MessageEvent   群聊事件
     * @param listIsContainsName Boolean    是否包含歌曲名称
     * @param name String   歌曲名称
     * @return Boolean  是否匹配到歌曲
     */
    private suspend fun MessageEvent.qqMusic(
        listIsContainsName: Boolean,
        name: String,
    ): Boolean {
        var url =
            "https://api.klizi.cn/API/music/vipqqyy.php?msg=${name}&uin=${uin}&skey=${RobotCore.Skey}"
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
        text?.also { // 如果不为空
            Regex(pattern).find(text)?.groups?.get(1)?.value?.let { // 如果不为空
                var music = Gson().fromJson(HttpUtil.getBody("${url}&n=${it.toInt()}"), QQMusic::class.java).data   // 获取歌曲
                if (music.music.isEmpty()) {    // 如果歌曲为空
                    send("skey失效，请重新登录")  // 发送消息
                    userLogin() // 重新登录
                    url =
                        "https://api.klizi.cn/API/music/vipqqyy.php?msg=${name}&uin=${uin}&skey=${RobotCore.Skey}" // 重新获取url
                    music = HttpUtil.getJsonClassFromUrl("${url}&n=${it.toInt()}", QQMusic::class.java).data    // 获取歌曲
                }
                when {
                    text.startsWith("下载") -> send("${downloadTip}${music.music}")   // 发送下载提示
                    else -> {   // 如果不是下载
                        val share = getQQMusicShare(
                            music.song,
                            music.singer,
                            music.url,
                            music.picture,
                            music.music
                        ) // 获取分享卡片
                        send(share) // 发送卡片
                    }

                }
            }
            return true
        }.isNull {
            send("哎呀，超时啦")
        }
        return false
    }

    /**
     * 网易云音乐功能
     * @receiver MessageEvent   消息事件
     * @param name String   搜索的名字
     * @return Boolean  没用
     */
    private suspend fun MessageEvent.neteaseMusic(
        name: String
    ): Boolean {

        val url = "https://api.klizi.cn/API/music/netease.php?msg=$name"
        val response = HttpUtil.getBody(url)    // 获取歌曲列表
        val musicList = response.substring(0, response.length - 12).trim()  // 获取歌曲列表
        if (musicList.isEmpty()||musicList==" ") {   // 如果歌曲列表为空
            send("没有找到相关歌曲") // 发送消息
        }
        val pattern = """^(?:下载|播放)?\s*(\d*)$"""    // 正则表达式
        val text = sendAndWait("${musicList}${listTip}", 30, TimeUnit.SECONDS, Regex(pattern))?.plainText   // 发送消息并等待回复
        text?.also {
            Regex(pattern).find(text)?.groups?.get(1)?.value?.let { // 如果不为空
                val music = HttpUtil.getJsonClassFromUrl("${url}&n=${it.toInt()}", NeteaseMusic::class.java)    // 获取歌曲
                when {
                    text.startsWith("下载") -> send("${downloadTip}${music.url}")  // 发送下载提示
                    else -> {   // 如果不是下载
                        val share = getNeteaseCloudMusicShare(
                            music.song,
                            music.singer,
                            "https://music.163.com/#/song?id=${music.url.split("=")[1]}",
                            music.img,
                            music.url
                        ) // 获取分享卡片
                        send(share) // 发送卡片
                    }

                }
            }
        }.isNull {
            send("哎呀，超时啦")
        }
        return true // 暂时没用
    }

    private suspend fun MessageEvent.kugouMusic(name: String){
        val url = "https://ovooa.com/API/kgdg/api.php?msg=$name"
        val list = HttpUtil.getJsonClassFromUrl(url,MusicList::class.java)
        if (list.code != 1){
            send(list.text)
            return
        }
        val ls= "${list.data.joinToString("\n") { "${it.name} --${it.singer}" }}\n$listTip"
        val pattern = """^(?:下载|播放)?\s*(\d*)$"""
        val text = sendAndWait(ls,30,TimeUnit.SECONDS,Regex(pattern))?.plainText
        text?.also {
            Regex(pattern).find(text)?.groups?.get(1)?.value?.let {
                val music = HttpUtil.getJsonClassFromUrl("${url}&n=${it.toInt()}",KugouMusic::class.java)
                when {
                    text.startsWith("下载") -> send("${downloadTip}${music.data.url}")
                    else -> {
                        val share = getKugouMusicShare(
                            music.data.song,
                            music.data.singer,
                            music.data.Music_Url,
                            music.data.cover,
                            music.data.url
                        )
                        send(share)
                    }
                }
            }
        }.isNull {
            send("哎呀，超时啦")
        }
    }

    /**
     * QQ音乐登录
     */
    private suspend fun MessageEvent.userLogin() {
        val loginState = Cookie().loginState    // 获取登录状态
        if (loginState.cookies["skey"] != null) {   // 如果有skey
            send( "登录成功~") // 发送消息
            RobotCore.Skey = loginState.cookies["skey"]!!      // 设置skey
            log.info("获取Skey成功 Skey:${RobotCore.Skey}")    // 打印日志
        }
    }
}