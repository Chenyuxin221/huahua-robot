package com.huahua.robot.entity

import lombok.Data
import org.apache.http.cookie.Cookie

/**
 * ClassName: RequestEntity
 * @description
 * @author 花云端
 * @date 2022-05-08 0:55
 */
@Data
data class RequestEntity(
    var cookies: List<Cookie>? = null,
    var response: String? = null
)
