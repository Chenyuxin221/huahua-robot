package com.huahua.robot.listener.scheduledTasks

import cn.hutool.core.date.ChineseDate
import cn.hutool.core.date.DateUtil
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import com.huahua.robot.config.RobotConfig
import com.huahua.robot.core.common.*
import com.huahua.robot.service.SwitchSateService
import com.huahua.robot.utils.FileUtil.getTempImage
import com.huahua.robot.utils.FileUtil.url
import com.huahua.robot.utils.HttpUtil
import kotlinx.coroutines.runBlocking
import love.forte.simbot.message.Image.Key.toImage
import love.forte.simbot.resources.Resource.Companion.toResource
import org.springframework.boot.logging.LogLevel
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 定时消息
 * @author 花云端
 * @date 2022-08-20 13:26
 */
@Component
class TimeTask(
    val switchSateService: SwitchSateService,
    val robotConfig: RobotConfig,
) {


    @Scheduled(cron = "0 0 8 * * ?")
    fun dailyMorningPaper() {
        val groups = robotConfig.morningPaperGroups
        groups.isNullOrEmpty().then { return }
        val url = "https://api.03c3.cn/zb/api.php"
        var body: JSONObject? = null
        try {
            body = JSON.parseObject(HttpUtil.get(url).response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (body == null) {
            logger { "哎呀，接口失效啦！" }
            return
        }
        val data = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        (body.getIntValue("code") != 200).then { return }
        "${data}.png".getTempImage(body.getString("imageUrl").url())?.also {
            groups!!.forEach { str ->
                Sender.sendGroupMsg(str, it.toResource().toImage())
            }
        }.isNull { logger(LogLevel.ERROR) { "哎呀，接口失效了" } }!!.delete()
    }

    /**
     * 每日凌晨将缓存数据上传至数据库
     *
     */
    @Scheduled(cron = "0 0 0 * * ?")
    fun updateMysql() {
        val values = switchSateService.getKeys("*")
        values.forEach {
            val value = switchSateService.getValue(it)
            val temp = it.split(":")
            val groupId = temp[0]
            val func = temp[1]
            val url = "http://127.0.0.1:8080/config/switch/set?groupId=${groupId}&func=${func}&state=${value}"
            val result = HttpUtil.get(url).response
            try {
                val json = JSON.parseObject(result)
                if (json.getIntValue("code") != 200) {
                    Sender.sendAdminMsg("更新失败")
                    return
                }
            } catch (e: Exception) {
                Sender.sendAdminMsg("更新失败：${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * 节日祝福
     * @receiver GroupMessageEvent 群事件
     */
    @Scheduled(cron = "0 0 20 * * *")
    fun happyHolidays() {
        val chineseDate = ChineseDate(Date()) //农历
        val blessingsArrays = lunarBlessing(chineseDate)
        if (blessingsArrays == null) {
            val date = DateUtil.date() //公历
            val month = date.month()
            val day = date.dayOfMonth()
            val hashMap = greetingsOnTheGregorianFestival(month, day) //公历节日
            var arrays = hashMap["${month}.${day}"]
            if (arrays.isNullOrEmpty()) { //没有正常节日
                //考虑父亲节母亲节
                //母亲节为五月的第二个星期日
                arrays = if (month == 5 && date.weekOfMonth() == 2 && date.dayOfWeek() == 7) {
                    arrayListOf("对了，今天是母亲节呢！你们有和母亲通电话吗！")
                }
                //父亲节为六月的第三个星期日
                else if (month == 6 && date.weekOfMonth() == 3 && date.dayOfWeek() == 7) {
                    arrayListOf("对了，今天是父亲节呢！你们有和父亲通电话吗！")
                } else return //没有公历节日则返回
            }
        }
        val blessings = blessingsArrays?.random()
        blessings.isNullOrEmpty().then { return }
        val bot = RobotCore.getBot()
        val groups = bot.groups
        runBlocking {
            groups.asFlow().collect {
                it.send(blessings!!)
            }
        }
    }

    /**
     * 农历节日祝福
     * @param chineseDate ChineseDate   农历
     * @return ArrayList<String>?   祝福列表
     */
    fun lunarBlessing(chineseDate: ChineseDate): ArrayList<String>? {
        val festivals = chineseDate.festivals
        festivals.isEmpty().then { return null }
        when (festivals) {
            "春节" -> {
                /**
                 * 懒得挨个弄，要加自己加
                 * 格式 arrayListOf("arg1","arg2","arg3")
                 */
//                return when(chineseDate.chineseZodiac){
//                    "鼠" -> arrayListOf("对了，祝大家${chineseDate.chineseZodiac}年快乐啊！")
//                    "牛" -> arrayListOf("对了，祝大家${chineseDate.chineseZodiac}年快乐啊！")
//                    "虎" -> arrayListOf("对了，祝大家${chineseDate.chineseZodiac}年快乐啊！")
//                    "兔" -> arrayListOf("对了，祝大家${chineseDate.chineseZodiac}年快乐啊！")
//                    "龙" -> arrayListOf("对了，祝大家${chineseDate.chineseZodiac}年快乐啊！")
//                    "蛇" -> arrayListOf("对了，祝大家${chineseDate.chineseZodiac}年快乐啊！")
//                    "马" -> arrayListOf("对了，祝大家${chineseDate.chineseZodiac}年快乐啊！")
//                    "羊" -> arrayListOf("对了，祝大家${chineseDate.chineseZodiac}年快乐啊！")
//                    "猴" -> arrayListOf("对了，祝大家${chineseDate.chineseZodiac}年快乐啊！")
//                    "鸡" -> arrayListOf("对了，祝大家${chineseDate.chineseZodiac}年快乐啊！")
//                    "狗" -> arrayListOf("对了，祝大家${chineseDate.chineseZodiac}年快乐啊！")
//                    "猪" -> arrayListOf("对了，祝大家${chineseDate.chineseZodiac}年快乐啊！")
//                    else -> null
//                }
                return arrayListOf("对了，祝大家${chineseDate.chineseZodiac}年快乐啊！")
            }

            "除夕" -> return arrayListOf("对了，祝大家除夕快乐鸭！")
            "元宵节 上元节" -> return arrayListOf("对了,祝大家元宵节/上元节快乐呀！")
            "中秋节" -> return arrayListOf("对了，祝大家中秋节快乐呀！")
            "端午节 端阳节" -> return arrayListOf("对了，祝大家端午节快乐呀！")
            "七夕节" -> return arrayListOf("对了，七夕节快乐！！你有找到对象吗！！")
            else -> return null
        }
    }

    /**
     * 公历祝福语
     * @param month Int 月份
     * @param dayOfMonth Int 几号
     * @return HashMap<String, ArrayList<String>>
     */
    fun greetingsOnTheGregorianFestival(month: Int, dayOfMonth: Int) = hashMapOf(
        "1.1" to arrayListOf("对了，元旦快乐呀！"),
        "3.5" to arrayListOf("对了，今天是学雷锋纪念日！你们有做好事吗！"),
        "3.8" to arrayListOf("对了，祝大家妇女节快乐呀！今天你们放半天假了吗！"),
        "3.12" to arrayListOf("对了，今天是植树节呢！今天你们有植树吗！"),
        "4.1" to arrayListOf("对了，祝大家愚人节快乐呀！今天你有捉弄小伙伴吗！今天你有与心爱的TA表白吗！"),
        "5.1" to arrayListOf("对了，劳动节快乐呀！大家今天有劳动吗~"),
        "5.4" to arrayListOf("对了，祝大家青年节快乐呀！学习新思想！争做新青年"),
        "6.1" to arrayListOf("对了，大家儿童节快乐呀~"),
        "7.1" to arrayListOf("对了，今天是建党节呢！你们有温习党的章程吗！"),
        "8.1" to arrayListOf("对了，今天是建军节呢！你们有考虑参军入伍，真人cs吗！"),
        "9.10" to arrayListOf("对了，今天是教师节呢！你们有和老师问候一句教师节快乐吗！"),
        "10.1" to arrayListOf("对了，今天是国庆节呢！！你们有放几天假呢！有看阅兵仪式吗！有考虑去现场看升国旗吗！有考虑出去旅游吗！有考虑去哪里旅游呢！")
    )


}