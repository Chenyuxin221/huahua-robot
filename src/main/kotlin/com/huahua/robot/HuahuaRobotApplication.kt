package com.huahua.robot

import love.forte.simboot.autoconfigure.EnableSimbot
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableSimbot
@SpringBootApplication
class HuahuaRobotApplication

fun main(args: Array<String>) {
    runApplication<HuahuaRobotApplication>(*args)
}
