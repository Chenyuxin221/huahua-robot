package com.huahua.robot.core.listener

import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.core.common.logger
import com.huahua.robot.core.entity.BootState

import com.huahua.robot.core.enums.RobotPermission
import com.huahua.robot.core.mapper.GroupBootStateMapper
import love.forte.simboot.annotation.Filter
import love.forte.simbot.event.GroupMessageEvent

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * 监听群开关机
 *
 * @author wuyou,花云端
 */
@Component
@SuppressWarnings("unused")
class BootListener {

    @Autowired
    lateinit var mapper:GroupBootStateMapper

    @RobotListen(isBoot = false, permission = RobotPermission.ADMINISTRATOR)
    @Filter("开机")
    suspend fun GroupMessageEvent.boot() {
        val groupCode = group().id.toString()
        logger { "群${groupCode}已开机" }
        val str = "-------开机成功-------\n" +
                "当前群聊：${group().name}\n" +
                "群号：${group().id}\n" +
                "操作人：${author().nickOrUsername}\n"+
                "--------------------"
        group().send(str)
        bootOrDown(groupCode, true)
    }

    @RobotListen(isBoot = true, permission = RobotPermission.ADMINISTRATOR)
    @Filter("关机")
    suspend fun GroupMessageEvent.down() {
        val groupCode = group().id.toString()
        logger { "群${groupCode}已关机" }
        val str = "-------关机成功-------\n" +
                "当前群聊：${group().name}\n" +
                "群号：${group().id}\n" +
                "操作人：${author().nickOrUsername}\n"+
                "--------------------"
        group().send(str)
        bootOrDown(groupCode,false)
    }

    private fun bootOrDown(groupCode: String, state: Boolean) {
        RobotCore.BOOT_MAP[groupCode] = state
        val map  = hashMapOf<String,Any>()
        map["group_code"] = groupCode
        val groupBootState = mapper.selectByMap(map)?.firstOrNull()


        if (groupBootState == null) {
            mapper.insert(BootState(groupCode = groupCode,state = state))

        } else {
            mapper.updateById(BootState(groupCode = groupCode,state = state))

        }
    }
}