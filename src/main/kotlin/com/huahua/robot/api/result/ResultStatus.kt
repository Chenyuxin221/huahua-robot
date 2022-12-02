package com.huahua.robot.api.result

import lombok.Getter
import lombok.ToString
import org.springframework.http.HttpStatus

@ToString
@Getter
enum class ResultStatus(var code: Int, var msg: String){
    SUCCESS(200, "成功"),
    USER_EXISTS(401,"该用户已存在"),
    USER_NOT_EXISTS(402,"该用户不存在"),
    DATA_IS_EMPTY(403,"数据为空"),
    DATA_EXIST(401,"数据已存在"),
    BAD_REQUEST(400, "坏的请求"),
    INTERNAL_SERVER_ERROR(500, "内部服务器错误"),
    UNKNOWN_ERROR(404,"未知错误"),
    OPERATION_FAILED(404,"操作失败")

}
