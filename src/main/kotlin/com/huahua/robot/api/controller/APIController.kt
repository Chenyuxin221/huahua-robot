package com.huahua.robot.api.controller

import com.huahua.robot.api.entity.Photo
import com.huahua.robot.api.mapper.PhotoMapper
import com.huahua.robot.api.result.Result
import com.huahua.robot.api.result.ResultStatus
import love.forte.simbot.logger.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import kotlin.reflect.jvm.jvmName

/**
 * 通用API接口
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
    fun portrait(): Result<out Photo?> {
        val count = photoMapper.selectCount(null)
        if (count < 1) {
            return Result.failure(ResultStatus.DATA_IS_EMPTY)
        }
        val url = photoMapper.selectById(Random().nextInt(count.toInt()))?.url
            ?: return Result.failure(ResultStatus.DATA_IS_EMPTY)
        return Result.success(Photo(url = url))
    }

}