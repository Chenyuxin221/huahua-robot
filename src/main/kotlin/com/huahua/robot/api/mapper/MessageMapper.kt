package com.huahua.robot.api.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.huahua.robot.api.entity.Message
import org.apache.ibatis.annotations.Mapper

/**
 * ClassName: MessageMapper
 * @description
 * @author 花云端
 * @date 2022-05-07 19:53
 */
//@Mapper
//@Component
@Mapper
interface MessageMapper : BaseMapper<Message?>