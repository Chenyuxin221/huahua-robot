package com.huahua.robot.api.controller

import com.google.gson.Gson
import com.huahua.robot.api.Response.MessageResponse
import com.huahua.robot.api.Response.MsgResponse
import com.huahua.robot.api.entity.Message
import com.huahua.robot.api.mapper.MessageMapper
import love.forte.simbot.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import kotlin.reflect.jvm.jvmName

/**
 * ClassName: MessageController
 * @description
 * @author 花云端
 * @date 2022-04-26 21:44
 */
@RestController
@RequestMapping("text")
class MessageController {

    @Autowired lateinit var messageMapper: MessageMapper
    private val log = LoggerFactory.getLogger(MessageController::class.jvmName)

    @PostMapping("/uploadMessage")
    fun uploadMessage(@RequestBody body: Message): String {
        val result:Int = messageMapper.insert(body)
        if (result<1)
            return Gson().toJson(MsgResponse(404,"哎呀，插入数据失败了"))
        return Gson().toJson(MsgResponse(200,"插入♂成功"))
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
    ): MessageResponse {
        val simbotV2LastMessageTime = 1651939839425L //数据库2.x最后一条聊天记录发送时间
        val isOldData = simbotV2LastMessageTime >= sendTime
        val map: HashMap<String, Any> = hashMapOf()
        map["groupId"] = groupId
        map["sendUserCode"] = userId
        //取到退群前发送的最后一条消息
        val messageList: MutableList<Message?>? = messageMapper.selectByMap(map)
        val message = if (messageList?.isNotEmpty() == true){
            messageList.first()
        }else{
            return MessageResponse(404,Message(),0)
        }
        if (message!=null){
            if (isOldData) {
                return MessageResponse(200,message,2)
            }
            return MessageResponse(200,message,3)
        }
        return MessageResponse(404,Message(),0)
    }
}