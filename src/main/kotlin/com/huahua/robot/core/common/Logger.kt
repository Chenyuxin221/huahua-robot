package com.huahua.robot.core.common

import org.slf4j.LoggerFactory

/**
 * 打印日志
 * @author wuyou
 */
fun logger(block: Log.() -> Any) {
    val logger = LoggerFactory.getLogger(getName(block))
    Log().apply {
        with(block()) {
            when (this) {
                !is Unit -> logs.add(this)
            }
        }
    }.logs.forEach {
        logger.info(it.toString())
    }
}

class Log() {
    val logs = mutableListOf<Any>()
    operator fun String.unaryMinus() {
        logs += this
    }
}

private fun getName(func: Any): String {
    val name = func.javaClass.name
    val slicedName = when {
        name.contains("Kt$") -> name.substringBefore("Kt$")
        name.contains("$") -> name.substringBefore("$")
        else -> name
    }
    return slicedName
}
