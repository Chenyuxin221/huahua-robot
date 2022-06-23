package com.huahua.robot.core.common

import com.huahua.robot.api.entity.Photo
import com.huahua.robot.api.mapper.PhotoMapper
import com.huahua.robot.utils.GlobalVariable
import com.huahua.robot.utils.LoadLocalImg
import kotlinx.coroutines.launch
import love.forte.simbot.LoggerFactory
import love.forte.simbot.OriginBotManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Configuration
import kotlin.reflect.jvm.jvmName

/**
 * ClassName: CmdRunner
 * @description
 * @author 花云端
 * @date 2022-05-13 20:19
 */
@Configuration
class CmdRunner constructor(
    var mapper:PhotoMapper
) : CommandLineRunner {



    val log = LoggerFactory.getLogger(CmdRunner::class.jvmName)

    @Suppress("OPT_IN_USAGE")
    override fun run(vararg args: String?) {
        GlobalVariable.BOT = OriginBotManager.getBot(GlobalVariable.BOTID)
        initGlobalVariable()
        log.info(RobotCore.ADMINISTRATOR)
        Sender.sendAdminMsg("我好了")
    }

    /**
     * 初始化全局变量
     */
    fun initGlobalVariable() {
        GlobalVariable.PhotoList = loadImage()
    }

    fun loadImage(): ArrayList<String> {
        val localImgList: ArrayList<String> = LoadLocalImg().loadLocalImage()
        var imgDBCount = mapper.selectCount(null)

        if (imgDBCount <= 0) {
            for (url: String in localImgList) {
                mapper.insert(Photo(url = url))
            }
            log.info("本地图片数量：${localImgList.size}\t数据库图片数量：${mapper.selectCount(null)}")
            return localImgList
        }
        if (imgDBCount < localImgList.size.toLong()) {
            val flag = if (localImgList.size - imgDBCount > 0) localImgList.size + 1 - imgDBCount else -1
            if (flag > 0) {
                for (i in flag.toInt()..localImgList.size) {
                    mapper.insert(Photo(flag, localImgList[i]))
                }
            }
           log.info("本地图片数量：${localImgList.size}\t数据库图片数量：${imgDBCount}")
        }
        imgDBCount = mapper.selectCount(null)
        if (imgDBCount<1){
            mapper.insert(Photo(null,"http://c2cpicdw.qpic.cn/offpic_new/1849950046//1849950046-3989411300-97779161DF8D2A02845B89E2E7B40951/0?term=2")) //添加默认图片
        }
        return localImgList
    }
}