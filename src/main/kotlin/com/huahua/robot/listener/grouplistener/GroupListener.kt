package com.huahua.robot.listener.grouplistener

import com.google.gson.Gson
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.entity.Chat
import com.huahua.robot.entity.LuckyTime
import com.huahua.robot.entity.Setu
import com.huahua.robot.entity.Tuizi
import com.huahua.robot.entity.setu.SetuIcon
import com.huahua.robot.utils.GlobalVariable
import com.huahua.robot.utils.HttpUtil
import io.ktor.util.reflect.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.TargetFilter
import love.forte.simboot.filter.MatchType
import love.forte.simbot.LoggerFactory
import love.forte.simbot.component.mirai.extra.catcode.catCodeToMessage
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.message.*
import love.forte.simbot.resources.FileResource
import love.forte.simbot.resources.Resource
import love.forte.simbot.resources.URLResource
import org.springframework.stereotype.Component
import java.io.File
import java.net.URL
import java.util.*
import kotlin.reflect.jvm.jvmName
import kotlin.time.Duration.Companion.minutes


@Component
@SuppressWarnings("all")
class GroupListener {
    private val lotteryPrefix: List<String> = listOf("chou", "cou", "c", "抽", "操", "艹", "草")
    private val lotterySuffix: List<String> = listOf("jiang", "j", "奖", "wo", "w", "我")
    private val log = LoggerFactory.getLogger(GroupListener::class.jvmName)

    /**
     * 菜单||项目文档||项目地址
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "菜单服务")
    suspend fun GroupMessageEvent.menu(){
        val msg = messageContent.plainText.trim()
        val functionMenuUrl = "https://www.yuque.com/qingsi-zwnmu/xyuvvi/wrbzgy"
        val gitHubUrl = "https://github.com/Chenyuxin221/huahua-robot"
        val result = if (msg=="帮助"){
            "功能菜单：${functionMenuUrl}\n"+
                    "项目地址：${gitHubUrl}"
        } else {""}
        if (result.isNotEmpty()){
            group().send(result)
        }
    }



    /**
     * 涩图功能
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "写真服务")
    @Filter(value = "来点好看的|来点好康的", matchType = MatchType.REGEX_CONTAINS)
    suspend fun GroupMessageEvent.setu() {
        val url = "http://localhost:8080/api/photo"
        val body: String = HttpUtil().getBody(url)
        val imgUrl = Gson().fromJson(body, Setu::class.java).url
        val image: Image<*> = bot.uploadImage(FileResource(File(imgUrl)))
        group().send(image)
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
        val img: Image<*> = bot.uploadImage(URLResource(URL(imgUrl2)))
        val receipt: MessageReceipt = group().send(img)
        bot.launch {
            if (receipt.isSuccess) {
                delay(6000)
                receipt.deleteIfSupport()
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
        val response = HttpUtil().getResponse(url)
        val dir = File("${System.getProperty("user.home")}\\.huahuabot\\image")
        if (!dir.exists()){
            log.info("目录创建成功")
            dir.mkdirs()
        }
        val imgFile = File("${dir.absolutePath}\\girl.jpg")
        imgFile.writeBytes(response.body()?.bytes()!!)
        val img = bot.uploadImage(FileResource(File(imgFile.absolutePath)))
        val receipt = group().send(img)
        bot.launch {
            if (receipt.isSuccess) {
                delay(60000)
                receipt.deleteIfSupport()
            }
        }
    }

    /**
     * 保存图片
     * @receiver GroupMessageEvent
     */
    suspend fun GroupMessageEvent.getImage() {
        val path = "D:\\新建文件夹\\bot\\kt-huahua-robot\\src\\main\\resources\\image\\"
        for (message: Message.Element<*> in messageContent.messages) {
            if (message is Image) {
                val imgResource: Resource = message.resource()
                catCodeToMessage("")
                val stream = imgResource.openStream()
                File("${path}emm.jpg").writeBytes(withContext(Dispatchers.IO) {
                    stream.readAllBytes()
                })
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
        val msg = messageContent.plainText
        val r18: Boolean = msg.lowercase(Locale.getDefault()).contains("r18")
        val setu: SetuIcon = getJson(if (r18) "1" else "0") ?: return
        val data = setu.data
        val stringBuilder = "标题: ${data[0].title}\n链接: ${data[0].url}"
        val flag = group().send(stringBuilder)
        try {
            if (!author().isAdmin()) {
                author().mute((60000).minutes)
            }
        } catch (e: java.lang.Exception) {
            e.message
        } finally {
            bot.launch {
                delay(30000)
                if (flag.isSuccess) {
                    flag.deleteIfSupport()
                }
            }
        }
    }

    private fun getJson(r18: String): SetuIcon? {
        var setu: SetuIcon? = null
        try {
            val key1 = "820458705ebe071883b3c2"
            val key2 = "198111555ec3242d2c6b42"
            val params: MutableMap<String, String> = HashMap(8)
            params["apikey"] = key1
            params["size1200"] = "true"
            params["r18"] = r18
            var web: String = HttpUtil()["http://api.lolicon.app/setu/", params, null].response.toString()
            setu = Gson().fromJson(web, SetuIcon::class.java)
            if (setu.code == 429) {
                params["apikey"] = key2
                web = HttpUtil()["http://api.lolicon.app/setu/", params, null].response.toString()
                setu = Gson().fromJson(web, SetuIcon::class.java)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        if (setu != null) {
            log.info("获取到涩图: ${setu.toString()}")
        }
        return setu
    }


    /**
     * 抽奖
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "抽奖服务")
    suspend fun GroupMessageEvent.luckDraw() {
        val msg = messageContent.plainText
        val lucky = lucky(60)
        var num = 0
        for (it in lotteryPrefix) {
            if (msg.contains(it)) {
                num++
                break
            }
        }
        for (it in lotterySuffix) {
            if (msg.contains(it)) {
                num++
                break
            }
        }
        if ((msg == "抽奖" || "cj" == msg) || num == 2) {
            if (author().isAdmin()) {
                group().send(At(author().id) + " 你抽个屁的奖".toText())
                return
            }
            author().mute((lucky.time * lucky.multiple).minutes)
            val message: Message = At(author().id) +
                    " 恭喜你抽到了${lucky.time}分钟".toText() +
                    if (lucky.multiple == 1) "".toText() else
                        ("，真是太棒了，你抽中的奖励翻了${lucky.multiple}倍，" +
                                "变成了${lucky.time * lucky.multiple}分钟").toText()
            group().send(message)
        }
    }


    /**
     * 幸运时间
     * @param timeFrame Int 最大时间
     * @return LuckyTime
     */
    private fun lucky(timeFrame: Int): LuckyTime {
        val time = Random().nextInt(timeFrame) + 1
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
        }
        return LuckyTime(time, multiple)
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
        val msg = messageContent.plainText.trim()
        val regex = Regex("^举牌.*").find(msg)?.value
        if (regex != null) {
            group().send(bot.uploadImage(URLResource(URL("http://api.klizi.cn/API/tw/source.php?text=${regex.substring(2)}"))))
            return
        }

        if (msg == "看看腿") {
            val url = "http://ovooa.com/API/meizi/"
            val tui = Gson().fromJson(HttpUtil().getBody(url), Tuizi::class.java).text
            group().send(bot.uploadImage(URLResource(URL(tui))))
            return
        }

        val r = Regex("^[\u4e00-\u9fa5].*$").find(msg)?.value
        if (r != null) {
            if (r.length == 4) {
                val chars = msg.toCharArray().joinToString("  ")
                group().send(chars)
                return
            }
        }

        for (message: Message.Element<*> in messageContent.messages) {
            if (message is At) {
                val atId = message.target
                if (atId == bot.id) {
                    val reply = when (msg) {
                        "丢" -> "你给爷表演个怎么自己丢自己"
                        "爬" -> "我不会，快教我！"
                        "跑" -> "芜湖！"
                        "谢谢" -> "不用谢"
                        "笔芯", "比心" -> "爱你哟~"
                        "牵" -> "嘤嘤嘤，牵手手"
                        "鄙视" -> "嘤嘤嘤"
                        else -> ""
                    }
                    if (reply.isNotEmpty()) {
                        group().send(At(author().id) + (" $reply").toText())
                        return
                    }
                    return
                }

                val url = when (msg) {
                    "丢" -> "http://api.klizi.cn/API/ce/diu.php?qq=$atId"
                    "爬" -> "http://api.klizi.cn/API/ce/paa.php?qq=$atId"
                    "跑" -> "http://api.klizi.cn/API/ce/pao.php?qq=$atId"
                    "赞" -> "http://api.klizi.cn/API/ce/zan.php?qq=$atId"
                    "牵" -> {
                        if (atId == GlobalVariable.MASTER) {
                            "http://api.klizi.cn/API/ce/qian.php?qq=$atId&qq1=${author().id}"
                        } else {
                            "http://api.klizi.cn/API/ce/qian.php?qq=${author().id}&qq1=$atId"
                        }
                    }
                    "谢谢", "听我说谢谢你" -> "http://api.klizi.cn/API/ce/xie.php?qq=$atId"
                    "比心", "笔芯" -> "http://api.klizi.cn/API/ce/xin.php?qq=$atId"
                    "鄙视" -> "http://api.klizi.cn/API/ce/bishi.php?qq=$atId"
                    else -> ""
                }
                println("url = $url")
                if (url.isNotEmpty()) {
                    group().send(bot.uploadImage(URLResource(URL(url))))
                    return
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
    suspend fun GroupMessageEvent.chat() {
        val msg = messageContent.plainText.trim()
        if (msg.isEmpty()) {
            return
        }
        val url = "http://ruohuan.xiaoapi.cn/API/other/xiaoai.php?msg=$msg"
        val chat: Chat = Gson().fromJson(HttpUtil().getBody(url), Chat::class.java)
        val reply = chat.text
        if (reply.isEmpty()) {
            group().send(At(author().id) + " ${msg}?".toText())
            return
        }
        group().send(At(author().id) + " $reply".toText())

    }

}