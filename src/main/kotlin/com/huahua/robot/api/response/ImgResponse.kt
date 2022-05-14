package com.huahua.robot.api.Response

import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

@NoArgsConstructor
@AllArgsConstructor
data class ImgResponse(
    val code: Int,
    val url: String?
)
