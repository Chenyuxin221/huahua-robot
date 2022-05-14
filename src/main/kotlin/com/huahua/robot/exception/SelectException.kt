package com.huahua.robot.exception

/**
 * ClassName: SelectException
 * @description 数据库查询异常
 * @author 花云端
 * @date 2022-04-27 13:51
 */
class SelectException : Exception {
    constructor()
    constructor(msg: String) : super(msg)
}
