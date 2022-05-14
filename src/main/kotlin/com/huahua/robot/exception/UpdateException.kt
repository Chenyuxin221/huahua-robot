package com.huahua.robot.exception

/**
 * ClassName: UpdateException
 * @description 数据库更新异常
 * @author 花云端
 * @date 2022-04-27 13:33
 */
class UpdateException : Exception{
    constructor()
    constructor(msg:String):super(msg)
}
