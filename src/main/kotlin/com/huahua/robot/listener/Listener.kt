@file:Suppress("UNUSED_VARIABLE", "unused")

package com.huahua.robot.listener

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.*
import com.huahua.robot.entity.Chat
import com.huahua.robot.entity.Tuizi
import com.huahua.robot.utils.*
import com.huahua.robot.utils.FileUtil.getTempImage
import com.huahua.robot.utils.FileUtil.getTempMusic
import com.huahua.robot.utils.FileUtil.url
import com.huahua.robot.utils.MessageUtil.Companion.getImageMessage
import com.huahua.robot.utils.PermissionUtil.Companion.botCompareToAuthor
import kotlinx.coroutines.*
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.ID
import love.forte.simbot.component.mirai.event.MiraiNudgeEvent
import love.forte.simbot.component.mirai.message.MiraiSendOnlyAudio
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.event.MessageEvent
import love.forte.simbot.logger.LoggerFactory
import love.forte.simbot.message.*
import love.forte.simbot.resources.FileResource
import love.forte.simbot.resources.Resource.Companion.toResource
import love.forte.simbot.resources.URLResource
import love.forte.simbot.tryToLong
import org.springframework.stereotype.Component
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.jvm.jvmName
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.minutes


@Component
@SuppressWarnings("all")
class Listener {
    private val log = LoggerFactory.getLogger(Listener::class.jvmName) // 日志
    private var nudgeCount = 0

    /**
     * 菜单||项目文档||项目地址
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "菜单服务")
    @Filter(".h", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.menu() {
        if (this is GroupMessageEvent) {
            if (group().id.tryToLong() == 745212995L) {
                val imgName = "menu.jpg"
                val imgUrl =
                    "https://c2cpicdw.qpic.cn/offpic_new/1849950046//1849950046-2298352361-0D5D6F2124C04A6B032BCB743CF3AD99/0?term=2&is_origin=1"
                val imgPath = FileUtil.imagePath() + "menu.jpg"
                if (File(imgPath).isFile)
                    send(File(imgPath).getImageMessage())
                else
                    imgName.getTempImage(imgUrl.url())?.let {
                        send(it.getImageMessage())
                    }

            }
        }
        val functionMenuUrl = "https://www.yuque.com/qingsi-zwnmu/xyuvvi/wrbzgy"    // 项目文档
        val gitHubUrl = "https://github.com/Chenyuxin221/huahua-robot"           // 项目地址
        val result = "功能菜单：${functionMenuUrl}\n项目地址：${gitHubUrl}"         // 菜单内容
        send(result)                                              // 发送菜单
    }

    @RobotListen(isBoot = true, desc = "盗版法欧莉酱")
    @Filter(".h{{num}}", targets = Filter.Targets(groups = arrayOf("745212995")))
    suspend fun GroupMessageEvent.menu(@FilterValue("num") num: Int) {
        val msg = when (num) {
            0 -> "https://www.yuque.com/docs/share/43264d27-99a7-4287-97c0-b387f5b0947e"
            1 -> """
                simbot2文档:  https://www.yuque.com/docs/share/e4b99402-40a6-4394-850f-6b87f7b7395f

                simbot3官网:  https://simbot.forte.love

                simbot3文档引导站:  https://docs.simbot.forte.love

                simbot3 GitHub: https://github.com/simple-robot
            """.trimIndent()

            2 -> "问题反馈: https://github.com/simple-robot/simpler-robot/issues/new/choose"
            3 -> "CatCode v1: https://github.com/ForteScarlet/CatCode\nCatCode v2: https://github.com/ForteScarlet/CatCode2"
            11 -> "2.xのDemo们：\nhttps://github.com/simple-robot"
            12 -> "示例代码: https://github.com/simple-robot/simbot-examples\nMaven Archetypes:\nhttps://github.com/simple-robot/simbot-archetypes"
            30 -> "tg群聊/频道:\nhttps://t.me/joinchat/UidrhxyTWuvWeylsEwNHIA\nhttps://t.me/simple_robot_rua"
            90 -> "粘贴板: \nhttps://paste.ubuntu.com/"
            else -> "没有编号[${num}]的帮助啦."
        }
        send(msg)
    }


    /**
     * 涩图功能
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "写真服务")
    @Filter(value = "来点好看的|来点好康的", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.setu() {
        val url = "http://localhost:8080/api/photo"
        val imageList = arrayListOf<String>()
        var count = 0
        while (true) {
            if (count == 10) {
                break
            }
            val body = JSON.parseObject(HttpUtil.getBody(url))
            if (body.getIntValue("code") == 200) {
                val data = JSON.parseObject(body.getString("data"))
                imageList.add(data.getString("url"))
                count += 1
            } else {
                log.error(body.getString("msg"))
            }
        }
        val flag: MessageReceipt?
        val message = getForwardImageMessages(imageList, RobotCore.BOTID.toLong())
        flag = send(message!!) // 发送消息并获取标记
        if (this is GroupMessageEvent) { //判断是否为群聊环境
            botCompareToAuthor().then { //判断bot是否有权限
                author().mute((5).minutes)  // 禁言5分钟
                messageContent.delete()// 撤回消息
            }
        }
        bot.launch {    // 启动协程
            if (flag != null && flag.isSuccess) {   // 判断发送是否成功
                delay(180000)    // 由于转发消息图片过多，上传较慢，故延迟三分钟
                flag.delete()  // 通过标记撤回此图片
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
            messageContent.delete()   // 撤回消息
        }
        bot.launch {    // 启动协程
            receipt?.let {
                if (it.isSuccess) {  // 判断发送是否成功
                    delay(60000)    // 延迟一分钟
                    it.delete()  // 通过标记撤回此图片
                }
            }.isNull {
                send("发送失败")
            }
        }

    }

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
                        it.delete()
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
    @Filter(value = "来点{{tags}}涩涩", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.sendSetuToR18(@FilterValue("tags") tag: String) = setu(tag.split(" "), true)


    /**
     * 涩图功能
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "涩图服务")
    @Filter(value = "来点{{tags}}涩图", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.sendSetu(@FilterValue("tags") tag: String) {
        setu(tag.split(" "), false)
    }

    /**
     * （真）涩图功能
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "涩图服务")
    @Filter(value = "来点涩涩", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.sendSetuToR18() = setu(listOf(), true)


    /**
     * 涩图功能
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "涩图服务")
    @Filter(value = "来点涩图", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.sendSetu() {
        setu(listOf(), false)
    }


    private suspend fun MessageEvent.setu(tag: List<String>, r18: Boolean) {
        val msg = messageContent.plainText  // 获取消息内容
        var url = "https://api.lolicon.app/setu/v2?r18=${
            if (r18) 1 else 0
        }"
        tag.isNotEmpty().then {
            tag.forEach {
                url += "&tag=${UrlUtil.encode(it)}"
            }
        }
        val body = HttpUtil.get(url)
        val dataList = body.getJSONResponse()?.get("data") as ArrayList<*>
        dataList.isEmpty().then {
            send("搜索失败，没有找到含有标签${tag.joinToString("&")}的图片")
            return
        }
        val data = JSON.parseObject(dataList[0].toString())
        val tags = (data["tags"] as ArrayList<*>).joinToString(",")
        val str = """
            Title: ${data.getString("title")},
            Author: ${data.getString("author")},
            R18: ${data.getString("r18")},
            Tags: ${tags},
            Url: ${JSON.parseObject(data.getString("urls")).getString("original")}
        """.trimIndent()  // 拼接消息内容
        if (this is GroupMessageEvent) {
            botCompareToAuthor().then {
                author().mute((1).minutes)
                messageContent.delete()
            }
        }
        val flag = send(str)  // 发送消息并获取标记
        bot.launch {    // 启动协程
            delay(30000)    // 延迟30秒
            flag?.also {
                it.isSuccess.then { it.delete() }
            }.isNull {
                Sender.sendAdminMsg("${getId()}发送消息[${str}]失败")
            }
        }
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
            (msg.length in 3..4 && it.length in 3..4 && msg.contains("人")).then {    // 判断消息内容长度是否在3到4个字符且包含人
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
    @Filter("exit()", matchType = MatchType.TEXT_CONTAINS)
    suspend fun MessageEvent.exit() {
        val command = """
            taskkill /f /im tail.exe
            taskkill /f /im cmd.exe
        """.trimIndent()
        when (this) {
            is GroupMessageEvent -> {
                if (author().id == RobotCore.ADMINISTRATOR.ID) {

                    withContext(Dispatchers.IO) {
                        bot.cancel()
                        Runtime.getRuntime().exec(command)
                    }
                    exitProcess(0)
                }
            }

            is FriendMessageEvent -> {
                if (friend().id == RobotCore.ADMINISTRATOR.ID) {
                    bot.cancel()
                    withContext(Dispatchers.IO) {
                        Runtime.getRuntime().exec(command)
                    }
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
    @Filter(value = "^!>$", targets = Filter.Targets(atBot = true), matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.chat() {
        val msg = messageContent.plainText.trim()   // 获取消息内容
        msg.isEmpty().then {    // 判断消息内容是否为空
            return  // 跳出方法
        }
        val url = "https://xiaobai.klizi.cn/API/other/xiaoai.php?data=&msg=$msg" // 获取接口链接
        val reply = HttpUtil.getJsonClassFromUrl(url, Chat::class.java).text    // 获取回复内容
        when (this) {
            is GroupMessageEvent -> {
                if (RobotCore.HaveReplied[author().id] == true) {
                    RobotCore.HaveReplied[author().id] = false
                    return
                }
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
                reply(reply)  // 发送消息
            }  // 发送消息

        }
    }


    @RobotListen(isBoot = true, desc = "听歌")
    @Filter(".music")
    suspend fun MessageEvent.voiceMusic() {
        val api = "https://ovooa.com/API/changya/"
        var body = ""
        var count = 1
        while (count < 50) {
            delay(500)
            log.info("正在进行第${count}次请求")
            val b = HttpUtil.getBody(api)
            if (!b.contains("502")) {
                body = b
                log.info("请求成功，正在返回结果")
                break
            }
            count++
        }
        if (body.isEmpty()) {
            send("播放失败，未知错误")
            return
        }
        val json = JSON.parseObject(body)
        if (!json["code"].equals(1)) {
            return
        }
        val data = JSON.parseObject(json.getString("data"))
        val file = "${data.getString("song_name")}.mp3".getTempMusic(data.getString("song_url").url())
        file.isNull {
            send("文件下载失败了QAQ")
            return
        }
        val music = MiraiSendOnlyAudio(FileResource(file!!))
        send(music)!!.isSuccess.then { file.delete() }
    }

    @RobotListen(isBoot = true, desc = "听歌")
    @Filter(".music {{name}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.appointVoiceMusic(@FilterValue("name") name: String) {
        name.isEmpty().then { return }
        var url = "https://xiaobapi.top/api/xb/api/cg.php?msg=${UrlUtil.encode(name)}"
        val musicList = HttpUtil.get(url).response.split("\\n")
        val text = sendAndWait("${musicList.joinToString("\n")}\t请输入序号选择", 30, TimeUnit.SECONDS)?.plainText
        text?.also {
            val num = Regex("""\d+""").find(text)?.value
            num.isNull { return }
            if (num!!.toInt() > musicList.size || num.toInt() <= 0) {
                send("输入范围为1-${musicList.size - 1}，请重新点歌")
                return
            }
            url += "&n=$num"
            val file = "${name}.mp3".getTempMusic(url.url())
            file?.also {
                send(MiraiSendOnlyAudio(it.toResource()))
            }.isNull {
                send("哎呀，歌曲下载失败啦")
                return
            }!!.delete()
        }
    }

    //    @RobotListen(isBoot = true, desc = "冒泡")
    suspend fun MessageEvent.aNaoDai() {
        val n = Random().nextInt(100)
        val url = "https://xiaobapi.top/api/xb/api/and.php"
        if (n == 50) {
            val file = "啊脑袋.mp3".getTempMusic(url.url())
            file.isNull { return }
            file!!.also {
                send(MiraiSendOnlyAudio(it.toResource()))
            }.delete()
        }
    }

    @RobotListen(isBoot = true, desc = "刷抖音")
    @Filter("刷抖音")
    suspend fun MessageEvent.douyin() {
        val url = "https://xiaobai.klizi.cn/API/other/douyin.php"

        val body = JSON.parseObject(HttpUtil.get(url).response)
        log.info(body.toJSONString())
        val video = if (RobotCore.ShortLinkState) getShortLinkUrl(body.getString("video"), 2)
            ?: body.getString("video") else body.getString("video")
        val str = """
        Title：${body.getString("desc")}
        Author：${body.getString("author")}
        Video:  $video
    """.trimIndent()
        val message = buildMessages {
            image(URLResource(body.getString("cover").url()))
            text(str)
        }
        send(message)
    }

    @RobotListen(isBoot = true, desc = "短链接生成开关")
    @Filter("{{state}}短链接", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.setShortLink(@FilterValue("state") state: String) =
        when (state) {
            "设置", "打开", "开启" -> {
                if (RobotCore.ShortLinkState) {
                    send("已经是开启状态了~")
                }
                RobotCore.ShortLinkState = true
            }

            "取消", "关闭" -> {
                if (!RobotCore.ShortLinkState) {
                    send("已经是关闭状态了~")
                }
                RobotCore.ShortLinkState = false
            }

            else -> {}
        }

    private fun getShortLinkUrl(url: String, type: Int, separator: String = ""): String? {
        when (type) {
            1 -> {
                val url1 =
                    "https://api.linhun.vip/api/dwz?url=${if (separator.isEmpty()) url else url.split(separator)[0]}"
                return HttpUtil.getBody(url1)
            }

            2 -> {
                val appid = "sqnmugmnspqditkh"
                val appSecret = "akw0SkhCOVoweWhWT3FQQ3dJNHgxUT09"
                val urlEncode = Base64.getUrlEncoder()
                    .encodeToString((if (separator.isEmpty()) url else url.split(separator)[0]).toByteArray())
                val url1 =
                    "https://www.mxnzp.com/api/shortlink/create?url=${urlEncode}&app_id=${appid}&app_secret=${appSecret}"
                log.error(url1)
                val body: JSONObject?
                try {
                    body = JSON.parseObject(HttpUtil.getBody(url1))
                } catch (e: Exception) {
                    return null
                }
                body.isNull { return null }
                (body!!.getIntValue("code") != 1).then { return null }
                val data = JSON.parseObject(body.getString("data"))
                return data.getString("shortUrl")
            }
        }
        return null
    }


    @RobotListen(isBoot = true, desc = "解析")
    @Filter("解析", matchType = MatchType.TEXT_STARTS_WITH)
    suspend fun MessageEvent.analysis() {
        val msg = messageContent.plainText
        val reg =
            """((http|ftp|https)://)(([a-zA-Z0-9._-]+\.[a-zA-Z]{2,6})|([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9&%_./-~-]*)?""".toRegex()
                .find(input = msg)?.value
        reg.isNullOrEmpty().then {  // 说明不是要用解析功能
            send("未匹配到链接")
            return
        }
        val api1 = "https://api.xcboke.cn/api/juhe?url=$reg" //解析接口
        val body = HttpUtil.getBody(api1)
        val result: JSONObject
        try {
            result = JSON.parseObject(body)
        } catch (e: Exception) {
            send("解析失败，api已失效")
            return
        }
        when (result.getIntValue("code")) {   //状态码判断
            200 -> log.info("解析成功")
            201 -> {
                send("解析失败")
                return
            }

            else -> {
                send(result.toJSONString())
                return
            }
        }
        val data = JSON.parseObject(result.getString("data"))
        var str = ""
        val url = data.getString("url")
        when {
            reg!!.contains("douyin") -> {       //抖音解析
                val music = JSON.parseObject(data.getString("music"))
                val title = if (data.getString("title").contains("@{0}")) {
                    "没有标题"
                } else {
                    data.getString("title")
                }
                val bgmUrl = music.getString("url")
                str = """
                    Time: ${TimeUtil.getStringTime(data.getLong("time"), TimeUnit.SECONDS)}
                    Title: ${title},
                    Author: ${data.getString("author")},
                    Uid: ${data.getString("uid")},
                    Video_Url: ${url},
                    like: ${data.getIntValue("like")},
                    Bgm_Author: ${music.getString("author")},
                    Bgm_Url: ${bgmUrl}
                """.trimIndent()
            }

            reg.contains("kuaishou") -> {   //快手解析
                str = """
                    Title: ${data.getString("title")},
                    Author: ${data.getString("author")},
                    Video_Url: ${url},
                """.trimIndent()
            }

            reg.contains("xiaochuankeji") -> {   //最右解析
                str = """
                    Title: ${data.getString("title")},
                    Author: ${data.getString("author")},
                    Video_Url: ${url},
                """.trimIndent()
            }

        }
        val message = buildMessages {
            image(URLResource(data.getString("cover").url()))
            text(str)
        }
        send(message)
    }

    @RobotListen(isBoot = true, desc = "戳一戳监听")
    suspend fun MiraiNudgeEvent.nudge() {
        if (originalEvent.target.id != RobotCore.BOTID.toLong()) return
        if (nudgeCount >= 20) {
            delay(300000)
            nudgeCount = 0
            return
        }
        nudgeCount++
        reply("哎呀，讨厌啦~")
        originalEvent.from.nudge().sendTo(originalEvent.subject)
        delay(30000)
    }

}

