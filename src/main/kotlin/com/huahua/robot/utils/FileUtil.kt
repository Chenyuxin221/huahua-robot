package com.huahua.robot.utils

import com.huahua.robot.core.common.RobotCore
import com.huahua.robot.core.common.isNull
import org.springframework.core.io.Resource
import java.io.*
import java.net.URL
import java.nio.file.Files

/**
 * ClassName: FileUtil
 * @description
 * @author 花云端
 * @date 2022-05-16 11:38
 */
object FileUtil {
    private val temp = "${System.getProperty("user.home")}${getSeparator()}.huahuabot${getSeparator()}"
    fun saveFile(path: String, fileName: String, byteArray: ByteArray) {
        createDir(path)
        File(fileName).writeBytes(byteArray)
    }

    fun createDir(path: String) {
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    @Throws(IOException::class)
    fun saveTempFile(inputStream: InputStream?, fileName: String?, folderName: String): String {
        val tempDir = File(RobotCore.TEMP_PATH + folderName + File.separator)
        val temp = File(RobotCore.TEMP_PATH + folderName + File.separator + fileName)
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw IOException("Destination '$tempDir' directory cannot be created")
        }
        if (!tempDir.canWrite()) {
            throw IOException("Destination '$tempDir' cannot be written to")
        }
        if (inputStream != null) {
            BufferedInputStream(inputStream).use { bis ->
                BufferedOutputStream(Files.newOutputStream(temp.toPath())).use { bos ->
                    var len: Int
                    val buf = ByteArray(10240)
                    while (bis.read(buf).also { len = it } != -1) {
                        bos.write(buf, 0, len)
                    }
                    bos.flush()
                }
            }
        }
        return temp.path
    }

    /**
     * 将resource文件保存到临时路径
     *
     * @param resource   resource文件
     * @param folderName 文件夹名
     * @throws IOException IOException
     */
    @Suppress("unused")
    @Throws(IOException::class)
    fun saveResourceToTempDirectory(resource: Resource, folderName: String) {
        saveTempFile(resource.inputStream, resource.filename, folderName)
    }

    /**
     * 判断文件是否存在
     *
     * @param path 文件路径
     * @return 如果存在返回true
     */
    fun exist(path: String?): Boolean {
        return null != path && File(path).exists()
    }

    private fun getLocalTempPath(): String {
        val str = "attrib +H \"$temp\""
        val localDir = File(temp)
        if (!localDir.exists()) {
            localDir.mkdirs()
            Runtime.getRuntime().exec(str)
        }
        return localDir.absolutePath + getSeparator()
    }

    private fun getLocalImagePath(): String {
        val pathStr = getLocalPath() + "image" + getSeparator()
        val path = File(pathStr)
        if (!path.exists()) {
            path.mkdirs()
        }

        return path.absolutePath + getSeparator()
    }

    fun Any.getSeparator(): String = File.separator

    fun String.getTempEmptyImage(): File {
        val path = File(getLocalImagePath())
        if (!path.exists()) {
            path.mkdirs()
        }
        val file = File(path.absolutePath + getSeparator() + this)
        if (file.exists()) {
            file.delete()
            file.createNewFile()
        }
        return file
    }

    fun String.getTempImage(byteArray: ByteArray?): File? {
        val file = this.getTempEmptyImage()
        byteArray?.let {
            file.writeBytes(it)
        }.isNull {
            return null
        }
        return file
    }

    fun String.getTempImage(url: URL): File? {
        val byteArray = HttpUtil.getResponse(url.toString()).body()?.bytes()
        return getTempImage(byteArray)
    }

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