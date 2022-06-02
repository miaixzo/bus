package org.aoju.bus.pay.metric;

import org.aoju.bus.core.exception.InstrumentException;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.http.Httpv;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.http.Httpz;
import org.aoju.bus.http.magic.HttpResponse;
import org.aoju.bus.pay.magic.Results;

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
        Results response = new Results();
        HttpResponse httpResponse = getToResponse(url, paramMap, headers);
        response.setBody(httpResponse.body().toString());
        response.setStatus(httpResponse.code());
        response.setHeaders(httpResponse.headers().toMultimap());
        return response;
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
        Results response = new Results();
        HttpResponse httpResponse = postToResponse(url, headers, paramMap);
        response.setBody(httpResponse.body().toString());
        response.setStatus(httpResponse.code());
        response.setHeaders(httpResponse.headers().toMultimap());
        return response;
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
        Results response = new Results();
        HttpResponse httpResponse = postToResponse(url, headers, data);
        response.setBody(httpResponse.body().toString());
        response.setStatus(httpResponse.code());
        response.setHeaders(httpResponse.headers().toMultimap());
        return response;
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
        Results response = new Results();
        HttpResponse httpResponse = patchToResponse(url, headers, data);
        response.setBody(httpResponse.body().toString());
        response.setStatus(httpResponse.code());
        response.setHeaders(httpResponse.headers().toMultimap());
        return response;
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
        Results response = new Results();
        HttpResponse httpResponse = deleteToResponse(url, headers, data);
        response.setBody(httpResponse.body().toString());
        response.setStatus(httpResponse.code());
        response.setHeaders(httpResponse.headers().toMultimap());
        return response;
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
        Results response = new Results();
        HttpResponse httpResponse = putToResponse(url, headers, data);
        response.setBody(httpResponse.body().toString());
        response.setStatus(httpResponse.code());
        response.setHeaders(httpResponse.headers().toMultimap());
        return response;
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
                            .create()
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
                            .create()
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
                            .create()
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
     * @return {@link HttpResponse} 请求返回的结果
     */
    private static HttpResponse getToResponse(String url, Map<String, Object> paramMap, Map<String, String> headers) {
        try {
            return Httpz.get()
                    .url(url)
                    .addHeaders(headers)
                    .addParams(paramMap)
                    .build()
                    .execute();
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * post 请求
     *
     * @param url     请求url
     * @param headers 请求头
     * @param data    请求参数
     * @return {@link HttpResponse} 请求返回的结果
     */
    private static HttpResponse postToResponse(String url, Map<String, String> headers, String data) {
        try {
            return Httpz.post()
                    .url(url)
                    .addHeaders(headers)
                    .body(data)
                    .build()
                    .execute();
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * post 请求
     *
     * @param url      请求url
     * @param headers  请求头
     * @param paramMap 请求参数
     * @return {@link HttpResponse} 请求返回的结果
     */
    private static HttpResponse postToResponse(String url, Map<String, String> headers, Map<String, Object> paramMap) {
        try {
            return Httpz.post()
                    .url(url)
                    .addHeaders(headers)
                    .addParams(paramMap)
                    .build()
                    .execute();
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * patch 请求
     *
     * @param url     请求url
     * @param headers 请求头
     * @param data    请求参数
     * @return {@link HttpResponse} 请求返回的结果
     */
    private static HttpResponse patchToResponse(String url, Map<String, String> headers, String data) {
      /*  return HttpRequest.patch(url)
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
     * @return {@link HttpResponse} 请求返回的结果
     */
    private static HttpResponse deleteToResponse(String url, Map<String, String> headers, String data) {
     /*   return HttpRequest.delete(url)
                .addHeaders(headers)
                .body(data)
                .execute();*/

        return null;
    }

    /**
     * put 请求
     *
     * @param url     请求url
     * @param headers 请求头
     * @param data    请求参数
     * @return {@link HttpResponse} 请求返回的结果
     */
    private static HttpResponse putToResponse(String url, Map<String, String> headers, String data) {
        try {
            return Httpz.put()
                    .url(url)
                    .addHeaders(headers)
                    .body(data)
                    .build()
                    .execute();
        } catch (Exception e) {
            throw new InstrumentException(e);
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
        Results response = new Results();
        HttpResponse httpResponse = patchToResponse(url, headers, paramMap);
        response.setBody(httpResponse.body().toString());
        response.setStatus(httpResponse.code());
        response.setHeaders(httpResponse.headers().toMultimap());
        return response;
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
        Results response = new Results();
        HttpResponse httpResponse = deleteToResponse(url, headers, paramMap);
        response.setBody(httpResponse.body().toString());
        response.setStatus(httpResponse.code());
        response.setHeaders(httpResponse.headers().toMultimap());
        return response;
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
        Results response = new Results();
        HttpResponse httpResponse = putToResponse(url, headers, paramMap);
        response.setBody(httpResponse.body().toString());
        response.setStatus(httpResponse.code());
        response.setHeaders(httpResponse.headers().toMultimap());
        return response;
    }

    /**
     * patch 请求
     *
     * @param url      请求url
     * @param headers  请求头
     * @param paramMap 请求参数
     * @return {@link HttpResponse} 请求返回的结果
     */
    private HttpResponse patchToResponse(String url, Map<String, String> headers, Map<String, Object> paramMap) {
        return (HttpResponse) Httpv.builder().build().sync(url).addBodyPara(paramMap).addHeader(headers).patch();
    }

    /**
     * delete 请求
     *
     * @param url      请求url
     * @param headers  请求头
     * @param paramMap 请求参数
     * @return {@link HttpResponse} 请求返回的结果
     */
    private HttpResponse deleteToResponse(String url, Map<String, String> headers, Map<String, Object> paramMap) {
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
     * @return {@link HttpResponse} 请求返回的结果
     */
    private HttpResponse putToResponse(String url, Map<String, String> headers, Map<String, Object> paramMap) {
        try {
            return Httpz.put()
                    .url(url)
                    .addHeaders(headers)
                    .addParams(paramMap)
                    .build()
                    .execute();
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

}
