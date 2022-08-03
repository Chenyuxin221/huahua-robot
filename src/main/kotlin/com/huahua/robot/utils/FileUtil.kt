package com.huahua.robot.utils

import com.huahua.robot.core.common.isNull
import love.forte.simbot.LoggerFactory
import java.io.File
import java.net.URL

/**
 * ClassName: FileUtil
 * @description
 * @author 花云端
 * @date 2022-05-16 11:38
 */
object FileUtil {
    private val temp = "${System.getProperty("user.home")}${getSeparator()}.huahuabot${getSeparator()}"
    private val log = LoggerFactory.getLogger(FileUtil::class.java)

    private fun getLocalTempPath(): String {
        val str = "attrib +H \"$temp\""
        val localDir = File(temp)
        if (!localDir.exists()) {
            localDir.mkdirs()
            Runtime.getRuntime().exec(str)
        }
        return localDir.absolutePath + getSeparator()
    }

    private fun getLocalFilePath(type: FileType): String {
        val pathStr = getLocalPath() + type.value + getSeparator()
        val path = File(pathStr)
        if (!path.exists()) {
            path.mkdirs()
        }
        return path.absolutePath + getSeparator()
    }

    private fun getTempEmptyFile(name: String, type: FileType): File {
        val path = File(getLocalFilePath(type))
        if (!path.exists()) {
            path.mkdirs()
        }
        val file = File(path.absolutePath + getSeparator() + name)
        if (file.exists()) {
            file.delete()
            file.createNewFile()
        }
        return file
    }

    private fun String.getTempFile(byteArray: ByteArray?, type: FileType): File? {
        val file = getTempEmptyFile(this, type)
        byteArray?.let {
            file.writeBytes(it)
        }.isNull {
            return null
        }
        return file
    }

    private fun String.getTempFile(url: URL, type: FileType): File? {
        val file = getTempEmptyFile(this, type)
        val byteArray = HttpUtil.getResponse(url.toString()).body()?.bytes()
        byteArray?.let {
            file.writeBytes(it)
        }.isNull {
            return null
        }
        return file
    }

    private fun deleteFile(file: File){
        if(!file.exists()){
            log.debug("文件删除失败，请检查路径是否正确")
            return
        }
        val files = file.listFiles()
        for (f in files!!) {
            if (f.isDirectory){
                deleteFile(f)
            }else{
                f.delete()
            }
        }
    }

    /**
     * 清空文件夹
     * @param file File 文件夹路径
     */
    fun File.clearDirectory() = deleteFile(this)



    /**
     * 保存文件到指定目录
     * @receiver String 文件名
     * @param path String   目录
     * @param url URL   网络地址
     * @return File?    文件
     */
    fun String.getTempFile(path: String, url: URL): File? {
        val dir = File(path)
        if (!dir.isDirectory) {
            return null
        }
        val file = File(path + getSeparator() + this)
        HttpUtil.getResponse(url.toString()).body()?.bytes()?.let {
            file.writeBytes(it)
        }.isNull {
            return null
        }
        return file
    }

    fun imagePath() = getLocalFilePath(FileType.IMAGE)

    /**
     * 保存并获取本地图片
     * @receiver String 文件全名
     * @param byteArray ByteArray?  字符流
     * @return File?    文件
     */
    fun String.getTempImage(byteArray: ByteArray?) = this.getTempFile(byteArray, FileType.IMAGE)

    /**
     * 保存并获取本地图片
     * @receiver String 文件全名
     * @param url URL   网络图片地址
     * @return File?    文件
     */
    fun String.getTempImage(url: URL) = this.getTempFile(url, FileType.IMAGE)


    /**
     * 保存并获取本地音乐
     * @receiver String 文件全名
     * @param byteArray ByteArray?
     * @return File?
     */
    fun String.getTempMusic(byteArray: ByteArray?) = this.getTempFile(byteArray, FileType.MUSIC)

    /**
     * 保存并获取本地音乐
     * @receiver String 文件全名
     * @param url URL   网络文件地址
     * @return File?    文件
     */
    fun String.getTempMusic(url: URL) = this.getTempFile(url, FileType.MUSIC)
    fun String.getTempEmptyImage() = getTempEmptyFile(this, FileType.IMAGE)
    fun Any.getSeparator(): String = File.separator

    /**
     * 链接字符串转URL
     * @receiver String 链接字符串
     * @return URL URL
     */
    fun String.url() = URL(this)

    /**
     * 获得文件
     * @receiver String 路径
     * @return File 文件
     */
    fun String.toFile() = File(this)


    fun Any.getLocalPath(): String = getLocalTempPath()

}

enum class FileType(val value: String) {
    IMAGE("image"),
    MUSIC("music"),
    VIDEO("video"),
    OTHER_TYPES("file")
}
