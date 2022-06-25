package com.huahua.robot.listener.grouplistener

import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.core.common.isNull
import com.huahua.robot.core.common.send
import com.huahua.robot.core.common.then
import com.huahua.robot.core.enums.RobotPermission
import com.huahua.robot.entity.LuckyTime
import com.huahua.robot.utils.FileUtil.getTempImage
import com.huahua.robot.utils.FileUtil.url
import com.huahua.robot.utils.MessageUtil.Companion.getImageMessage
import com.huahua.robot.utils.Permission
import com.huahua.robot.utils.PermissionUtil.Companion.botCompareToAuthor
import com.huahua.robot.utils.PermissionUtil.Companion.botPermission
import love.forte.simbot.ID
import love.forte.simbot.LoggerFactory
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.message.At
import love.forte.simbot.message.Message
import love.forte.simbot.message.plus
import love.forte.simbot.message.toText
import org.springframework.stereotype.Component
import java.util.*
import kotlin.time.Duration.Companion.minutes

/**
 * ClassName: GroupListener
 * @description
 * @author 花云端
 * @date 2022-06-13 17:34
 */
@Component
class GroupListener {

    private val lotteryPrefix: List<String> = listOf("chou", "cou", "c", "抽", "操", "艹", "草")    //抽奖前缀
    private val lotterySuffix: List<String> = listOf("jiang", "j", "奖", "wo", "w", "我") // 抽奖后缀
    private val log = LoggerFactory.getLogger(GroupListener::class.java)

    /**
     * 抽奖
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "抽奖服务", permission = RobotPermission.MEMBER)
    suspend fun GroupMessageEvent.luckDraw() {
        val msg = messageContent.plainText  // 获取消息内容
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
            when {
                botCompareToAuthor() -> {  //判断bot是否拥有禁言权限
                    author().mute((lucky.time * lucky.multiple).minutes)    // 禁言指定时间
                    val message: Message = At(author().id) +
                            " 恭喜你抽到了${lucky.time}分钟".toText() +
                            if (lucky.multiple == 1) "".toText() else
                                ("，真是太棒了，你抽中的奖励翻了${lucky.multiple}倍，" +
                                        "变成了${lucky.time * lucky.multiple}分钟").toText() // 拼接字符串
                    send(message)   // 发送消息
                }
                botPermission() == Permission.ADMINISTRATORS -> { // 没有禁言权限但是bot有管理员权限
                    send(At(author().id) + " 你抽个屁的奖".toText())  // 发送消息
                }
                else -> {  //  没有禁言权限也没有管理员权限 也就是bot为普通成员
                    send("哎呀，无法奖励你~权限不够呢")  // 发送消息
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
                    }.isNull {
                        send("图片获取失败，请稍后再试")
                    }?.delete()
                }
            }
        }
    }
}