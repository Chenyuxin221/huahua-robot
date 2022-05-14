package com.huahua.robot.api.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.huahua.robot.api.entity.Photo
import org.apache.ibatis.annotations.Mapper
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

/**
 * ClassName: ImagesUrlMapper
 * @description
 * @author 花云端
 * @date 2022-04-27 12:45
 */
@Mapper
interface PhotoMapper:BaseMapper<Photo?>