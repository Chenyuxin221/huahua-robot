package com.huahua.robot.music.entity.kugoumusic.music

import com.fasterxml.jackson.annotation.JsonProperty

data class Data(
    val song: String,
    val singer: String,
    val url: String,
    val cover: String,
    @JsonProperty("Music_Url")
    val Music_Url: String,
)
