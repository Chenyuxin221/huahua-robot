package com.huahua.robot.service

interface SwitchSateService {
    /**
     * 设置开关状态
     * @param group String  群号
     * @param switch String 开关名字
     * @param state Boolean 状态
     */
    fun set(groupId: String, switch: String, state: Boolean)

    /**
     * 获取开关状态
     * @param group String  群号
     * @param switch String 开关名字
     * @return Boolean 状态
     */
    fun get(groupId: String, switch: String): Boolean?

    /**
     *
     * @param group String  群号
     * @param switch String 开关名字
     * @return Boolean 是否成功
     */
    fun delete(groupId: String, switch: String): Boolean?

    /**
     * 获取所有keys
     * @param pattern String
     * @return MutableSet<String>
     */
    fun getKeys(pattern: String): MutableSet<String>

    /**
     * 通过key获取value
     * @param key String    key
     * @return Boolean? 值
     */
    fun getValue(key: String): Boolean?
}