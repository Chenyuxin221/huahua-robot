package com.huahua.robot.music.entity.kugoumusic.music

import com.fasterxml.jackson.annotation.JsonProperty

data class KugouMusic(
    val code: Int,
    val text: String,
    val data: Data
)
