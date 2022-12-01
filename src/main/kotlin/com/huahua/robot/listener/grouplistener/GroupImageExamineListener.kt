package com.huahua.robot.listener.grouplistener

import com.alibaba.fastjson2.JSON
import com.baidubce.http.ApiExplorerClient
import com.baidubce.http.HttpMethodName
import com.baidubce.model.ApiExplorerRequest
import com.huahua.robot.api.entity.Porn
import com.huahua.robot.api.mapper.PornMapper
import com.huahua.robot.config.Baidubce
import com.huahua.robot.config.Config
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.isNull
import com.huahua.robot.core.common.send
import com.huahua.robot.core.common.then
import com.huahua.robot.utils.FileUtil.getTempImage
import com.huahua.robot.utils.HttpUtil
import com.huahua.robot.utils.PermissionUtil.Companion.botCompareToAuthor
import love.forte.simbot.ID
import love.forte.simbot.logger.LoggerFactory
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.message.Image
import org.springframework.stereotype.Component
import org.springframework.util.DigestUtils
import java.io.FileInputStream
import java.lang.String.format
import java.util.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes


/**
 * ClassName: GroupImageExamineListener
 * @description
 * @author 花云端
 * @date 2022-08-16 19:38
 */
@Component
class GroupImageExamineListener(
    val mapper: PornMapper,
) {
    private val log = LoggerFactory.getLogger(GroupImageExamineListener::class)

    @RobotListen(isBoot = true, desc = "图像审核")
    suspend fun GroupMessageEvent.imageTExamine() {
        Config.baidubce.isNull {    //判断是否有配置项
            return
        }
        val list = Config.exclude_groups
        val id = Baidubce.client_id
        val secret = Baidubce.client_secret
        messageContent.messages.forEach {
            if (it is Image) {
                list.isNotEmpty().then {
                    list.forEach { str ->
                        if (group().id == str.ID) {
                            log.info("已过滤群--->${group().name}")
                            return
                        }
                    }
                }
                val img = it.resource().name
                val file = "${UUID.randomUUID()}.jpg".getTempImage(it.resource().openStream().readAllBytes())
                file.isNull {
                    send("图片存储失败啦")
                    return
                }
                val fis = FileInputStream(file!!)
                val md5 = DigestUtils.md5Digest(fis).joinToString("") { format("%02X", it) }
                fis.close()
                file.delete()
                val result = mapper.selectByMap(mapOf(Pair("md5", md5)))
                if (result.size > 0) {
                    val data = result.first()
                    when (data.type) {
                        1 -> return
                        2 -> {
                            if (botCompareToAuthor()) {
                                send("检测到${data.tip}，已经撤回")
                                author().mute((30).minutes)
                                messageContent.delete()
                            } else {
                                send("检测到${data.tip}，请主动撤回")
                            }
                            return
                        }

                        3 -> {
                            if (botCompareToAuthor()) {
                                send("检测到涩图，已经撤回")
                                author().mute((24).hours)
                                messageContent.delete()
                            } else {
                                send("检测到涩图，请主动撤回")
                            }
                            return
                        }
                    }
                }
                val token = getToken(id, secret)
                val api = "https://aip.baidubce.com/rest/2.0/solution/v1/img_censor/v2/user_defined"
                val request = ApiExplorerRequest(HttpMethodName.POST, api)
                request.addQueryParameter("access_token", token)
                request.addHeaderParameter("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                request.setJsonBody("imgUrl=$img")
                val client = ApiExplorerClient()
                try {
                    val response = client.sendRequest(request)
                    val jsResponse = JSON.parseObject(response.result)
                    log
                    when (jsResponse["conclusion"]) {
                        "合规" -> {
                            mapper.insert(Porn(url = img, md5 = md5, type = 1, tip = "正常"))
                            return
                        }

                        "疑似" -> {
                            mapper.insert(Porn(url = img, md5 = md5, type = 2, tip = "疑似存在低俗内容"))
                            if (botCompareToAuthor()) {
                                send("检测到疑似涩图，已经撤回")
                                author().mute((30).minutes)
                                messageContent.delete()
                            } else {
                                send("检测到疑似涩图，请主动撤回")
                            }
                            return
                        }

                        "不合规" -> {
                            mapper.insert(Porn(url = img, md5 = md5, type = 3, tip = "色情图片"))
                            if (botCompareToAuthor()) {
                                send("检测到涩图，已经撤回")
                                author().mute((24).hours)
                                messageContent.delete()
                            } else {
                                send("检测到涩图，请主动撤回")
                            }
                            return
                        }
                    }
                } catch (e: Exception) {
                    send(e.printStackTrace())
                }
            }
        }
    }

    fun getToken(id: String, secret: String): String {
        val body = HttpUtil.post {
            url = "https://aip.baidubce.com/oauth/2.0/token"
            params = {
                map["grant_type"] = "client_credentials"
                map["client_id"] = id
                map["client_secret"] = secret
            }
        }
        return JSON.parseObject(body.response).getString("access_token")
    }
}