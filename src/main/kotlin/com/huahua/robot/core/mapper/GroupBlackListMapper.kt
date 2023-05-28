package com.huahua.robot.core.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.huahua.robot.core.entity.GroupBlackList
import org.apache.ibatis.annotations.Mapper

/**
 * ClassName: 群黑名单映射器
 * @author 花云端
 * @date 2023-04-18 22:32
 */
@Mapper
interface GroupBlackListMapper : BaseMapper<GroupBlackList>
