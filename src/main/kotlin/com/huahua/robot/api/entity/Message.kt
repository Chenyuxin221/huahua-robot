package com.huahua.robot.api.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import lombok.ToString

@TableName("message")
@AllArgsConstructor
@NoArgsConstructor
@ToString
data class Message(
    @TableId("id", type = IdType.AUTO)
    val id: Int? = null,
    @TableField("groupId")
    val groupId: String,
    @TableField("groupName")
    val groupName: String,
    @TableField("sendMsg")
    val sendMsg: String,
    @TableField("sendUserCode")
    val sendUserCode: String,
    @TableField("sendUserName")
    val sendUserName: String,
    @TableField("sendTime")
    val sendTime: Long,
)
