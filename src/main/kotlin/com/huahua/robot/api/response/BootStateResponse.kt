package com.huahua.robot.api.response

import com.huahua.robot.api.entity.GroupBootState

data class BootStateResponse(
    val code: Int,
    val data: GroupBootState?,
    val msg: String
)
