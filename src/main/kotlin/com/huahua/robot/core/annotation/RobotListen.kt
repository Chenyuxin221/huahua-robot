package com.huahua.robot.core.annotation

import com.huahua.robot.core.enums.RobotPermission
import love.forte.simboot.annotation.ContentTrim
import love.forte.simboot.annotation.Listener
import love.forte.simbot.event.GroupMessageEvent

/**
 * @author wuyou
 * @author 花云端
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Listener
@ContentTrim
@Suppress("OPT_IN_USAGE", "unused")
annotation class RobotListen(
    /**
     * 描述信息
     */
    val desc: String = "",
    /**
     * 执行监听器所需的权限
     */
    val permission: RobotPermission = RobotPermission.MEMBER,
    /**
     * 没有权限时的提示信息
     */
    val noPermissionTip: String = "操作失败,您没有权限",

    /**
     * 机器人没有执行权限时的提示信息
     */
    val botNotHavePermissionTip:String = "操作失败，机器人权限不足",

    /**
     * 是否在当前群开机的时候执行,仅当监听类型是[GroupMessageEvent]时有效
     */
    val isBoot: Boolean = false,

    /**
     * bot执行时所需要的权限
     */
    val permissionsRequiredByTheRobot: RobotPermission = RobotPermission.MEMBER

)