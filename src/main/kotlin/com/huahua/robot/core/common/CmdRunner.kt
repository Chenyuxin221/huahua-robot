package com.huahua.robot.core.common

import com.huahua.robot.api.entity.Photo
import com.huahua.robot.api.mapper.PhotoMapper
import com.huahua.robot.utils.GlobalVariable
import com.huahua.robot.utils.LoadLocalImg
import kotlinx.coroutines.launch
import love.forte.simbot.OriginBotManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Configuration

/**
 * ClassName: CmdRunner
 * @description
 * @author 花云端
 * @date 2022-05-13 20:19
 */
@Configuration
class CmdRunner : CommandLineRunner {

    @Autowired(required = false) private lateinit var mapper: PhotoMapper

    override fun run(vararg args: String?) {
        GlobalVariable().BOT = OriginBotManager.getBot(GlobalVariable().BOTID)
        initGlobalVariable()
        GlobalVariable().BOT?.launch {
            GlobalVariable().BOT?.friend(GlobalVariable().MASTER)?.send("我好了...")
        }
    }

    /**
     * 初始化全局变量
     */
    fun initGlobalVariable() {
        GlobalVariable().PhotoList = loadImage()
    }

    fun loadImage(): ArrayList<String> {
        val localImgList: ArrayList<String> = LoadLocalImg().loadLocalImage()
        val imgDBCount = mapper.selectCount(null)

        if (imgDBCount <= 0) {
            for (url: String in localImgList) {
                mapper.insert(Photo(url = url))
            }
            println("加载完成：\n本地图片数量：${localImgList.size}\n数据库图片数量：${mapper.selectCount(null)}")
            return localImgList
        }
        if (imgDBCount < localImgList.size.toLong()) {
            val flag = if (localImgList.size - imgDBCount > 0) localImgList.size + 1 - imgDBCount else -1
            if (flag > 0) {
                for (i in flag.toInt()..localImgList.size) {
                    mapper.insert(Photo(flag, localImgList[i]))
                }
            }
            println("加载完成：\n本地图片数量：${localImgList.size}\n数据库图片数量：${imgDBCount}")
        }
        return localImgList
    }
}