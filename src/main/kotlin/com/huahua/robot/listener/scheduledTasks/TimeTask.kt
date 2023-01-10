package com.huahua.robot.listener.scheduledTasks

import com.alibaba.fastjson2.JSON
import com.huahua.robot.core.common.Sender
import com.huahua.robot.core.common.isNull
import com.huahua.robot.core.common.logger
import com.huahua.robot.core.common.then
import com.huahua.robot.service.SwitchSateService
import com.huahua.robot.utils.FileUtil.getTempImage
import com.huahua.robot.utils.FileUtil.url
import com.huahua.robot.utils.HttpUtil
import love.forte.simbot.message.Image.Key.toImage
import love.forte.simbot.resources.Resource.Companion.toResource
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.logging.LogLevel
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
class TimeTask(
    val switchSateService: SwitchSateService,
) {


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
                Sender.sendGroupMsg(str, it.toResource().toImage())
            }
        }.isNull { logger(LogLevel.ERROR) { "哎呀，接口失效了" } }!!.delete()
    }

    /**
     * 每日凌晨将缓存数据上传至数据库
     * 尚未测试
     */
    @Scheduled(cron = "0 0 0 * * ?")
    fun updateMysql() {
        val keys = switchSateService.getKeys("*")
        keys.forEach {
            val value = switchSateService.getValue(it).toString()
            value.isEmpty().then { return }
            val temp = it.split(":")
            val groupId = temp[0]
            val func = temp[1]
            val url = "http://127.0.0.1:8080/config/switch/set?groupId=${groupId}&func=${func}&state=${value}"
            val result = HttpUtil.get(url).response
            try {
                val json = JSON.parseObject(result)
                if (json.getIntValue("code") != 200) {
                    Sender.sendAdminMsg("更新失败")
                    return
                }
            } catch (e: Exception) {
                Sender.sendAdminMsg("更新失败：${e.message}")
                e.printStackTrace()
            }
        }
    }


}