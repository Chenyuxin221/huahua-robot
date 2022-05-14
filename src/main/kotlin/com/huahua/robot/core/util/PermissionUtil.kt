package com.huahua.robot.core.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import love.forte.simbot.definition.Role

/**
 * 权限工具类
 * @author wuyou
 */
class PermissionUtil private constructor() {

    companion object {
        suspend fun compare(role: Flow<Role>, permission: RobotPermission): Boolean {
            return RobotRole.valueOf(role.first().name).level.and(permission.level) > 0
        }
    }
}

enum class RobotPermission(val level: Int) {
    MEMBER(0b1), ADMINISTRATOR(0b10), OWNER(0b100);
}

enum class RobotRole(val level: Int) {
    MEMBER(0b1), ADMINISTRATOR(0b11), OWNER(0b111);
}