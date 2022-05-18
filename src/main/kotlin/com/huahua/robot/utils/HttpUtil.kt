package com.huahua.robot.utils

import com.alibaba.fastjson2.JSON
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
    private var store: CookieStore = BasicCookieStore()
    private var closeableHttpClient: CloseableHttpClient

    init {
        // 忽略Invalid cookie header
        val defaultConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).build()
        closeableHttpClient = HttpClients.custom().setDefaultCookieStore(store).setDefaultRequestConfig(defaultConfig)
//            .setProxy(HttpHost("127.0.0.1", 5533))
            .build()
    }

    fun downloadFile(url: String, path: String): Boolean {
        try {
            val file = Paths.get(path)
            if (file.parent.notExists()) {
                Files.createDirectories(file.parent)
            }
            Files.newOutputStream(file).use { output ->
                closeableHttpClient.execute(HttpGet(URIBuilder(url).build())).use {
                    it.entity.writeTo(output)
                    return true
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
        return false
    }


    /**
     * 设置cookie
     */
    private fun setCookies(httpRequestBase: HttpRequestBase, cookies: Map<String, String>?) {
        if (cookies != null) {
            val cookie = StringBuilder()
            cookies.forEach {
                cookie.append(it.key).append("=").append(it.value).append(";")
            }
            httpRequestBase.setHeader(SM.COOKIE, cookie.toString())
        }
    }


    /**
     * 网络请求具体实现
     */
    private fun request(httpRequestBase: HttpRequestBase): ResponseEntity {
        try {
            val responseEntity = ResponseEntity()
            httpRequestBase.setHeader(HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36")
            httpRequestBase.setHeader(HttpHeaders.ACCEPT,
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            try {
                closeableHttpClient.execute(httpRequestBase).use {
                    val entity = it.entity
                    responseEntity.entity = EntityUtils.toByteArray(entity)
                    responseEntity.response = String(responseEntity.entity, StandardCharsets.UTF_8)
                    responseEntity.cookies = store.cookies.stream()
                        .collect(Collectors.toMap({ obj: Cookie -> obj.name },
                            { obj: Cookie -> obj.value },
                            { k1: String, _: String -> k1 }))
                    responseEntity.headers = it.allHeaders.clone()
                    httpRequestBase.setHeader(SM.COOKIE, "")
                    store.clear()
                }
            } catch (e: IOException) {
                logger(LogLevel.WARN, e) { "" }
            }
            return responseEntity
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
        return ResponseEntity()
    }

    private fun request(method: io.ktor.http.HttpMethod, requestEntity: RequestEntity.() -> Unit): ResponseEntity {
        val entity = RequestEntity().apply { requestEntity() }
        val params = entity.getParams()
        val url = if (params.isEmpty()) entity.url else "${entity.url}?${params.toQueryString()}"
        val uri = URIBuilder(url).build()
        val httpRequest: HttpRequestBase = when (method) {
            io.ktor.http.HttpMethod.Get -> HttpGet(uri)
            io.ktor.http.HttpMethod.Post -> HttpPost(uri).also {
                if (entity.json.isNotBlank()) {
                    it.entity = StringEntity(entity.json)
                    it.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)
                }
            }
            io.ktor.http.HttpMethod.Delete -> HttpDelete(uri)
            io.ktor.http.HttpMethod.Put -> HttpPut(uri).also {
                if (entity.json.isNotBlank()) {
                    it.entity = StringEntity(entity.json)
                    it.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)
                }
            }
            else -> null
        } ?: return ResponseEntity()
        setCookies(httpRequest, entity.getCookies())
        entity.getHeaders().forEach {
            it?.let {
                httpRequest.setHeader(it)
            }
        }
        return request(httpRequest)
    }

    private fun <K, V> Map<K, V>.toQueryString(): String =
        this.map { "${it.key}=${URLEncoder.encode(it.value.toString(), "utf-8")}" }.joinToString("&")

    fun get(requestEntity: RequestEntity.() -> Unit): ResponseEntity {
        return request(io.ktor.http.HttpMethod.Get, requestEntity)
    }

    fun post(requestEntity: RequestEntity.() -> Unit): ResponseEntity {
        return request(io.ktor.http.HttpMethod.Post, requestEntity)
    }

    fun delete(requestEntity: RequestEntity.() -> Unit): ResponseEntity {
        return request(io.ktor.http.HttpMethod.Delete, requestEntity)
    }

    fun put(requestEntity: RequestEntity.() -> Unit): ResponseEntity {
        return request(io.ktor.http.HttpMethod.Put, requestEntity)
    }

    fun get(uri: String): ResponseEntity {
        return get { this.url = uri }
    }

    fun post(uri: String): ResponseEntity {
        return post { url = uri }
    }

    fun delete(uri: String): ResponseEntity {
        return delete { url = uri }
    }

    fun put(uri: String): ResponseEntity {
        return put { url = uri }
    }

    /**
     * 解析腾讯接口返回结果的一个方法
     */
    fun getJson(url: String, separator: String): com.alibaba.fastjson2.JSONObject {
        val body = get(url).response.replace(" ", "")
        var jsonStr = body.substring(body.indexOf(separator) + separator.length + 1)
        jsonStr = jsonStr.substring(0, jsonStr.indexOf("</script>")).replace(Regex(":\\s?undefined"), ":\"\"")
        return JSON.parseObject(jsonStr)
    }

    /**
     * 获取网页正文
     * @param url String    网址
     * @return String 网页正文
     */
    fun getBody(url: String): String {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val result = client.newCall(request).execute().body()
        return result?.string().toString()
    }

    /**
     * 获取网页返回请求体
     * @param url String    网址
     * @return Response 返回体
     */
    fun getResponse(url: String): Response {
        return OkHttpClient().newCall(Request.Builder().url(url).build()).execute()
    }

}

class RequestEntity {
    lateinit var url: String
    var params: (Params.() -> Unit) = { }
    var header: (Params.() -> Unit) = { }
    var cookies: (Params.() -> Unit) = { }
    var json: String = ""
    lateinit var method: HttpMethod
    fun getParams(): MutableMap<String, String> {
        return Params().apply { params() }.map
    }

    fun getHeaders(): Array<Header?> {
        val headers = Params().apply { header() }.map
        val arr = arrayOfNulls<Header>(headers.size)
        headers.onEachIndexed { index, it ->
            arr[index] = BasicHeader(it.key, it.value)
        }
        return arr
    }

    fun getCookies(): MutableMap<String, String> {
        return Params().apply { cookies() }.map
    }
}

class Params {
    var map: MutableMap<String, String> = HashMap()
    operator fun String.minus(value: String) {
        map[this] = value
    }

    operator fun Map<String, String?>.unaryMinus() {
        forEach {
            it.value?.let { v ->
                map[it.key] = v
            }
        }
    }
}
class ResponseEntity {
    var cookies: Map<String, String> = HashMap()
    var headers: Array<Header> = emptyArray()
    var response: String = ""
    lateinit var entity: ByteArray

    override fun toString(): String {
        return "RequestEntity(cookies=$cookies, headers=${headers.contentToString()}, response=$response)"
    }

    fun getJSONResponse(): com.alibaba.fastjson2.JSONObject? = if (response.isBlank()) null else JSON.parseObject(response)

}
