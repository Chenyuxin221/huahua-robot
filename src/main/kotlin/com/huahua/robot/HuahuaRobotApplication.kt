package com.huahua.robot

import love.forte.simboot.spring.autoconfigure.EnableSimbot
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling


@EnableSimbot
@SpringBootApplication
@EnableScheduling
class HuahuaRobotApplication

fun main(args: Array<String>) {
    runApplication<HuahuaRobotApplication>(*args)
}
