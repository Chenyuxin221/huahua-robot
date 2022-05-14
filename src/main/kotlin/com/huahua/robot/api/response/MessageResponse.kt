package com.huahua.robot.api.Response

import com.huahua.robot.api.entity.Message

data class MessageResponse(
    val code: Int,
    val msg: Message?,
    val version: Int,
)
