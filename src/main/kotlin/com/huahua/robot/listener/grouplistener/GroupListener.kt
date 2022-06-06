package com.huahua.robot.listener.grouplistener

import com.google.gson.Gson
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.send
import com.huahua.robot.core.common.sendAndWait
import com.huahua.robot.core.enums.RobotPermission
import com.huahua.robot.entity.Chat
import com.huahua.robot.entity.LuckyTime
import com.huahua.robot.entity.Setu
import com.huahua.robot.entity.Tuizi
import com.huahua.robot.entity.setu.SetuIcon
import com.huahua.robot.utils.GlobalVariable
import com.huahua.robot.utils.HttpUtil
import com.huahua.robot.utils.Permission
import com.huahua.robot.utils.PermissionUtil.Companion.botCompareToAuthor
import com.huahua.robot.utils.PermissionUtil.Companion.botPermission
import com.huahua.robot.utils.UrlUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.TargetFilter
import love.forte.simboot.filter.MatchType
import love.forte.simbot.ExperimentalSimbotApi
import love.forte.simbot.LoggerFactory
import love.forte.simbot.event.ContinuousSessionContext
import love.forte.simbot.event.GroupMessageEvent
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
class GroupListener {
    private val lotteryPrefix: List<String> = listOf("chou", "cou", "c", "抽", "操", "艹", "草")    //抽奖前缀
    private val lotterySuffix: List<String> = listOf("jiang", "j", "奖", "wo", "w", "我") // 抽奖后缀
    private val log = LoggerFactory.getLogger(GroupListener::class.jvmName) // 日志

    /**
     * 菜单||项目文档||项目地址
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "菜单服务")
    @Filter(".h|.help", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.menu() {
        val functionMenuUrl = "https://www.yuque.com/qingsi-zwnmu/xyuvvi/wrbzgy"    // 项目文档
        val gitHubUrl = "https://github.com/Chenyuxin221/huahua-robot"           // 项目地址
        val result = "功能菜单：${functionMenuUrl}\n项目地址：${gitHubUrl}"         // 菜单内容
        group().send(result)                                              // 发送菜单
    }


    /**
     * 涩图功能
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "写真服务")
    @Filter(value = "来点好看的|来点好康的", matchType = MatchType.REGEX_CONTAINS)
    suspend fun GroupMessageEvent.setu() {
        val url = "http://localhost:8080/api/photo"
        val imgUrl = HttpUtil.getJsonClassFromUrl(url, Setu::class.java).url    // 获取图片地址
        val image: Image<*> = bot.uploadImage(FileResource(File(imgUrl)))   // 上传图片
        if (botCompareToAuthor()) {  // 判断bot是否有操作权限
            author().mute((5).minutes)  // 禁言5分钟
            delete()   // 撤回消息
        }
        val flag = group().send(image)  // 发送图片并获取标记
        bot.launch {    // 启动协程
            if (flag.isSuccess) {   // 判断发送是否成功
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
    suspend fun GroupMessageEvent.buyerShow() {
        val imgUrl = "https://api.vvhan.com/api/tao"
        val imgUrl2 = "https://api.uomg.com/api/rand.img3"
        val img: Image<*> = bot.uploadImage(URLResource(URL(imgUrl2)))  // 上传图片
        val receipt: MessageReceipt = group().send(img) // 发送图片并获取标记
        if (botCompareToAuthor()) { //判断是否有操作权限
            author().mute((5).minutes)  // 禁言5分钟
            delete()   // 撤回消息
        }
        bot.launch {    // 启动协程
            if (receipt.isSuccess) {    // 判断发送是否成功
                delay(6000)   // 延迟一分钟
                receipt.deleteIfSupport()   // 通过标记撤回此图片
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
    suspend fun GroupMessageEvent.showGirls() {
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
        val receipt = group().send(img) // 发送图片并获取标记
        bot.launch {    // 启动协程
            if (receipt.isSuccess) {    // 判断发送是否成功
                delay(60000)    // 延迟一分钟
                receipt.deleteIfSupport()   // 通过标记撤回此图片
            }
        }
    }

    /**
     * （真）涩图功能
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "涩图服务")
    @Filter(value = "来份涩图|来份r18涩图", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.sendSetu() {
        val msg = messageContent.plainText  // 获取消息内容
        val r18: Boolean = msg.lowercase(Locale.getDefault()).contains("r18")   // 判断是否包含r18
        val setu: SetuIcon = getJson(if (r18) "1" else "0") ?: return   // 获取涩图实体
        val data = setu.data    // 获取涩图数据
        val stringBuilder = "标题: ${data[0].title}\n链接: ${data[0].url}"  // 拼接消息内容
        val flag = group().send(stringBuilder)  // 发送消息并获取标记
        try {   // 尝试
            if (botCompareToAuthor()) {   // 判断是否有操作权限
                author().mute((300000).minutes) // 禁言5分钟
                delete() // 撤回消息
            }
        } catch (e: java.lang.Exception) {  // 异常处理
            e.message   // 打印异常信息
        } finally {  // 最终
            bot.launch {    // 启动协程
                delay(30000)    // 延迟30秒
                if (flag.isSuccess) {   // 判断发送是否成功
                    flag.deleteIfSupport()  // 通过标记撤回此图片
                }
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
            if (setu != null) {    // 判断涩图是否为空
                if (setu.code == 429) {  // 判断涩图状态码是否为429
                    web = HttpUtil.get(
                        "http://api.lolicon.app/setu?apikey=${key1}&siez1200=true&r18=${
                            r18
                        }"  // 换key获取涩图
                    ).response  // 获取涩图
                    setu = Gson().fromJson(web, SetuIcon::class.java)   // 解析返回的Json存入实体
                }
            }
        } catch (e: java.lang.Exception) {  // 异常处理
            e.printStackTrace() // 打印异常信息
        }
        if (setu != null) { // 判断涩图是否为空
            log.info("获取到涩图: ${setu.toString()}")   // 打印日志
        }
        return setu // 返回涩图实体
    }


    /**
     * 抽奖
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "抽奖服务", permission = RobotPermission.MEMBER)
    suspend fun GroupMessageEvent.luckDraw() {
        val msg = messageContent.plainText  // 获取消息内容
        val lucky = lucky(60)   // 获取抽奖结果
        var num = 0 // 初始化触发次数
        for (it in lotteryPrefix) { // 遍历抽奖前缀
            if (msg.contains(it) || msg.lowercase().contains(it)) { // 判断消息内容是否包含抽奖前缀
                num++   // 触发次数加一
                break   // 跳出循环
            }
        }   // 循环结束
        for (it in lotterySuffix) { // 遍历抽奖后缀
            if (msg.contains(it) || msg.lowercase().contains(it)) { // 判断消息内容是否包含抽奖后缀
                num++   // 触发次数加一
                break   // 跳出循环
            }
        }
        if ((msg == "抽奖" || "cj" == msg.lowercase() || msg == "奖励我") || num == 2)  // 判断消息内容是否为抽奖
            when {
                botCompareToAuthor() -> {  //判断bot是否拥有禁言权限
                    author().mute((lucky.time * lucky.multiple).minutes)    // 禁言指定时间
                    val message: Message = At(author().id) +
                            " 恭喜你抽到了${lucky.time}分钟".toText() +
                            if (lucky.multiple == 1) "".toText() else
                                ("，真是太棒了，你抽中的奖励翻了${lucky.multiple}倍，" +
                                        "变成了${lucky.time * lucky.multiple}分钟").toText() // 拼接字符串
                    group().send(message)   // 发送消息
                }
                botPermission() == Permission.ADMINISTRATORS -> { // 没有禁言权限但是bot有管理员权限
                    group().send(At(author().id) + " 你抽个屁的奖".toText())  // 发送消息
                }
                else -> {  //  没有禁言权限也没有管理员权限 也就是bot为普通成员
                    group().send("哎呀，无法奖励你~权限不够呢")  // 发送消息
                }
            }
    }


    /**
     * 幸运时间
     * @param timeFrame Int 最大时间
     * @return LuckyTime
     */
    private fun lucky(timeFrame: Int): LuckyTime {
        val time = Random().nextInt(timeFrame) + 1  // 获取随机数
        val multiple: Int = when (Random().nextInt(100)) {
            11 -> 2
            22 -> 3
            33 -> 4
            44 -> 5
            55 -> 6
            66 -> 7
            77 -> 8
            88 -> 9
            99 -> 10
            else -> 1
        }   // 获取抽奖倍数
        return LuckyTime(time, multiple)    // 返回抽奖结果
    }

    /**
     * 娱乐区
     */

    /**
     * 1.at+丢
     * 2.at+爬
     * 3.at+跑
     * 4.at+赞
     * 5.at+谢谢
     * 6.at+比心
     * 7.看看腿
     * 8.举牌+文本
     * @receiver GroupMessageEvent
     * @see At
     */
    @RobotListen(isBoot = true, desc = "作图服务")
    suspend fun GroupMessageEvent.fakePictures() {
        val msg = messageContent.plainText.trim()   // 获取消息内容
        val regex = Regex("^举牌.*").find(msg)?.value // 获取举牌内容
        if (regex != null) {    // 判断举牌内容是否为空
            group().send(   // 发送消息
                bot.uploadImage(    // 上传图片
                    URLResource(    // 获取图片资源
                        URL(    // 获取图片链接
                            "https://api.klizi.cn/API/tw/source.php?text=${
                                regex.substring(2)
                            }"  // 拼接图片链接
                        )
                    )
                )
            )
            return  // 跳出方法
        }

        if (msg == "看看腿") { // 判断消息内容是否为看看腿
            val url = "http://ovooa.com/API/meizi/"
            val tui = HttpUtil.getJsonClassFromUrl(url, Tuizi::class.java).text // 获取腿实体类
            group().send(bot.uploadImage(URLResource(URL(tui))))    // 发送图片
            return  // 跳出方法
        }

        val r = Regex("^[\u4e00-\u9fa5].*$").find(msg)?.value   // 获取消息内容中的中文
        println(r)
        if (r != null) {    // 判断消息内容不为空
            if (r.length in 3..4 && msg.contains("人")) {    // 判断消息内容长度是否在3到4个字符且包含人
                val chars = msg.toCharArray().joinToString("  ")    // 将消息内容转换成字符数组并使用空格分隔
                group().send(chars)   // 发送消息
                return  // 跳出方法
            }
        }

        for (message in messageContent.messages) {  // 遍历消息内容
            if (message is At) {    // 判断消息内容是否为at消息
                val atId = message.target   // 获取at的id
                if (atId == bot.id) {   // 判断at的id是否为bot的id
                    val reply = when (msg) {
                        "丢" -> "你给爷表演个怎么自己丢自己"
                        "爬" -> "我不会，快教我！"
                        "跑" -> "芜湖！"
                        "谢谢" -> "不用谢"
                        "笔芯", "比心" -> "爱你哟~"
                        "牵" -> "嘤嘤嘤，牵手手"
                        "鄙视" -> "嘤嘤嘤"
                        else -> ""
                    }   // 获取回复内容
                    if (reply.isNotEmpty()) {   // 判断回复内容是否为空
                        group().send(At(author().id) + (" $reply").toText())    // 发送回复
                        return  // 跳出方法
                    }
                    return  // 跳出方法
                }

                val url = when (msg) {  // 获取图片链接
                    "丢" -> "https://api.klizi.cn/API/ce/diu.php?qq=$atId"   // 丢图片链接
                    "爬" -> "https://api.klizi.cn/API/ce/paa.php?qq=$atId"   // 爬图片链接
                    "跑" -> "https://api.klizi.cn/API/ce/pao.php?qq=$atId"   // 跑图片链接
                    "赞" -> "https://api.klizi.cn/API/ce/zan.php?qq=$atId"   // 赞图片链接
                    "牵" -> {
                        if (atId == GlobalVariable.MASTER) {    // 判断at的id是否为管理员id
                            "https://api.klizi.cn/API/ce/qian.php?qq=${atId}&qq1=${author().id}"    // id调换为at的id和此人id
                        } else {
                            "https://api.klizi.cn/API/ce/qian.php?qq=${author().id}&qq1=$atId"      // id调换为此人id和at的id
                        }
                    }   // 牵图片链接
                    "谢谢" -> "https://api.klizi.cn/API/ce/xie.php?qq=$atId"  // 谢图片链接
                    "比心", "笔芯" -> "https://api.klizi.cn/API/ce/xin.php?qq=$atId"    // 比心图片链接
                    "鄙视" -> "https://api.klizi.cn/API/ce/bishi.php?qq=$atId"    // 鄙视图片链接
                    else -> ""  // 空图片链接
                }
                val dir = File("${GlobalVariable.botTemp}\\image")  // 获取图片存放目录
                if (!dir.exists()) {    // 判断图片存放目录是否存在
                    dir.mkdirs()    // 创建图片存放目录
                }
                if (url.isNotEmpty()) {   // 判断图片链接是否为空
                    group().send(bot.uploadImage(URLResource(URL(url))))    // 发送图片
                }
            }
        }
    }

    @OptIn(ExperimentalSimbotApi::class)
    @RobotListen(desc = "搜图", isBoot = true)
    @Filter("搜图", matchType = MatchType.TEXT_CONTAINS)
    suspend fun GroupMessageEvent.searchMap(session: ContinuousSessionContext) {
        val url = "https://yandex.com/images/search?family=yes&rpt=imageview&url="
        for (message in messageContent.messages) {  // 遍历消息内容
            if (message is Image) {   // 判断消息内容是否为图片消息
                val imgUrl = message.resource().name    // 获取图片链接
                group().send(url + UrlUtil.encode(imgUrl))   // 发送图片链接
                return  // 跳出方法
            }
        }
        val pic = sendAndWait("请发送图片...", 30, TimeUnit.SECONDS) // 获取图片
        pic.let {   // 判断图片是否为空
            if (it is Image<*>) {   // 判断图片是否为图片消息
                val imgUrl = it.resource().name   // 获取图片链接
                send(url + URLEncoder.encode(imgUrl))   // 发送图片链接
                return  // 跳出方法
            }
        }

    }

    /**
     * 聊天
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "陪聊服务")
    @Filter(target = TargetFilter(atBot = true))
    suspend fun GroupMessageEvent.chat() {
        val msg = messageContent.plainText.trim()   // 获取消息内容
        if (msg.isEmpty()) {    // 判断消息内容是否为空
            return  // 跳出方法
        }
        val url = "http://ruohuan.xiaoapi.cn/API/other/xiaoai.php?msg=$msg" // 获取接口链接
        val reply = HttpUtil.getJsonClassFromUrl(url, Chat::class.java).text    // 获取回复内容
        if (reply.isEmpty()) {  // 判断回复内容是否为空
            group().send(At(author().id) + " ${msg}?".toText())  // 发送消息
            return  // 跳出方法
        }
        group().send(At(author().id) + " $reply".toText())  // 发送消息

    }
}
