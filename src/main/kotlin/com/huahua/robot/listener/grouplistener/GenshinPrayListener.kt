package com.huahua.robot.listener.grouplistener

import com.alibaba.fastjson2.JSON
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.then
import com.huahua.robot.utils.MessageUtil.Companion.getImageMessage
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.logger.LoggerFactory
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.message.buildMessages
import love.forte.simbot.message.toText
import love.forte.simbot.tryToLong
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.net.URLEncoder

/**
 * ClassName: GenshinPrayListener
 * @description
 * @author 花云端
 * @date 2022-09-14 20:10
 */
@Component
class GenshinPrayListener {
    @Value("\${huahua.config.genshinPray.code:#{null}}")
    val code: String? = ""

    val log = LoggerFactory.getLogger(GenshinPrayListener::class)
    val api = "http://127.0.0.1:8088"

    @RobotListen(desc = "单次抽卡", isBoot = true)
    @Filter("{{type}}单抽", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.rolePrayOne(@FilterValue("type") type: String) {
        val userId = author().id.tryToLong()
        var url = ""
        var prop = ""
        when (type) {
            "角色", "人物" -> {
                url = "${api}/api/RolePray/PrayOne?memberCode=$userId"
                prop = "纠缠之缘"
            }

            "武器" -> {
                url = "${api}/api/ArmPray/PrayOne?memberCode=$userId"
                prop = "纠缠之缘"
            }

            "常驻" -> {
                url = "${api}/api/PermPray/PrayOne?memberCode=$userId"
                prop = "相遇之缘"
            }

            "全角", "全角色", "全人物" -> {
                url = "${api}/api/FullRolePray/PrayOne?memberCode=$userId"
                prop = "告白之缘"
            }

            "全武器" -> {
                url = "${api}/api/FullArmPray/PrayOne?memberCode=$userId"
                prop = "告白之缘"
            }
        }
        val result = getImageFile(url)
        result.isEmpty().then { return }
        val data = JSON.parseObject(result)
        val imgUrl = "E:\\GenshinPray\\image\\${data.getString("imgPath")}"
        val text = when (type) {
            "角色", "人物" -> "本次祈愿消耗${data.getIntValue("prayCount")}个${prop}，距离下次小保底还有${
                data.getIntValue(
                    "role90Surplus"
                )
            }抽，大保底还剩${data.getIntValue("role180Surplus")}抽"

            "武器" -> "本次祈愿消耗${data.getIntValue("prayCount")}个${prop}，距离下次保底还有${data.getIntValue("arm80Surplus")}抽"
            "常驻" -> "本次祈愿消耗${data.getIntValue("prayCount")}个${prop}，距离下次保底还有${data.getIntValue("perm90Surplus")}抽"
            "全角", "全角色", "全人物" -> "本次祈愿消耗${data.getIntValue("prayCount")}个${prop}，距离下次保底还有${
                data.getIntValue(
                    "fullRole90Surplus"
                )
            }抽"

            "全武器" -> "本次祈愿消耗${data.getIntValue("prayCount")}个${prop}，距离下次保底还有${data.getIntValue("fullArm80Surplus")}抽"
            else -> ""
        }
        val message = buildMessages {
            +text.toText()
            +File(imgUrl).getImageMessage()
        }
        reply(message)
    }

    @RobotListen(desc = "十连抽卡", isBoot = true)
    @Filter("{{type}}十连", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.rolePrayTen(@FilterValue("type") type: String) {
        var url = ""
        var prop = ""
        val userId = author().id.tryToLong()
        when (type) {
            "角色", "人物" -> {
                url = "${api}/api/RolePray/PrayTen?memberCode=$userId"
                prop = "纠缠之缘"
            }

            "武器" -> {
                url = "${api}/api/ArmPray/PrayTen?memberCode=$userId"
                prop = "纠缠之源"
            }

            "常驻" -> {
                url = "${api}/api/PermPray/PrayTen?memberCode=$userId"
                prop = "相遇之缘"
            }

            "全角", "全角色", "全人物" -> {
                url = "${api}/api/FullRolePray/PrayTen?memberCode=$userId"
                prop = "告白之缘"
            }

            "全武器" -> {
                url = "${api}/api/FullArmPray/PrayTen?memberCode=$userId"
                prop = "告白之缘"
            }
        }
        val result = getImageFile(url)
        result.isEmpty().then { return }
        val data = JSON.parseObject(result)
        val imgUrl = "E:\\GenshinPray\\image\\${data.getString("imgPath")}"
        val text = when (type) {
            "角色", "人物" -> "本次祈愿消耗${data.getIntValue("prayCount")}个${prop}，距离下次小保底还有${
                data.getIntValue(
                    "role90Surplus"
                )
            }抽，大保底还剩${data.getIntValue("role180Surplus")}抽"

            "武器" -> "本次祈愿消耗${data.getIntValue("prayCount")}个${prop}，距离下次保底还有${data.getIntValue("arm80Surplus")}抽"
            "常驻" -> "本次祈愿消耗${data.getIntValue("prayCount")}个${prop}，距离下次保底还有${data.getIntValue("perm90Surplus")}抽"
            "全角", "全角色", "全人物" -> "本次祈愿消耗${data.getIntValue("prayCount")}个${prop}，距离下次保底还有${
                data.getIntValue(
                    "fullRole90Surplus"
                )
            }抽"

            "全武器" -> "本次祈愿消耗${data.getIntValue("prayCount")}个${prop}，距离下次保底还有${data.getIntValue("fullArm80Surplus")}抽"
            else -> ""
        }
        val message = buildMessages {
            +text.toText()
            +File(imgUrl).getImageMessage()
        }
        reply(message)
    }

    /**
     * 原神接口链接
     * @param url String
     */
    private fun getImageFile(url: String): String {
        url.isEmpty().then {
            log.error("url链接为空")
            return ""
        }
        val client = OkHttpClient()
        val request = Request.Builder().addHeader("Authorzation", code!!).url(url).build()
        val response = client.newCall(request).execute()
        val resJson = JSON.parseObject(response.body!!.string())
        if (resJson.getIntValue("code") != 0) {
            log.error(URLEncoder.encode(resJson.getString("message"), "utf-8"))
            return ""
        }
        val data = JSON.parseObject(resJson.getString("data"))
        return data.toJSONString()
    }


}