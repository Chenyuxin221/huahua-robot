package com.huahua.robot.api.controller

/**
 * ClassName: GroupBootStateController
 * @description
 * @author 花云端
 * @date 2022-06-02 18:32
 */
//@RestController
//@RequestMapping("/groupBootState")
//class GroupBootStateController(
//    @Autowired
//    private val mapper: GroupBootStateMapper
//) {
//
//    /**
//     * 获取群开关机状态
//     * @param groupId String    群id
//     * @return String   群状态
//     */
//    @GetMapping("/getState")
//    fun getState(groupId: String) =
//        mapper.selectByMap(mapOf("group_code" to groupId)).ifEmpty { null }.let {
//            when (it) {
//                null -> RestResponse.error<String>(RestCode.DATA_NOT_FOUND)
//                else -> RestResponse.success(GroupBootState(null, it.first().groupCode, it.first().state))
//            }
//        }
//
//
//    /**
//     *设置群开机状态
//     * @param groupId String    群号
//     * @param state Boolean   状态
//     * @return String    成功或失败
//     */
//    @GetMapping("/setState")
//    fun setState(@RequestParam("groupId") groupId: String, @RequestParam("state") state: Boolean): String {
//        val list = mapper.selectByMap(mapOf("group_code" to groupId))
//        if (list == null || list.isEmpty()) {
//            mapper.insert(GroupBootState(null, groupId, state))
//            return Gson().toJson(MsgResponse(200, "没有找到该群，已自动添加"))
//        }
//        val wrapper = QueryWrapper<GroupBootState>().eq("group_code", groupId)
//        mapper.update(GroupBootState(null, groupId, state), wrapper)
//        return Gson().toJson(MsgResponse(200, "设置成功"))
//    }
//}