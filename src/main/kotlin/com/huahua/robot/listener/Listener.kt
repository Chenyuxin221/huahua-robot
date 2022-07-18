@file:Suppress("UNUSED_VARIABLE", "unused")

package com.huahua.robot.listener

import com.google.gson.Gson
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.*
import com.huahua.robot.entity.Chat
import com.huahua.robot.entity.Setu
import com.huahua.robot.entity.Tuizi
import com.huahua.robot.entity.setu.SetuIcon
import com.huahua.robot.utils.FileUtil.getTempImage
import com.huahua.robot.utils.FileUtil.toFile
import com.huahua.robot.utils.FileUtil.url
import com.huahua.robot.utils.HttpUtil
import com.huahua.robot.utils.MessageUtil.Companion.getImageMessage
import com.huahua.robot.utils.PermissionUtil.Companion.botCompareToAuthor
import com.huahua.robot.utils.UrlUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.TargetFilter
import love.forte.simboot.filter.MatchType
import love.forte.simbot.ID
import love.forte.simbot.LoggerFactory
import love.forte.simbot.action.sendIfSupport
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.event.MessageEvent
import love.forte.simbot.message.*
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.jvm.jvmName
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.minutes


@Component
@SuppressWarnings("all")
class Listener {
    private val log = LoggerFactory.getLogger(Listener::class.jvmName) // 日志

    /**
     * 菜单||项目文档||项目地址
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "菜单服务")
    @Filter("\\.h|\\.help", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.menu() {
        val functionMenuUrl = "https://www.yuque.com/qingsi-zwnmu/xyuvvi/wrbzgy"    // 项目文档
        val gitHubUrl = "https://github.com/Chenyuxin221/huahua-robot"           // 项目地址
        val result = "功能菜单：${functionMenuUrl}\n项目地址：${gitHubUrl}"         // 菜单内容
        send(result)                                              // 发送菜单
    }


    /**
     * 涩图功能
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "写真服务")
    @Filter(value = "来点好看的|来点好康的", matchType = MatchType.REGEX_CONTAINS)
    suspend fun MessageEvent.setu() {
        val url = "http://localhost:8080/api/photo"
        val imgPath = HttpUtil.getJsonClassFromUrl(url, Setu::class.java).url    // 获取图片地址
        if (this is GroupMessageEvent && botCompareToAuthor()) {  // 判断bot是否有操作权限
            author().mute((5).minutes)  // 禁言5分钟
            delete()   // 撤回消息
        }
        val flag = sendIfSupport(imgPath.toFile().getImageMessage())  // 发送图片并获取标记
        bot.launch {    // 启动协程
            if (flag != null && flag.isSuccess) {   // 判断发送是否成功
                delay(60000)    // 延迟一分钟
                flag.deleteIfSupport()  // 通过标记撤回此图片
            }
        }
    }

    /**
     * 淘宝买家秀
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "买家秀")
    @Filter(value = "买家秀", matchType = MatchType.TEXT_EQUALS)
    suspend fun MessageEvent.buyerShow() {
        val imgUrl = "https://api.vvhan.com/api/tao"
        val imgUrl2 = "https://api.uomg.com/api/rand.img3"
        val receipt = send(imgUrl2.getImageMessage()) // 发送图片并获取标记
        if (this is GroupMessageEvent && botCompareToAuthor()) { //判断是否有操作权限
            author().mute((5).minutes)  // 禁言5分钟
            delete()   // 撤回消息
        }
        bot.launch {    // 启动协程
            receipt?.let {
                if (it.isSuccess) {  // 判断发送是否成功
                    delay(60000)    // 延迟一分钟
                    it.deleteIfSupport()  // 通过标记撤回此图片
                }
            }.isNull {
                send("发送失败")
            }
        }

    }

    //待修改 需要下载本地asd
    /**
     * 美女图
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "养眼的服务")
    @Filter(value = "来点妹子", matchType = MatchType.TEXT_EQUALS)
    suspend fun MessageEvent.showGirls() {
        val url = "https://api.iyk0.com/sjmn"
        val imgFile = "girl.jpg".getTempImage(url.url()) // 获取图片文件
        imgFile?.also { file ->
            val r = send(file.getImageMessage())
            bot.launch {
                r?.also {
                    it.isSuccess.then {
                        delay(60000)
                        it.deleteIfSupport()
                    }
                }.isNull {
                    Sender.sendAdminMsg("${getId()}发送消息[${file.absolutePath}]失败")
                }
            }
        }.isNull {
            send("图片获取失败，请稍后再试")
        }?.delete()
    }

    /**
     * （真）涩图功能
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "涩图服务")
    @Filter(value = "来份涩图|来份r18涩图", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.sendSetu() {
        val msg = messageContent.plainText  // 获取消息内容
        val r18: Boolean = msg.lowercase(Locale.getDefault()).contains("r18")   // 判断是否包含r18
        val setu: SetuIcon = getJson(if (r18) "1" else "0") ?: return   // 获取涩图实体
        val data = setu.data    // 获取涩图数据
        val str = "标题: ${data[0].title}\n链接: ${data[0].url}"  // 拼接消息内容
        val flag = send(str)  // 发送消息并获取标记
        bot.launch {    // 启动协程
            delay(30000)    // 延迟30秒
            flag?.also {
                it.isSuccess.then { it.deleteIfSupport() }
            }.isNull {
                Sender.sendAdminMsg("${getId()}发送消息[${str}]失败")
            }
        }

    }

    private fun getJson(r18: String): SetuIcon? {
        var setu: SetuIcon? = null  // 初始化涩图实体
        try {   // 尝试
            val key1 = "820458705ebe071883b3c2" // 获取涩图key
            val key2 = "198111555ec3242d2c6b42" // 获取涩图key2
            var web: String =
                HttpUtil.get("http://api.lolicon.app/setu?apikey=${key1}&siez1200=true&r18=$r18").response // 获取涩图
            setu = Gson().fromJson(web, SetuIcon::class.java)   // 解析涩图
            setu?.also {
                (it.code == 429).then {  // 判断涩图状态码是否为429
                    web = HttpUtil.get(
                        "http://api.lolicon.app/setu?apikey=${key1}&siez1200=true&r18=${
                            r18
                        }"  // 换key获取涩图
                    ).response  // 获取涩图
                    setu = Gson().fromJson(web, SetuIcon::class.java)   // 解析返回的Json存入实体
                }
                log.info("获取到涩图: ${setu.toString()}")   // 打印日志
                return setu
            }.isNull {
                log.info("获取涩图失败")   // 打印日志
                return null
            }
        } catch (e: java.lang.Exception) {  // 异常处理
            e.printStackTrace() // 打印异常信息
        }
        return setu

    }


    /**
     * 娱乐区
     */

    /**
     * 看看腿
     * @receiver MessageEvent
     */
    @RobotListen(isBoot = true, desc = "图片服务")
    @Filter(value = "看看腿|来点腿子", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.lookAtTheLegs() {
        val url = "http://ovooa.com/API/meizi/"
        val tui = HttpUtil.getJsonClassFromUrl(url, Tuizi::class.java).text // 获取腿实体类
        println(tui)
        send(tui.getImageMessage())    // 发送图片
    }

    /**
     * 复读机
     * @receiver MessageEvent
     */
    @RobotListen(isBoot = true, desc = "复读机")
    suspend fun MessageEvent.repeat() {
        val msg = messageContent.plainText.trim()  // 获取消息内容
        Regex("^[\u4e00-\u9fa5]{0,}$").find(msg)?.value?.also {   // 判断是否为空
            (msg.length in 3..4 && it.length in 3..4 && msg.contains("人"))
                .then {    // 判断消息内容长度是否在3到4个字符且包含人
                    val chars = msg.toCharArray().joinToString("  ")    // 将消息内容转换成字符数组并使用空格分隔
                    send(chars)   // 发送消息
                }
        }.isNull {  // 如果为空
            return  // 返回
        }
    }

    /**
     * 举牌
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "作图服务")
    suspend fun MessageEvent.fakePictures() {
        val msg = messageContent.plainText.trim()   // 获取消息内容
        Regex("^举牌.*").find(msg)?.value?.also {
            send("https://api.klizi.cn/API/tw/source.php?text=${it.substring(2)}".getImageMessage())
        }.isNull {
            return
        }
    }


    @RobotListen(desc = "搜图", isBoot = true)
    @Filter("搜图", matchType = MatchType.TEXT_CONTAINS)
    suspend fun MessageEvent.searchMap() {
        val url = "https://yandex.com/images/search?family=yes&rpt=imageview&url="
        messageContent.messages.forEach {
            (it is Image).then {   // 判断消息内容是否为图片消息
                val imgUrl = (it as Image).resource().name    // 获取图片链接
                send(url + UrlUtil.encode(imgUrl))   // 发送图片链接
                return  // 跳出方法
            }
        }
        val pic = sendAndWait("请发送图片...", 30, TimeUnit.SECONDS) // 获取图片
        pic?.messages?.first()?.also {
            (it is Image).then {
                val imgUrl = (it as Image).resource().name   // 获取图片链接
                send(url + UrlUtil.encode(imgUrl))   // 发送图片链接
                return  // 跳出方法
            }
        }.isNull {
            return
        }
    }

    @RobotListen(desc = "结束程序")
    @Filter("退出", matchType = MatchType.TEXT_CONTAINS)
    suspend fun MessageEvent.exit() {
        when (this) {
            is GroupMessageEvent -> {
                if (author().id == RobotCore.ADMINISTRATOR.ID) {
                    send("5s后结束程序...")
                    delay(5000)
                    exitProcess(0)
                }
            }
            is FriendMessageEvent -> {
                if (friend().id == RobotCore.ADMINISTRATOR.ID) {
                    send("5s后结束程序...")
                    delay(5000)
                    exitProcess(0)
                }
            }
        }
    }

    /**
     * 聊天
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "陪聊服务")
    @Filter(target = TargetFilter(atBot = true))
    suspend fun MessageEvent.chat() {
        val msg = messageContent.plainText.trim()   // 获取消息内容
        msg.isEmpty().then {    // 判断消息内容是否为空
            return  // 跳出方法
        }
        val url = "http://ruohuan.xiaoapi.cn/API/other/xiaoai.php?msg=$msg" // 获取接口链接
        val reply = HttpUtil.getJsonClassFromUrl(url, Chat::class.java).text    // 获取回复内容
        when (this) {
            is GroupMessageEvent -> {
                RobotCore.HaveReplied[author().id] ?: return
                (reply.isEmpty()).then {
                    (group().id == "1043409458".ID).then {
                        send(At("2984131619".ID) + " $msg".toText())
                    }.onElse {
                        send(At(author().id) + " $msg".toText())
                    }
                    return
                }
                send(At(author().id) + " $reply".toText())  // 发送消息
            }  // 发送消息
            is FriendMessageEvent -> {
                (reply.isEmpty()).then {
                    send("喵喵喵？")
                    return
                }
                send(reply)  // 发送消息
            }  // 发送消息

        }
    }
}
