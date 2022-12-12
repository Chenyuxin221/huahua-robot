<div align="center">
    <img src="http://gchat.qpic.cn/gchatpic_new/0/0-0-C59648BA0E460CA10E258D353318D713/0?term=2" alt="logo" style="width:233px ;height:233px;border-radius:50%"/>
    <p>
    	<h2>
        	花花的聊天机器人项目
    	</h2>
</div>

该项目为Springboot项目，此项目基于[simple bot v3](https://github.com/simple-robot/simbot-component-mirai)，实现了其中的mirai组件

## 项目文档

菜单地址：[3.x 功能菜单](https://www.yuque.com/qingsi-zwnmu/xyuvvi/wrbzgy)

内置WebApi: [api文档](https://console-docs.apipost.cn/preview/2994e3757e2103c4/f6807ee950c44a1e?target_id=f67ce078-7aa4-44a7-bfcb-9605bde46489#fb8ff78b-4e48-49e8-9c5c-00e25d7476d6)

## 项目地址

[GitHub](https://github.com/Chenyuxin221/huahua-robot)

## 运行环境

maven,java,mysql，kotlin

## 你需要掌握的知识

kotlin 基础，Springboot 常用注解

## 如何使用它

### 下载项目

安装了git可以直接执行`clone git@github.com:Chenyuxin221/huahua-robot.git`

或者可以去GitHub上下载压缩包，解压缩后导入到你idea

Idea可以直接克隆 具体教程自己去[百度](https://www.baidu.com) 篇幅太长

再不会的自己去[百度](https://www.baidu.com)

### 配置bot

在[simbot-bots](./src/main/resources/simbot-bots)目录下创建bot.bot文件

bot.bot

```json
{
  "component": "simbot.mirai",
  "code": 123456789,
  "passwordInfo": {
    "type": "md5_text",
    "md5": "使用md5加密后的bot密码"
  },
  "config": {
    "workingDir": ".",
    "heartbeatPeriodMillis": 60000,
    "statHeartbeatPeriodMillis": 300000,
    "heartbeatTimeoutMillis": 5000,
    "heartbeatStrategy": "STAT_HB",
    "reconnectionRetryTimes": 2147483647,
    "autoReconnectOnForceOffline": false,
    "protocol": "ANDROID_PAD",
    "highwayUploadCoroutineCount": 16,
    "deviceInfo": {
      "type": "auto"
    },
    "noNetworkLog": false,
    "noBotLog": false,
    "isShowingVerboseEventLog": false,
    "cacheDir": "cache",
    "contactListCache": {
      "saveIntervalMillis": 60000,
      "friendListCacheEnabled": false,
      "groupMemberListCacheEnabled": false
    },
    "loginCacheEnabled": true,
    "convertLineSeparator": true,
    "recallMessageCacheStrategyConfig": {
      "type": "memory_lru"
    }
  }
}
```

### 配置个人数据

在[resources](./src/main/resources/)目录下创建[application.properties]()文件

application.properties

```properties
simbot.core.keep-alive=true
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=你的数据库地址
spring.datasource.username=用户名
spring.datasource.password=用户密码
huahua.account.admin.id=管理员QQ号
huahua.account.bot.id=BotQQ号
spring.redis.host=127.0.0.1
spring.redis.port=6379
#可选
huahua.config.boot-command=“启动文件路径”
huahua.config.morningPaper.groups=123,456,789 #需要每日早报的群
#可选(涩图检测)
huahua.config.exclude_Groups=123,456 #过滤掉话多的群，钱多可以不用
huahua.config.baidubce.client_id=百度ai Api Key
huahua.config.baidubce.client_secret=百度ai Secret KEY
#chatgpt的token，具体教程可以翻看最下面
huahua.config.gpt.token=你的token
huahua.config.gpt.cf_clearance=你的cf_clearance
```

#### 付费接口地址

[百度AI平台](https://ai.baidu.com/ai-doc/ANTIPORN/Wkhu9d5iy)

#### 例：启动文件.bat

```bat
@ECHO OFF 
java -jar "你打包好后的jar包"
```

#### 数据表结构

执行[sql](./src/main/resources/sql)目录下的*.sql文件

### 插件

#### 原神抽卡模拟（如不需要请自行删除[GenshinPrayListener](./src/main/kotlin/com/huahua/robot/listener/grouplistener/GenshinPrayListener.kt)）

[插件地址](https://github.com/GardenHamster/GenshinPray/blob/main/Document.md)

### 运行Bot

运行[HuahuaRobotApplication.kt](./src/main/kotlin/com/huahua/robot/HuahuaRobotApplication.kt)文件

## 项目结构

```text
├─main
│  ├─kotlin
│  │  └─com
│  │      └─huahua
│  │          └─robot
│  │              │  HuahuaRobotApplication.kt			  ---启动文件
│  │              │  
│  │              ├─api									---API目录
│  │              │  ├─controller						 ---控制层
│  │              │  │      
│  │              │  ├─entity							 ---实体类
│  │              │  │      
│  │              │  ├─enums							 ---暂时没用	枚举
│  │              │  │      
│  │              │  ├─mapper							 ---mapper层
│  │              │  │      
│  │              │  └─response							 ---返回响应类
│  │              │          
│  │              ├─core								--- 核心
│  │              │  ├─annotation						 ---注解
│  │              │  │      RobotListen.kt				  ---监听注解
│  │              │  │      
│  │              │  ├─common							 ---一些常用的类 例如AOP
│  │              │  │      
│  │              │  ├─entity							 ---实体类
│  │              │  │      
│  │              │  ├─enums							 ---枚举
│  │              │  │      
│  │              │  ├─listener							  ---核心里的一些监听 做一些开关机操作
│  │              │  │      
│  │              │  ├─mapper							  ---数据库操作
│  │              │  │      
│  │              │  └─util								  ---工具类
│  │              │          PermissionUtil.kt
│  │              │          
│  │              ├─entity								  ---实体类
│  │              │          
│  │              ├─exception							  ---异常
│  │              │      
│  │              ├─listener
│  │              │  ├─grouplistener					   ---群监听
│  │              │  │      
│  │              │  └─privatelistener                       ---私聊监听
│  │              │          
│  │              ├─music								  --点歌
│  │              │  │  
│  │              │  ├─entity							   ---歌曲实体类
│  │              │  │          
│  │              │  └─util								   ---工具类
│  │              │          
│  │              └─utils								   ---通用工具类
│  │                      
│  └─resources											  ---资源目录
│      │  application.properties						    ---Springboot配置文件
│      │  
│      ├─simbot-bots									   ---机器人目录 每个机器人都是单独的.bot文件
│      │      
│      ├─sql											  ---数据表
│      │      group_boot_state.sql
│      │      images_url.sql
│      │      message.sql
│      │      
│      ├─static
│      └─templates
```

## 注意事项

- ### 无法登录提示环境异常 循环验证登不上去

​ 请在手机上打开设备锁，再重新申请验证

- ### 不要在*.properties文件里加一些奇奇怪怪的配置项
- ### 如何获取ChatGpt的token

前提：需要你能登录谷歌邮箱
注册需要非国内手机号，可以通过[此网站](https://sms-activate.org/cn)获取，详细教程可以百度
注册并登录[官网](https://chat.openai.com/)
登录完成后，按f12然后选择网络视图，之后的操作不要关闭
闲聊两句，点击名称里面的conversation文件，选择标头往下面翻找到请求标头(前面有个箭头)，在
往下翻找到cookie（黑体加粗），仔细寻找里面的__Secure-next-auth.session-token，
=后面的就是需要的token了，大概是后面的所有如果后面没有;的话
cf_clearance同理，不过通常就在token的前面