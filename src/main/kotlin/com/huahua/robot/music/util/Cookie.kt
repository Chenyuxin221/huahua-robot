package com.huahua.robot.music.util

import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.core.common.Sender
import com.huahua.robot.core.common.logger
import com.huahua.robot.utils.*
import com.huahua.robot.utils.FileUtil.getTempImage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.stream.Collectors
import kotlin.collections.HashMap

/**
 * ClassName: Cookie
 * @description
 * @author 花云端
 * @date 2022-05-18 15:57
 */
class Cookie {
    private val uin: String = RobotCore.ADMINISTRATOR
    private var isWaitScan = false
    private val check =
        "https://ssl.ptlogin2.qq.com/check?regmaster=&pt_tea=2&pt_vcode=1&uin=%s&appid=716027609&js_ver=21122814&js_type=1&login_sig=u1cFxLxCIZyhQiuufGpUqedhK9g9VlQWIXW1ybpCg-G0-q9wd0mdzw3R9vNHFz2S&u1=https://graph.qq.com/oauth2.0/login_jump&r=0.004892586794276843&pt_uistyle=40"
    private val cookie: MutableMap<String, String> = HashMap()
    private val nowTime = "now_time";
    private val count = 0

    private fun scanLogin(): Boolean {
        if (isWaitScan) {
            return false
        }
        cookie.clear()
        val responseEntity: ResponseEntity = HttpUtil.get(String.format(check, uin))
        cookie.putAll(responseEntity.cookies)
        val path = loginQrCode
        Sender.sendPrivateMsg(RobotCore.ADMINISTRATOR,MessageUtil.getImageMessage(File(path)))
        isWaitScan = true
        try {
            while (true) {
                Thread.sleep(1000)
                val response: String = loginState.response
                if (response.contains("ptuiCB('0'")) {
                    val url = Arrays.stream(response.split("'".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                        .filter { i: String -> i.contains("http") }.collect(Collectors.toList())[0]
                    val responseEntity1: ResponseEntity = HttpUtil.get(url)
                    cookie.putAll(responseEntity1.cookies)
                    cookie[nowTime] = System.currentTimeMillis().toString() + ""
                    logger { "scan login success." }
                    isWaitScan = false
                    return true
                } else if (response.contains("ptuiCB('65'")) {
                    val imagePath = loginQrCode
                    Sender.sendPrivateMsg(RobotCore.ADMINISTRATOR, MessageUtil.getImageMessage(imagePath))
                }
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 初始化cookie
     */
    private val scanCookies: Unit
        get() {
            val url =
                "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=716027609&target=self&style=40&s_url=https://y.qq.com/"
            val responseEntity: ResponseEntity = HttpUtil.get(url)
            responseEntity.cookies.let { cookie.putAll(it) }
        }


    /**
     * 获取二维码
     *
     * @return 二维码图片路径
     */
    private val loginQrCode: String
        get() {
            val now = System.currentTimeMillis().toString() + ""
            scanCookies
            val url1 =
                String.format("https://ssl.ptlogin2.qq.com/ptqrshow?appid=716027609&e=2&l=M&s=3&d=72&v=4&t=%s&pt_3rd_aid=0",
                    now)
            val responseEntity: ResponseEntity = HttpUtil.get(url1)
            cookie.putAll(responseEntity.cookies)
            cookie["key"] = now
            val bytes: ByteArray = responseEntity.entity
            val file = getTempImage("QR.png")
            try {
                file.outputStream().write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return file.absolutePath
        }

    /**
     * 获取登录状态
     *
     * @return 返回的登录实体
     */
    val loginState: ResponseEntity
        get() {
            scanLogin()
            val map: Map<String, String?> = cookie
            val urlCheckTimeout =
                ("https://ssl.ptlogin2.qq.com/ptqrlogin?u1=https://y.qq.com/&ptqrtoken=" + getPtqrtoken(map["qrsig"]) + "&ptredirect=0&h=1&t=1&g=1&from_ui=1&ptlang=2052&action=0-0-" + map["key"] + "&js_ver=10233&js_type=1&login_sig=" + map["pt_login_sig"] + "&pt_uistyle=40&aid=716027609&")
            return HttpUtil.get {
                url = urlCheckTimeout
                cookies = { -map }
            }
        }
    /**
     * 计算qrsig
     *
     * @return 计算后的结果
     */
    private fun getPtqrtoken(qrsig: String?): Int {
        var e = 0
        val n = qrsig!!.length
        for (j in 0 until n) {
            e += (e shl 5)
            e += qrsig.toCharArray()[j].code
            e = 2147483647 and e
        }
        return e
    }
}