package com.huahua.robot.listener.grouplistener

import com.google.gson.Gson
import com.huahua.robot.api.entity.Message
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.utils.MessageUtil
import love.forte.di.annotation.Beans
import love.forte.simbot.LoggerFactory
import love.forte.simbot.event.GroupMessageEvent
import okhttp3.*
import org.springframework.stereotype.Component
import java.io.IOException
import kotlin.reflect.jvm.jvmName


/**
 * ClassName: SaveGroupMessage
 * @description
 * @author 花云端
 * @date 2022-02-20 21:08
 */
@Component
class SaveGroupMessage {

    private val log = LoggerFactory.getLogger(SaveGroupMessage::class.jvmName)

    @RobotListen(desc = "消息存储服务")
    suspend fun GroupMessageEvent.saveMessage() {
        val url = "http://127.0.0.1:8080/text/uploadMessage"
        val encodeMessage = MessageUtil().encodeMessage(messageContent.messages)
        val message = Message(
            groupId = group().id.toString(),
            groupName = group().name,
            sendMsg = encodeMessage,
            sendUserCode = author().id.toString(),
            sendUserName = author().nickOrUsername,
            sendTime = System.currentTimeMillis())
        val str:String = Gson().toJson(message).toString()
        log.info(str)
        val body = RequestBody.create(MediaType.parse("application/json"), str)
        val request = Request.Builder()
            .post(body)
            .url(url)
            .addHeader("Content-Type", "application/json; charset=UTF-8")
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                log.error("Failed request api :(${e.message})")
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body()?.string()
                log.info(result)
            }

        })
    }
}