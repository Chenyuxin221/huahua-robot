package com.huahua.robot.utils

import kotlinx.coroutines.runBlocking
import love.forte.simbot.definition.GroupMember
import love.forte.simbot.definition.Member
import love.forte.simbot.event.GroupMessageEvent

/**
 * ClassName: PermissionUtil
 * @description 权限工具类
 * @author 花云端
 * @date 2022-05-15 19:58
 */
class PermissionUtil {


    /**
     * 获取成员权限码
     * @param member GroupMember?   群成员
     * @return Int
     * -1：  member为空
     * 1；   成员
     * 2：   管理员
     * 3：   群主
     */
    private fun getPermissionCode(member: GroupMember) = runBlocking t@{
        if (member.isAdmin()) {
            return@t Permission.ADMINISTRATORS
        } else if (member.isOwner()) {
            return@t Permission.OWNER
        } else {
            return@t Permission.MEMBER
        }
    }

    /**
     * 获取成员权限码
     * @param member GroupMember?   群成员
     * @return Int
     * -1：  member为空
     * 1；   成员
     * 2：   管理员
     * 3：   群主
     */
    private fun getPermissionCode(member: Member) = runBlocking t@{
        if (member.isAdmin()) {
            return@t Permission.ADMINISTRATORS
        } else if (member.isOwner()) {
            return@t Permission.OWNER
        } else {
            return@t Permission.MEMBER
        }
    }


    companion object {

        /**
         * 获取群Bot的权限
         * @param event GroupMessageEvent 群事件
         * @return Int 权限码
         * 1：成员
         * 2：管理员
         * 3：群主
         */
        fun botPermission(event: GroupMessageEvent) =
            runBlocking { PermissionUtil().getPermissionCode(event.group().member(event.bot.id)!!) }

        /**
         * 获取发言人的权限
         * @param event GroupMessageEvent 群事件
         * @return Int 权限码
         * 1：成员
         * 2：管理员
         * 3：群主
         */
        fun authorPermission(event: GroupMessageEvent) =
            runBlocking { PermissionUtil().getPermissionCode(event.author()) }

        /**
         * 获取指定群成员的权限
         * @param member GroupMember? 群成员
         * @return Int 权限码
         * 1：成员
         * 2：管理员
         * 3：群主
         */
        fun memberPermission(member: GroupMember) = runBlocking { PermissionUtil().getPermissionCode(member) }

        /**
         * 获取指定群成员的权限
         * @param member GroupMember? 群成员
         * @return Int 权限码
         * 1：成员
         * 2：管理员
         * 3：群主
         */
        fun memberPermission(member: Member) = runBlocking { PermissionUtil().getPermissionCode(member) }

        /**
         * 比较bot和发言人的权限
         * @param event GroupMessageEvent 群事件
         * @return Boolean  bot权限大于发言人则返回true，否则返回false
         */
        fun botCompareToAuthor(event: GroupMessageEvent) =
            runBlocking { botPermission(event) > authorPermission(event) }

        /**
         * 比较bot和成员的权限
         * @param event GroupMessageEvent   群事件
         * @param member GroupMember    群成员
         * @return Boolean  bot权限大于成员则返回true，否则返回false
         */
        fun botCompareToMember(event: GroupMessageEvent, member: GroupMember) =
            runBlocking { botPermission(event) > memberPermission(member) }

        /**
         * 比较bot和成员的权限
         * @param event GroupMessageEvent   群事件
         * @param member Member    群成员
         * @return Boolean  bot权限大于成员则返回true，否则返回false
         */
        fun botCompareToMember(event: GroupMessageEvent, member: Member) =
            runBlocking { botPermission(event) > memberPermission(member) }
    }
}

enum class Permission(level: Int) {
    MEMBER(1), ADMINISTRATORS(2), OWNER(3)
}
