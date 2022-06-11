package com.huahua.robot.music.entity.kugoumusic.list

data class MusicList(
    /**
     * 状态码
     */
    val code:Int,
    /**
     * 状态信息
     */
    val text:String,
    /**
     * 歌曲列表
     */
    val data: List<ListData>
)
