package com.huahua.robot.listener

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONException
import com.github.plexpt.chatgpt.Chatbot
import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.core.common.isNull
import com.huahua.robot.core.common.logger
import com.huahua.robot.core.common.send
import com.huahua.robot.service.SwitchSateService
import com.huahua.robot.utils.Permission
import com.huahua.robot.utils.PermissionUtil.Companion.authorPermission
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.ID
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.event.MessageEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.logging.LogLevel
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.stereotype.Component
import javax.annotation.Resource


/**
 * 或许已失效.
 */

/**
 * ClassName: ChatGptListener
 * @description
 * @author 花云端
 * @date 2022-12-12 21:15
 */
@Component
class ChatGptListener(
    val switchSateService: SwitchSateService,
) {
    /**
     * 获取配置项里面的token和cfClearance
     */
    @Value("\${huahua.config.gpt.token:#{null}}")
    var chatGtpToken: String? = ""

    @Value("\${huahua.config.gpt.cf_clearance:#{null}}")
    var cfClearance: String? = ""

    @Resource
    lateinit var redisTemplate: RedisTemplate<String, String>

    /**
     *
     */
    val map = hashMapOf<String, Chatbot>()

    @RobotListen("ChatGpt", isBoot = true)
    @Filter(">{{questions}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.chatGpt(@FilterValue("questions") questions: String) {
        try {
            if (this is GroupMessageEvent) {
                val result = switchSateService.get(group().id.toString(), "聊天")
                if (result == null) {  //没有将群加入缓存
                    switchSateService.set(group().id.toString(), "聊天", false)
                    return
                }
                if (!result) {   //处于关闭状态
                    logger { "当前群未开启此功能" }
                    reply("当前群未开启此功能")
                    return
                }
            }
            redisTemplate.valueSerializer = RedisSerializer.json()
            var tokenTemplate = redisTemplate.opsForValue().get("chatGtpToken") //读取缓存里的token
            var cfClearanceTemplate = redisTemplate.opsForValue().get("chatGtpCfClearance") //读取缓存里的cfClearance
            if (tokenTemplate.isNullOrEmpty() || cfClearanceTemplate.isNullOrEmpty()) {   //没有token或cfClearance缓存
                if (chatGtpToken == null) {    //没有配置token
                    logger(LogLevel.ERROR) {
                        "如需要此功能，请配置token或者使用指令（token绑定+Token）"
                    }
                    return
                }
                if (cfClearance == null) { //没有配置cfClearance
                    logger(LogLevel.ERROR) {
                        "如需要此功能，请配置cfClearance或者使用指令（cf绑定+cfClearance）"
                    }
                    return
                }
                redisTemplate.opsForValue().set("chatGtpToken", chatGtpToken!!) //将token缓存到redis
                redisTemplate.opsForValue().set("chatGtpCfClearance", cfClearance!!)
            }
            tokenTemplate = redisTemplate.opsForValue().get("chatGtpToken") // 重新读取缓存内的token
            cfClearanceTemplate = redisTemplate.opsForValue().get("chatGtpCfClearance")
            val userAgent =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36 Edg/108.0.1462.46"
            val chatBot = when (this) {
                is GroupMessageEvent -> {
                    val bot = map["${group().id}:${author().id}"]
                    if (bot == null) {
                        map["${group().id}:${author().id}"] = Chatbot(tokenTemplate, cfClearanceTemplate, userAgent)
                    }
                    map["${group().id}:${author().id}"]
                }

                is FriendMessageEvent -> {
                    val bot = map[friend().id.toString()]
                    if (bot == null) {
                        map[friend().id.toString()] = Chatbot(tokenTemplate, cfClearanceTemplate, userAgent)
                    }
                    map[friend().id.toString()]
                }

                else -> null
            }
            chatBot.isNull {
                reply("哎呀，chatBot初始化失败了")
                return
            }
            val response = chatBot!!.getChatResponse(questions)
            var message = response["message"].toString()
            if (message.isEmpty()) {
                logger { JSON.toJSONString(response) }
                return
            }
            if (message == "null") {
                message = "请求过于频繁，请稍后再试..."
            }
            reply(message)
        } catch (e: JSONException) {
            if (map.isNotEmpty()) {
                map.clear()
            }
            reply("哎呀，cookie失效或openAI更新了")
        } catch (e: Exception) {
            reply(e.message ?: "出错了")
            e.printStackTrace()
        }
    }

    @RobotListen("token绑定", isBoot = true)
    @Filter("token绑定{{token}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.bindingToken(@FilterValue("token") token: String) {
        val permission = authorOwnOrNotPermission(this)
        if (!permission) {
            send("哎呀，你的权限不足,无法绑定你的token呢")
            return
        }

        redisTemplate.opsForValue().set("chatGtpToken", token) // 将接收到的token缓存进redis
        val result = redisTemplate.opsForValue().get("chatGtpToken")
        if (result.isNullOrEmpty()) {
            reply("qaq绑定失败了~")
            return
        }
        reply("绑定成功,你现在的Token为：$result")

    }

    @RobotListen("cfClearance绑定", isBoot = true)
    @Filter("cf绑定{{cfClearance}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.bindingCfClearance(@FilterValue("cfClearance") cfClearance: String) {
        val permission: Boolean = authorOwnOrNotPermission(this)
        if (!permission) {
            send("哎呀，你的权限不足,无法绑定你的cfClearance呢")
            return
        }

        redisTemplate.opsForValue().set("chatGtpCfClearance", cfClearance) // 将接收到的token缓存进redis
        val result = redisTemplate.opsForValue().get("chatGtpCfClearance")
        if (result.isNullOrEmpty()) {
            reply("qaq绑定失败了~")
            return
        }
        reply("绑定成功,你现在的chatGtpCfClearance为：$result")
    }


    /**
     * 绑定权限
     * @param event MessageEvent
     * @return Boolean
     */
    private suspend fun authorOwnOrNotPermission(event: MessageEvent) = when (event) {
        is GroupMessageEvent -> event.author().id == RobotCore.ADMINISTRATOR.ID || event.authorPermission() != Permission.MEMBER
        is FriendMessageEvent -> event.friend().id == RobotCore.ADMINISTRATOR.ID
        else -> false
    }
}