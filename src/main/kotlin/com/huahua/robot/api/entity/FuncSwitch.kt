package com.huahua.robot.api.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

/**
 * 功能开关
 * @property id Int? 主键
 * @property groupId String 群号
 * @property func String    功能
 * @property state Boolean  开关状态
 * @constructor
 */
@TableName("func_switch")
@NoArgsConstructor
@AllArgsConstructor
data class FuncSwitch(
    @TableId("id", type = IdType.AUTO)
    val id: Int? = null,
    @TableField("groupId")
    val groupId: String,
    @TableField("func")
    val func: String,
    @TableField("state")
    val state: Boolean,
)
