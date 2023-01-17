package com.huahua.robot.service.impl

import com.huahua.robot.core.common.logger
import com.huahua.robot.service.SwitchSateService
import org.springframework.boot.logging.LogLevel
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.stereotype.Service
import javax.annotation.Resource

/**
 * ClassName: SwitchSateServiceImpl
 * @description
 * @author 花云端
 * @date 2022-12-09 16:53
 */
@Service
class SwitchSateServiceImpl : SwitchSateService {

    @Resource
    lateinit var redisTemplate: RedisTemplate<String, Boolean>

    override fun set(groupId: String, switch: String, state: Boolean) {
        redisTemplate.stringSerializer = RedisSerializer.string()
        redisTemplate.keySerializer = RedisSerializer.string()
        redisTemplate.valueSerializer = RedisSerializer.json()
        val key = "${groupId}:$switch"
        redisTemplate.opsForValue().set(key, state)
    }

    override fun get(groupId: String, switch: String): Boolean? {
        redisTemplate.keySerializer = RedisSerializer.string()
        redisTemplate.valueSerializer = RedisSerializer.json()
        val key = "${groupId}:$switch"
        if (!redisTemplate.hasKey(key)) {
            logger(LogLevel.ERROR) {
                "未查询到key"
            }
            return null
        }

        return redisTemplate.opsForValue().get(key)
    }

    override fun delete(groupId: String, switch: String): Boolean? {
        redisTemplate.keySerializer = RedisSerializer.string()
        redisTemplate.valueSerializer = RedisSerializer.json()
        val key = "${groupId}:$switch"
        if (!redisTemplate.hasKey(key)) {
            logger(LogLevel.ERROR) {
                "未查询到key"
            }
            return null
        }
        return redisTemplate.delete(key)
    }

    override fun getKeys(pattern: String): MutableSet<String> = redisTemplate.keys(pattern)

    override fun getValue(key: String) = redisTemplate.opsForValue().get(key)


}