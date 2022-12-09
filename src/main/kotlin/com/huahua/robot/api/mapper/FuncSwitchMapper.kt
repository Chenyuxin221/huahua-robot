package com.huahua.robot.api.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.huahua.robot.api.entity.FuncSwitch
import org.apache.ibatis.annotations.Mapper

/**
 * ClassName: SwitchStatusMpper
 * @description
 * @author 花云端
 * @date 2022-12-09 18:04
 */
@Mapper
interface FuncSwitchMapper : BaseMapper<FuncSwitch>