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
class Result<T>(
    /** 返回状态码 */
    val code: Int,
    /** 返回信息 */
    val msg: String,
    /** 返回参数 */
    val data: T,
    timesTamp: Long,
) {
    companion object {
        private val time = System.currentTimeMillis()

        /**
         * 成功
         * @return Result<Nothing?> nothing
         */
        fun success() = Result(ResultStatus.SUCCESS.code, ResultStatus.SUCCESS.msg, null, time)
        fun <T> success(data: T) = Result(ResultStatus.SUCCESS.code, ResultStatus.SUCCESS.msg, data, time)
        fun <T> success(resultStatus: ResultStatus, data: T) =
            Result(resultStatus.code, resultStatus.msg, data, time)

        /**
         * 自定义成功返回
         * @param restCode ResultCode
         * @param data T
         * @return Result<T>
         */
        fun <T> success(restCode: ResultCode, data: T) = Result(restCode.code, restCode.message, data, time)


        /**
         * 失败
         * @param data T
         * @return Result<T>
         */
        fun failure(resultStatus: ResultStatus) = Result(resultStatus.code, resultStatus.msg, null, time)
        fun <T> failure(data: T) = Result(ResultStatus.SUCCESS.code, ResultStatus.SUCCESS.msg, data, time)
        fun <T> failure(resultStatus: ResultStatus, data: T) =
            Result(resultStatus.code, resultStatus.msg, data, time)

        /**
         * 自定义失败返回
         * @param restCode ResultCode
         * @return Result<Nothing?>
         */
        fun failure(restCode: ResultCode) = Result(restCode.code, restCode.message, null, time)

    }
}