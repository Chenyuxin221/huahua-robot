package com.huahua.robot.listener.grouplistener

import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.Sender
import com.huahua.robot.core.common.send
import com.huahua.robot.core.common.then
import com.huahua.robot.utils.Permission
import com.huahua.robot.utils.PermissionUtil.Companion.authorPermission
import love.forte.di.annotation.Beans
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.ID
import love.forte.simbot.component.mirai.event.MiraiMemberJoinEvent
import love.forte.simbot.component.mirai.event.MiraiMemberJoinRequestEvent
import love.forte.simbot.event.GroupMessageEvent

/**
 * ClassName: JoinGroupListner
 * @description 加群监听
 * @author 花云端
 * @date 2022-05-10 17:26
 */
@Beans
class JoinGroupListener {

    /**
     * 入群申请自动同意
     */
    private var state = hashMapOf<ID,Boolean>()

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
        group().send("你大概是第${group.currentMember-1}个加入本群的")
        group().send("别忘了给我点点小星星哦\nhttps://github.com/Chenyuxin221/huahua-robot")
        group().send("最后最后，有需要的话可以发送\".h|.help\"查看帮助哦")
    }

    /**
     * 加群监听
     * @receiver MiraiMemberJoinRequestEvent    加群请求事件
     */
    @RobotListen(isBoot = true, desc = "加群请求监听")
    suspend fun MiraiMemberJoinRequestEvent.joinGroup() {
        var text = message  //加群请求消息
        val group = group() //所在群
        val member = user() //加群人
        if (message.isEmpty()){
            text = "啊这...他好像什么也没填"
        }
        stateInit()
        Sender.sendGroupMsg(group.id.toString(), "入群申请：\n申请人：${member.nickOrUsername}\n申请原因：${text}\n请管理员前往处理")
        state[group().id]?.then {
            accept()
            Sender.sendGroupMsg(group.id.toString(),"已自动同意申请")
        }
    }

    /**
     * 自动入群设置
     * @receiver GroupMessageEvent
     * @param state String 设置
     */
    @RobotListen("自动同意入群申请设置", isBoot = true)
    @Filter("{{state}}自动同意", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.automaticConsent(@FilterValue("state") text:String){
        stateInit()
        if (authorPermission() < Permission.ADMINISTRATORS){
            send("玩蛋去吧")
            return
        }
        text.isEmpty().then { return }
        when(text){
            "设置","打开","开启" -> {
                if (state[group().id]==true){
                    send("已经处于开启状态了")
                    return
                }
                state[group().id] = true
                send("已开启群聊【${group().id}】 的自动同意入群申请")
            }
            "取消","关闭" ->{
                if (state[group().id]==false){
                    send("该功能已处于关闭状态")
                    return
                }
                state[group().id] = true
                send("已关闭群聊【${group().id}】 的自动同意入群申请")
            }
            else -> return
        }
    }

    @RobotListen(desc = "状态", isBoot = true)
    @Filter(".set")
    suspend fun GroupMessageEvent.state(){
        stateInit()
        val str = """
            --------------------
            自动同意入群：${state[group().id]}
            --------------------
        """.trimIndent()
        send(str)
    }
    suspend fun GroupMessageEvent.stateInit(){
        if (state[group().id] == null){
            state[group().id] = true
        }
    }
    suspend fun MiraiMemberJoinRequestEvent.stateInit(){
        if (state[group().id] == null){
            state[group().id] = true
        }
    }
}
