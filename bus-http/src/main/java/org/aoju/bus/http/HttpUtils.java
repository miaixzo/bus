/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http;

import org.aoju.bus.core.consts.MediaType;
import org.aoju.bus.core.lang.exception.HttpUncheckException;
import org.aoju.bus.core.utils.JsonUtils;
import org.aoju.bus.core.utils.ObjectUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Http 辅助类
 *
 * @author Kimi Liu
 * @version 3.0.0
 * @since JDK 1.8
 */
public class HttpUtils {

    public static HttpClient client = new HttpClient.Builder()
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS).build();

    /**
     * sync processing of GET request
     *
     * @param url url url address
     * @return The result of the request is output<code>String</code>。
     */
    public static String get(String url) throws HttpUncheckException {
        Request request = new Request.Builder().url(url)
                .get().build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            return response.body().string();
        } catch (IOException e) {
            throw new HttpUncheckException(e);
        }
    }

    /**
     * async processing of GET requests, custom request types
     *
     * @param url      url
     * @param callback callback
     */
    public static void get(String url, Callback callback) {
        Request request = new Request.Builder().url(url)
                .get().build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    /**
     * async processing of POST requests, custom request types
     *
     * @param url      url
     * @param map      map
     * @param callback callback
     */
    public static void post(String url, Map<String, Object> map, Callback callback) {
        String data = getParameterMap(map);
        RequestBody requestBody = RequestBody.create(MediaType.TEXT_HTML_TYPE, data);
        Request request = new Request.Builder().url(url)
                .post(requestBody).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    /**
     * sync processing of POST requests, custom request types
     *
     * @param url url
     * @param map map
     * @return The result of the request is output<code>String or byte[]</code>。
     */
    public static String post(String url, Map<String, Object> map) throws HttpUncheckException {
        try {
            String data = getParameterMap(map);
            RequestBody requestBody = RequestBody.create(MediaType.APPLICATION_FORM_URLENCODED_TYPE, data);
            Request request = new Request.Builder().url(url)
                    .post(requestBody).build();
            Call call = client.newCall(request);
            Response response = call.execute();
            return response.body().string();
        } catch (IOException e) {
            throw new HttpUncheckException(e);
        }
    }

    /**
     * sync processing of POST requests, custom request types
     *
     * @param url    url
     * @param map    map
     * @param isByte isByte return a value type
     * @return The result of the request is output<code>String or byte[]</code>。
     */
    public static Object post(String url, Map<String, Object> map, boolean isByte) {
        try {
            String data = getParameterMap(map);
            RequestBody requestBody = RequestBody.create(MediaType.APPLICATION_FORM_URLENCODED_TYPE, data);
            Request request = new Request.Builder().url(url)
                    .post(requestBody).build();
            Call call = client.newCall(request);
            Response response = call.execute();
            if (isByte) {
                return response.body().bytes();
            }
            return response.body().string();
        } catch (IOException e) {
            throw new HttpUncheckException(e);
        }
    }

    /**
     * sync processing of POST requests, custom request types
     *
     * @param url       url
     * @param data      data
     * @param hearder   hearder
     * @param mediaType mediaType
     * @return The result of the request is output<code>String</code>。
     */
    public static String post(String url, String data, Map<String, Object> hearder, MediaType mediaType) {
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request.Builder builder = new Request.Builder().url(url);

        if (hearder != null) {
            Set<String> set = hearder.keySet();
            for (String key : set) {
                builder.addHeader(key, String.valueOf(hearder.get(key)));
            }
        }
        Request request = builder
                .post(requestBody).build();
        Call call = client.newCall(request);
        String result = "";
        try {
            result = call.execute().body().string();
        } catch (IOException e) {
            throw new HttpUncheckException(e);
        }
        return result;
    }

    /**
     * sync processing of POST requests, custom request types
     *
     * @param url       url
     * @param map       map
     * @param hearder   hearder
     * @param mediaType mediaType
     * @return The result of the request is output<code>String</code>。
     */
    public static String post(String url, Map<String, Object> map, Map<String, Object> hearder, MediaType mediaType) {
        String data = "";
        if (map != null) {
            if (MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {
                data = JsonUtils.toJson(map);
            } else {
                data = getParameterMap(map);
            }
        }
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request.Builder builder = new Request.Builder().url(url);

        if (hearder != null) {
            Set<String> set = hearder.keySet();
            for (String key : set) {
                builder.addHeader(key, String.valueOf(hearder.get(key)));
            }
        }
        Request request = builder
                .post(requestBody).build();
        Call call = client.newCall(request);
        String result = "";
        try {
            result = call.execute().body().string();
        } catch (IOException e) {
            throw new HttpUncheckException(e);
        }
        return result;
    }

    /**
     * handle POST requests asynchronously and customize the request type
     *
     * @param url       url
     * @param map       map
     * @param hearder   hearder
     * @param mediaType mediaType
     * @param callback  callback
     */
    public static void post(String url, Map<String, Object> map, Map<String, Object> hearder, MediaType mediaType, Callback callback) {
        String data = "";
        if (map != null) {
            if (MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {
                data = JsonUtils.toJson(map);
            } else {
                data = getParameterMap(map);
            }
        }
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request.Builder builder = new Request.Builder().url(url);

        if (hearder != null) {
            Set<String> set = hearder.keySet();
            for (String key : set) {
                builder.addHeader(key, String.valueOf(hearder.get(key)));
            }
        }
        Request request = builder
                .post(requestBody).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    /**
     * Process the Http request Map
     *
     * @param map map
     * @return The result is output<code>String</code>。
     */
    protected static String getParameterMap(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        if (ObjectUtils.isNotEmpty(map)) {
            Set<String> keys = map.keySet();
            for (String key : keys) {
                sb.append(key).append("=").append(map.get(key)).append("&");
            }
        }
        return sb.toString();
    }

}
