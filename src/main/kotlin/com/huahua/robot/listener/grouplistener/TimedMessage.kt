package com.huahua.robot.listener.grouplistener

import com.alibaba.fastjson2.JSON
import com.huahua.robot.core.common.Sender
import com.huahua.robot.core.common.isNull
import com.huahua.robot.core.common.then
import com.huahua.robot.utils.FileUtil.getTempImage
import com.huahua.robot.utils.FileUtil.url
import com.huahua.robot.utils.HttpUtil
import love.forte.simbot.logger.LoggerFactory
import love.forte.simbot.message.Image.Key.toImage
import love.forte.simbot.resources.Resource.Companion.toResource
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * ClassName: TimedMessage
 * @description
 * @author 花云端
 * @date 2022-08-20 13:26
 */
@Component
class TimedMessage {
    private val log = LoggerFactory.getLogger(TimedMessage::class)
    private val sender = Sender

    @Value("\${huahua.config.morningPaper.groups:#{null}}")
    val groups: List<String>? = arrayListOf()

    @Scheduled(cron = "0 0 8 * * ?")
    fun dailyMorningPaper() {
        groups.isNullOrEmpty().then { return }
        val url = "https://api.03c3.cn/zb/api.php"
        val body = JSON.parseObject(HttpUtil.get(url).response)
        val data = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        (body.getIntValue("code") != 200).then { return }
        "${data}.png".getTempImage(body.getString("imageUrl").url())?.also {
            groups!!.forEach { str ->
                sender.sendGroupMsg(str, it.toResource().toImage())
            }
        }.isNull { log.error("接口失效！") }!!.delete()
    }

}