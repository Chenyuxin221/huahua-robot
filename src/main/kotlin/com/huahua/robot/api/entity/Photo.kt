package com.huahua.robot.api.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

/**
 * ClassName: ProtrayPerson
 * @description
 * @author 花云端
 * @date 2022-05-05 18:39
 */
@TableName("images_url")
@NoArgsConstructor
@AllArgsConstructor
data class Photo(
    @TableId("id", type = IdType.AUTO)
    val id: Long? = null,
    @TableField("url")
    val url: String,
)