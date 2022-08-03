package com.huahua.robot.core.common


import com.huahua.robot.core.mapper.GroupBootStateMapper
import love.forte.simbot.ID
import love.forte.simbot.bot.Bot
import love.forte.simbot.bot.OriginBotManager
import love.forte.simbot.event.EventListenerManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
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
class RobotCore (
    private val listenerManager: EventListenerManager
        ){

    @Autowired
    lateinit var mapper: GroupBootStateMapper

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

    @Value("\${huahua.account.admin.id}")
    private fun getAdminId(adminId: String) {
        ADMINISTRATOR = adminId
    }

    @Value("\${huahua.account.bot.id}")
    private fun getBotId(botId: String) {
        BOTID = botId.ID
    }

    @Value("\${huahua.config.boot-command}")
    private fun getBootCommandPath(path:String){
        BOOTCOMMANDPATH = path
    }


    companion object {
        var applicationContext: ApplicationContext? = null

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
        var ADMINISTRATOR: String = ""

        /**
         * 主机器人Id
         */
        var BOTID: ID = "".ID

        /**
         * 运行脚本路径
         */
        var BOOTCOMMANDPATH:String = ""

        /**
         * 用户Skey
         */
        var Skey = ""

        /**
         * 写真列表
         */
        var PhotoList = arrayListOf<String>()

        /**
         * 是否回复过消息
         */
        var HaveReplied = hashMapOf<ID,Boolean>()


        /**
         * 点歌是否自动跳转
         */
        var MusicJump = false

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

        fun getBot(): Bot {
            @Suppress("OPT_IN_USAGE")
            return OriginBotManager.getAnyBot()
        }
    }
}

inline fun <T> T.isNull(block: () -> Unit): T {
    if (this == null) block()
    return this
}

inline fun Boolean.then(block: () -> Unit) = this.also { if (this) block() }
inline operator fun Boolean.invoke(block: () -> Unit) = this.then(block)
inline fun Boolean?.onElse(block: () -> Unit): Boolean = this.let {
    it?.not()?.then(block).isNull { block() }
    it ?: false
}

inline operator fun Boolean?.minus(block: () -> Unit) = this.onElse(block)