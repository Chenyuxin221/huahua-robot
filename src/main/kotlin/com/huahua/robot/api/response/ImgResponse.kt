package com.huahua.robot.api.response

import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

@NoArgsConstructor
@AllArgsConstructor
data class ImgResponse(
    val code: Int,
    val url: String?
)
