package com.huahua.robot.music

import net.mamoe.mirai.message.data.MusicKind

data class MusicInfo(
    /**
     * 音乐id
     */
    var mid: String,

    /**
     * 标题
     */
    var title: String,

    /**
     * 子标题
     */
    var subtitle: String?,

    /**
     * 艺术家
     */
    var artist: String,

    /**
     * 专辑
     */
    var album: String,

    /**
     * 封面链接
     */
    var previewUrl: String,

    /**
     * 跳转链接
     */
    var jumpUrl: String,

    /**
     * 文件名
     */
    var fileName: String,

    /**
     * 是否要付费播放
     */
    var payPlay: Boolean,

    /**
     * 播放链接
     */
    var musicUrl: String,

    /**
     * 类型
     */
    var type: MusicKind = MusicKind.QQMusic
)
