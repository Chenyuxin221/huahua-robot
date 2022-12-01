package com.huahua.robot.api.controller

import com.huahua.robot.api.entity.Message
import com.huahua.robot.api.mapper.MessageMapper
import com.huahua.robot.api.response.MessageResponse
import com.huahua.robot.api.result.Result
import com.huahua.robot.api.result.ResultCode
import com.huahua.robot.api.result.ResultStatus
import love.forte.simbot.logger.LoggerFactory
import org.springframework.web.bind.annotation.*

/**
 * ClassName: MessageController 没啥用了 数据懒得取了
 * @description
 * @author 花云端
 * @date 2022-04-26 21:44
 */
@RestController
@RequestMapping("text")
class MessageController(
    val messageMapper: MessageMapper
) {

    private val log = LoggerFactory.getLogger(MessageController::class)

    /**
     *
     * @param body Message  需要上传的消息对象
     * @return String   json格式的字符串
     */
    @PostMapping("/uploadMessage")
    fun uploadMessage(@RequestBody body: Message): Result<out ResultCode?> {
        val result: Int = messageMapper.insert(body)
        if (result < 1)
            return Result.failure(ResultCode(404, "插入失败，未知错误"))
        return Result.success(ResultCode(200, "插入♂成功"))
    }

    /**
     * 获取成员记录的最后一条记录
     * @param groupId String    群号
     * @param userId String     qq号
     * @param sendTime Long     退群时间
     * @return MessageResponse  消息结构体
     * @see MessageResponse
     */
    @GetMapping("/getMessage")
    fun getMessage(
        @RequestParam("groupId") groupId: String,
        @RequestParam("userId") userId: String,
        @RequestParam("Time") sendTime: Long,
    ): Result<out Message?> {
        val map: HashMap<String, Any> = hashMapOf()
        map["groupId"] = groupId
        map["sendUserCode"] = userId
        //取到退群前发送的最后一条消息
        val messageList: MutableList<Message?>? = messageMapper.selectByMap(map)
        val message = if (messageList?.isNotEmpty() == true) {
            messageList.first()
        } else {
            return Result.failure(ResultStatus.DATA_IS_EMPTY)
        }
        if (message!=null){
            return Result.success(message)
        }
        return Result.failure(ResultStatus.DATA_IS_EMPTY)
    }
}

