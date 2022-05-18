package com.huahua.robot.core.common

import org.slf4j.LoggerFactory
import org.springframework.boot.logging.LogLevel

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

fun logger(level: LogLevel, block: Log.() -> Any) {
    val logger = LoggerFactory.getLogger(getName(block))
    Log().apply {
        with(block()) {
            when (this) {
                !is Unit -> logs.add(this)
            }
        }
    }.logs.forEach {
        when (level) {
            LogLevel.TRACE -> logger.trace(it.toString())
            LogLevel.DEBUG -> logger.debug(it.toString())
            LogLevel.INFO -> logger.info(it.toString())
            LogLevel.WARN -> logger.warn(it.toString())
            LogLevel.ERROR -> logger.error(it.toString())
            else -> {}
        }
    }
}

fun logger(level: LogLevel, e: Throwable, block: Log.() -> Any) {
    val logger = LoggerFactory.getLogger(getName(block))
    Log().apply {
        with(block()) {
            when (this) {
                !is Unit -> logs.add(this)
            }
        }
    }.logs.forEach {
        when (level) {
            LogLevel.TRACE -> logger.trace(it.toString(), e)
            LogLevel.DEBUG -> logger.debug(it.toString(), e)
            LogLevel.INFO -> logger.info(it.toString(), e)
            LogLevel.WARN -> logger.warn(it.toString(), e)
            LogLevel.ERROR -> logger.error(it.toString(), e)
            else -> {}
        }
    }
}

class Log {
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
