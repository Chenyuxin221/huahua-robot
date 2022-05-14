package com.huahua.robot.core.enums

import love.forte.simbot.definition.Role

/**
 * 权限
 * @author wuyou
 */
enum class RobotPermission(private val level: Int) {
    MEMBER(0b1), ADMINISTRATOR(0b10), OWNER(0b100);

    operator fun compareTo(role: Role): Int {
        return if (this.level > RobotPermission.valueOf(role.name).level) 1 else -1
    }
}