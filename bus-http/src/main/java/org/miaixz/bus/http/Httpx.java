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
package org.miaixz.bus.http;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.tls.SSLContextBuilder;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.accord.ConnectionPool;
import org.miaixz.bus.http.bodys.FormBody;
import org.miaixz.bus.http.bodys.MultipartBody;
import org.miaixz.bus.http.bodys.RequestBody;
import org.miaixz.bus.http.metric.Dispatcher;
import org.miaixz.bus.http.plugin.httpx.HttpProxy;
import org.miaixz.bus.logger.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A utility class for sending HTTP requests with a simplified API. This class provides static methods for common
 * request types like GET, POST, PUT, DELETE, and HEAD. It uses a shared, lazily-initialized {@link Httpd} instance for
 * all requests.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Httpx {

    /**
     * The lazily-initialized, thread-safe {@link Httpd} instance.
     */
    private static Httpd httpd;

    static {
        new Httpx(SSLContextBuilder.newTrustManager());
    }

    /**
     * Default constructor that initializes the client with default timeouts.
     */
    public Httpx() {
        this(30, 30, 30);
    }

    /**
     * Constructor that initializes the client with a custom trust manager.
     *
     * @param x509TrustManager The trust manager for SSL connections.
     */
    public Httpx(X509TrustManager x509TrustManager) {
        this(null, null, 30, 30, 30, 64, 5, 5, 5, SSLContextBuilder.newSslSocketFactory(x509TrustManager),
                x509TrustManager, (hostname, session) -> true);
    }

    /**
     * Constructor that initializes the client with custom timeouts.
     *
     * @param connTimeout  The connection timeout in seconds.
     * @param readTimeout  The read timeout in seconds.
     * @param writeTimeout The write timeout in seconds.
     */
    public Httpx(int connTimeout, int readTimeout, int writeTimeout) {
        this(null, null, connTimeout, readTimeout, writeTimeout, Normal._64, 5, 5, 5);
    }

    /**
     * Constructor that initializes the client with detailed custom settings.
     *
     * @param connTimeout        The connection timeout in seconds.
     * @param readTimeout        The read timeout in seconds.
     * @param writeTimeout       The write timeout in seconds.
     * @param maxRequests        The maximum number of parallel requests.
     * @param maxRequestsPerHost The maximum number of parallel requests per host.
     * @param maxIdleConnections The maximum number of idle connections.
     * @param keepAliveDuration  The keep-alive duration for idle connections in minutes.
     */
    public Httpx(int connTimeout, int readTimeout, int writeTimeout, int maxRequests, int maxRequestsPerHost,
            int maxIdleConnections, int keepAliveDuration) {
        this(null, null, connTimeout, readTimeout, writeTimeout, maxRequests, maxRequestsPerHost, maxIdleConnections,
                keepAliveDuration);
    }

    /**
     * Constructor that initializes the client with DNS and proxy settings.
     *
     * @param dns                The custom DNS resolver.
     * @param httpProxy          The HTTP proxy configuration.
     * @param connTimeout        The connection timeout in seconds.
     * @param readTimeout        The read timeout in seconds.
     * @param writeTimeout       The write timeout in seconds.
     * @param maxRequests        The maximum number of parallel requests.
     * @param maxRequestsPerHost The maximum number of parallel requests per host.
     * @param maxIdleConnections The maximum number of idle connections.
     * @param keepAliveDuration  The keep-alive duration for idle connections in minutes.
     */
    public Httpx(DnsX dns, HttpProxy httpProxy, int connTimeout, int readTimeout, int writeTimeout, int maxRequests,
            int maxRequestsPerHost, int maxIdleConnections, int keepAliveDuration) {
        this(dns, httpProxy, connTimeout, readTimeout, writeTimeout, maxRequests, maxRequestsPerHost,
                maxIdleConnections, keepAliveDuration, null, null, null);
    }

    /**
     * The main constructor that initializes the underlying {@link Httpd} client with all possible configurations.
     *
     * @param dns                The custom DNS resolver.
     * @param httpProxy          The HTTP proxy configuration.
     * @param connTimeout        The connection timeout in seconds.
     * @param readTimeout        The read timeout in seconds.
     * @param writeTimeout       The write timeout in seconds.
     * @param maxRequests        The maximum number of parallel requests.
     * @param maxRequestsPerHost The maximum number of parallel requests per host.
     * @param maxIdleConnections The maximum number of idle connections.
     * @param keepAliveDuration  The keep-alive duration for idle connections in minutes.
     * @param sslSocketFactory   The SSL socket factory for HTTPS connections.
     * @param x509TrustManager   The trust manager for SSL connections.
     * @param hostnameVerifier   The hostname verifier for HTTPS connections.
     */
    public Httpx(final DnsX dns, final HttpProxy httpProxy, int connTimeout, int readTimeout, int writeTimeout,
            int maxRequests, int maxRequestsPerHost, int maxIdleConnections, int keepAliveDuration,
            SSLSocketFactory sslSocketFactory, javax.net.ssl.X509TrustManager x509TrustManager,
            HostnameVerifier hostnameVerifier) {
        synchronized (Httpx.class) {
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMaxRequests(maxRequests);
            dispatcher.setMaxRequestsPerHost(maxRequestsPerHost);
            ConnectionPool connectPool = new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.MINUTES);
            Httpd.Builder builder = new Httpd.Builder();

            builder.dispatcher(dispatcher);
            builder.connectionPool(connectPool);
            builder.addNetworkInterceptor(chain -> {
                Request request = chain.request();
                return chain.proceed(request);
            });
            if (ObjectKit.isNotEmpty(dns)) {
                builder.dns(hostname -> {
                    try {
                        return dns.lookup(hostname);
                    } catch (Exception e) {
                        Logger.error("DNS lookup failed: {}", e.getMessage());
                    }
                    return DnsX.SYSTEM.lookup(hostname);
                });
            }
            if (ObjectKit.isNotEmpty(httpProxy)) {
                builder.proxy(httpProxy.proxy());
                if (null != httpProxy.user && null != httpProxy.password) {
                    builder.proxyAuthenticator(httpProxy.authenticator());
                }
            }
            builder.connectTimeout(connTimeout, TimeUnit.SECONDS);
            builder.readTimeout(readTimeout, TimeUnit.SECONDS);
            builder.writeTimeout(writeTimeout, TimeUnit.SECONDS);
            if (ObjectKit.isNotEmpty(sslSocketFactory)) {
                builder.sslSocketFactory(sslSocketFactory, x509TrustManager);
            }
            if (ObjectKit.isNotEmpty(hostnameVerifier)) {
                builder.hostnameVerifier(hostnameVerifier);
            }
            httpd = builder.build();
        }
    }

    /**
     * Sends a simple GET request with the default UTF-8 encoding.
     *
     * @param url The URL to send the request to.
     * @return The response body as a {@link String}.
     */
    public static String get(final String url) {
        return get(url, Charset.DEFAULT_UTF_8);
    }

    /**
     * Sends a simple GET request with a custom encoding.
     *
     * @param url     The URL to send the request to.
     * @param charset The custom character set for the response.
     * @return The response body as a {@link String}.
     */
    public static String get(final String url, final String charset) {
        return execute(Builder.builder().url(url).requestCharset(charset).responseCharset(charset).build());
    }

    /**
     * Sends a GET request, either synchronously or asynchronously.
     *
     * @param url     The URL to send the request to.
     * @param isAsync If {@code true}, the request is sent asynchronously.
     * @return The response body as a {@link String} (for synchronous requests) or an empty string (for asynchronous
     *         requests).
     */
    public static String get(final String url, final boolean isAsync) {
        if (isAsync) {
            return enqueue(Builder.builder().url(url).method(HTTP.GET).build());
        }
        return get(url);
    }

    /**
     * Sends a GET request with query parameters and default UTF-8 encoding.
     *
     * @param url     The URL to send the request to.
     * @param formMap A map of query parameters.
     * @return The response body as a {@link String}.
     */
    public static String get(final String url, final Map<String, String> formMap) {
        return get(url, formMap, null, Charset.DEFAULT_UTF_8);
    }

    /**
     * Sends an asynchronous GET request with a callback.
     *
     * @param url      The URL to send the request to.
     * @param callback The callback to handle the response or failure.
     */
    public static void get(String url, Callback callback) {
        Request request = new Request.Builder().url(url).get().build();
        NewCall call = httpd.newCall(request);
        call.enqueue(callback);
    }

    /**
     * Sends a GET request with query and header parameters and default UTF-8 encoding.
     *
     * @param url       The URL to send the request to.
     * @param formMap   A map of query parameters.
     * @param headerMap A map of header parameters.
     * @return The response body as a {@link String}.
     */
    public static String get(final String url, final Map<String, String> formMap, Map<String, String> headerMap) {
        return get(url, formMap, headerMap, Charset.DEFAULT_UTF_8);
    }

    /**
     * Sends a GET request with query parameters, header parameters, and a custom encoding.
     *
     * @param url       The URL to send the request to.
     * @param formMap   A map of query parameters.
     * @param headerMap A map of header parameters.
     * @param charset   The custom character set for the response.
     * @return The response body as a {@link String}.
     */
    public static String get(
            final String url,
            final Map<String, String> formMap,
            Map<String, String> headerMap,
            final String charset) {
        return execute(
                Builder.builder().url(url).headerMap(headerMap).formMap(formMap).requestCharset(charset)
                        .responseCharset(charset).build());
    }

    /**
     * Sends an asynchronous POST request with form data and a callback.
     *
     * @param url      The URL to send the request to.
     * @param formMap  A map of form data.
     * @param callback The callback to handle the response or failure.
     */
    public static void post(String url, Map<String, String> formMap, Callback callback) {
        StringBuilder data = new StringBuilder();
        if (MapKit.isNotEmpty(formMap)) {
            Set<String> keys = formMap.keySet();
            for (String key : keys) {
                data.append(key).append(Symbol.EQUAL).append(formMap.get(key)).append(Symbol.AND);
            }
        }
        RequestBody requestBody = RequestBody.of(MediaType.TEXT_HTML_TYPE, data.toString());
        Request request = new Request.Builder().url(url).post(requestBody).build();
        NewCall call = httpd.newCall(request);
        call.enqueue(callback);
    }

    /**
     * Sends a simple POST request with an empty body.
     *
     * @param url The URL to send the request to.
     * @return The response body as a {@link String}.
     */
    public static String post(final String url) {
        return post(url, null);
    }

    /**
     * Sends a POST request with form data using {@code application/x-www-form-urlencoded} content type.
     *
     * @param url     The URL to send the request to.
     * @param formMap A map of form data.
     * @return The response body as a {@link String}.
     */
    public static String post(final String url, final Map<String, String> formMap) {
        String data = Normal.EMPTY;
        if (MapKit.isNotEmpty(formMap)) {
            data = formMap.entrySet().stream().map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining(Symbol.AND));
        }
        return post(url, data, MediaType.APPLICATION_FORM_URLENCODED);
    }

    /**
     * Sends a POST request with a raw data body and a specified content type.
     *
     * @param url         The URL to send the request to.
     * @param data        The raw request body data.
     * @param contentType The content type of the request body.
     * @return The response body as a {@link String}.
     */
    public static String post(final String url, final String data, final String contentType) {
        return post(url, data, contentType, Charset.DEFAULT_UTF_8);
    }

    /**
     * Sends a POST request with form data and a specified content type.
     *
     * @param url         The URL to send the request to.
     * @param formMap     A map of form data.
     * @param contentType The content type of the request body.
     * @return The response body as a {@link String}.
     */
    public static String post(final String url, final Map<String, String> formMap, final String contentType) {
        return post(url, formMap, contentType, Charset.DEFAULT_UTF_8);
    }

    /**
     * Sends a POST request with form data and header parameters.
     *
     * @param url       The URL to send the request to.
     * @param formMap   A map of form data.
     * @param headerMap A map of header parameters.
     * @return The response body as a {@link String}.
     */
    public static String post(
            final String url,
            final Map<String, String> formMap,
            final Map<String, String> headerMap) {
        return post(url, formMap, headerMap, MediaType.APPLICATION_FORM_URLENCODED);
    }

    /**
     * Sends a POST request with a raw data body, a specified content type, and a custom encoding.
     *
     * @param url         The URL to send the request to.
     * @param data        The raw request body data.
     * @param contentType The content type of the request body.
     * @param charset     The custom character set for the request and response.
     * @return The response body as a {@link String}.
     */
    public static String post(final String url, final String data, final String contentType, final String charset) {
        return execute(
                Builder.builder().url(url).method(HTTP.POST).data(data).contentType(contentType).requestCharset(charset)
                        .responseCharset(charset).build());
    }

    /**
     * Sends a POST request with a raw data body, header parameters, and a specified content type.
     *
     * @param url         The URL to send the request to.
     * @param data        The raw request body data.
     * @param headerMap   A map of header parameters.
     * @param contentType The content type of the request body.
     * @return The response body as a {@link String}.
     */
    public static String post(
            final String url,
            final String data,
            final Map<String, String> headerMap,
            final String contentType) {
        return execute(
                Builder.builder().url(url).method(HTTP.POST).data(data).headerMap(headerMap).contentType(contentType)
                        .requestCharset(Charset.DEFAULT_UTF_8).responseCharset(Charset.DEFAULT_UTF_8).build());
    }

    /**
     * Sends a POST request with form data, a specified content type, and a custom encoding.
     *
     * @param url         The URL to send the request to.
     * @param formMap     A map of form data.
     * @param contentType The content type of the request body.
     * @param charset     The custom character set for the request and response.
     * @return The response body as a {@link String}.
     */
    public static String post(
            final String url,
            final Map<String, String> formMap,
            final String contentType,
            final String charset) {
        return execute(
                Builder.builder().url(url).method(HTTP.POST).formMap(formMap).contentType(contentType)
                        .requestCharset(charset).responseCharset(charset).build());
    }

    /**
     * Sends a POST request with form data, header parameters, and a specified content type.
     *
     * @param url         The URL to send the request to.
     * @param formMap     A map of form data.
     * @param headerMap   A map of header parameters.
     * @param contentType The content type of the request body.
     * @return The response body as a {@link String}.
     */
    public static String post(
            final String url,
            final Map<String, String> formMap,
            final Map<String, String> headerMap,
            final String contentType) {
        return post(url, formMap, headerMap, contentType, Charset.DEFAULT_UTF_8);
    }

    /**
     * Sends a POST request with form data, header parameters, a specified content type, and a custom encoding.
     *
     * @param url         The URL to send the request to.
     * @param formMap     A map of form data.
     * @param headerMap   A map of header parameters.
     * @param contentType The content type of the request body.
     * @param charset     The custom character set for the request and response.
     * @return The response body as a {@link String}.
     */
    public static String post(
            final String url,
            final Map<String, String> formMap,
            final Map<String, String> headerMap,
            final String contentType,
            final String charset) {
        return execute(
                Builder.builder().url(url).method(HTTP.POST).headerMap(headerMap).formMap(formMap)
                        .contentType(contentType).requestCharset(charset).responseCharset(charset).build());
    }

    /**
     * Sends a simple PUT request with a null body and default UTF-8 encoding.
     *
     * @param url The URL to send the request to.
     * @return The response body as a {@link String}.
     */
    public static String put(final String url) {
        return put(url, (String) null, Charset.DEFAULT_UTF_8);
    }

    /**
     * Sends a PUT request with form data using {@code application/x-www-form-urlencoded} content type.
     *
     * @param url     The URL to send the request to.
     * @param formMap A map of form data.
     * @return The response body as a {@link String}.
     */
    public static String put(final String url, final Map<String, String> formMap) {
        return put(url, formMap, MediaType.APPLICATION_FORM_URLENCODED, Charset.DEFAULT_UTF_8);
    }

    /**
     * Sends a PUT request with a raw data body and a specified content type.
     *
     * @param url         The URL to send the request to.
     * @param data        The raw request body data.
     * @param contentType The content type of the request body.
     * @return The response body as a {@link String}.
     */
    public static String put(final String url, final String data, final String contentType) {
        return put(url, data, contentType, Charset.DEFAULT_UTF_8);
    }

    /**
     * Sends a PUT request with form data and a specified content type.
     *
     * @param url         The URL to send the request to.
     * @param formMap     A map of form data.
     * @param contentType The content type of the request body.
     * @return The response body as a {@link String}.
     */
    public static String put(final String url, final Map<String, String> formMap, final String contentType) {
        return put(url, formMap, contentType, Charset.DEFAULT_UTF_8);
    }

    /**
     * Sends a PUT request with form data and header parameters.
     *
     * @param url       The URL to send the request to.
     * @param formMap   A map of form data.
     * @param headerMap A map of header parameters.
     * @return The response body as a {@link String}.
     */
    public static String put(final String url, final Map<String, String> formMap, final Map<String, String> headerMap) {
        return put(url, formMap, headerMap, MediaType.APPLICATION_FORM_URLENCODED);
    }

    /**
     * Sends a PUT request with a raw data body, a specified content type, and a custom encoding.
     *
     * @param url         The URL to send the request to.
     * @param data        The raw request body data.
     * @param contentType The content type of the request body.
     * @param charset     The custom character set for the request and response.
     * @return The response body as a {@link String}.
     */
    public static String put(final String url, final String data, final String contentType, final String charset) {
        return execute(
                Builder.builder().url(url).method(HTTP.PUT).data(data).contentType(contentType).requestCharset(charset)
                        .responseCharset(charset).build());
    }

    /**
     * Sends a PUT request with a raw data body, header parameters, and a specified content type.
     *
     * @param url         The URL to send the request to.
     * @param data        The raw request body data.
     * @param headerMap   A map of header parameters.
     * @param contentType The content type of the request body.
     * @return The response body as a {@link String}.
     */
    public static String put(
            final String url,
            final String data,
            final Map<String, String> headerMap,
            final String contentType) {
        return execute(
                Builder.builder().url(url).method(HTTP.PUT).data(data).headerMap(headerMap).contentType(contentType)
                        .requestCharset(Charset.DEFAULT_UTF_8).responseCharset(Charset.DEFAULT_UTF_8).build());
    }

    /**
     * Sends a PUT request with form data, a specified content type, and a custom encoding.
     *
     * @param url         The URL to send the request to.
     * @param formMap     A map of form data.
     * @param contentType The content type of the request body.
     * @param charset     The custom character set for the request and response.
     * @return The response body as a {@link String}.
     */
    public static String put(
            final String url,
            final Map<String, String> formMap,
            final String contentType,
            final String charset) {
        return execute(
                Builder.builder().url(url).method(HTTP.PUT).formMap(formMap).contentType(contentType)
                        .requestCharset(charset).responseCharset(charset).build());
    }

    /**
     * Sends a PUT request with form data, header parameters, and a specified content type.
     *
     * @param url         The URL to send the request to.
     * @param formMap     A map of form data.
     * @param headerMap   A map of header parameters.
     * @param contentType The content type of the request body.
     * @return The response body as a {@link String}.
     */
    public static String put(
            final String url,
            final Map<String, String> formMap,
            final Map<String, String> headerMap,
            final String contentType) {
        return put(url, formMap, headerMap, contentType, Charset.DEFAULT_UTF_8);
    }

    /**
     * Sends a PUT request with form data, header parameters, a specified content type, and a custom encoding.
     *
     * @param url         The URL to send the request to.
     * @param formMap     A map of form data.
     * @param headerMap   A map of header parameters.
     * @param contentType The content type of the request body.
     * @param charset     The custom character set for the request and response.
     * @return The response body as a {@link String}.
     */
    public static String put(
            final String url,
            final Map<String, String> formMap,
            final Map<String, String> headerMap,
            final String contentType,
            final String charset) {
        return execute(
                Builder.builder().url(url).method(HTTP.PUT).headerMap(headerMap).formMap(formMap)
                        .contentType(contentType).requestCharset(charset).responseCharset(charset).build());
    }

    /**
     * Sends a simple DELETE request with default UTF-8 encoding.
     *
     * @param url The URL to send the request to.
     * @return The response body as a {@link String}.
     */
    public static String delete(final String url) {
        return delete(url, null, Charset.DEFAULT_UTF_8);
    }

    /**
     * Sends a DELETE request with form data and default UTF-8 encoding.
     *
     * @param url     The URL to send the request to.
     * @param formMap A map of form data.
     * @return The response body as a {@link String}.
     */
    public static String delete(final String url, final Map<String, String> formMap) {
        return delete(url, formMap, Charset.DEFAULT_UTF_8);
    }

    /**
     * Sends a DELETE request with form data and a custom encoding.
     *
     * @param url     The URL to send the request to.
     * @param formMap A map of form data.
     * @param charset The custom character set for the request and response.
     * @return The response body as a {@link String}.
     */
    public static String delete(final String url, final Map<String, String> formMap, final String charset) {
        return execute(
                Builder.builder().url(url).method(HTTP.DELETE).formMap(formMap).requestCharset(charset)
                        .responseCharset(charset).build());
    }

    /**
     * Sends a DELETE request with form data and header parameters.
     *
     * @param url       The URL to send the request to.
     * @param formMap   A map of form data.
     * @param headerMap A map of header parameters.
     * @return The response body as a {@link String}.
     */
    public static String delete(
            final String url,
            final Map<String, String> formMap,
            final Map<String, String> headerMap) {
        return delete(url, formMap, headerMap, Charset.DEFAULT_UTF_8);
    }

    /**
     * Sends a DELETE request with form data, header parameters, and a custom encoding.
     *
     * @param url       The URL to send the request to.
     * @param formMap   A map of form data.
     * @param headerMap A map of header parameters.
     * @param charset   The custom character set for the request and response.
     * @return The response body as a {@link String}.
     */
    public static String delete(
            final String url,
            final Map<String, String> formMap,
            final Map<String, String> headerMap,
            final String charset) {
        return execute(
                Builder.builder().url(url).method(HTTP.DELETE).headerMap(headerMap).formMap(formMap)
                        .requestCharset(charset).responseCharset(charset).build());
    }

    /**
     * Sends a simple HEAD request with default UTF-8 encoding.
     *
     * @param url The URL to send the request to.
     * @return The response headers as a formatted string.
     */
    public static String head(final String url) {
        return head(url, Charset.DEFAULT_UTF_8);
    }

    /**
     * Sends a HEAD request with a custom encoding.
     *
     * @param url     The URL to send the request to.
     * @param charset The custom character set.
     * @return The response headers as a formatted string.
     */
    public static String head(final String url, final String charset) {
        return head(url, null, charset);
    }

    /**
     * Sends a HEAD request with header parameters and a custom encoding.
     *
     * @param url       The URL to send the request to.
     * @param headerMap A map of header parameters.
     * @param charset   The custom character set.
     * @return The response headers as a formatted string.
     */
    public static String head(final String url, final Map<String, String> headerMap, final String charset) {
        StringBuilder result = new StringBuilder();
        try {
            Request request = builder(
                    Builder.builder().url(url).method(HTTP.HEAD).headerMap(headerMap).requestCharset(charset)
                            .responseCharset(charset).build()).url(url).build();
            Response response = httpd.newCall(request).execute();
            if (response.headers() != null) {
                response.headers().toMultimap().forEach(
                        (key, values) -> result.append(key).append(": ").append(String.join(", ", values))
                                .append("\n"));
            }
            response.close();
        } catch (Exception e) {
            Logger.error("HEAD request failed for URL [{}]: {}", url, e.getMessage());
        }
        return result.toString();
    }

    /**
     * Sends a POST request with form data and file uploads.
     *
     * @param url     The URL to send the request to.
     * @param formMap A map of form data.
     * @param list    A list of file paths to upload.
     * @return The response body as a {@link String}.
     */
    public static String post(final String url, final Map<String, String> formMap, final List<String> list) {
        MediaType contentType = MediaType
                .valueOf(MediaType.APPLICATION_FORM_URLENCODED + Symbol.SEMICOLON + Charset.DEFAULT_UTF_8);
        RequestBody bodyParams = RequestBody
                .of(contentType, MapKit.isNotEmpty(formMap) ? formMap.toString() : Normal.EMPTY);
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MediaType.MULTIPART_FORM_DATA_TYPE).addFormDataPart("params", Normal.EMPTY, bodyParams);

        for (String path : list) {
            File file = new File(path);
            if (file.exists()) {
                requestBodyBuilder.addFormDataPart("file", file.getName(), RequestBody.of(contentType, file));
            } else {
                Logger.warn("File not found: {}", path);
            }
        }
        RequestBody requestBody = requestBodyBuilder.build();
        Request request = new Request.Builder().url(url).post(requestBody).build();
        String result = Normal.EMPTY;
        try {
            Response response = httpd.newCall(request).execute();
            if (response.body() != null) {
                byte[] bytes = response.body().bytes();
                result = new String(bytes, Charset.DEFAULT_UTF_8);
            }
            response.close();
        } catch (Exception e) {
            Logger.error("Requesting HTTP Error for URL [{}]: {}", url, e.getMessage());
        }
        return result;
    }

    /**
     * A private helper method to build a {@link Request.Builder} from the internal {@link Builder}.
     *
     * @param builder The internal builder with request details.
     * @return A {@link Request.Builder} configured with the provided details.
     */
    private static Request.Builder builder(final Builder builder) {
        if (StringKit.isBlank(builder.requestCharset)) {
            builder.requestCharset = Charset.DEFAULT_UTF_8;
        }
        if (StringKit.isBlank(builder.responseCharset)) {
            builder.responseCharset = Charset.DEFAULT_UTF_8;
        }
        if (StringKit.isBlank(builder.method)) {
            builder.method = HTTP.GET;
        }
        if (StringKit.isBlank(builder.contentType)) {
            builder.contentType = MediaType.APPLICATION_FORM_URLENCODED;
        }
        if (builder.tracer) {
            Logger.info("Request Builder: {}", builder);
        }

        Request.Builder request = new Request.Builder();

        if (MapKit.isNotEmpty(builder.headerMap)) {
            builder.headerMap.forEach(request::addHeader);
        }
        String method = builder.method.toUpperCase();
        String contentType = String.format("%s;charset=%s", builder.contentType, builder.requestCharset);
        if (StringKit.equals(method, HTTP.GET)) {
            if (MapKit.isNotEmpty(builder.formMap)) {
                String form = builder.formMap.entrySet().stream()
                        .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining(Symbol.AND));
                builder.url = String.format(
                        "%s%s%s",
                        builder.url,
                        builder.url.contains(Symbol.QUESTION_MARK) ? Symbol.AND : Symbol.QUESTION_MARK,
                        form);
            }
            request.get();
        } else if (StringKit.equals(method, HTTP.HEAD)) {
            request.head();
        } else if (ArrayKit.contains(new String[] { HTTP.POST, HTTP.PUT, HTTP.DELETE, HTTP.PATCH }, method)) {
            RequestBody requestBody = null;
            if (StringKit.isNotEmpty(builder.data)) {
                requestBody = RequestBody.of(MediaType.valueOf(contentType), builder.data);
            } else if (MapKit.isNotEmpty(builder.formMap)) {
                FormBody.Builder form = new FormBody.Builder(java.nio.charset.Charset.forName(Charset.DEFAULT_UTF_8));
                builder.formMap.forEach((key, value) -> form.add(key, StringKit.toString(value)));
                requestBody = form.build();
            }
            request.method(method, requestBody);
        } else {
            throw new InternalException("Unsupported request method: " + method);
        }
        return request;
    }

    /**
     * A private helper method for executing a synchronous request.
     *
     * @param builder The internal builder with request details.
     * @return The response body as a {@link String}.
     */
    private static String execute(final Builder builder) {
        String result = Normal.EMPTY;
        try {
            Request request = builder(builder).url(builder.url).build();
            Response response = httpd.newCall(request).execute();
            if (response.body() != null) {
                byte[] bytes = response.body().bytes();
                result = new String(bytes, builder.responseCharset);
            }
            if (builder.tracer) {
                Logger.info("Response for URL [{}]: {}", builder.url, result);
            }
            response.close();
        } catch (Exception e) {
            Logger.error("Request failed for URL [{}]: {}", builder.url, e.getMessage());
        }
        return result;
    }

    /**
     * A private helper method for executing an asynchronous request.
     *
     * @param builder The internal builder with request details.
     * @return An empty string, as the result is handled by the callback.
     */
    private static String enqueue(final Builder builder) {
        Request request = builder(builder).url(builder.url).build();
        NewCall call = httpd.newCall(request);
        String[] result = { Normal.EMPTY };
        call.enqueue(new Callback() {

            /**
             * Called when the request fails.
             * <p>
             * This method is invoked when the asynchronous HTTP request encounters an error.
             * </p>
             *
             * @param call The {@link NewCall} that failed.
             * @param e    The {@link IOException} that describes the failure.
             */
            @Override
            public void onFailure(NewCall call, IOException e) {
                Logger.error("Async request failed for URL [{}]: {}", builder.url, e.getMessage());
            }

            /**
             * Called when the response is received successfully.
             * <p>
             * This method is invoked when the asynchronous HTTP request completes successfully. The response body is
             * processed and stored in the result array.
             * </p>
             *
             * @param call     The {@link NewCall} that succeeded.
             * @param response The {@link Response} received from the server.
             * @throws IOException if an I/O error occurs while processing the response.
             */
            @Override
            public void onResponse(NewCall call, Response response) throws IOException {
                try {
                    if (response.body() != null) {
                        byte[] bytes = response.body().bytes();
                        result[0] = new String(bytes, builder.responseCharset);
                        if (builder.tracer) {
                            Logger.info("Async response for URL [{}]: {}", builder.url, result[0]);
                        }
                    }
                } finally {
                    response.close();
                }
            }
        });
        return result[0];
    }

    /**
     * An internal builder class for configuring HTTP requests in a fluent way.
     */
    @lombok.Builder
    @lombok.ToString
    private static class Builder {

        /**
         * The request URL.
         */
        private String url;
        /**
         * The HTTP method (e.g., GET, POST).
         */
        private String method;
        /**
         * The raw request body data.
         */
        private String data;
        /**
         * The content type of the request body.
         */
        private String contentType;
        /**
         * A map of form data parameters.
         */
        private Map<String, String> formMap;
        /**
         * A map of header parameters.
         */
        private Map<String, String> headerMap;

        /**
         * The character set for the request body.
         */
        private String requestCharset;
        /**
         * The character set for the response body.
         */
        private String responseCharset;
        /**
         * Whether to enable logging for this request.
         */
        private boolean tracer;
    }

}
