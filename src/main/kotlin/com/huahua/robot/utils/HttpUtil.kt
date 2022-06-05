package com.huahua.robot.utils

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import com.google.gson.Gson
import com.huahua.robot.core.common.logger
import io.ktor.http.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.http.Header
import org.apache.http.HttpHeaders
import org.apache.http.client.CookieStore
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.*
import org.apache.http.client.utils.URIBuilder
import org.apache.http.cookie.Cookie
import org.apache.http.cookie.SM
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils
import org.springframework.boot.logging.LogLevel
import java.io.IOException
import java.net.URISyntaxException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ExecutionException
import java.util.stream.Collectors
import kotlin.io.path.notExists

@SuppressWarnings("unused")
object HttpUtil {
    // 缓存Cookie
    private var store: CookieStore = BasicCookieStore() // 初始化一个CookieStore实例
    private var closeableHttpClient: CloseableHttpClient    // 初始化一个CloseableHttpClient实例


    init {
        // 忽略Invalid cookie header
        val defaultConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).build()
        closeableHttpClient = HttpClients.custom().setDefaultCookieStore(store).setDefaultRequestConfig(defaultConfig)
//            .setProxy(HttpHost("127.0.0.1", 5533))
            .build()
    }

    fun downloadFile(url: String, path: String): Boolean {
        try {
            val file = Paths.get(path)  // 创建文件路径
            if (file.parent.notExists()) {  // 判断文件路径是否存在
                Files.createDirectories(file.parent)    // 创建文件路径
            }
            Files.newOutputStream(file).use { output -> // 创建文件输出流
                closeableHttpClient.execute(HttpGet(URIBuilder(url).build())).use { // 创建HttpGet请求
                    it.entity.writeTo(output)   // 写入文件
                    return true // 下载成功
                }
            }
        } catch (e: IOException) {  // 异常处理
            e.printStackTrace() // 打印异常信息
        } catch (e: URISyntaxException) {   // 异常处理
            e.printStackTrace() // 打印异常信息
        }
        return false    // 下载失败
    }


    /**
     * 设置cookie
     */
    private fun setCookies(httpRequestBase: HttpRequestBase, cookies: Map<String, String>?) {
        if (cookies != null) {
            val cookie = StringBuilder()    // 创建Cookie
            cookies.forEach {   // 遍历cookies
                cookie.append(it.key).append("=").append(it.value).append(";")  // 拼接Cookie
            }
            httpRequestBase.setHeader(SM.COOKIE, cookie.toString()) // 设置Cookie
        }
    }


    /**
     * 网络请求具体实现
     */
    private fun request(httpRequestBase: HttpRequestBase): ResponseEntity {
        try {
            val responseEntity = ResponseEntity()   // 创建响应实体
            httpRequestBase.setHeader(HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36")   // 设置User-Agent
            httpRequestBase.setHeader(HttpHeaders.ACCEPT,
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")  // 设置Accept
            try {   // 尝试获取响应
                closeableHttpClient.execute(httpRequestBase).use {  // 创建HttpGet请求
                    val entity = it.entity  // 获取响应实体
                    responseEntity.entity = EntityUtils.toByteArray(entity) // 获取响应内容
                    responseEntity.response = String(responseEntity.entity, StandardCharsets.UTF_8) // 获取响应内容
                    responseEntity.cookies = store.cookies.stream() // 获取响应Cookie
                        .collect(Collectors.toMap({ obj: Cookie -> obj.name },
                            { obj: Cookie -> obj.value },
                            { k1: String, _: String -> k1 }))   // 收集响应Cookie
                    responseEntity.headers = it.allHeaders.clone()  // 获取响应头
                    httpRequestBase.setHeader(SM.COOKIE, "")    // 清空Cookie
                    store.clear()   // 清空Cookie
                }
            } catch (e: IOException) {  // 异常处理
                logger(LogLevel.WARN, e) { "" } // 打印异常信息
            }
            return responseEntity   // 返回响应实体
        } catch (e: InterruptedException) { // 异常处理
            Thread.currentThread().interrupt()  // 中断线程
        } catch (e: ExecutionException) {   // 异常处理
            e.printStackTrace() // 打印异常信息
        }
        return ResponseEntity() // 返回响应实体
    }

    private fun request(method: HttpMethod, requestEntity: RequestEntity.() -> Unit): ResponseEntity { // 创建请求实体
        val entity = RequestEntity().apply { requestEntity() }  // 获取请求实体
        val params = entity.getParams() // 获取请求参数
        val url = if (params.isEmpty()) entity.url else "${entity.url}?${params.toQueryString()}"   // 获取请求地址
        val uri = URIBuilder(url).build()   // 创建URI
        val httpRequest: HttpRequestBase = when (method) {  // 创建HttpRequestBase
            HttpMethod.Get -> HttpGet(uri) // 创建HttpGet请求
            HttpMethod.Post -> HttpPost(uri).also {    // 创建HttpPost请求
                if (entity.json.isNotBlank()) { // 判断请求内容是否为空
                    it.entity = StringEntity(entity.json)   // 设置请求内容
                    it.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)   // 设置请求内容类型
                }
            }
            HttpMethod.Delete -> HttpDelete(uri)   // 创建HttpDelete请求
            HttpMethod.Put -> HttpPut(uri).also {  // 创建HttpPut请求
                if (entity.json.isNotBlank()) { // 判断请求内容是否为空
                    it.entity = StringEntity(entity.json)   // 设置请求内容
                    it.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)   // 设置请求内容类型
                }
            }
            else -> null    // 不支持的请求方式
        } ?: return ResponseEntity()    // 返回响应实体
        setCookies(httpRequest, entity.getCookies())    // 设置Cookie
        entity.getHeaders().forEach {   // 遍历请求头
            it?.let {   // 判断是否为空
                httpRequest.setHeader(it)   // 设置请求头
            }
        }
        return request(httpRequest) // 返回响应实体
    }

    private fun <K, V> Map<K, V>.toQueryString(): String =
        this.map { "${it.key}=${URLEncoder.encode(it.value.toString(), "utf-8")}" }.joinToString("&")   // 转换为QueryString

    fun get(requestEntity: RequestEntity.() -> Unit): ResponseEntity {  // 创建Get请求
        return request(HttpMethod.Get, requestEntity)  // 创建请求实体
    }

    fun post(requestEntity: RequestEntity.() -> Unit): ResponseEntity { // 创建Post请求
        return request(HttpMethod.Post, requestEntity) // 创建请求实体
    }

    fun delete(requestEntity: RequestEntity.() -> Unit): ResponseEntity {   // 创建Delete请求
        return request(HttpMethod.Delete, requestEntity)    // 创建请求实体
    }

    fun put(requestEntity: RequestEntity.() -> Unit): ResponseEntity {  // 创建Put请求
        return request(HttpMethod.Put, requestEntity)   // 创建请求实体
    }

    fun get(uri: String): ResponseEntity {  // 创建Get请求
        return get { this.url = uri }   // 设置请求地址
    }

    fun post(uri: String): ResponseEntity { // 创建Post请求
        return post { url = uri }   // 设置请求地址
    }

    fun delete(uri: String): ResponseEntity {   // 创建Delete请求
        return delete { url = uri }   // 设置请求地址
    }

    fun put(uri: String): ResponseEntity {  // 创建Put请求
        return put { url = uri }    // 设置请求地址
    }

    /**
     * 解析腾讯接口返回结果的一个方法
     */
    fun getJson(url: String, separator: String): com.alibaba.fastjson2.JSONObject { // 创建解析腾讯接口返回结果的一个方法
        val body = get(url).response.replace(" ", "")   // 获取响应内容
        var jsonStr = body.substring(body.indexOf(separator) + separator.length + 1)    // 获取响应内容的json字符串
        jsonStr = jsonStr.substring(0, jsonStr.indexOf("</script>")).replace(Regex(":\\s?undefined"), ":\"\"")  // 获取script标签中的json字符串
        return JSON.parseObject(jsonStr)    // 解析json字符串
    }

    /**
     * 获取网页正文
     * @param url String    网址
     * @return String 网页正文
     */
    fun getBody(url: String): String {
        val client = OkHttpClient() // 创建OkHttpClient对象
        val request = Request.Builder().url(url).build()    // 创建Request对象
        val result = client.newCall(request).execute().body()   // 获取响应内容
        return result?.string().toString()  // 返回响应内容
    }

    /**
     * 从返回json字符串的网址中获取json对象
     * @param url String    网址
     * @param clazz Class<T>    实体类
     * @return T    实体类
     */
    fun <T> getJsonClassFromUrl(url: String,clazz:Class<T>):T{
        return Gson().fromJson(getBody(url),clazz)  // 从返回json字符串的网址中获取json对象
    }


    /**
     * 获取网页返回请求体
     * @param url String    网址
     * @return Response 返回体
     */
    fun getResponse(url: String): Response {
        return OkHttpClient().newCall(Request.Builder().url(url).build()).execute() // 获取网页返回请求体
    }

}

class RequestEntity {
    lateinit var url: String
    var params: (Params.() -> Unit) = { }   // 参数
    var header: (Params.() -> Unit) = { }   // 请求头
    var cookies: (Params.() -> Unit) = { }  // Cookie
    var json: String = ""   // json字符串
    lateinit var method: HttpMethod // 请求方式
    fun getParams(): MutableMap<String, String> {   // 获取参数
        return Params().apply { params() }.map  // 获取参数
    }

    fun getHeaders(): Array<Header?> {  // 获取请求头
        val headers = Params().apply { header() }.map       // 获取请求头
        val arr = arrayOfNulls<Header>(headers.size)    // 创建请求头数组
        headers.onEachIndexed { index, it ->    // 遍历请求头
            arr[index] = BasicHeader(it.key, it.value)  // 将请求头添加到数组中
        }
        return arr  // 返回请求头数组
    }

    fun getCookies(): MutableMap<String, String> {  // 获取Cookie
        return Params().apply { cookies() }.map // 获取Cookie
    }
}

class Params {  // 参数
    var map: MutableMap<String, String> = HashMap() // 参数集合
    operator fun String.minus(value: String) {  // 参数添加
        map[this] = value   // 将参数添加到集合中
    }

    operator fun Map<String, String?>.unaryMinus() {    // 参数添加
        forEach {   // 遍历参数集合
            it.value?.let { v ->    // 如果参数值不为空
                map[it.key] = v // 将参数添加到集合中
            }
        }
    }
}
class ResponseEntity {
    var cookies: Map<String, String> = HashMap()    // Cookie
    var headers: Array<Header> = emptyArray()   // 请求头
    var response: String = ""   // 响应内容
    lateinit var entity: ByteArray  // 响应实体

    override fun toString(): String {   // 返回响应内容
        return "RequestEntity(cookies=$cookies, headers=${headers.contentToString()}, response=$response)"  // 返回响应内容
    }

    fun getJSONResponse(): JSONObject? = if (response.isBlank()) null else JSON.parseObject(response) // 返回响应内容的json对象

}
