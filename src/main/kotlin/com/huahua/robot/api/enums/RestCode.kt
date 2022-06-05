package com.huahua.robot.api.enums

/**
 * ClassName: RestCode
 * @description
 * @author 花云端
 * @date 2022-06-02 19:41
 */
enum class RestCode(val code: Int, val msg: String) {
    /**
     * 返回的状态码和msg
     */
    OK(200, "成功"),
    /**
     * 未知异常(有未处理的异常)
     */
    UNKNOWN_ERROR(404, "未知异常"),
    /**
     * 数据未找到
     */
    DATA_NOT_FOUND(404, "数据不存在"),
    /**
     * 数据已存在
     */
    DATA_ALREADY_EXIST(404, "数据已存在")

}