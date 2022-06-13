package com.huahua.robot.listener

import com.google.gson.Gson
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.*
import com.huahua.robot.entity.Chat
import com.huahua.robot.entity.Setu
import com.huahua.robot.entity.Tuizi
import com.huahua.robot.entity.setu.SetuIcon
import com.huahua.robot.utils.GlobalVariable
import com.huahua.robot.utils.HttpUtil
import com.huahua.robot.utils.PermissionUtil.Companion.botCompareToAuthor
import com.huahua.robot.utils.UrlUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.TargetFilter
import love.forte.simboot.filter.MatchType
import love.forte.simbot.LoggerFactory
import love.forte.simbot.action.sendIfSupport
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.event.MessageEvent
import love.forte.simbot.message.*
import love.forte.simbot.resources.FileResource
import love.forte.simbot.resources.URLResource
import org.springframework.stereotype.Component
import java.io.File
import java.net.URL
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.jvm.jvmName
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
        val imgUrl = HttpUtil.getJsonClassFromUrl(url, Setu::class.java).url    // 获取图片地址
        val image: Image<*> = bot.uploadImage(FileResource(File(imgUrl)))   // 上传图片
        if (this is GroupMessageEvent && botCompareToAuthor()) {  // 判断bot是否有操作权限
            author().mute((5).minutes)  // 禁言5分钟
            delete()   // 撤回消息
        }
        val flag = sendIfSupport(image)  // 发送图片并获取标记
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
        val img: Image<*> = bot.uploadImage(URLResource(URL(imgUrl2)))  // 上传图片
        val receipt = send(img) // 发送图片并获取标记
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
        val response = HttpUtil.getResponse(url)    // 获取响应体
        val dir = File("${GlobalVariable.botTemp}\\image")  // 获取临时文件夹
        if (!dir.exists()) {    // 判断文件夹是否存在
            log.info("目录创建成功")  // 打印日志
            dir.mkdirs()    // 创建文件夹
        }
        val imgFile = File("${dir.absolutePath}\\girl.jpg") // 获取图片文件
        imgFile.writeBytes(response.body()?.bytes()!!)  //将图片写入本地文件
        val img = bot.uploadImage(FileResource(File(imgFile.absolutePath))) // 上传文件
        val receipt = send(img) // 发送图片并获取标记
        bot.launch {    // 启动协程
            receipt?.let {
                if (it.isSuccess) {  // 判断发送是否成功
                    delay(60000)    // 延迟一分钟
                    it.deleteIfSupport()  // 通过标记撤回此图片
                } else null

            }.isNull {
                Sender.sendAdminMsg("${getId()}发送消息[${img}]失败")

            }
        }
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
        val stringBuilder = "标题: ${data[0].title}\n链接: ${data[0].url}"  // 拼接消息内容
        val flag = send(stringBuilder)  // 发送消息并获取标记
        bot.launch {    // 启动协程
            delay(30000)    // 延迟30秒
            flag?.let {
                if (it.isSuccess) {  // 判断发送是否成功
                    it.deleteIfSupport()  // 通过标记撤回此图片
                } else null
            }.isNull {
                Sender.sendAdminMsg("${getId()}发送消息[${stringBuilder}]失败")
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
                if (it.code == 429) {  // 判断涩图状态码是否为429
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
        send(bot.uploadImage(URLResource(URL(tui))))    // 发送图片
    }

    /**
     * 复读机
     * @receiver MessageEvent
     */
    @RobotListen(isBoot = true, desc = "复读机")
    suspend fun MessageEvent.repeat() {
        val msg = messageContent.plainText.trim()  // 获取消息内容
        Regex("^[\u4e00-\u9fa5]{0,}$").find(msg)?.value?.also {   // 判断是否为空
            if (msg.length in 3..4 &&
                it.length in 3..4 &&
                msg.contains("人")
            ) {    // 判断消息内容长度是否在3到4个字符且包含人
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
            send(   // 发送消息
                bot.uploadImage(    // 上传图片
                    URLResource(    // 获取图片资源
                        URL(    // 获取图片链接
                            "https://api.klizi.cn/API/tw/source.php?text=${
                                it.substring(2)
                            }"  // 拼接图片链接
                        )
                    )
                )
            )
        }.isNull {
            return
        }
    }


    @RobotListen(desc = "搜图", isBoot = true)
    @Filter("搜图", matchType = MatchType.TEXT_CONTAINS)
    suspend fun MessageEvent.searchMap() {
        val url = "https://yandex.com/images/search?family=yes&rpt=imageview&url="
        for (message in messageContent.messages) {  // 遍历消息内容
            if (message is Image) {   // 判断消息内容是否为图片消息
                val imgUrl = message.resource().name    // 获取图片链接
                send(url + UrlUtil.encode(imgUrl))   // 发送图片链接
                return  // 跳出方法
            }
        }
        val pic = sendAndWait("请发送图片...", 30, TimeUnit.SECONDS) // 获取图片
        if (pic is Image<*>) {   // 判断图片是否为图片消息
            val imgUrl = pic.resource().name   // 获取图片链接
            send(url + URLEncoder.encode(imgUrl))   // 发送图片链接
            return  // 跳出方法
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
        if (msg.isEmpty()) {    // 判断消息内容是否为空
            return  // 跳出方法
        }
        val url = "http://ruohuan.xiaoapi.cn/API/other/xiaoai.php?msg=$msg" // 获取接口链接
        val reply = HttpUtil.getJsonClassFromUrl(url, Chat::class.java).text    // 获取回复内容

        when (this) {
            is GroupMessageEvent -> {
                if (reply.isEmpty()) {
                    send(At(author().id) + " ${msg}?".toText())
                    return
                }
                send(At(author().id) + " $reply".toText())  // 发送消息
            }  // 发送消息
            is FriendMessageEvent -> {
                if (reply.isEmpty()) {
                    send("喵喵喵？")
                    return
                }
                send(reply)  // 发送消息
            }  // 发送消息

        }
    }
}
