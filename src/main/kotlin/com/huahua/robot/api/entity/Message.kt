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
    val groupId: String = "群Id",
    @TableField("groupName")
    val groupName: String = "群名称",
    @TableField("sendMsg")
    val sendMsg: String = "消息体",
    @TableField("sendUserCode")
    val sendUserCode: String ="用户Id",
    @TableField("sendUserName")
    val sendUserName: String ="用户名",
    @TableField("sendTime")
    val sendTime: Long ?= 0L,
)
