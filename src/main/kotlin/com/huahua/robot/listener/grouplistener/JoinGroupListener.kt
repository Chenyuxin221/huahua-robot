package com.huahua.robot.listener.grouplistener

import com.huahua.robot.config.RobotConfig
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.*
import com.huahua.robot.service.SwitchSateService
import com.huahua.robot.utils.Permission
import com.huahua.robot.utils.PermissionUtil.Companion.authorPermission
import com.huahua.robot.utils.Timer
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import love.forte.di.annotation.Beans
import love.forte.simboot.annotation.Filter
import love.forte.simbot.ID
import love.forte.simbot.component.mirai.event.MiraiMemberJoinEvent
import love.forte.simbot.component.mirai.event.MiraiMemberJoinRequestEvent
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.event.JoinRequestEvent
import love.forte.simbot.event.RequestEvent
import love.forte.simbot.message.At
import love.forte.simbot.message.buildMessages
import love.forte.simbot.tryToLong
import net.mamoe.mirai.contact.MemberPermission
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * ClassName: JoinGroupListner
 * @description 加群监听
 * @author 花云端
 * @date 2022-05-10 17:26
 */
@Beans
class JoinGroupListener(
    private val switchSateService: SwitchSateService,
    private val robotConfig: RobotConfig,
) {

    /**
     * 入群申请自动同意
     */
    private var state = hashMapOf<ID, Boolean>()
    private val groupReply = hashMapOf<String, Timer<MiraiMemberJoinEvent>>()
    private val shieldGroups = robotConfig.joinGroupShield
    private var result: Int? = null

    @RobotListen("测试")
    @Filter("入群回复状态")
    suspend fun GroupMessageEvent.groupingReplyDetectionStatus() {
        if (shieldGroups != null) {
            if (group().id.toString() in shieldGroups) {
                reply("false")
            } else {
                reply("true")
            }
        } else {
            reply("未配置")
        }
        return
    }

    /**
     * 加群监听
     * @description 加群监听
     * @receiver MiraiMemberJoinEvent   入群事件
     */
    @RobotListen(isBoot = true, desc = "加群监听")
    suspend fun MiraiMemberJoinEvent.joinGroup() {
        val group = group() //所在群
        val member = member()   //加群人
        result = null //初始化
        group().send("呐呐，欢迎 ${member.nickname} 加入本聊,请查看群公告")
        group().send("你大概是第${group.currentMember - 1}个加入本群的")
        group().send("别忘了给我点点小星星哦\nhttps://github.com/Chenyuxin221/huahua-robot")
        group().send("最后最后，有需要的话可以发送\".h|.help\"查看帮助哦")
        //屏蔽群聊，因为少部分群人少不太需要此功能 故在代码屏蔽
        val switchName = "groupValidate"
        if (!shieldGroups.isNullOrEmpty()) if (group().id.toString() in shieldGroups) return
        var validate = switchSateService.get(group.id.toString(), switchName)
        validate?.let {
            logger { it.toString() }
            if (!it) return
        }.isNull {
            // 初始化
            switchSateService.set(group.id.toString(), switchName, true)
        }
        validate = switchSateService.get(group.id.toString(), switchName)
        if (validate!! && bot.originalBot.getGroup(group().id.tryToLong())!!.botPermission != MemberPermission.MEMBER) {
            val timeout = 30L   //超时时间
            val a = Random().nextInt(1, 101)
            val b = Random().nextInt(1, 101)
            val c = Random().nextInt(2)
            val message = buildMessages {
                +At(member().id)
                +" 请你在「${timeout}分钟」内发送「${a} ${if (c == 0) "+" else "-"} $b」的计算结果，不然会被请出去的哦"
            }
            val timer = Timer(timeout, TimeUnit.MINUTES, this, true) {
                onStart {
                    result = calc(a, b, c)
                    runBlocking {
                        group().send(message)
                    }
                }
                onCancel {
                    logger { "已终止计时器" }
                    groupReply.clear()
                }
                onFinish {
                    runBlocking {
                        // 暂时不踢人了
//                        group().member(member.id)?.kick("没在指定时间内回复，如需入群请重新申请")
                        group().send("看来他在「${timeout}分钟」内没有计算出结果呢")
                        groupReply.clear()
                    }
                }
            }
            groupReply["${group.id}:${member.id}"] = timer
        }
    }

    private fun calc(a: Int, b: Int, oper: Int) = when (oper) {
        0 -> a + b
        1 -> a - b
        else -> 999
    }

    @RobotListen("回复监听", isBoot = true)
    suspend fun GroupMessageEvent.replyToListen() {
        val key = "${group().id}:${author().id}"
        val regex = "\\d+".toRegex()
        val r = regex.find(messageContent.plainText)?.value
        val reply = groupReply[key]
        if (reply != null) {
            r?.let { s ->
                result?.let {
                    if (result == s.toInt()) {
                        reply("√ 验证失败，请v管理员${group().member(RobotCore.ADMINISTRATOR.ID)?.nickOrUsername ?: ""}50跳过验证(bushi)")
                        reply.cancel()

                    } else {
                        val a = Random().nextInt(1, 101)
                        val b = Random().nextInt(1, 101)
                        val c = Random().nextInt(2)
                        result = calc(a, b, c)
                        send("这你都不会，建议读个小学吧,已刷新题目「${a} ${if (c == 0) "+" else "-"} $b 」")
                    }
                    return
                }.isNull {
                    reply.cancel()
                    return
                }

            }.isNull {
                send("格式校验失败！！请输入数字")
            }
        }
    }

    var a: JoinRequestEvent? = null     //入群请求
    var tampMap = mutableMapOf<String, String>()    //存入请求人QQ号

    /**
     * 入群请求
     * @receiver JoinRequestEvent
     */
    @RobotListen(isBoot = true, desc = "入群请求监听")
    suspend fun JoinRequestEvent.joinGroup() {
        val text = message ?: "啊这...他好像什么也没填"   // 加群填写的文本
        val member = user() // 发出入群申请的用户
        val type = when (type) {
            RequestEvent.Type.APPLICATION -> "主动入群"
            RequestEvent.Type.INVITATION -> "成员「${inviter()?.id ?: "NULL"}」邀请"
        }

        val msg =
            "入群申请\n申请人:${member.username}「${member.id}」\n申请理由/答案：${text}\n申请状态:${type}\n是否同意申请:\n同意：yes\n拒绝:no"
        val event = (this as MiraiMemberJoinRequestEvent)
        Sender.sendGroupMsg(group().id.toString(), msg)
        var result = switchSateService.get(group().id.toString(), "加群自动同意")  //从redis获取结果
        if (result == null) {  //没有结果
            switchSateService.set(group().id.toString(), "加群自动同意", false) //将该群状态初始化为false
        }
        result = switchSateService.get(group().id.toString(), "加群自动同意") // 重新获取结果
        if (result!!) {  // 此时不应该为null，获取返回结果
            accept()
            Sender.sendGroupMsg(group().id.toString(), "已自动同意该申请")
            return
        }
        tampMap["id"] = id.toString()
        a = this
    }

    /**
     * 是否同意入群申请
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "入群答复申请")
    suspend fun GroupMessageEvent.joinGroup() {
        if (a == null) return
        val message = messageContent.plainText
        val pattern = """^(yes|no|y|n)+$"""
        val result = Regex(pattern).find(message)?.groups?.get(0)?.value ?: return
        if (!getBotManagerPermission(group(), author()) &&  //成员没有机器人管理权限
            authorPermission() < Permission.ADMINISTRATORS  //成员群权限小于管理员
        ) {
            return
        }
        if (tampMap["id"] == a!!.id.toString()) {
            withTimeoutOrNull(3600000) {
                when (result) {
                    "yes", "y" -> {
                        a!!.accept()
                        send("「${author().nickOrUsername}」已同意入群申请")
                    }

                    "no", "n" -> {
                        a!!.reject()
                        send("「${author().nickOrUsername}」已拒绝入群申请")
                    }
                }
                this.cancel()
            }.isNull {
                tampMap.clear() //清空缓存map
                logger { "超时了" }
            }
        }
    }
}
