package com.huahua.robot.utils

import com.alibaba.fastjson.JSONObject
import com.huahua.robot.entity.RequestEntity
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.http.util.EntityUtils
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URISyntaxException
import java.util.function.BiConsumer

@SuppressWarnings("unused")
class HttpUtil {
    private var STORE: org.apache.http.client.CookieStore? = null
    private var CLOSEABLE_HTTP_CLIENT: org.apache.http.impl.client.CloseableHttpClient? = null

    init {
        // 缓存Cookie
        STORE = org.apache.http.impl.client.BasicCookieStore()
        CLOSEABLE_HTTP_CLIENT =
            org.apache.http.impl.client.HttpClients.custom().setDefaultCookieStore(STORE).build()
        /*.setProxy(new HttpHost("172.24.58.136",5553))*/
    }

    /**
     * get请求
     *
     * @param url 请求的URL
     */
    operator fun get(url: String?): RequestEntity {
        val cookies: Map<String, String> = HashMap(0)
        val params: Map<String, String> = HashMap(0)
        return HttpUtil()[url, params, cookies]
    }

    /**
     * get请求
     *
     * @param url     请求的URL
     * @param params  请求的参数
     * @param cookies 请求携带的cookie
     */
    operator fun get(url: String?, params: Map<String, String>?, cookies: Map<String, String>?): RequestEntity {
        try {
            val uriBuilder: org.apache.http.client.utils.URIBuilder = org.apache.http.client.utils.URIBuilder(url)
            params?.forEach { (param: String?, value: String?) ->
                uriBuilder.addParameter(param,
                    value)
            }
            val httpGet: org.apache.http.client.methods.HttpGet =
                org.apache.http.client.methods.HttpGet(uriBuilder.build())
            setCookies(httpGet, cookies)
            return request(httpGet)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
        return RequestEntity()
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


    /**
     * post请求
     *
     * @param url 请求的URL
     */
    fun post(url: String?): RequestEntity {
        val cookies: Map<String, String> = HashMap(0)
        val params: Map<String, String> = HashMap(0)
        return post(url, params, cookies)
    }

    /**
     * post请求
     *
     * @param url     请求的URL
     * @param params  请求的参数
     * @param cookies 请求携带的cookie
     */
    fun post(url: String?, params: Map<String, String>?, cookies: Map<String, String>?): RequestEntity {
        try {
            val httpPost: org.apache.http.client.methods.HttpPost = org.apache.http.client.methods.HttpPost(url)
            if (params != null) {
                val paramsList: MutableList<org.apache.http.NameValuePair> =
                    ArrayList<org.apache.http.NameValuePair>()
                params.forEach(BiConsumer { key: String?, value: String? ->
                    paramsList.add(org.apache.http.message.BasicNameValuePair(key, value))
                })
                val formEntity: org.apache.http.client.entity.UrlEncodedFormEntity =
                    org.apache.http.client.entity.UrlEncodedFormEntity(paramsList, "utf-8")
                formEntity.setContentType("Content-Type:application/json")
                httpPost.setEntity(formEntity)
            }
            setCookies(httpPost, cookies)
            return request(httpPost)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return RequestEntity()
    }

    /**
     * 设置cookie
     */
    private fun setCookies(
        httpRequestBase: org.apache.http.client.methods.HttpRequestBase,
        cookies: Map<String, String>?,
    ) {
        if (cookies != null) {
            val cookie = StringBuilder()
            cookies.forEach(BiConsumer { key: String?, value: String? ->
                cookie.append(key).append("=").append(value).append(";")
            })
            httpRequestBase.setHeader("Cookie", cookie.toString())
            httpRequestBase.setHeader("User-Agent",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1")
        }
    }

    /**
     * 网络请求具体实现
     */
    private fun request(httpRequestBase: org.apache.http.client.methods.HttpRequestBase): RequestEntity {
        try {

            val requestEntity = RequestEntity()
            httpRequestBase.setHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36 SE 2.X MetaSr 1.0")
            try {
                CLOSEABLE_HTTP_CLIENT?.execute(httpRequestBase).use { closeableHttpResponse ->
                    requestEntity.response = EntityUtils.toString(closeableHttpResponse?.entity)
                    requestEntity.cookies = STORE?.cookies
                    httpRequestBase.setHeader("Cookie", "")
                    STORE?.clear()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return requestEntity

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return RequestEntity()
    }

    /**
     * 解析返回结果的一个方法
     */
    fun getJson(body: String, separator: String): JSONObject {
        var body = body
        body = body.replace(" ", "")
        var jsonStr = body.substring(body.indexOf(separator) + separator.length + 1)
        jsonStr = jsonStr.substring(0, jsonStr.indexOf("</script>"))
        return JSONObject.parseObject(jsonStr)
    }
}
