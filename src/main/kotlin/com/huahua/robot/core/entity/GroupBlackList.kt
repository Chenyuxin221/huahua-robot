package com.huahua.robot.core.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import lombok.ToString

/**
 * @author 花云端
 * @date 2023/4/18 22:37
 * 群黑名单实体类
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("group_blacklist")
data class GroupBlackList(
    @TableId("id", type = IdType.AUTO)
    val id: Int? = null,
    @TableField("group_code")
    val groupCode: String,
    @TableField("member_code")
    val memberCode: String
)
