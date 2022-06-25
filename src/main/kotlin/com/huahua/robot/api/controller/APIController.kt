package com.huahua.robot.api.controller


import com.google.gson.Gson
import com.huahua.robot.api.mapper.PhotoMapper
import com.huahua.robot.api.response.ImgResponse
import com.huahua.robot.api.response.MsgResponse
import love.forte.simbot.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import kotlin.reflect.jvm.jvmName

/**
 * ClassName: APIController
 * @description
 * @author 花云端
 * @date 2022-04-27 10:27
 */
@RestController
@RequestMapping("/api")
class APIController(
    val photoMapper: PhotoMapper
) {
    private val log = LoggerFactory.getLogger(APIController::class.jvmName)

    @GetMapping("/photo")
    fun portrait(): String {
        val count = photoMapper.selectCount(null)
        if (count < 1) {
            return Gson().toJson(MsgResponse(404,"哎呀，啥都没有"))
        }
        val url = photoMapper.selectById(Random().nextInt(count.toInt()))?.url
        val result = Gson().toJson(ImgResponse(200,url))
        log.info(result)
        return result
    }

}