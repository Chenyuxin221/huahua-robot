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
class BootListener {

    @Autowired
    lateinit var mapper:GroupBootStateMapper

    @RobotListen(isBoot = false, permission = RobotPermission.ADMINISTRATOR)
    @Filter("开机")
    suspend fun GroupMessageEvent.boot() {
        val groupCode = group().id.toString()
        logger { "群${groupCode}以开机" }
        group().send("群${groupCode}以开机")
        bootOrDown(groupCode, true)
    }

    @RobotListen(isBoot = true, permission = RobotPermission.ADMINISTRATOR)
    @Filter("关机")
    suspend fun GroupMessageEvent.down() {
        val groupCode = group().id.toString()
        logger { "群${groupCode}以关机" }
        group().send("群${groupCode}以关机")
    }

    private fun bootOrDown(groupCode: String, state: Boolean) {
        RobotCore.BOOT_MAP[groupCode] = state
        val map  = hashMapOf<String,Any>()
        map["group_code"] = groupCode
        val groupBootState = mapper?.selectByMap(map)?.firstOrNull()


        if (groupBootState == null) {
            mapper?.insert(BootState(groupCode = groupCode,state = state))

        } else {
            mapper?.updateById(BootState(groupCode = groupCode,state = state))

        }
    }
}