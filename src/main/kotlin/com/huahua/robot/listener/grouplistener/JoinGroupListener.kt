package com.huahua.robot.listener.grouplistener

import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.Sender
import com.huahua.robot.core.common.isNull
import com.huahua.robot.core.common.logger
import com.huahua.robot.core.common.send
import com.huahua.robot.service.SwitchSateService
import com.huahua.robot.utils.Permission
import com.huahua.robot.utils.PermissionUtil.Companion.authorPermission
import com.huahua.robot.utils.Timer
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import love.forte.di.annotation.Beans
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
import java.util.concurrent.TimeUnit

/**
 * ClassName: JoinGroupListner
 * @description 加群监听
 * @author 花云端
 * @date 2022-05-10 17:26
 */
@Beans
class JoinGroupListener(
    val switchSateService: SwitchSateService,
) {

    /**
     * 入群申请自动同意
     */
    private var state = hashMapOf<ID, Boolean>()
    private val groupReply = hashMapOf<String, Timer<MiraiMemberJoinEvent>>()

    /**
     * 加群监听
     * @description 加群监听
     * @receiver MiraiMemberJoinEvent   入群事件
     */
    @RobotListen(isBoot = true, desc = "加群监听")
    suspend fun MiraiMemberJoinEvent.joinGroup() {
        val group = group() //所在群
        val member = member()   //加群人
        group().send("呐呐，欢迎 ${member.nickname} 加入本聊,请查看群公告")
        group().send("你大概是第${group.currentMember - 1}个加入本群的")
        group().send("别忘了给我点点小星星哦\nhttps://github.com/Chenyuxin221/huahua-robot")
        group().send("最后最后，有需要的话可以发送\".h|.help\"查看帮助哦")
        if (bot.originalBot.getGroup(group().id.tryToLong())!!.botPermission != MemberPermission.MEMBER) {
            val timeout = 10L   //超时时间
            val message = buildMessages {
                +At(member().id)
                +"请你在「${timeout}分钟」内发言，不然会被请出去的哦"
            }
            val timer = Timer(timeout, TimeUnit.MINUTES, this, true) {
                onStart { runBlocking { group().send(message) } }
                onFinish {
                    runBlocking {
                        group().member(member.id)?.kick("没在指定时间内回复，如需入群请重新申请")
                        group().send("看来他没有在「${timeout}分钟」内发言呢")
                    }
                }
            }
            groupReply["${group.id}:${member.id}"] = timer
        }
    }

    @RobotListen("回复检测", isBoot = true)
    suspend fun GroupMessageEvent.groupJoiningReplyDetection() {
        if (groupReply.isEmpty()) return
        val key = "${group().id}:${author().id}"
        groupReply[key]?.cancel()
    }


    var a: JoinRequestEvent? = null     //入群请求
    var tampMap = mutableMapOf<String, String>()    //存入请求人QQ号

    /**
     * 测试功能
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
     * 测试功能
     * @receiver GroupMessageEvent
     */
    @RobotListen(isBoot = true, desc = "入群答复申请")
    suspend fun GroupMessageEvent.joinGroup() {
        if (a == null) return
        val message = messageContent.plainText
        val pattern = """^(yes|no)+$"""
        val result = Regex(pattern).find(message)?.groups?.get(0)?.value ?: return
        if (!getBotManagerPermission(group(), author()) &&  //成员没有机器人管理权限
            authorPermission() < Permission.ADMINISTRATORS  //成员群权限小于管理员
        ) {
            return
        }
        if (tampMap["id"] == a!!.id.toString()) {
            withTimeoutOrNull(3600000) {
                when (result) {
                    "yes" -> {
                        a!!.accept()
                        send("「${author().nickOrUsername}」已同意入群申请")
                    }

                    "no" -> {
                        a!!.reject()
                        send("「${author().nickOrUsername}」已拒绝入群申请")
                    }
                }
                this.cancel()
            }.isNull {
                tampMap.clear()
                logger { "超时了" }
            }
        }
    }
}
