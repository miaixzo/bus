/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.pay.metric;

import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.http.Httpz;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.pay.magic.Results;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Map;

/**
 * Http 代理类
 */
public abstract class AbstractHttpDelegate {

    /**
     * post 请求
     *
     * @param url  请求url
     * @param data 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String post(String url, String data) {
        return Httpx.post(url, data, MediaType.APPLICATION_FORM_URLENCODED);
    }

    /**
     * get 请求
     *
     * @param url      请求url
     * @param paramMap 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String get(String url, Map<String, Object> paramMap) {
        return Httpx.get(url, paramMap);
    }

    /**
     * get 请求
     *
     * @param url      请求url
     * @param paramMap 请求参数
     * @param headers  请求头
     * @return {@link Results} 请求返回的结果
     */
    public static Results get(String url, Map<String, Object> paramMap, Map<String, String> headers) {
        Results results = new Results();
        Response response = getToResponse(url, paramMap, headers);
        results.setBody(response.body().toString());
        results.setStatus(response.code());
        results.setHeaders(response.headers().toMultimap());
        return results;
    }

    /**
     * post 请求
     *
     * @param url      请求url
     * @param paramMap 请求参数
     * @param headers  请求头
     * @return {@link Results}  请求返回的结果
     */
    public static Results post(String url, Map<String, Object> paramMap, Map<String, String> headers) {
        Results results = new Results();
        Response response = postToResponse(url, headers, paramMap);
        results.setBody(response.body().toString());
        results.setStatus(response.code());
        results.setHeaders(response.headers().toMultimap());
        return results;
    }

    /**
     * post 请求
     *
     * @param url     请求url
     * @param data    请求参数
     * @param headers 请求头
     * @return {@link Results}  请求返回的结果
     */
    public static Results post(String url, String data, Map<String, String> headers) {
        Results results = new Results();
        Response response = postToResponse(url, headers, data);
        results.setBody(response.body().toString());
        results.setStatus(response.code());
        results.setHeaders(response.headers().toMultimap());
        return results;
    }

    /**
     * patch 请求
     *
     * @param url     请求url
     * @param data    请求参数
     * @param headers 请求头
     * @return {@link Results}  请求返回的结果
     */
    public static Results patch(String url, String data, Map<String, String> headers) {
        Results results = new Results();
        Response response = patchToResponse(url, headers, data);
        results.setBody(response.body().toString());
        results.setStatus(response.code());
        results.setHeaders(response.headers().toMultimap());
        return results;
    }

    /**
     * delete 请求
     *
     * @param url     请求url
     * @param data    请求参数
     * @param headers 请求头
     * @return {@link Results}  请求返回的结果
     */
    public static Results delete(String url, String data, Map<String, String> headers) {
        Results results = new Results();
        Response response = deleteToResponse(url, headers, data);
        results.setBody(response.body().toString());
        results.setStatus(response.code());
        results.setHeaders(response.headers().toMultimap());
        return results;
    }

    /**
     * put 请求
     *
     * @param url     请求url
     * @param data    请求参数
     * @param headers 请求头
     * @return {@link Results}  请求返回的结果
     */
    public static Results put(String url, String data, Map<String, String> headers) {
        Results results = new Results();
        Response response = putToResponse(url, headers, data);
        results.setBody(response.body().toString());
        results.setStatus(response.code());
        results.setHeaders(response.headers().toMultimap());
        return results;
    }

    /**
     * 上传文件
     *
     * @param url      请求url
     * @param data     请求参数
     * @param certPath 证书路径
     * @param certPass 证书密码
     * @param filePath 上传文件路径
     * @param protocol 协议
     * @return {@link String}  请求返回的结果
     */
    public static String upload(String url, String data, String certPath, String certPass, String filePath, String protocol) {
       /* try {
            File file = FileKit.newFile(filePath);
            return HttpRequest.post(url)
                    .setSSLSocketFactory(SSLSocketFactoryBuilder
                            .of()
                            .setProtocol(protocol)
                            .setKeyManagers(getKeyManager(certPass, certPath, null))
                            .setSecureRandom(new SecureRandom())
                            .build()
                    )
                    .header("Content-Type", "multipart/form-data;boundary=\"boundary\"")
                    .form("file", file)
                    .form("meta", data)
                    .execute()
                    .body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/
        return null;
    }

    /**
     * 上传文件
     *
     * @param url      请求url
     * @param data     请求参数
     * @param certPath 证书路径
     * @param certPass 证书密码
     * @param filePath 上传文件路径
     * @return {@link String}  请求返回的结果
     */
    public static String upload(String url, String data, String certPath, String certPass, String filePath) {
        // return upload(url, data, certPath, certPass, filePath, SSLSocketFactoryBuilder.TLSv1);
        return null;
    }

    /**
     * post 请求
     *
     * @param url      请求url
     * @param data     请求参数
     * @param certPath 证书路径
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public static String post(String url, String data, String certPath, String certPass, String protocol) {
     /*   try {
            return HttpRequest.post(url)
                    .setSSLSocketFactory(SSLSocketFactoryBuilder
                            .of()
                            .setProtocol(protocol)
                            .setKeyManagers(getKeyManager(certPass, certPath, null))
                            .setSecureRandom(new SecureRandom())
                            .build()
                    )
                    .body(data)
                    .execute()
                    .body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
*/
        return null;
    }

    /**
     * post 请求
     *
     * @param url      请求url
     * @param data     请求参数
     * @param certPath 证书路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public static String post(String url, String data, String certPath, String certPass) {
        // return post(url, data, certPath, certPass, SSLSocketFactoryBuilder.TLSv1);
        return null;
    }

    /**
     * post 请求
     *
     * @param url      请求url
     * @param data     请求参数
     * @param certFile 证书文件输入流
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public static String post(String url, String data, InputStream certFile, String certPass, String protocol) {
     /*   try {
            return HttpRequest.post(url)
                    .setSSLSocketFactory(SSLSocketFactoryBuilder
                            .of()
                            .setProtocol(protocol)
                            .setKeyManagers(getKeyManager(certPass, null, certFile))
                            .setSecureRandom(new SecureRandom())
                            .build()
                    )
                    .body(data)
                    .execute()
                    .body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/

        return null;
    }

    /**
     * post 请求
     *
     * @param url      请求url
     * @param data     请求参数
     * @param certFile 证书文件输入流
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public static String post(String url, String data, InputStream certFile, String certPass) {
        // return post(url, data, certFile, certPass, SSLSocketFactoryBuilder.TLSv1);
        return null;
    }

    /**
     * get 请求
     *
     * @param url      请求url
     * @param paramMap 请求参数
     * @param headers  请求头
     * @return {@link Response} 请求返回的结果
     */
    private static Response getToResponse(String url, Map<String, Object> paramMap, Map<String, String> headers) {
        try {
            return Httpz.get()
                    .url(url)
                    .addHeaders(headers)
                    .addParams(paramMap)
                    .build()
                    .execute();
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * post 请求
     *
     * @param url     请求url
     * @param headers 请求头
     * @param data    请求参数
     * @return {@link Response} 请求返回的结果
     */
    private static Response postToResponse(String url, Map<String, String> headers, String data) {
        try {
            return Httpz.post()
                    .url(url)
                    .addHeaders(headers)
                    .body(data)
                    .build()
                    .execute();
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * post 请求
     *
     * @param url      请求url
     * @param headers  请求头
     * @param paramMap 请求参数
     * @return {@link Response} 请求返回的结果
     */
    private static Response postToResponse(String url, Map<String, String> headers, Map<String, Object> paramMap) {
        try {
            return Httpz.post()
                    .url(url)
                    .addHeaders(headers)
                    .addParams(paramMap)
                    .build()
                    .execute();
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * patch 请求
     *
     * @param url     请求url
     * @param headers 请求头
     * @param data    请求参数
     * @return {@link Response} 请求返回的结果
     */
    private static Response patchToResponse(String url, Map<String, String> headers, String data) {
       /*return HttpRequest.patch(url)
                .addHeaders(headers)
                .body(data)
                .execute();*/
        return null;
    }

    /**
     * delete 请求
     *
     * @param url     请求url
     * @param headers 请求头
     * @param data    请求参数
     * @return {@link Response} 请求返回的结果
     */
    private static Response deleteToResponse(String url, Map<String, String> headers, String data) {
       /*  return HttpRequest.delete(url)
                .addHeaders(headers)
                .body(data)
                .execute(); */

        return null;
    }

    /**
     * put 请求
     *
     * @param url     请求url
     * @param headers 请求头
     * @param data    请求参数
     * @return {@link Response} 请求返回的结果
     */
    private static Response putToResponse(String url, Map<String, String> headers, String data) {
        try {
            return Httpz.put()
                    .url(url)
                    .addHeaders(headers)
                    .body(data)
                    .build()
                    .execute();
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    private static KeyManager[] getKeyManager(String certPass, String certPath, InputStream certFile) throws Exception {
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        if (certFile != null) {
            clientStore.load(certFile, certPass.toCharArray());
        } else {
            clientStore.load(new FileInputStream(certPath), certPass.toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(clientStore, certPass.toCharArray());
        return kmf.getKeyManagers();
    }

    /**
     * get 请求
     *
     * @param url 请求url
     * @return {@link String} 请求返回的结果
     */
    public String get(String url) {
        return Httpx.get(url);
    }

    /**
     * post 请求
     *
     * @param url      请求url
     * @param paramMap 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String post(String url, Map<String, Object> paramMap) {
        return Httpx.post(url, paramMap);
    }

    /**
     * patch 请求
     *
     * @param url      请求url
     * @param paramMap 请求参数
     * @param headers  请求头
     * @return {@link Results}  请求返回的结果
     */
    public Results patch(String url, Map<String, Object> paramMap, Map<String, String> headers) {
        Results results = new Results();
        Response response = patchToResponse(url, headers, paramMap);
        results.setBody(response.body().toString());
        results.setStatus(response.code());
        results.setHeaders(response.headers().toMultimap());
        return results;
    }

    /**
     * delete 请求
     *
     * @param url      请求url
     * @param paramMap 请求参数
     * @param headers  请求头
     * @return {@link Results}  请求返回的结果
     */
    public Results delete(String url, Map<String, Object> paramMap, Map<String, String> headers) {
        Results results = new Results();
        Response response = deleteToResponse(url, headers, paramMap);
        results.setBody(response.body().toString());
        results.setStatus(response.code());
        results.setHeaders(response.headers().toMultimap());
        return results;
    }

    /**
     * put 请求
     *
     * @param url      请求url
     * @param paramMap 请求参数
     * @param headers  请求头
     * @return {@link Results}  请求返回的结果
     */
    public Results put(String url, Map<String, Object> paramMap, Map<String, String> headers) {
        Results results = new Results();
        Response response = putToResponse(url, headers, paramMap);
        results.setBody(response.body().toString());
        results.setStatus(response.code());
        results.setHeaders(response.headers().toMultimap());
        return results;
    }

    /**
     * patch 请求
     *
     * @param url      请求url
     * @param headers  请求头
     * @param paramMap 请求参数
     * @return {@link Response} 请求返回的结果
     */
    private Response patchToResponse(String url, Map<String, String> headers, Map<String, Object> paramMap) {
        // Httpv.builder().build().sync(url).addBodyPara(paramMap).addHeader(headers).patch();
        return null;
    }

    /**
     * delete 请求
     *
     * @param url      请求url
     * @param headers  请求头
     * @param paramMap 请求参数
     * @return {@link Response} 请求返回的结果
     */
    private Response deleteToResponse(String url, Map<String, String> headers, Map<String, Object> paramMap) {
      /*  return HttpRequest.delete(url)
                .addHeaders(headers)
                .form(paramMap)
                .execute();*/
        return null;
    }

    /**
     * put 请求
     *
     * @param url      请求url
     * @param headers  请求头
     * @param paramMap 请求参数
     * @return {@link Response} 请求返回的结果
     */
    private Response putToResponse(String url, Map<String, String> headers, Map<String, Object> paramMap) {
        try {
            return Httpz.put()
                    .url(url)
                    .addHeaders(headers)
                    .addParams(paramMap)
                    .build()
                    .execute();
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

}
