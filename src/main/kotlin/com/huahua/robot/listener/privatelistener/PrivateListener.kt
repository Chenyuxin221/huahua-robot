package com.huahua.robot.listener.privatelistener

import com.huahua.robot.core.annotation.RobotListen
import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.core.common.isNull
import com.huahua.robot.core.common.send
import com.huahua.robot.core.common.then
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import love.forte.di.annotation.Beans
import love.forte.simboot.annotation.Filter
import love.forte.simboot.filter.MatchType
import love.forte.simbot.ID
import love.forte.simbot.LoggerFactory
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.message.Image
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.reflect.jvm.jvmName
import kotlin.system.exitProcess

@Beans
class PrivateListener {
    val log = LoggerFactory.getLogger(PrivateListener::class.jvmName)

    @RobotListen(desc = "图片直链")
    suspend fun FriendMessageEvent.sendImageUrl() {
        messageContent.messages.forEach {
            if (it is Image) {
                val resource = it.resource()
                send("图片直链：\n${resource.name}")
                log.info("用户ID：${friend().id}\t图片地址:${resource.name}")
            }
        }
    }

    @RobotListen(desc = "解除禁言")
    @Filter("解", matchType = MatchType.TEXT_STARTS_WITH)
    suspend fun FriendMessageEvent.unmute() {
        if (friend().id != RobotCore.ADMINISTRATOR.ID) {
            return
        }
        val reg = """\d+""".toRegex().find(messageContent.plainText)
        reg?.let{
          val groupId = it.value.ID
          val result = bot.group(groupId)?.member(friend().id)?.unmute()
            result?.then {
                send("解禁成功！")
            }.isNull {
                send("解除禁言失败，请检查群号是否正确")
            }
        }.isNull {
            send("没有匹配到群号！")
            return
        }

    }

    @RobotListen(desc = "重启服务")
    @Filter(".restart")
    suspend fun FriendMessageEvent.restartBot() {
        (friend().id == RobotCore.ADMINISTRATOR.ID).then {
            val command = "CMD /C START CMD /K ${RobotCore.BOOTCOMMANDPATH}"
            Runtime.getRuntime().exec(command)
            exitProcess(0)
        }
    }

    /**
     * 慎用！！！
     * 重启后可能会导致bot失联
     * 如要使用注意以下几点：
     *  1、 脚本里切换工作目录到脚本当前目录
     *  2、 bot自启问题解决：
     *      1） 移除系统登录密码 使系统开机自动进入桌面
     *      2） 将脚本添加到win自带计划任务
     * @receiver FriendMessageEvent
     */
//    @RobotListen(desc = "系统重启服务")
//    @Filter("重启电脑")
    suspend fun FriendMessageEvent.restartSystem() {
        if (friend().id != RobotCore.ADMINISTRATOR.ID) {
            send("可恶，你自己重开吧")
            return
        }
        creatBootLink().then {
            val command = "cmd /c shutdown -r -t 0"
            withContext(Dispatchers.IO) {
                Runtime.getRuntime().exec(command)
            }
            send("正在重启系统...")
            exitProcess(0)
        }
    }


    private suspend fun FriendMessageEvent.creatBootLink(): Boolean {
        val systemName = System.getProperties().getProperty("os.name").split(" ")[0]
        if (systemName != "Windows") {
            send("该功能仅限Windows客户端使用")
            return false
        }
        val startupPath = System.getenv("STARTUP")
        startupPath.isNull {
            log.error("请前往配置环境变量，变量名为 STARTUP, 值为系统启动目录")
            return false
        }
        val bootFile = "F:\\botRunner\\bot.bat"
        val fileName = File(bootFile).name
        val files = File(startupPath).listFiles()
        var boolean = false
        if (files != null && files.isNotEmpty()) {
            files.forEach {
                if (it.name == fileName) {
                    boolean = true
                }
            }
        }
        boolean.not().then {
            val link = Paths.get("${startupPath}\\${fileName}")
            val file = Paths.get(bootFile)
            try {
                withContext(Dispatchers.IO) {
                    Files.createSymbolicLink(link, file)
                }
            } catch (e: java.nio.file.FileSystemException) {
                log.error("用户权限不足！！请前往本地安全策略-用户权限分配-创建符号链接 将EveryOne加入")
            } catch (e: java.nio.file.FileAlreadyExistsException) {
                log.error("软连接已存在")
            }
        }
        return true
    }

}