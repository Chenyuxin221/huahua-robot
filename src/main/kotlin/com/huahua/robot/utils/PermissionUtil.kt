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
     * 获取成员权限
     * @param member GroupMember    成员
     * @return Permission   权限枚举
     */
    private fun getPermissionCode(member: GroupMember) = runBlocking {
        if (member.isAdmin()) { //判断是否是管理员
            Permission.ADMINISTRATORS   //如果是管理员，则返回管理员权限码
        } else if (member.isOwner()) {  //判断是否是群主
            Permission.OWNER        //如果是群主，则返回群主权限码
        } else {                      //如果不是管理员，也不是群主，则返回普通成员权限码
            Permission.MEMBER    //如果不是管理员，也不是群主，则返回普通成员权限码
        }
    }

    /**
     * 获取成员权限
     * @param member Member 群成员
     * @return Permission   权限
     */
    private fun getPermissionCode(member: Member) = runBlocking {
        if (member.isAdmin()) { //判断是否为管理员
            Permission.ADMINISTRATORS   //如果是管理员，则返回管理员权限
        } else if (member.isOwner()) {  //判断是否为群主
            Permission.OWNER       //如果是群主，则返回群主权限
        } else {                      //如果不是管理员，也不是群主，则返回普通成员权限
            Permission.MEMBER    //如果不是管理员，也不是群主，则返回普通成员权限
        }
    }



    companion object {  //静态内部类

        /**
         * 获取bot在群中的权限
         * @receiver GroupMessageEvent  群消息事件
         * @return Permission   权限
         */
        fun GroupMessageEvent.botPermission() =
            runBlocking {
                PermissionUtil().getPermissionCode(
                    this@botPermission.group().member(this@botPermission.bot.id)!!  //获取群Bot的权限
                )
            }

        /**
         * 获取该成员在群中的权限
         * @receiver GroupMessageEvent  群消息事件
         * @return Permission   权限
         */
        fun GroupMessageEvent.authorPermission() =
            runBlocking { PermissionUtil().getPermissionCode(this@authorPermission.author()) }  //获取成员的权限

        /**
         * 获取群成员在群中的权限
         * @param member GroupMember    群成员
         * @return Permission   权限
         */
        fun memberPermission(member: GroupMember) =
            runBlocking { PermissionUtil().getPermissionCode(member) }  //获取成员的权限

        /**
         * 获取成员在群中的权限
         * @param member Member   群成员
         * @return Permission   权限
         */
        fun memberPermission(member: Member) = runBlocking { PermissionUtil().getPermissionCode(member) }   //获取成员的权限

        /**
         * 比较bot和该成员的权限
         * @receiver GroupMessageEvent  群消息事件
         * @return Boolean  是否有权限
         */
        fun GroupMessageEvent.botCompareToAuthor() =
            runBlocking { botPermission() > authorPermission() }   //比较bot和该成员的权限

        /**
         * 比较bot和群成员的权限
         * @receiver GroupMessageEvent  群消息事件
         * @param member GroupMember    群成员
         * @return Boolean  是否有权限
         */
        fun GroupMessageEvent.botCompareToMember(member: GroupMember) =
            runBlocking { botPermission() > memberPermission(member) }  //比较bot和群成员的权限

        /**
         *  比较bot和群成员的权限
         * @receiver GroupMessageEvent  群消息事件
         * @param member Member  群成员
         * @return Boolean  是否有权限
         */
        fun GroupMessageEvent.botCompareToMember(member: Member) =
            runBlocking { botPermission() > memberPermission(member) }  //比较bot和群成员的权限


    }
}

/**
 * 权限枚举
 */
enum class Permission(level: Int) { //权限枚举
    MEMBER(1),  //普通成员
    ADMINISTRATORS(2),  //管理员
    OWNER(3);   //群主
}
