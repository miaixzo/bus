/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.pay.metric;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLSocketFactory;

import lombok.SneakyThrows;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.PaymentException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.SSLContextBuilder;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.bodys.RequestBody;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.pay.Checker;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Provider;
import org.miaixz.bus.pay.cache.PayCache;
import org.miaixz.bus.pay.magic.ErrorCode;
import org.miaixz.bus.pay.magic.Message;
import org.miaixz.bus.pay.magic.Voucher;

/**
 * Abstract provider for handling payment requests.
 *
 * @param <T> The type of the global object, extending {@link Voucher}.
 * @param <K> The type of the context object, extending {@link Context}.
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractProvider<T extends Voucher, K extends Context> implements Provider<T> {

    /**
     * The context object containing configuration and other necessary information.
     */
    protected K context;

    /**
     * The API address support object.
     */
    protected Complex complex;

    /**
     * The cache support object.
     */
    protected CacheX cache;

    /**
     * Constructs a new AbstractProvider.
     *
     * @param context The global information.
     */
    public AbstractProvider(K context) {
        this(context, null);
    }

    /**
     * Constructs a new AbstractProvider.
     *
     * @param context The global information.
     * @param complex The API address.
     */
    public AbstractProvider(K context, Complex complex) {
        this(context, complex, PayCache.INSTANCE);
    }

    /**
     * Constructs a new AbstractProvider.
     *
     * @param context The global information.
     * @param complex The API address.
     * @param cache   The cache support.
     */
    public AbstractProvider(K context, Complex complex, CacheX cache) {
        Assert.notNull(context, "[context] is null");
        Logger.debug(
                true,
                "Pay",
                "Payment provider initialization started: provider={}, complexType={}, sandbox={}",
                getClass().getSimpleName(),
                complex == null ? null : complex.getClass().getName(),
                complex != null && complex.isSandbox());
        this.context = context;
        this.complex = complex;
        this.cache = ObjectKit.isEmpty(cache) ? PayCache.INSTANCE : cache;
        if (!Checker.isSupportedPay(this.context, complex)) {
            Logger.warn(
                    false,
                    "Pay",
                    "Payment provider initialization rejected: provider={}, reason={}",
                    getClass().getSimpleName(),
                    "unsupportedConfiguration");
            throw new PaymentException(ErrorCode._PARAMETER_INCOMPLETE);
        }
        // Verify the legitimacy of the configuration
        Checker.checkConfig(this.context, complex);
        Logger.debug(
                false,
                "Pay",
                "Payment provider initialized: provider={}, complexType={}, cacheType={}, certMode={}, appIdPresent={}, merchantPresent={}",
                getClass().getSimpleName(),
                complex == null ? null : complex.getClass().getName(),
                this.cache == null ? null : this.cache.getClass().getName(),
                this.context.isCertMode(),
                StringKit.isNotEmpty(this.context.getAppId()),
                StringKit.isNotEmpty(this.context.getMchId()));
    }

    /**
     * Gets all scopes marked as default from the {@link PayScope} array.
     *
     * @param scopes The array of scopes.
     * @return A list of default scopes.
     */
    public static List<String> getDefaultScopes(PayScope[] scopes) {
        if (null == scopes || scopes.length == 0) {
            return null;
        }
        return Arrays.stream(scopes).filter((PayScope::isDefault)).map(PayScope::getScope).collect(Collectors.toList());
    }

    /**
     * Gets the actual scope strings from the {@link PayScope} array.
     *
     * @param scopes Variable arguments, supporting any {@link PayScope}.
     * @return A list of scope strings.
     */
    public static List<String> getScopes(PayScope... scopes) {
        if (null == scopes || scopes.length == 0) {
            return null;
        }
        return Arrays.stream(scopes).map(PayScope::getScope).collect(Collectors.toList());
    }

    /**
     * Performs a GET request.
     *
     * @param url     The request URL.
     * @param formMap The request parameters.
     * @return The result of the request.
     */
    public static String get(String url, Map<String, String> formMap) {
        return Httpx.get(url, formMap);
    }

    /**
     * Performs a POST request.
     *
     * @param url  The request URL.
     * @param data The request data.
     * @return The result of the request.
     */
    public static String post(String url, String data) {
        Logger.info(
                true,
                "Pay",
                "Payment HTTP request started: method=POST, url={}, dataBytes={}",
                url == null ? null : url.replaceFirst("\\?.*$", ""),
                data == null ? 0 : data.getBytes(Charset.UTF_8).length);
        try {
            Response response = Httpz.post().url(url).body(data).build().execute();
            String body = response.body().string();
            Logger.info(
                    false,
                    "Pay",
                    "Payment HTTP request completed: method=POST, url={}, status={}, responseBytes={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    response.code(),
                    body == null ? 0 : body.getBytes(Charset.UTF_8).length);
            return body;
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Pay",
                    e,
                    "Payment HTTP request failed: method=POST, url={}, exception={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs a POST request.
     *
     * @param url       The request URL.
     * @param data      The request data.
     * @param headerMap The request headers.
     * @return The result of the request as a {@link Message}.
     */
    public static Message post(String url, String data, Map<String, String> headerMap) {
        try {
            Response response = postTo(url, headerMap, data);
            String body = response.body().string();
            Logger.info(
                    false,
                    "Pay",
                    "Payment HTTP response assembled: method=POST, url={}, status={}, headerCount={}, responseBytes={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    response.code(),
                    response.headers() == null ? 0 : response.headers().size(),
                    body == null ? 0 : body.getBytes(Charset.UTF_8).length);
            return Message.builder().body(body).status(response.code()).headers(response.headers().toMultimap())
                    .build();
        } catch (IOException e) {
            Logger.error(
                    false,
                    "Pay",
                    e,
                    "Payment HTTP response read failed: method=POST, url={}, exception={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs a GET request.
     *
     * @param url       The request URL.
     * @param formMap   The request parameters.
     * @param headerMap The request headers.
     * @return The result of the request as a {@link Message}.
     */
    public static Message get(String url, Map<String, String> formMap, Map<String, String> headerMap) {
        try {
            Response response = getTo(url, formMap, headerMap);
            String body = response.body().string();
            Logger.info(
                    false,
                    "Pay",
                    "Payment HTTP response assembled: method=GET, url={}, status={}, headerCount={}, responseBytes={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    response.code(),
                    response.headers() == null ? 0 : response.headers().size(),
                    body == null ? 0 : body.getBytes(Charset.UTF_8).length);
            return Message.builder().body(body).status(response.code()).headers(response.headers().toMultimap())
                    .build();
        } catch (IOException e) {
            Logger.error(
                    false,
                    "Pay",
                    e,
                    "Payment HTTP response read failed: method=GET, url={}, exception={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs a POST request.
     *
     * @param url       The request URL.
     * @param formMap   The request parameters.
     * @param headerMap The request headers.
     * @return The result of the request as a {@link Message}.
     */
    public static Message post(String url, Map<String, String> formMap, Map<String, String> headerMap) {
        try {
            Response response = postTo(url, headerMap, formMap);
            String body = response.body().string();
            Logger.info(
                    false,
                    "Pay",
                    "Payment HTTP response assembled: method=POST, url={}, status={}, headerCount={}, responseBytes={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    response.code(),
                    response.headers() == null ? 0 : response.headers().size(),
                    body == null ? 0 : body.getBytes(Charset.UTF_8).length);
            return Message.builder().body(body).status(response.code()).headers(response.headers().toMultimap())
                    .build();
        } catch (IOException e) {
            Logger.error(
                    false,
                    "Pay",
                    e,
                    "Payment HTTP response read failed: method=POST, url={}, exception={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs a POST request with a file.
     *
     * @param url       The request URL.
     * @param formMap   The request parameters.
     * @param headerMap The request headers.
     * @param file      The file to be uploaded.
     * @return The result of the request as a {@link Message}.
     */
    public static Message post(String url, Map<String, String> formMap, Map<String, String> headerMap, File file) {
        try {
            Logger.info(
                    true,
                    "Pay",
                    "Payment file request started: method=POST, url={}, paramCount={}, file={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    formMap == null ? 0 : formMap.size(),
                    file == null ? null : file.getName());
            Response response = postTo(url, headerMap, formMap);
            String body = response.body().string();
            Logger.info(
                    false,
                    "Pay",
                    "Payment file request completed: method=POST, url={}, status={}, file={}, responseBytes={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    response.code(),
                    file == null ? null : file.getName(),
                    body == null ? 0 : body.getBytes(Charset.UTF_8).length);
            return Message.builder().body(body).status(response.code()).headers(response.headers().toMultimap())
                    .build();
        } catch (IOException e) {
            Logger.error(
                    false,
                    "Pay",
                    e,
                    "Payment file request failed: method=POST, url={}, file={}, exception={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    file == null ? null : file.getName(),
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs a POST request with SSL certificate.
     *
     * @param url      The request URL.
     * @param data     The request data.
     * @param certPath The certificate path.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public static String post(String url, String data, String certPath, String certPass, String protocol) {
        Logger.info(
                true,
                "Pay",
                "Payment SSL request started: method=POST, url={}, certSource={}, protocol={}, dataBytes={}",
                url == null ? null : url.replaceFirst("\\?.*$", ""),
                certPath == null ? "stream" : "path",
                protocol,
                data == null ? 0 : data.getBytes(Charset.UTF_8).length);
        try {
            if (StringKit.isEmpty(protocol)) {
                protocol = Protocol.TLSv1.name;
            }
            Httpd httpd = new Httpd().newBuilder()
                    .sslSocketFactory(getSslSocketFactory(certPath, null, certPass, protocol)).build();
            final Request request = new Request.Builder().url(url)
                    .post(RequestBody.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE, data)).build();
            NewCall call = httpd.newCall(request);
            Response response = call.execute();
            String body = response.body().string();
            Logger.info(
                    false,
                    "Pay",
                    "Payment SSL request completed: method=POST, url={}, status={}, protocol={}, responseBytes={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    response.code(),
                    protocol,
                    body == null ? 0 : body.getBytes(Charset.UTF_8).length);
            return body;
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Pay",
                    e,
                    "Payment SSL request failed: method=POST, url={}, certSource={}, protocol={}, exception={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    certPath == null ? "stream" : "path",
                    protocol,
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs a POST request with SSL certificate.
     *
     * @param url      The request URL.
     * @param data     The request data.
     * @param certFile The certificate file input stream.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public static String post(String url, String data, InputStream certFile, String certPass, String protocol) {
        Logger.info(
                true,
                "Pay",
                "Payment SSL request started: method=POST, url={}, certSource={}, protocol={}, dataBytes={}",
                url == null ? null : url.replaceFirst("\\?.*$", ""),
                "stream",
                protocol,
                data == null ? 0 : data.getBytes(Charset.UTF_8).length);
        try {
            if (StringKit.isEmpty(Protocol.TLSv1.name)) {
                protocol = Protocol.TLSv1.name;
            }
            Httpd httpd = new Httpd().newBuilder()
                    .sslSocketFactory(getSslSocketFactory(null, certFile, certPass, protocol)).build();
            final Request request = new Request.Builder().url(url)
                    .post(RequestBody.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE, data)).build();
            NewCall call = httpd.newCall(request);
            Response response = call.execute();
            String body = response.body().string();
            Logger.info(
                    false,
                    "Pay",
                    "Payment SSL request completed: method=POST, url={}, status={}, protocol={}, responseBytes={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    response.code(),
                    protocol,
                    body == null ? 0 : body.getBytes(Charset.UTF_8).length);
            return body;
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Pay",
                    e,
                    "Payment SSL request failed: method=POST, url={}, certSource={}, protocol={}, exception={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    "stream",
                    protocol,
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs a PUT request.
     *
     * @param url       The request URL.
     * @param data      The request data.
     * @param headerMap The request headers.
     * @return The result of the request as a {@link Message}.
     */
    public static Message put(String url, String data, Map<String, String> headerMap) {
        try {
            Response response = putTo(url, headerMap, data);
            String body = response.body().string();
            Logger.info(
                    false,
                    "Pay",
                    "Payment HTTP response assembled: method=PUT, url={}, status={}, headerCount={}, responseBytes={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    response.code(),
                    response.headers() == null ? 0 : response.headers().size(),
                    body == null ? 0 : body.getBytes(Charset.UTF_8).length);
            return Message.builder().body(body).status(response.code()).headers(response.headers().toMultimap())
                    .build();
        } catch (IOException e) {
            Logger.error(
                    false,
                    "Pay",
                    e,
                    "Payment HTTP response read failed: method=PUT, url={}, exception={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Uploads a file.
     *
     * @param url      The request URL.
     * @param data     The request data.
     * @param certPath The certificate path.
     * @param certPass The certificate password.
     * @param filePath The path of the file to be uploaded.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public static String upload(
            String url,
            String data,
            String certPath,
            String certPass,
            String filePath,
            String protocol) {

        Logger.info(
                true,
                "Pay",
                "Payment upload request started: method=POST, url={}, certSource={}, file={}, protocol={}, dataBytes={}",
                url == null ? null : url.replaceFirst("\\?.*$", ""),
                certPath == null ? "stream" : "path",
                filePath == null ? null : Paths.get(filePath).getFileName(),
                protocol,
                data == null ? 0 : data.getBytes(Charset.UTF_8).length);
        SSLSocketFactory sslSocketFactory = getSslSocketFactory(certPath, null, certPass, protocol);

        try {
            Response response = Httpz.post().url(url).addFile(null, null, FileKit.newFile(filePath))
                    .addHeader(HTTP.CONTENT_TYPE, "multipart/form-data;boundary=\"boundary\"").build().execute();
            String body = response.body().string();
            Logger.info(
                    false,
                    "Pay",
                    "Payment upload request completed: method=POST, url={}, status={}, file={}, responseBytes={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    response.code(),
                    filePath == null ? null : Paths.get(filePath).getFileName(),
                    body == null ? 0 : body.getBytes(Charset.UTF_8).length);
            return body;
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Pay",
                    e,
                    "Payment upload request failed: method=POST, url={}, file={}, exception={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    filePath == null ? null : Paths.get(filePath).getFileName(),
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Uploads a file.
     *
     * @param url      The request URL.
     * @param data     The request data.
     * @param certPath The certificate path.
     * @param certPass The certificate password.
     * @param filePath The path of the file to be uploaded.
     * @return The result of the request.
     */
    public static String upload(String url, String data, String certPath, String certPass, String filePath) {
        return upload(url, data, certPath, certPass, filePath, Protocol.TLSv1.name);
    }

    /**
     * Performs a GET request.
     *
     * @param url       The request URL.
     * @param formMap   The request parameters.
     * @param headerMap The request headers.
     * @return The {@link Response} object.
     */
    private static Response getTo(String url, Map<String, String> formMap, Map<String, String> headerMap) {
        Logger.info(
                true,
                "Pay",
                "Payment HTTP request started: method=GET, url={}, paramCount={}, headerCount={}",
                url == null ? null : url.replaceFirst("\\?.*$", ""),
                formMap == null ? 0 : formMap.size(),
                headerMap == null ? 0 : headerMap.size());
        try {
            Response response = Httpz.get().url(url).addHeader(headerMap).addParam(formMap).build().execute();
            Logger.info(
                    false,
                    "Pay",
                    "Payment HTTP request completed: method=GET, url={}, status={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    response.code());
            return response;
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Pay",
                    e,
                    "Payment HTTP request failed: method=GET, url={}, exception={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs a POST request.
     *
     * @param url       The request URL.
     * @param headerMap The request headers.
     * @param data      The request data.
     * @return The {@link Response} object.
     */
    private static Response postTo(String url, Map<String, String> headerMap, String data) {
        Logger.info(
                true,
                "Pay",
                "Payment HTTP request started: method=POST, url={}, headerCount={}, dataBytes={}",
                url == null ? null : url.replaceFirst("\\?.*$", ""),
                headerMap == null ? 0 : headerMap.size(),
                data == null ? 0 : data.getBytes(Charset.UTF_8).length);
        try {
            Response response = Httpz.post().url(url).addHeader(headerMap).body(data).build().execute();
            Logger.info(
                    false,
                    "Pay",
                    "Payment HTTP request completed: method=POST, url={}, status={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    response.code());
            return response;
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Pay",
                    e,
                    "Payment HTTP request failed: method=POST, url={}, exception={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs a POST request.
     *
     * @param url       The request URL.
     * @param headerMap The request headers.
     * @param formMap   The request parameters.
     * @return The {@link Response} object.
     */
    private static Response postTo(String url, Map<String, String> headerMap, Map<String, String> formMap) {
        Logger.info(
                true,
                "Pay",
                "Payment HTTP request started: method=POST, url={}, paramCount={}, headerCount={}",
                url == null ? null : url.replaceFirst("\\?.*$", ""),
                formMap == null ? 0 : formMap.size(),
                headerMap == null ? 0 : headerMap.size());
        try {
            Response response = Httpz.post().url(url).addHeader(headerMap).addParam(formMap).build().execute();
            Logger.info(
                    false,
                    "Pay",
                    "Payment HTTP request completed: method=POST, url={}, status={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    response.code());
            return response;
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Pay",
                    e,
                    "Payment HTTP request failed: method=POST, url={}, exception={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs a PUT request.
     *
     * @param url       The request URL.
     * @param headerMap The request headers.
     * @param data      The request data.
     * @return The {@link Response} object.
     */
    private static Response putTo(String url, Map<String, String> headerMap, String data) {
        Logger.info(
                true,
                "Pay",
                "Payment HTTP request started: method=PUT, url={}, headerCount={}, dataBytes={}",
                url == null ? null : url.replaceFirst("\\?.*$", ""),
                headerMap == null ? 0 : headerMap.size(),
                data == null ? 0 : data.getBytes(Charset.UTF_8).length);
        try {
            Response response = Httpz.put().url(url).addHeader(headerMap).body(data).build().execute();
            Logger.info(
                    false,
                    "Pay",
                    "Payment HTTP request completed: method=PUT, url={}, status={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    response.code());
            return response;
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Pay",
                    e,
                    "Payment HTTP request failed: method=PUT, url={}, exception={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the KeyManager.
     *
     * @param certPass The certificate password.
     * @param certPath The certificate path.
     * @param certFile The certificate file input stream.
     * @return An array of KeyManagers.
     */
    @SneakyThrows
    private static KeyManager[] getKeyManager(String certPass, String certPath, InputStream certFile) {
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        if (certFile != null) {
            clientStore.load(certFile, certPass.toCharArray());
        } else {
            clientStore.load(Files.newInputStream(Paths.get(certPath)), certPass.toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(clientStore, certPass.toCharArray());
        return kmf.getKeyManagers();
    }

    /**
     * Gets the SSLSocketFactory.
     *
     * @param certPath The certificate path.
     * @param certFile The certificate file input stream.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The SSLSocketFactory.
     */
    @SneakyThrows
    private static SSLSocketFactory getSslSocketFactory(
            String certPath,
            InputStream certFile,
            String certPass,
            String protocol) {
        SSLContextBuilder sslContextBuilder = SSLContextBuilder.of();
        sslContextBuilder.setProtocol(protocol);
        sslContextBuilder.setKeyManagers(getKeyManager(certPass, certPath, certFile));
        sslContextBuilder.setSecureRandom(new SecureRandom());
        return sslContextBuilder.buildChecked().getSocketFactory();
    }

    /**
     * Handles exceptions and returns a unified response.
     *
     * @param e The specific exception.
     * @return A {@link Message} object representing the error.
     */
    protected Message responseError(Exception e) {
        String errorCode = ErrorCode._FAILURE.getKey();
        String errorMsg = e.getMessage();
        if (e instanceof PaymentException) {
            PaymentException authException = ((PaymentException) e);
            errorCode = authException.getErrcode();
            if (StringKit.isNotEmpty(authException.getErrmsg())) {
                errorMsg = authException.getErrmsg();
            }
        }
        Logger.warn(
                false,
                "Pay",
                e,
                "Payment response error assembled: provider={}, errorCode={}, exception={}",
                getClass().getSimpleName(),
                errorCode,
                e == null ? null : e.getClass().getSimpleName());
        return Message.builder().errcode(errorCode).errmsg(errorMsg).build();
    }

    /**
     * Gets the scope information separated by a separator.
     *
     * @param separator     The separator between multiple scopes.
     * @param encode        Whether to encode the scope.
     * @param defaultScopes The default scopes to use when the client has not configured any.
     * @return The formatted scope string.
     */
    protected String getScopes(String separator, boolean encode, List<String> defaultScopes) {
        List<String> scopes = context.getScopes();
        if (null == scopes || scopes.isEmpty()) {
            if (null == defaultScopes || defaultScopes.isEmpty()) {
                return Normal.EMPTY;
            }
            scopes = defaultScopes;
        }
        if (null == separator) {
            // Default to space
            separator = Symbol.SPACE;
        }
        String scope = String.join(separator, scopes);
        return encode ? UrlEncoder.encodeAll(scope) : scope;
    }

    /**
     * Performs a GET request.
     *
     * @param url The request URL.
     * @return The result of the request.
     */
    public String get(String url) {
        return Httpx.get(url);
    }

    /**
     * Performs a POST request.
     *
     * @param url     The request URL.
     * @param formMap The request parameters.
     * @return The result of the request.
     */
    public String post(String url, Map<String, String> formMap) {
        return Httpx.post(url, formMap);
    }

    /**
     * Performs a PUT request.
     *
     * @param url       The request URL.
     * @param formMap   The request parameters.
     * @param headerMap The request headers.
     * @return The result of the request as a {@link Message}.
     */
    public Message put(String url, Map<String, Object> formMap, Map<String, String> headerMap) {
        try {
            Response response = putTo(url, headerMap, formMap);
            String body = response.body().string();
            Logger.info(
                    false,
                    "Pay",
                    "Payment HTTP response assembled: method=PUT, url={}, status={}, headerCount={}, responseBytes={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    response.code(),
                    response.headers() == null ? 0 : response.headers().size(),
                    body == null ? 0 : body.getBytes(Charset.UTF_8).length);
            return Message.builder().body(body).status(response.code()).headers(response.headers().toMultimap())
                    .build();
        } catch (IOException e) {
            Logger.error(
                    false,
                    "Pay",
                    e,
                    "Payment HTTP response read failed: method=PUT, url={}, exception={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs a PUT request.
     *
     * @param url       The request URL.
     * @param headerMap The request headers.
     * @param formMap   The request parameters.
     * @return The {@link Response} object.
     */
    private Response putTo(String url, Map<String, String> headerMap, Map<String, Object> formMap) {
        Logger.info(
                true,
                "Pay",
                "Payment HTTP request started: method=PUT, url={}, paramCount={}, headerCount={}",
                url == null ? null : url.replaceFirst("\\?.*$", ""),
                formMap == null ? 0 : formMap.size(),
                headerMap == null ? 0 : headerMap.size());
        try {
            Response response = Httpz.put().url(url).addHeader(headerMap).addParam(formMap).build().execute();
            Logger.info(
                    false,
                    "Pay",
                    "Payment HTTP request completed: method=PUT, url={}, status={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    response.code());
            return response;
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Pay",
                    e,
                    "Payment HTTP request failed: method=PUT, url={}, exception={}",
                    url == null ? null : url.replaceFirst("\\?.*$", ""),
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

}
