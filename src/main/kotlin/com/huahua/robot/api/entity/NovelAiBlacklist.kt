package com.huahua.robot.api.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

@NoArgsConstructor
@AllArgsConstructor
@TableName("novelai_blacklist")
data class NovelAiBlacklist(
    @TableId("id", type = IdType.AUTO)
    val id: Long? = null,
    @TableField("value")
    val value: String,
)
