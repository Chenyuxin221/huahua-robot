package com.huahua.robot.api.response

import com.google.gson.Gson
import com.huahua.robot.api.enums.RestCode
import lombok.Data

/**
 * ClassName: RestResponse
 * @description
 * @author 花云端
 * @date 2022-06-02 19:33
 */
@Data
class RestResponse<T> constructor(
    private val code: Int = RestCode.OK.code,
    private val msg: String = RestCode.OK.msg,
) {
    private var result: T? = null

    companion object {

        fun <T> success() = RestResponse<T>()

        fun <T> success(result: T): RestResponse<T> {
            val response = RestResponse<T>()
            response.result = result
            return response
        }

        fun <T> error(code: RestCode): RestResponse<T> {
            return RestResponse(code.code, code.msg)
        }
    }
}