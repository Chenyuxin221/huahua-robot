package com.huahua.robot.api.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

@AllArgsConstructor
@NoArgsConstructor
@TableName("group_boot_state")
data class GroupBootState(
    @TableId("id", type = IdType.AUTO)
    val id: Long? = null,
    @TableField("group_code")
    val groupCode:String,
    @TableField("state")
    val state:Boolean
)
