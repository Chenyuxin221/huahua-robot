package com.huahua.robot.api.controller

import com.huahua.robot.api.entity.Manager
import com.huahua.robot.api.mapper.ManagerMapper
import com.huahua.robot.api.result.Result
import com.huahua.robot.api.result.ResultStatus
import org.springframework.web.bind.annotation.*

/**
 * ClassName: ManagerController
 * @description
 * @author 花云端
 * @date 2022-10-06 19:00
 */
@RestController
@RequestMapping("manager")
class ManagerController(
    val mapper: ManagerMapper,
) {

    @GetMapping("/add")
    fun addManager(
        @RequestParam("groupId") groupId: String,
        @RequestParam("userId") userId: String,
    ): Result<Nothing?> {
        val params = mutableMapOf<String, Any>()
        params["groupId"] = groupId
        params["userId"] = userId
        val list = mapper.selectByMap(params)
        return if (list.size <= 0) {
            mapper.insert(Manager(null, groupId, userId))
            Result.success()
        } else {
            Result.failure(ResultStatus.USER_EXISTS)
        }
    }

    @GetMapping("/delete")
    fun deleteManager(
        @RequestParam("groupId") groupId: String,
        @RequestParam("userId") userId: String,
    ): Result<Nothing?> {
        val params = mutableMapOf<String, Any>()
        params["groupId"] = groupId
        params["userId"] = userId
        val list = mapper.selectByMap(params)
        return if (list.size <= 0) {
            Result.failure(ResultStatus.USER_NOT_EXISTS)
        } else {
            mapper.deleteByMap(params)
            Result.success()
        }
    }

    @GetMapping("/query")
    fun queryManager(
        @RequestParam("groupId") groupId: String,
        @RequestParam("userId") userId: String,
    ): Result<out Any?> {
        if (groupId == "1" && userId == "1") {
            val all = mapper.selectByMap(mapOf(Pair("groupId", groupId)))
            if (all.size > 0) {
                return Result.success(all)
            }
        }
        val params = mutableMapOf<String, Any>()
        params["groupId"] = groupId
        params["userId"] = userId
        val list = mapper.selectByMap(params)
        return if (list.size <= 0) {
            Result.failure(ResultStatus.USER_NOT_EXISTS)
        } else {
            Result.success(Manager(null, groupId, userId))
        }
    }

    @ResponseBody
    @GetMapping("/queryAll")
    fun queryAllManager(@RequestParam("groupId") groupId: String): Result<out MutableList<Manager>?> {
        val list = mapper.selectByMap(mapOf(Pair("groupId", groupId)))
        return if (list.size > 0) {
            Result.success(list)
        } else {
            Result.failure(ResultStatus.DATA_IS_EMPTY)
        }
    }

}