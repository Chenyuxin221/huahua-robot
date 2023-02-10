package com.huahua.robot.utils

import com.huahua.robot.core.common.logger
import org.springframework.boot.logging.LogLevel
import java.io.File

/**
 * ClassName: LoadlocalImg
 * @description
 * @author 花云端
 * @date 2022-04-27 10:39
 */
class LoadLocalImg {
    private val imageList: ArrayList<String> = arrayListOf()

    /**
     * 扫描本地文件
     */
    fun loadLocalImage():ArrayList<String>{
        //这里自己添加本地图片扫描的文件夹
        scanFile("D:\\新建文件夹\\pictures\\images")
        scanFile("D:\\新建文件夹\\pictures\\image")
        scanFile("D:\\新建文件夹\\pictures\\m_image")
        scanFile("D:\\新建文件夹\\pictures\\imgs")
        scanFile("D:\\AliDownload")
        scanFile("D:\\新建文件夹\\pictures\\ssly_down")
        return imageList
    }

    /**
     * 扫描指定文件夹文件
     * @param path String
     * @return ArrayList<String> 目录内所有文件路径
     */
    private fun scanFile(path: String) {
        val directory = File(path)

        /**
         * 文件路径列表
         */
        val filesPath: ArrayList<String> = arrayListOf()
        /**
         * 如果当前不是目录则抛出异常
         */
        if (!directory.isDirectory) {
            logger(LogLevel.ERROR) { "$path input path is not a Directory, please input the right path of the Directory" }
            return
//            throw ScanFilesException("$path input path is not a Directory, please input the right path of the Directory")
        }
        if (directory.isDirectory) {
            val fileList: Array<out File>? = directory.listFiles()
            if (fileList != null) {
                for (i in fileList.indices) {
                    /**
                     * 如果当前是文件夹，则递归扫描文件夹
                     */
                    if (fileList[i].isDirectory) {
                        scanFile(fileList[i].absolutePath)
                        /**
                         * 非文件夹
                         */
                    } else {
                        if (isImage(fileList[i].absolutePath)) {
                            imageList.add(fileList[i].absolutePath)
                        }
                    }
                }

            }
        }
    }

    /**
     * 判断该文件是否为图片
     * @param path String 文件路径
     */
    private fun isImage(path: String): Boolean {
        val suffix = path.substring(path.lastIndexOf(".") + 1, path.length)
        val imageArray = arrayOf("png", "jpg", "PNG", "JPEG")
        if (suffix in imageArray) {
            return true
        }
        return false
    }
}