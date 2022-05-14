package com.huahua.robot.entity.setu

data class Data(
    val p:String,
    val uid:Int,
    val author:String,
    val r18:Boolean,
    val width:Int,
    val pid:Int,
    val title:String,
    val url:String,
    val height:Int,
    val tags: List<String>
)
