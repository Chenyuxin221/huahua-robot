package com.huahua.robot.listener.grouplistener

import cn.hutool.extra.pinyin.PinyinUtil
import cn.hutool.extra.tokenizer.TokenizerUtil
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONException
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.*
import com.huahua.robot.entity.LuckyTime
import com.huahua.robot.service.SwitchSateService
import com.huahua.robot.utils.FileUtil.getTempImage
import com.huahua.robot.utils.FileUtil.url
import com.huahua.robot.utils.HttpUtil
import com.huahua.robot.utils.MessageUtil.Companion.getImageMessage
import com.huahua.robot.utils.Permission
import com.huahua.robot.utils.PermissionUtil.Companion.authorPermission
import com.huahua.robot.utils.PermissionUtil.Companion.botPermission
import com.huahua.robot.utils.PostType
import com.huahua.robot.utils.TimeUtil
import com.huahua.robot.utils.TimeUtil.name
import edu.stanford.nlp.pipeline.CoreDocument
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import love.forte.di.annotation.Beans
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.ID
import love.forte.simbot.component.mirai.event.MiraiGroupMessageEvent
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.logger.LoggerFactory
import love.forte.simbot.message.At
import love.forte.simbot.message.plus
import love.forte.simbot.message.toText
import org.apache.http.conn.HttpHostConnectException
import java.io.File
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * ClassName: GroupListener
 * @description
 * @author 花云端
 * @date 2022-06-13 17:34
 */
@Beans
class GroupListener(
    val switchSateService: SwitchSateService,
) {

    private val lotterySuffix: List<String> = listOf("jiang", "j", "奖", "wo", "w", "我") // 抽奖后缀
    private val log = LoggerFactory.getLogger(GroupListener::class.java)


    @RobotListen("单抽功能", isBoot = true)
    suspend fun GroupMessageEvent.lucky() {
        var res = switchSateService.get(group().id.toString(), "抽奖")
        if (res == null) {
            switchSateService.set(group().id.toString(), "抽奖", false)
            res = switchSateService.get(group().id.toString(), "抽奖")
        }
        if (!res!!) return

        val engine = TokenizerUtil.createEngine()
        val result = engine.parse(messageContent.plainText)
        var boolean = false
        result.forEach {
            val test = it.text  // 单词文本
            val noPermissionTips = "抽奖失败"   // 无权限提示文本
            if (test == "抽奖" || PinyinUtil.getPinyin(test) == "chou jiang") {   //是否触发关键词
                if (botPermission() > authorPermission()) { //是否拥有禁言权限
                    boolean = true  // 是否可以禁言
                } else {
                    reply(noPermissionTips)   //无权限提示
                }
            }
        }
        if (boolean) {
            val baseTimeUpperLimit = 60 //基础时间上限
            val luckDraw = getLuckDraw(baseTimeUpperLimit)
            val muteTime = luckDraw.time * luckDraw.multiple    // 最后的禁言时间
            val timeUnit = TimeUnit.MINUTES // 时间单位
            var tips = "恭喜你抽到了${luckDraw.time}${timeUnit.name()}" // 抽奖提示，无翻倍
            if (luckDraw.multiple > 1) {  // 如果有翻倍，则追加文本提示
                tips += if (luckDraw.multiple < 5) {
                    ",恭喜你抽中了${luckDraw.multiple}倍"
                } else {
                    ",哎呀！恭喜你抽中大奖了呢！中了${luckDraw.multiple}倍"
                }
                tips += ",变成了${muteTime}${timeUnit.name()}"
            }
            reply(tips)
            author().mute(muteTime.toLong(), timeUnit)
        }
    }

    @RobotListen("连抽服务", isBoot = true)
    @Filter("{{num}}连抽", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.lucky2(@FilterValue("num") num: String) {
        val res = switchSateService.get(group().id.toString(), "抽奖")
        if (res == null) {
            switchSateService.set(group().id.toString(), "抽奖", false)
            return
        }
        if (!res) return
        val baseTimeUpperLimit = 60 // 随机时间上限
        var reg = """\d+""".toRegex().find(input = num)?.value
        var result = 0 //总次数
        reg?.let { //匹配到了阿拉伯数字
            result = reg!!.toInt()
        }.isNull {    // 未匹配到阿拉伯数字，匹配汉字
            reg = """[零一二三四五六七八九十百壹贰叁肆伍陆柒捌玖拾佰]*""".toRegex().find(num)?.value
            if (reg == null) {
                logger { "The draw failed, please check the grammar" }
                return
            }
            result = chineseNumeralConversion(reg!!)
        }
        val multipleList = arrayListOf<LuckDraw>()  // 抽奖列表
        val loopBoundary = 0
        while (result > loopBoundary) {
            multipleList.add(getLuckDraw(baseTimeUpperLimit))   // 存入每次的抽奖列表
            result-- // 总次数减一
        }
        multipleList.isEmpty().then {
            reply("抽奖失败，非法的次数")
            return
        }
        var totalTime = 0 // 总时间
        multipleList.forEach() {
            totalTime += it.time * it.multiple
        }
        val maximumTimeLimitOfProhibition = 43200 // QQ禁言时间上限
        /*
         * 如果抽到的总时间大于QQ禁言总时间，则将总时间设置为上限时间
         */
        if (totalTime > maximumTimeLimitOfProhibition) {
            totalTime = maximumTimeLimitOfProhibition
        }
        var luckDrawDetails = multipleList.joinToString("，") {
            "${it.time}*${it.multiple}"
        }
        if (luckDrawDetails.length > 100) {
            luckDrawDetails = "哎呀，太长啦！"
        }
        val message = "恭喜你，抽中了总共${totalTime}分钟,分别抽中了\n[${luckDrawDetails}]"
        reply(message)
        if (botPermission() > authorPermission()) {
            author().mute(totalTime.toLong(), TimeUnit.MINUTES)
        }

    }

    private fun getLuckDraw(time: Int): LuckDraw {
        val time = Random().nextInt(1, time)// 随机抽奖基数
        val multiplier = when (Random().nextInt(100)) { // 时间倍数，目前最高十倍
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
        return LuckDraw(time, multiplier)
    }

    /**
     * 抽奖实体类
     * @property time Int 时间
     * @property multiple Int 倍数
     * @constructor null
     */
    data class LuckDraw(val time: Int, val multiple: Int)


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
     * 解析汉语中的数字组合 支持大写
     * 解析范围 零到一百到九百
     * @param number String 数字
     * @return Int  阿拉伯数字
     */
    private fun chineseNumeralConversion(number: String): Int {
        val len = number.length
        val chars = number.toCharArray()
        if (len == 1) {
            return when (chars[0]) {
                '零' -> 0
                '一', '壹' -> 1
                '二', '贰' -> 2
                '三', '叁' -> 3
                '四', '肆' -> 4
                '五', '伍' -> 5
                '六', '陆' -> 6
                '七', '柒' -> 7
                '八', '捌' -> 8
                '九', '玖' -> 9
                '十', '拾' -> 10
                '百', '佰' -> 100
                else -> -1
            }
        }
        if (len == 2) {
            var num: Int = when (chars[0]) {
                '一', '壹' -> 1
                '二', '贰' -> 2
                '三', '叁' -> 3
                '四', '肆' -> 4
                '五', '伍' -> 5
                '六', '陆' -> 6
                '七', '柒' -> 7
                '八', '捌' -> 8
                '九', '玖' -> 9
                '十', '拾' -> 10
                else -> -1
            }
            num *= when (chars[1]) {
                '十', '拾' -> 10
                '百', '佰' -> 100
                else -> 1
            }
            return num
        }
        if (len == 3) {
            var num: Int = when (chars[0]) {
                '二', '贰' -> 2
                '三', '叁' -> 3
                '四', '肆' -> 4
                '五', '伍' -> 5
                '六', '陆' -> 6
                '七', '柒' -> 7
                '八', '捌' -> 8
                '九', '玖' -> 9
                '十', '拾' -> 10
                else -> 0
            }
            if (chars[1] == '十' || chars[1] == '拾') {
                num *= 10
            }
            num += when (chars[2]) {
                '一', '壹' -> 1
                '二', '贰' -> 2
                '三', '叁' -> 3
                '四', '肆' -> 4
                '五', '伍' -> 5
                '六', '陆' -> 6
                '七', '柒' -> 7
                '八', '捌' -> 8
                '九', '玖' -> 9
                '十', '拾' -> 10
                else -> 0
            }
            return num
        }
        return -1
    }


    /**
     * 一些作图操作 --静态图片
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "作图服务")
    suspend fun GroupMessageEvent.picture() {
        val msg = messageContent.plainText.trim()   // 获取消息内容
        messageContent.messages.forEach {
            (it is At).then {
                val atId = (it as At).target
                (atId == bot.id).then {
                    when (msg) {
                        "丢" -> "你给爷表演个怎么自己丢自己"
                        "爬" -> "我不会，快教我！"
                        "跑" -> "芜湖！"
                        "谢谢" -> "不用谢"
                        "笔芯", "比心" -> "爱你哟~"
                        "牵" -> "嘤嘤嘤，牵手手"
                        "鄙视" -> "嘤嘤嘤"
                        else -> ""
                    }.also { str ->
                        str.isNotEmpty().then {
                            send(At(author().id) + (" $str").toText())
                            RobotCore.HaveReplied[author().id] = true
                            return
                        }
                    }
                }
                when (msg) {  // 获取图片链接
                    "丢" -> "https://api.klizi.cn/API/ce/diu.php?qq=$atId"   // 丢图片链接
                    "爬" -> "https://api.klizi.cn/API/ce/paa.php?qq=$atId"   // 爬图片链接
                    "跑" -> "https://api.klizi.cn/API/ce/pao.php?qq=$atId"   // 跑图片链接
                    "赞" -> "https://api.klizi.cn/API/ce/zan.php?qq=$atId"   // 赞图片链接
                    "牵" -> {
                        if (atId == RobotCore.ADMINISTRATOR.ID) {    // 判断at的id是否为管理员id
                            "https://api.klizi.cn/API/ce/qian.php?qq=${RobotCore.ADMINISTRATOR.ID}&qq1=${author().id}"    // id调换为at的id和此人id
                        } else {
                            "https://api.klizi.cn/API/ce/qian.php?qq=${author().id}&qq1=$atId"      // id调换为此人id和at的id
                        }
                    }   // 牵图片链接
                    "谢谢" -> "https://api.klizi.cn/API/ce/xie.php?qq=$atId"  // 谢图片链接
                    "比心", "笔芯" -> "https://api.klizi.cn/API/ce/xin.php?qq=$atId"    // 比心图片链接
                    "鄙视" -> "https://api.klizi.cn/API/ce/bishi.php?qq=$atId"    // 鄙视图片链接
                    else -> ""
                }.ifEmpty {
                    return
                }.also { url ->
                    "pic.jpg".getTempImage(url.url())?.also {
                        send(url.getImageMessage())
                    }.isNull {
                        send("图片获取失败，请稍后再试")
                    }?.delete()
                }
            }
        }
    }

    @RobotListen(desc = "点赞功能", isBoot = true)
    @Filter("赞我")
    suspend fun GroupMessageEvent.praiseMe() =
        "praiseMe.jpg".getTempImage("https://api.klizi.cn/API/ce/zan.php?qq=${author().id}".url())?.also {
            send(it.getImageMessage())
        }.isNull {
            send("图片获取失败，请稍后再试")
        }?.delete()


    @RobotListen(isBoot = true, desc = "作图服务-动态图片")
    suspend fun GroupMessageEvent.gifPicture() {
        messageContent.messages.forEach {
            if (it is At) {
                val id = it.target
                when (messageContent.plainText.trim()) {
                    "咬" -> "https://api.xingzhige.com/API/bite/?qq=$id"
                    "抓" -> "https://api.xingzhige.com/API/grab/?qq=$id"
                    "拍拍", "拍瓜", "排" -> "https://api.xingzhige.com/API/paigua/?qq=$id"
                    "顶球", "顶" -> "https://api.xingzhige.com/API/dingqiu/?qq=$id"
                    "看看", "看这个" -> "https://api.xingzhige.com/API/Lookatthis/?qq=$id"
                    "贴贴", "抱抱", "蹭蹭" -> "https://api.xingzhige.com/API/baororo/?qq=$id"
                    "笑", "笑死" -> "https://api.xingzhige.com/API/LaughTogether/?qq=$id"
                    else -> ""
                }.ifEmpty {
                    return
                }.also { url ->
                    "pic.gif".getTempImage(url.url())?.also { file ->
                        send(file.getImageMessage())
                        RobotCore.HaveReplied[author().id] = true
                    }.isNull {
                        send("图片获取失败，请稍后再试")
                    }?.delete()
                }
            }
        }
    }

    @RobotListen(isBoot = true, desc = "小短句")
    suspend fun GroupMessageEvent.shortSentence() {
        val msg = messageContent.plainText.trim()
        val url = "https://api.xingzhige.com/API/cp_generate/"
        val params = hashMapOf<String, Any>()
        val atList = mutableListOf<ID>()
        messageContent.messages.forEach {
            if (it is At) {
                atList.add(it.target)
            }
        }
        log.info("AT数量 ：${atList.size}")
        when (atList.size) {
            1 -> {
                var atName = group().member(atList[0])?.nickOrUsername
                atName.isNullOrEmpty().then { return }
                var authorName = author().nickOrUsername
                if (atName!!.length > 6) atName = group().member(atList[0])!!.username
                if (authorName.length > 6) authorName = author().username
                if (atName.length > 6) atName = atName.substring(0, 6)
                if (authorName.length > 6) authorName = authorName.substring(0, 6)
                when (msg) {
                    "嗑" -> {
                        if (atList[0] == RobotCore.ADMINISTRATOR.ID) {
                            val admin = group().member(RobotCore.ADMINISTRATOR.ID)
                            var adminName = admin!!.nickOrUsername
                            if (adminName.length > 6) adminName = admin.username
                            if (adminName.length > 6) adminName = adminName.substring(0, 6)
                            params["g"] = group().member(RobotCore.ADMINISTRATOR.ID)!!.nickOrUsername
                            params["s"] = authorName
                        } else {
                            params["g"] = authorName
                            params["s"] = atName
                        }
                    }
                }
            }

            2 -> {
                var name1 = group().member(atList[0])!!.nickOrUsername
                var name2 = group().member(atList[1])!!.nickOrUsername
                if (name1.length > 6) name1 = group().member(atList[0])!!.username
                if (name2.length > 6) name2 = group().member(atList[1])!!.username
                if (name1.length > 6) name1 = name1.substring(0, 6)
                if (name2.length > 6) name2 = name2.substring(0, 6)
                when (msg) {
                    "嗑" -> {
                        when {
                            atList[0] == RobotCore.ADMINISTRATOR.ID -> {
                                params["g"] = name2
                                params["s"] = author().nickOrUsername
                            }

                            atList[1] == RobotCore.ADMINISTRATOR.ID -> {
                                params["g"] = name1
                                params["s"] = author().nickOrUsername
                            }

                            atList[0] == atList[1] -> {
                                send("不能和自己嗑")
                            }

                            else -> {
                                params["g"] = name1
                                params["s"] = name2
                            }
                        }
                    }
                }
            }

            3 -> send("不支持多人运动")
            4 -> send("不支持多人运动")
            else -> return
        }
        if (params.isEmpty()) {
            return
        }
        val result = HttpUtil.post(url, params, PostType.DATA)
        val code = JSON.parseObject(result).getInteger("code")
        if (code == 0) {
            val data = JSON.parseObject(result).getString("data")
            val reply = JSON.parseObject(data).getString("msg")
            RobotCore.HaveReplied[author().id] = true
            send(reply)
        } else {
            send("请求失败，请稍后再试")
        }
    }


    @RobotListen("自助头衔", isBoot = true)
    @Filter("头衔{{title}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.receiveTitle(@FilterValue("title") title: String) {
        (botPermission() != Permission.OWNER).then { return }
        if (title.trim().encodeToByteArray().size > 18) {
            send("哎呀，太长啦，怼不进去")
            return
        }
        val event = this as MiraiGroupMessageEvent
        val member = event.originalEvent.group[author().id.number]
        member!!.specialTitle = title.trim()
        reply("头衔「${title}」拿好哦")
    }

    @RobotListen("随机冒泡", isBoot = true)
    suspend fun GroupMessageEvent.bubbling() {
        val array: Array<Any> = arrayOf(
            "对了，今天天好蓝！",
            "对了，Hello World~",
            "还有，今天天真蓝!",
            "哎呀，今天云真白!",
            "还有，今天蓝真天!",
            "呐呐，想要一只猫猫呢~",
            "哎呀，忘吃药了..",
            "呐呐，(●'◡'●)",
            "我爱你，而且，(●'◡'●)",
            "哦~(●'◡'●)",
            "我爱你，但是，忘吃药了..",
            "我爱你，但是，想要一只猫猫呢",
            "我爱你，但是，看到了一只可爱的猫猫呢!",
            "我爱你，而且，今天白真云!",
            "我爱你，而且，越来越冷了呢",
            "呐，喵",
            "话说..喵",
            "呐呐，好困...（Zzz",
            "话说..忘吃药了..",
            "对了，该自我维护了呢",
            "喵~",
            "还有！新年快乐！",
            "对了，happy new year~"
        )
        val imgArray = arrayOf(
            "https://c2cpicdw.qpic.cn/offpic_new/1849950046//1849950046-3406106310-6A4C4572302DA0BE613BE13725E5075E/0?term=2&is_origin=0",
            "https://c2cpicdw.qpic.cn/offpic_new/1849950046//1849950046-524956038-EF02CC56DB9FAC02698AE8D05279843B/0?term=2&is_origin=0",
            "https://c2cpicdw.qpic.cn/offpic_new/1849950046//1849950046-3318315239-8E94922E30B63A90D2A7DE35E4524D05/0?term=2&is_origin=0",
            "https://c2cpicdw.qpic.cn/offpic_new/1849950046//1849950046-1763955784-2253B7FAC116B6FE4BE781763398DBC4/0?term=2&is_origin=0",
            "https://c2cpicdw.qpic.cn/offpic_new/1849950046//1849950046-893522533-0E719031D6A037338CA980FADC80F284/0?term=2&is_origin=0",
            "https://c2cpicdw.qpic.cn/offpic_new/1849950046//1849950046-633278185-7B5004A5ED0FCF042BF5AF737EA1762B/0?term=2&is_origin=0"

        )
        when (Random().nextInt(200)) {
            99 -> {
                send(array[Random().nextInt(array.size - 1)])
                return
            }

            55 -> {
                val img = imgArray[Random().nextInt(imgArray.size - 1)]
                send(img.getImageMessage())
                return
            }
        }
        val time = TimeUtil.getNowTime()
        when (Random().nextInt(999)) {
            66 -> send(String.format("呐呐呐，你知道吗，今天是今年的第%s天呢", LocalDate.now().dayOfYear))
            77 -> send(String.format("啊！现在的时间是%s哒~", time))
            606 -> send(String.format("说起来，现在的时间是%s哒~", time))
            188 -> send(messageContent.messages)
        }
    }

    var count = -1
    var dic = ""

    @RobotListen(desc = "AI绘图", isBoot = true)
    @Filter("/drawing {{tags}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.drawing(@FilterValue("tags") tags: String) {
        val api = "http://127.0.0.1:8080/AIBlackList"
        if (dic.isNotEmpty() && count > 100) {
            val d = File(dic)
            if (d.isDirectory) {
                d.listFiles()!!.forEach {
                    if (it.isFile) {
                        it.delete()
                    }
                }
            }
        }
        when (tags) {
            "-help" -> {
                send("权重修饰符：()")
                return
            }

            "-ban showall" -> {
                val body = HttpUtil.getBody("${api}/queryAll")
                val result = JSON.parseObject(body).getJSONArray("data").joinToString(",")
                reply(result)
                return
            }
        }
        val tgs = tags.lowercase().split(",")
        val t1 = tags.lowercase().split("，")
        val tagList = mutableListOf<String>()
        if (tgs.isNotEmpty() || t1.isNotEmpty()) {
            tgs.forEach {
                val text: String = if (it.contains("(") && it.contains(")")) {
                    it.substring(it.lastIndexOf("(") + 1, it.indexOf(")"))
                } else if (it.contains("(")) {
                    it.substring(it.lastIndexOf("(") + 1)
                } else if (it.contains(")")) {
                    it.substring(0, it.indexOf(")"))
                } else {
                    it
                }
                tagList.add(text.lowercase())
            }
            if (t1.size > 1) {
                t1.forEach {
                    val text = if (it.contains("(") && it.contains(")")) {
                        it.substring(it.lastIndexOf("(") + 1, it.indexOf(")"))
                    } else if (it.contains("(")) {
                        it.substring(it.lastIndexOf("(") + 1)
                    } else if (it.contains(")")) {
                        it.substring(0, it.indexOf(")"))
                    } else {
                        it
                    }
                    tagList.add(text.lowercase())
                }
            }
        }

        if (tagList.isNotEmpty()) {
            tagList.forEach {
                if (it in RobotCore.AiBLACKLIST) {
                    send("检索到敏感词汇【${it}】,拒绝执行")
                    return
                }
            }
        }
        if (author().id == RobotCore.ADMINISTRATOR.ID) {
            val pattern = """^[A-Za-z0-9, ]+$"""
            when (tags) {

                "-ban add" -> {
                    val text = sendAndWait("请输入", 30, TimeUnit.SECONDS, Regex(pattern))?.plainText
                    text?.also {
                        Regex(pattern).find(text)?.groups?.get(0)?.value?.lowercase()?.let {
                            val list = it.split(",")
                            val failureList = mutableListOf<Map<String, String>>()
                            for (i in list.indices) {
                                val js =
                                    JSON.parseObject(HttpUtil.getBody("${api}/insert?value=${list[i].lowercase()}"))
                                if (js.getIntValue("code") != 200) {
                                    failureList.add(mapOf(Pair(list[i], js.getString("msg"))))
                                    continue
                                }

                            }
                            val msg =
                                "已完成操作\n总数：${list.size}\n成功：${list.size - failureList.size}\n失败：${failureList.size}\n失败详情：${
                                    failureList.fold("") { acc, map -> acc + map.keys + "," }
                                }\n${failureList.fold("") { acc, map -> acc + map.keys + "：" + map.values + "\n" }}"
                            send(msg)
                            refreshAiBlackList().not().then {
                                send("黑名单刷新失败！")
                            }
                        }
                    }
                    return
                }

                "-ban delete", "-ban remove" -> {
                    val text = sendAndWait("请输入", 30, TimeUnit.SECONDS, Regex(pattern))?.plainText
                    text?.also {
                        Regex(pattern).find(text)?.groups?.get(0)?.value?.lowercase()?.let {
                            val list = it.split(",")
                            for (i in list.indices) {
                                val js = JSON.parseObject(HttpUtil.getBody("${api}/delete?value=${list[i]}"))
                                if (js.getIntValue("code") != 200) {
                                    send("【${list[i]}】删除失败：${js.getString("msg")}")
                                    break
                                }
                            }
                            refreshAiBlackList().not().then {
                                send("黑名单刷新失败！")
                            }
                        }
                    }
                    return
                }

                "-ban update" -> {
                    val text = sendAndWait("请输入", 30, TimeUnit.SECONDS, Regex(pattern))?.plainText
                    text?.also {
                        Regex(pattern).find(text)?.groups?.get(0)?.value?.lowercase()?.let {
                            val list = it.split(",")

                            if (list.size != 2) {
                                send("格式错误！！\n正确格式：修改前,修改后\n使用半角符号")
                                return
                            }
                            val js =
                                JSON.parseObject(HttpUtil.getBody("${api}/update?value=${list[0]}&newValue=${list[1]}"))
                            if (js.getIntValue("code") != 200) {
                                send("修改失败：${js.getString("msg")}")
                            }
                            reply("修改成功")
                            refreshAiBlackList().not().then {
                                send("黑名单刷新失败！")
                            }
                        }
                    }
                    return
                }
            }
        }

        val aiApi = "http://127.0.0.1:7860/api/huahua"
        val body = HttpUtil.post {
            url = aiApi
            json =
                "{\"fn_index\":101,\"data\":[\"masterpiece,best quality,official art,extremely detailed CG unity 8k wallpaper,${tags} \",\"lowres, bad anatomy, bad hands, text, error, missing fingers, extra digit, fewer digits, cropped, worst quality, low quality, normal quality, jpeg artifacts, signature, watermark, username, blurry, bad feet\",\"None\",\"None\",20,\"Euler a\",true,false,1,1,7,-1,-1,0,0,0,false,512,512,true,0.7,0,0,\"None\",0.9,5,\"0.0001\",false,\"None\",\"\",0.1,false,false,false,false,\"\",\"Seed\",\"\",\"Nothing\",\"\",true,false,false,null]}"
        }.response
        try {
            logger { body }
            val data = JSON.parseObject(body).getString("data")
            val imgPath = JSON.parseObject(data.substring(2, data.indexOf("]"))).getString("name")
            val file = File(imgPath)
            dic = file.parent
            count = File(dic).listFiles()!!.size
            if (!file.isFile) {
                send("文件创建失败了..，请重新尝试获取")
                return
            }
            reply(file.getImageMessage())
        } catch (e: JSONException) {
            e.printStackTrace()
            send("请求失败，api崩溃了")
        } catch (e: HttpHostConnectException) {
            send("请求失败，api未开启")
        } catch (e: StringIndexOutOfBoundsException) {
            send("请求失败，索引超界")
            send(e.printStackTrace())
        } catch (e: Exception) {
            send("未知错误：${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 刷新本地黑名单
     */
    private fun refreshAiBlackList(): Boolean {
        val url = "http://127.0.0.1:8080/AIBlackList/queryAll"
        val json = JSON.parseObject(HttpUtil.get(url).response)
        if (json.getIntValue("code") != 200) {
            logger { "黑名单刷新失败" }
            return false
        }

        val list = json.getJSONArray("data").toMutableList()
        val result = mutableListOf<String>()
        list.forEach {
            result.add(it.toString())
        }
        RobotCore.AiBLACKLIST = result
        return true
    }

    /**
     * 刷听歌时长
     * @receiver GroupMessageEvent 群监听
     */
    @RobotListen(isBoot = true, desc = "我刷听歌时长")
    @Filter("刷听歌时长")
    suspend fun GroupMessageEvent.musicTimes() {
        val api = "https://fkapi.rjk66.cn/qqsc/qqsc.php"
        val id = author().id.toString()
        val response = HttpUtil.getBody("$api?uin=$id")
        reply(JSON.parseObject(response).getString("msg"))
    }


    @RobotListen(isBoot = true, desc = "违禁词")
    suspend fun GroupMessageEvent.prohibitedWords() {
//        val pinyin = PinyinUtil.getPinyin(messageContent.plainText).split(" ").filter { it.isNotEmpty() }.joinToString(" ")
//        val engine = TokenizerUtil.createEngine()
//        val result = engine.parse(messageContent.plainText)
//        result.forEach{
//            println(it.text)
//        }
        // 政治判断
        val isPolitical = isPoliticalContent(messageContent.plainText)
        if (isPolitical) {
            logger { "The text contains illegal political content" }
            reply("疑似政治违规内容")
        }
    }

    private fun isPoliticalContent(text: String): Boolean {
        /**
         * 中文包下载地址：https://huggingface.co/stanfordnlp/corenlp-chinese/tree/v4.5.4
         * 下载完成后加到resources/lib 目录
         */
        val pipeline = StanfordCoreNLP("StanfordCoreNLP-chinese.properties")
        val document = CoreDocument(text)
        pipeline.annotate(document)
        val politicalEntities = document.entityMentions().filter {
            it.entityType() == "POLITICAL"
        }
        return politicalEntities.isNotEmpty()
    }

}