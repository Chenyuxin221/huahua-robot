package com.huahua.robot.api.result

import lombok.Getter
import lombok.ToString

/**
 * ClassName: Result
 * @description
 * @author 花云端
 * @date 2022-10-06 20:12
 */
@ToString
@Getter
class Result<T> constructor(
    /** 返回状态码 */
    val code: Int,
    /** 返回信息 */
    val msg: String,
    /** 返回参数 */
    val data: T,
    val time: Long,
) {
    companion object {
        private val timeTamp = System.currentTimeMillis()

        /**
         * 成功
         * @return Result<Nothing?> nothing
         */
        fun success() = Result(ResultStatus.SUCCESS.code, ResultStatus.SUCCESS.msg, null, timeTamp)
        fun <T> success(data: T) = Result(ResultStatus.SUCCESS.code, ResultStatus.SUCCESS.msg, data, timeTamp)
        fun <T> success(resultStatus: ResultStatus, data: T) =
            Result(resultStatus.code, resultStatus.msg, data, timeTamp)

        /**
         * 自定义成功返回
         * @param restCode ResultCode
         * @param data T
         * @return Result<T>
         */
        fun <T> success(restCode: ResultCode, data: T) = Result(restCode.code, restCode.message, data, timeTamp)


        /**
         * 失败
         * @param data T
         * @return Result<T>
         */
        fun failure(resultStatus: ResultStatus) = Result(resultStatus.code, resultStatus.msg, null, timeTamp)
        fun <T> failure(data: T) = Result(ResultStatus.SUCCESS.code, ResultStatus.SUCCESS.msg, data, timeTamp)
        fun <T> failure(resultStatus: ResultStatus, data: T) =
            Result(resultStatus.code, resultStatus.msg, data, timeTamp)

        /**
         * 自定义失败返回
         * @param restCode ResultCode
         * @return Result<Nothing?>
         */
        fun failure(restCode: ResultCode) = Result(restCode.code, restCode.message, null, timeTamp)

    }
}