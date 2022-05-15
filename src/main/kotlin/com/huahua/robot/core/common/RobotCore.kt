@file:Suppress("MemberVisibilityCanBePrivate")
package com.huahua.robot.core.common


import com.huahua.robot.core.mapper.GroupBootStateMapper
import love.forte.simbot.Bot
import love.forte.simbot.OriginBotManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.File
import java.util.concurrent.*
import javax.annotation.PostConstruct

/**
 * @author wuyou,花云端
 */
@Suppress("unused")
@Order(1)
@Component
 class RobotCore {

    @Autowired
    lateinit var mapper:GroupBootStateMapper


    @PostConstruct
    fun init() {
        setApplicationContext()
        initGroupBootMap()
    }

    @Synchronized
    private fun setApplicationContext() {
        robotCore = this
    }

    private fun initGroupBootMap() {
        mapper.selectList(null)?.forEach {
            BOOT_MAP[it.groupCode] = it.state
        }

    }

    companion object {

        private val bot: Bot? = OriginBotManager.getAnyBot()

        /**
         * 项目名
         */
        const val PROJECT_NAME: String = "huahua-robot"

        /**
         * 项目路径
         */
        val PROJECT_PATH: String = System.getProperty("user.home") + File.separator

        /**
         * 临时路径
         */
        val TEMP_PATH: String = System.getProperty("java.io.tmpdir") + File.separator + PROJECT_NAME + File.separator

        /**
         * python路径
         */
        var PYTHON_PATH: String? = null

        /**
         * 线程池
         */
        var THREAD_POOL: ExecutorService? = null

        /**
         * 机器人管理员
         */
        val ADMINISTRATOR: List<String> = ArrayList(listOf("1849950046"))

        /**
         * 缓存群开关
         */
        val BOOT_MAP: MutableMap<String?, Boolean> = HashMap()

        var robotCore: RobotCore? = null

        init {
            val pythonEnvPath = "venv"
            PYTHON_PATH = if (File(PROJECT_PATH + pythonEnvPath).exists()) {
                PROJECT_PATH + "venv" + File.separator + "Scripts" + File.separator + "python"
            } else {
                null
            }
            THREAD_POOL = ThreadPoolExecutor(
                50,
                50,
                200,
                TimeUnit.SECONDS,
                LinkedBlockingQueue(50),
                ThreadFactory { Thread() })
        }

        fun isBotAdministrator(accountCode: String): Boolean {
            return ADMINISTRATOR.contains(accountCode)
        }
        fun getBot(): Bot?{
            return bot
        }
    }
}