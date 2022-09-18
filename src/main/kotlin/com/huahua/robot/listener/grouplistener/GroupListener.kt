package com.huahua.robot.listener.grouplistener

import com.alibaba.fastjson2.JSON
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.core.common.isNull
import com.huahua.robot.core.common.send
import com.huahua.robot.core.common.then
import com.huahua.robot.core.enums.RobotPermission
import com.huahua.robot.entity.LuckyTime
import com.huahua.robot.utils.FileUtil.getTempImage
import com.huahua.robot.utils.FileUtil.url
import com.huahua.robot.utils.HttpUtil
import com.huahua.robot.utils.MessageUtil.Companion.getImageMessage
import com.huahua.robot.utils.Permission
import com.huahua.robot.utils.PermissionUtil.Companion.authorPermission
import com.huahua.robot.utils.PermissionUtil.Companion.botCompareToAuthor
import com.huahua.robot.utils.PermissionUtil.Companion.botCompareToMember
import com.huahua.robot.utils.PermissionUtil.Companion.botPermission
import com.huahua.robot.utils.PostType
import com.huahua.robot.utils.TimeUtil
import love.forte.di.annotation.Beans
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.ID
import love.forte.simbot.LoggerFactory
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.component.mirai.event.MiraiGroupMessageEvent
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.message.At
import love.forte.simbot.message.Message
import love.forte.simbot.message.plus
import love.forte.simbot.message.toText
import love.forte.simbot.utils.item.toList
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

/**
 * ClassName: GroupListener
 * @description
 * @author 花云端
 * @date 2022-06-13 17:34
 */
@Beans
class GroupListener {

    private val lotteryPrefix: List<String> = listOf("chou", "cou", "c", "抽", "操", "艹", "草")    //抽奖前缀
    private val lotterySuffix: List<String> = listOf("jiang", "j", "奖", "wo", "w", "我") // 抽奖后缀
    private val log = LoggerFactory.getLogger(GroupListener::class.java)

    /**
     * 获取bot的skey pskey
     * @receiver GroupMessageEvent
     */
    fun GroupMessageEvent.getPsKey() {
        val bot = (bot as MiraiBot).originalBot
        val client = bot.javaClass.getMethod("getClient").invoke(bot)
        val wLoginSigInfo = client.javaClass.getMethod("getWLoginSigInfo").invoke(client)
        val sKey = wLoginSigInfo.javaClass.getDeclaredMethod("getSKey").invoke(wLoginSigInfo)
        val psKeyMap = wLoginSigInfo.javaClass.getDeclaredMethod("getPsKeyMap").invoke(wLoginSigInfo) as HashMap<*, *>
        val map = hashMapOf<String, String>()

        map["psKey"] = (psKeyMap["vip.qq.com"]?.getFieldValue("data") as ByteArray).decodeToString()
        map["sKey"] = (sKey.getFieldValue("data") as ByteArray).decodeToString()
    }

    fun Any.getFieldValue(fieldName: String): Any {
        return javaClass.getDeclaredField(fieldName).let {
            it.isAccessible = true
            it.get(this)
        }
    }

    fun Any.invoke(methodName: String, vararg args: Any): Any {
        return javaClass.getDeclaredMethod(methodName).let {
            it.isAccessible = true
            if (it.parameterCount > 0) it.invoke(this, args)
            else it.invoke(this)
        }
    }

    /**
     * 抽奖
     * @receiver GroupMessageEvent
     */
    @RobotListen(
        isBoot = true,
        desc = "抽奖服务",
        permission = RobotPermission.MEMBER,
        permissionsRequiredByTheRobot = RobotPermission.ADMINISTRATOR
    )
    suspend fun GroupMessageEvent.luckDraw() {
        val msg = messageContent.plainText  // 获取消息内容
        var time = 0
        val url =
            """((http|ftp|https)://)(([a-zA-Z0-9._-]+\.[a-zA-Z]{2,6})|([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9&%_./-~-]*)?""".toRegex()
                .find(input = msg)?.value
        if (url != null) {
            return
        }
        if (msg.contains("连抽")) {
            var reg = """\d+""".toRegex().find(input = msg)?.value
            var result = 0
            reg?.let { //匹配到了阿拉伯数字
                result = reg!!.toInt()
            }.isNull {    // 未匹配到阿拉伯数字，匹配汉字
                reg = """[零一二三四五六七八九十百壹贰叁肆伍陆柒捌玖拾佰]*""".toRegex().find(msg)?.value
                if (reg == null) {
                    println("抽奖失败，请检查语法")
                    return
                }
                result = chineseNumeralConversion(reg!!)
            }
            for (i in result downTo 1) {
                val lucky = lucky(80)
                time += lucky.time * lucky.multiple
            }
            if (time > 43200) {
                time = 43200
            }
            val timeMillisecond = TimeUnit.MINUTES.toMillis(time.toLong())
            val format = TimeUtil.millisecondFormat(timeMillisecond)
            if (botCompareToAuthor()) {
                val r = author().mute((time).minutes)
                r.then {
                    send(At(author().id) + " 恭喜你抽到了${format}的禁言套餐".toText())
                }
            } else if (botPermission() == Permission.ADMINISTRATORS &&
                authorPermission() == Permission.OWNER ||
                authorPermission() == Permission.ADMINISTRATORS
            ) {
                send(At(author().id) + " 你抽个屁的奖".toText())  // 发送消息
            } else {
                send("哎呀，无法奖励你~权限不够呢")  // 发送消息
            }
            return
        }
        val lucky = lucky(80)   // 获取抽奖结果
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
            if (botCompareToAuthor()) {
                author().mute((lucky.time * lucky.multiple).minutes)    // 禁言指定时间
                val message: Message =
                    At(author().id) + " 恭喜你抽到了${lucky.time}分钟".toText() + if (lucky.multiple == 1) "".toText() else ("，真是太棒了，你抽中的奖励翻了${lucky.multiple}倍，" + "变成了${lucky.time * lucky.multiple}分钟").toText() // 拼接字符串
                send(message)   // 发送消息
            } else if (botPermission() == Permission.ADMINISTRATORS &&
                authorPermission() == Permission.OWNER ||
                authorPermission() == Permission.ADMINISTRATORS
            ) { // 没有禁言权限但是bot有管理员权限
                messageContent.messages.forEach {
                    if (it is At) {
                        if (botCompareToMember(group().member(it.target)!!)) {
                            group().member(it.target)!!.mute((lucky.time * lucky.multiple).minutes)
                            send(At(it.target) + " 恭喜你，${author().nickOrUsername}帮你抽中了${lucky.time * lucky.multiple}分钟".toText())
                        } else {
                            send(At(author().id) + " 哎呀，无法帮${group().member(it.target)!!.nickOrUsername}抽奖，权限不够呢".toText())
                        }
                        return
                    }
                }
                send(At(author().id) + " 你抽个屁的奖".toText())  // 发送消息
            } else {  //  没有禁言权限也没有管理员权限 也就是bot为普通成员
                send("哎呀，无法奖励你~权限不够呢")  // 发送消息
            }
    }

    @RobotListen
    @Filter(".lucky")
    suspend fun GroupMessageEvent.randomMute() {
        if (authorPermission() == Permission.MEMBER) {
            send("抽奖失败，你的权限不足")
            return
        }
        val probability = 1 / group().currentMember.toFloat() * 100
        val members = group().members.toList()
        var luckyNumber = Random().nextInt(members.size)
        var luckyDog = members[luckyNumber]
        if (botCompareToMember(luckyDog)) {
            luckyDog.mute((5).minutes)
            send("${luckyDog.nickOrUsername} 成为了幸运观众，中奖概率为 1/${group().currentMember}".toText())
        } else {
            luckyNumber = Random().nextInt(members.size)
            luckyDog = members[luckyNumber]
            send("${luckyDog.nickOrUsername} 成为了幸运观众，中奖概率为 1/${group().currentMember}".toText())
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
                var autherName = author().nickOrUsername
                if (atName!!.length > 6) atName = group().member(atList[0])!!.username
                if (autherName.length > 6) autherName = author().username
                if (atName.length > 6) atName = atName.substring(0, 6)
                if (autherName.length > 6) autherName = autherName.substring(0, 6)
                when (msg) {
                    "嗑" -> {
                        if (atList[0] == RobotCore.ADMINISTRATOR.ID) {
                            val admin = group().member(RobotCore.ADMINISTRATOR.ID)
                            var adminName = admin!!.nickOrUsername
                            if (adminName.length > 6) adminName = admin.username
                            if (adminName.length > 6) adminName = adminName.substring(0, 6)
                            params["g"] = group().member(RobotCore.ADMINISTRATOR.ID)!!.nickOrUsername
                            params["s"] = autherName
                        } else {
                            params["g"] = autherName
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
        log.error(title)
        if (title.encodeToByteArray().size > 18) {
            send("哎呀，太长啦，怼不进去")
            return
        }
        val event = this as MiraiGroupMessageEvent
        val member = event.originalEvent.group[author().id.number]
        member!!.specialTitle = title
        reply("头衔「${title}」拿好哦")
    }

}