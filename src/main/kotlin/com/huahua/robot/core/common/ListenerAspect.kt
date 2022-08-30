package com.huahua.robot.core.common

import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.enums.RobotPermission
import kotlinx.coroutines.runBlocking
import love.forte.simbot.component.mirai.event.MiraiGroupMemberEvent
import love.forte.simbot.event.Event
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.utils.item.toList
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component


/**
 * 拦截监听器
 * @author wuyou,花云端
 */

@Component
@Aspect
class ListenerAspect {

    /**
     * 拦截监听器方法
     */
    @Around("@annotation(com.huahua.robot.core.annotation.RobotListen) && @annotation(annotation)) && args(continuation)")
    fun ProceedingJoinPoint.doAroundAdvice(annotation: RobotListen): Any? {
        val start = System.currentTimeMillis()
        val event = args.find { it is Event } ?: return proceed()
        fun proceedSuccess(): Any? {
            logger {
                -"执行了监听器${signature.name}(${annotation.desc})"
                -("执行拦截器耗时: " + (System.currentTimeMillis() - start))
            }

            return proceed()
        }

        fun proceedFailed(tip: String) {
            logger {
                -"执行监听器${signature.name}(${annotation.desc})失败, $tip"
                -("执行拦截器耗时: " + (System.currentTimeMillis() - start))
            }
            return
        }
        when (event) {
            is GroupMessageEvent -> {
                val group = runBlocking { event.group() }
                val author = runBlocking { event.author() }
                val role = runBlocking { author.roles.toList()[0] }
                val botPermission = runBlocking { group.member(event.bot.id)?.roles?.toList()?.get(0) }

                // 判断是否开机
                if (annotation.isBoot && !RobotCore.BOOT_MAP.getOrDefault(group.id.toString(), false)) {
                    return proceedFailed("当前群未开机")
                }
                // 判断是否有权限
                if (
                    annotation.permission != RobotPermission.MEMBER &&
                    annotation.permission > role && !RobotCore.isBotAdministrator(author.id.toString())
                ) {
                    if (annotation.noPermissionTip.isNotBlank()) {
                        Sender.sendGroupMsg(group, annotation.noPermissionTip)
                        return proceedFailed("权限不足")
                    }
                }
                // 判断机器人是否有执行权限
                if (botPermission == null ||
                    annotation.permissionsRequiredByTheRobot > botPermission &&
                    annotation.permission != RobotPermission.MEMBER
                ) {
                    if (annotation.botNotHavePermissionTip.isNotBlank()) {
                        Sender.sendGroupMsg(group, annotation.botNotHavePermissionTip)
                        return proceedFailed("机器人权限不足")
                    }
                }
            }

            is MiraiGroupMemberEvent<*> -> {
                val group = runBlocking { event.group() }
                // 判断是否开机
                if (annotation.isBoot && !RobotCore.BOOT_MAP.getOrDefault(group.id.toString(), false)) {
                    return proceedFailed("当前群未开机")
                }
            }
        }
        return proceedSuccess()
    }
}

