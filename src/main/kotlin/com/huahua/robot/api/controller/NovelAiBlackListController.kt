package com.huahua.robot.api.controller

import com.huahua.robot.api.controller.Type.*
import com.huahua.robot.api.entity.NovelAiBlacklist
import com.huahua.robot.api.mapper.NovelAiBlacklistMapper
import com.huahua.robot.api.result.Result
import com.huahua.robot.api.result.ResultStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * ClassName: NovelAiBacklListController
 * @description
 * @author 花云端
 * @date 2022-11-07 14:48
 */
@RestController
@RequestMapping("AIBlackList")
class NovelAiBlackListController(
    val mapper: NovelAiBlacklistMapper,
) {

    @GetMapping("queryAll")
    fun queryAll(): Result<MutableList<String>> {
        val list = mapper.selectList(null)
        val result = mutableListOf<String>()
        list.forEach {
            result.add(it.value)
        }
        return Result.success(result)
    }

    @GetMapping("query")
    fun query(@RequestParam("value") value: String) = dao(value, SELECT)

    @GetMapping("delete")
    fun remove(@RequestParam("value") value: String) = dao(value, DELETE)

    @GetMapping("insert")
    fun insert(@RequestParam("value") value: String) = dao(value, INSERT)

    @GetMapping("update")
    fun update(
        @RequestParam("value") value: String,
        @RequestParam("newValue") newValue: String,
    ) = dao(value, UPDATE, newValue)


    private fun dao(value: String, type: Type, newValue: String = ""): Result<Nothing?> {
        val result = mapper.selectByMap(mapOf(Pair("value", value)))
        when (type) {
            INSERT -> {
                if (result.isNotEmpty()) return Result.failure(ResultStatus.DATA_EXIST)
                val num = mapper.insert(NovelAiBlacklist(value = value))
                if (num <= 0) return Result.failure(ResultStatus.OPERATION_FAILED)
                return Result.success()
            }

            DELETE -> {
                if (result.isEmpty()) return Result.failure(ResultStatus.DATA_IS_EMPTY)
                val num = mapper.deleteByMap(mapOf(Pair("value", value)))
                if (num <= 0) return Result.failure(ResultStatus.OPERATION_FAILED)
                return Result.success()
            }

            SELECT -> {
                if (result.isEmpty()) return Result.failure(ResultStatus.DATA_IS_EMPTY)
                return Result.success()
            }

            UPDATE -> {
                if (result.isEmpty()) return Result.failure(ResultStatus.DATA_IS_EMPTY)
                val num = mapper.updateById(NovelAiBlacklist(result[0].id, newValue))
                if (num <= 0) return Result.failure(ResultStatus.OPERATION_FAILED)
                return Result.success()
            }
        }
    }

}

enum class Type {
    INSERT,
    DELETE,
    UPDATE,
    SELECT;
}