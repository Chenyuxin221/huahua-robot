package com.huahua.robot.api.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

@TableName("porn")
@NoArgsConstructor
@AllArgsConstructor
data class Porn(
    @TableId("id", type = IdType.AUTO)
    val id: Long? = null,
    @TableField("url")
    val url: String,
    @TableField("md5")
    val md5: String,
    @TableField("type")
    val type: Int,
    @TableField("tips")
    val tip: String,
)
