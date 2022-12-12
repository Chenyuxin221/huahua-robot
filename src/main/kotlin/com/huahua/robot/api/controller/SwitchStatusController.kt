package com.huahua.robot.api.controller

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper
import com.huahua.robot.api.entity.FuncSwitch
import com.huahua.robot.api.mapper.FuncSwitchMapper
import com.huahua.robot.api.result.Result
import com.huahua.robot.api.result.ResultStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * ClassName: SwitchStatusController
 * @description
 * @author 花云端
 * @date 2022-12-09 18:05
 */
@RestController
@RequestMapping("/config/switch")
class SwitchStatusController(
    val mapper: FuncSwitchMapper,
) {

    @GetMapping("get")
    fun get(
        @RequestParam("groupId") groupId: String,
        @RequestParam("func") func: String,
    ): Result<out FuncSwitch?> {
        val map = mutableMapOf<String, Any>()
        map["groupId"] = groupId
        map["func"] = func
        val result = mapper.selectByMap(map).firstOrNull() ?: return Result.failure(ResultStatus.DATA_IS_EMPTY)
        return Result.success(
            FuncSwitch(
                id = result.id,
                groupId = result.groupId,
                func = result.func,
                state = result.state
            )
        )
    }

    @GetMapping("set")
    fun set(
        @RequestParam("groupId") groupId: String,
        @RequestParam("func") func: String,
        @RequestParam("state") state: Boolean,
    ): Result<out FuncSwitch?> {
        val map = mutableMapOf<String, Any>()
        map["groupId"] = groupId
        map["func"] = func
        val res = mapper.selectByMap(map).firstOrNull()
        println(res)
        if (res != null) {
            val wrapper = UpdateWrapper<FuncSwitch>()
            wrapper.eq("groupId", groupId).eq("func", func).set("state", state)
            mapper.update(null, wrapper)
        } else {
            map["state"] = state
            mapper.insert(FuncSwitch(groupId = groupId, func = func, state = state))
        }
        return Result.success(FuncSwitch(groupId = groupId, func = func, state = state))
    }

    @GetMapping("/getAll")
    fun getAll(): Result<out MutableList<FuncSwitch>?> {
        val result = mapper.selectList(null)
        if (result != null) {
            return Result.success(result)
        }
        return Result.failure(null)
    }
}