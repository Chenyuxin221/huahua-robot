package com.huahua.robot.api.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

/**
 * ClassName: Manager
 * @description
 * @author 花云端
 * @date 2022-10-06 18:56
 */
@NoArgsConstructor
@AllArgsConstructor
@TableName("manager")
data class Manager(
    @TableId("id", type = IdType.AUTO)
    val id: Long? = null,
    @TableField("groupId")
    val groupId: String,
    @TableField("userId")
    val userId: String,
)