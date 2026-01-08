/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.PaymentException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.SSLContextBuilder;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.bodys.RequestBody;
import org.miaixz.bus.pay.Checker;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Provider;
import org.miaixz.bus.pay.cache.PayCache;
import org.miaixz.bus.pay.magic.ErrorCode;
import org.miaixz.bus.pay.magic.Voucher;
import org.miaixz.bus.pay.magic.Message;

import lombok.SneakyThrows;

/**
 * Abstract provider for handling payment requests.
 *
 * @param <T> The type of the global object, extending {@link Voucher}.
 * @param <K> The type of the context object, extending {@link Context}.
 * @author Kimi Liu
 * @since Java 17+
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
        this.context = context;
        this.complex = complex;
        this.cache = ObjectKit.isEmpty(cache) ? PayCache.INSTANCE : cache;
        if (!Checker.isSupportedPay(this.context, complex)) {
            throw new PaymentException(ErrorCode._PARAMETER_INCOMPLETE);
        }
        // Verify the legitimacy of the configuration
        Checker.checkConfig(this.context, complex);
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
        try {
            return Httpz.post().url(url).body(data).build().execute().body().string();
        } catch (Exception e) {
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
            return Message.builder().body(response.body().string()).status(response.code())
                    .headers(response.headers().toMultimap()).build();
        } catch (IOException e) {
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
            return Message.builder().body(response.body().string()).status(response.code())
                    .headers(response.headers().toMultimap()).build();
        } catch (IOException e) {
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
            return Message.builder().body(response.body().string()).status(response.code())
                    .headers(response.headers().toMultimap()).build();
        } catch (IOException e) {
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
            Response response = postTo(url, headerMap, formMap);
            return Message.builder().body(response.body().string()).status(response.code())
                    .headers(response.headers().toMultimap()).build();
        } catch (IOException e) {
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
        try {
            if (StringKit.isEmpty(protocol)) {
                protocol = Protocol.TLSv1.name;
            }
            Httpd httpd = new Httpd().newBuilder()
                    .sslSocketFactory(getSslSocketFactory(certPath, null, certPass, protocol)).build();
            final Request request = new Request.Builder().url(url)
                    .post(RequestBody.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE, data)).build();
            NewCall call = httpd.newCall(request);
            return call.execute().body().string();
        } catch (Exception e) {
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
        try {
            if (StringKit.isEmpty(Protocol.TLSv1.name)) {
                protocol = Protocol.TLSv1.name;
            }
            Httpd httpd = new Httpd().newBuilder()
                    .sslSocketFactory(getSslSocketFactory(null, certFile, certPass, protocol)).build();
            final Request request = new Request.Builder().url(url)
                    .post(RequestBody.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE, data)).build();
            NewCall call = httpd.newCall(request);
            return call.execute().body().string();
        } catch (Exception e) {
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
            return Message.builder().body(response.body().string()).status(response.code())
                    .headers(response.headers().toMultimap()).build();
        } catch (IOException e) {
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

        SSLSocketFactory sslSocketFactory = getSslSocketFactory(certPath, null, certPass, protocol);

        try {
            return Httpz.post().url(url).addFile(null, null, FileKit.newFile(filePath))
                    .addHeader("Content-Type", "multipart/form-data;boundary=\"boundary\"").build().execute().body()
                    .string();
        } catch (Exception e) {
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
        try {
            return Httpz.get().url(url).addHeader(headerMap).addParam(formMap).build().execute();
        } catch (Exception e) {
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
        try {
            return Httpz.post().url(url).addHeader(headerMap).body(data).build().execute();
        } catch (Exception e) {
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
        try {
            return Httpz.post().url(url).addHeader(headerMap).addParam(formMap).build().execute();
        } catch (Exception e) {
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
        try {
            return Httpz.put().url(url).addHeader(headerMap).body(data).build().execute();
        } catch (Exception e) {
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
            return Message.builder().body(response.body().string()).status(response.code())
                    .headers(response.headers().toMultimap()).build();
        } catch (IOException e) {
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
        try {
            return Httpz.put().url(url).addHeader(headerMap).addParam(formMap).build().execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
