package com.huahua.robot.utils

import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 计时器
 *
 * @author wuyou
 */
class Timer<T>(
    private val time: Long,
    private val timeUnit: TimeUnit,
    private val arg: T,
    autoStart: Boolean = false,
    timer: Timer<T>.() -> Unit,
) {
    private val timeMap = mutableMapOf<Long, MutableList<(T) -> Unit>>()
    private var start: ((T) -> Unit)? = null
    private var finish: ((T) -> Unit)? = null
    private var cancel: ((T) -> Unit)? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val isStarted = AtomicBoolean(false)

    init {
        timer()
        if (autoStart) start()
    }

    /**
     * 开始计时
     */
    fun start() {
        if (isStarted.get()) {
            throw IllegalStateException("Timer is already started")
        }
        isStarted.set(true)
        start?.invoke(arg)
        var t = 1L
        scope.launch {
            do {
                timeMap[t]?.forEach {
                    scope.launch {
                        it.invoke(arg)
                    }
                }
                if (timeUnit == TimeUnit.MILLISECONDS) {
                    delay(1)
                } else {
                    delay(timeUnit.toMillis(1))
                }
            } while (t++ < time)
            finish?.invoke(arg)
            isStarted.set(false)
        }
    }

    /**
     * 中断计时器
     */
    fun cancel() {
        scope.cancel()
        cancel?.invoke(arg)
    }

    /**
     * 设置计时器结束时执行的方法
     */
    fun onFinish(block: (T) -> Unit) {
        finish = block
    }

    /**
     * 设置计时器取消时执行的方法
     */
    fun onCancel(block: (T) -> Unit) {
        cancel = block
    }

    /**
     * 设置计时器开始时执行的方法
     */
    fun onStart(block: (T) -> Unit) {
        start = block
    }

    operator fun get(time: Long): List<(T) -> Unit>? = timeMap[time]
    operator fun set(time: Long, block: (T) -> Unit) =
        timeMap.putIfAbsent(time, mutableListOf(block))?.also { it += block }

    operator fun Long.invoke(block: (T) -> Unit) = timeMap.putIfAbsent(this, mutableListOf(block))?.also { it += block }

    operator fun Int.invoke(block: (T) -> Unit) =
        timeMap.putIfAbsent(this.toLong(), mutableListOf(block))?.also { it += block }
}