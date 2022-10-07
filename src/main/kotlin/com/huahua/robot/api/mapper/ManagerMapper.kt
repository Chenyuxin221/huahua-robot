package com.huahua.robot.api.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.huahua.robot.api.entity.Manager
import org.apache.ibatis.annotations.Mapper

/**
 * ClassName: ManagerMapper
 * @description
 * @author 花云端
 * @date 2022-10-06 18:56
 */
@Mapper
interface ManagerMapper : BaseMapper<Manager>