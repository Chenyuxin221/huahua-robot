package com.huahua.robot.core.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import lombok.ToString

/**
 *
 * @property id Int?
 * @property groupCode String
 * @property state Boolean
 * @constructor 花云端
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("group_boot_state")
data class BootState(
    @TableId("id", type = IdType.AUTO)
    val id:Int? = null,
    @TableField("group_code")
    val groupCode:String,
    @TableField("state")
    val state:Boolean
)
