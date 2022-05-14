package com.huahua.robot.core.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import love.forte.simbot.Bot
import love.forte.simbot.ID
import love.forte.simbot.OriginBotManager
import love.forte.simbot.definition.Group


/**
 * @author wuyou
 */
@Suppress("unused")
class Sender private constructor() {
    companion object {
        private val bot: Bot? = OriginBotManager.getAnyBot()


        fun send(group: Group, message: String) {
            CoroutineScope(Dispatchers.Default).launch {
                group.send(message)
            }
        }

        fun send(group: String, message: String) {
            send(group.ID, message)
        }

        private fun send(group: ID, message: String) {
            CoroutineScope(Dispatchers.Default).launch {
                bot?.group(group)?.let { send(it, message) }
            }
        }
    }
}