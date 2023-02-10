package com.huahua.robot.utils

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * 获取容器里的bean
 * @author 花云端
 * @date 2023-02-11 0:32
 */
class SpringContextUtil : ApplicationContextAware {
    private lateinit var applicationContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    private fun getApplicationContext() = applicationContext

    /**
     * 通过name获取bean
     * @param name String
     * @return Any
     */
    fun getBean(name: String) = getApplicationContext().getBean(name)

    /**
     * 通过class获取bean
     * @param clazz Class<T>
     * @return T
     */
    fun <T> getBean(clazz: Class<T>) = getApplicationContext().getBean(clazz)

    /**
     * 通过name和class拿到指定的bean
     * @param name String
     * @param clazz Class<T>
     * @return T
     */
    fun <T> getBean(name: String, clazz: Class<T>) = getApplicationContext().getBean(name, clazz)
}