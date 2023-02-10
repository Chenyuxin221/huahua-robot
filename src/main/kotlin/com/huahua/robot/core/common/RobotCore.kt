package com.huahua.robot.core.common


import com.huahua.robot.config.AccountConfig
import com.huahua.robot.config.RobotConfig
import com.huahua.robot.core.mapper.GroupBootStateMapper
import com.huahua.robot.utils.SpringContextUtil
import love.forte.simboot.spring.autoconfigure.EnableSimbot
import love.forte.simbot.ID
import love.forte.simbot.bot.Bot
import love.forte.simbot.event.EventListenerManager
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
@EnableSimbot
class RobotCore(
    private val listenerManager: EventListenerManager,
    private val groupBootStateMapper: GroupBootStateMapper,
    private val applicationContext: ApplicationContext,
    private val accountConfig: AccountConfig,
    private val robotConfig: RobotConfig,
) {
    @PostConstruct
    fun init() {
        setApplicationContext()
        initGroupBootMap()
        initCode()
    }

    private fun initCode() {
        ADMINISTRATOR = accountConfig.adminId
        BOTID = accountConfig.botId
        BOOTCOMMANDPATH = robotConfig.bootCommand
    }

    @Synchronized
    private fun setApplicationContext() {
        robotCore = this
        RobotCore.applicationContext = applicationContext
    }

    private fun initGroupBootMap() {
        groupBootStateMapper.selectList(null)?.forEach {
            BOOT_MAP[it.groupCode] = it.state
        }

    }


    companion object {
        lateinit var applicationContext: ApplicationContext

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

        var accountConfig: AccountConfig? = null

        var robotConfig: RobotConfig? = null

        /**
         * 机器人管理员
         */
        var ADMINISTRATOR = -1

        /**
         * 主机器人Id
         */
        var BOTID = -1
        var AiBLACKLIST = mutableListOf<String>()

        /**
         * 运行脚本路径
         */
        var BOOTCOMMANDPATH: String = ""

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
        var HaveReplied = hashMapOf<ID, Boolean>()

        /**
         * 短链接状态
         */
        var ShortLinkState = false


        /**
         * 点歌是否自动跳转
         */
        var MusicJump = false

        /**
         * 缓存群开关
         */
        val BOOT_MAP: MutableMap<String?, Boolean> = HashMap()

        var robotCore: RobotCore? = null

        private var bot: Bot? = null

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

        fun isBotAdministrator(accountCode: Int): Boolean {
            return ADMINISTRATOR == accountCode
        }

        fun setBot(bot: Bot) {
            this.bot = bot
        }

        fun getBot(): Bot {
            return bot!!
        }
    }
}

inline fun <T> T.isNull(block: () -> Unit): T {
    if (this == null) block()
    return this
}

fun <T> botBean(clazz: Class<T>) = SpringContextUtil().getBean(clazz)
inline fun Boolean.then(block: () -> Unit) = this.also { if (this) block() }
inline operator fun Boolean.invoke(block: () -> Unit) = this.then(block)
inline fun Boolean?.onElse(block: () -> Unit): Boolean = this.let {
    it?.not()?.then(block).isNull { block() }
    it ?: false
}

inline operator fun Boolean?.minus(block: () -> Unit) = this.onElse(block)

