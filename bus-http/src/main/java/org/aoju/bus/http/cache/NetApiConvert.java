/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.http.cache;

import org.aoju.bus.core.io.BufferSource;
import org.aoju.bus.core.io.Sink;
import org.aoju.bus.core.lang.Header;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.http.Builder;
import org.aoju.bus.http.Headers;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.Response;
import org.aoju.bus.http.accord.platform.Platform;
import org.aoju.bus.http.bodys.RequestBody;
import org.aoju.bus.http.bodys.ResponseBody;
import org.aoju.bus.http.metric.Handshake;
import org.aoju.bus.http.metric.http.DelegatingConnection;
import org.aoju.bus.http.metric.http.HttpHeaders;
import org.aoju.bus.http.metric.http.HttpMethod;
import org.aoju.bus.http.metric.http.StatusLine;
import org.aoju.bus.http.secure.CipherSuite;
import org.aoju.bus.http.secure.TlsVersion;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 在Java和Httpd表示之间进行转换的方法
 *
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8+
 */
public final class NetApiConvert {
    /**
     * 合成响应标头:请求发送时的本地时间
     */
    private static final String SENT_MILLIS = Platform.get().getPrefix() + "-Sent-Millis";

    /**
     * 合成响应标头:接收到响应的本地时间
     */
    private static final String RECEIVED_MILLIS = Platform.get().getPrefix() + "-Received-Millis";

    private NetApiConvert() {
    }

    public static Response createResponseForCachePut(URI uri, URLConnection urlConnection)
            throws IOException {

        HttpURLConnection httpUrlConnection = (HttpURLConnection) urlConnection;

        Response.Builder responseBuilder = new Response.Builder();

        // 请求:从URL连接创建一个
        Headers responseHeaders = createHeaders(urlConnection.getHeaderFields());
        // 不同的缓存需要一些请求头
        Headers varyHeaders = varyHeaders(urlConnection, responseHeaders);
        if (varyHeaders == null) {
            return null;
        }

        // 调用API需要一个占位符主体;真实的身体将被分开
        String requestMethod = httpUrlConnection.getRequestMethod();
        RequestBody placeholderBody = HttpMethod.requiresRequestBody(requestMethod)
                ? RequestBody.create(null, Normal.EMPTY_BYTE_ARRAY)
                : null;

        Request request = new Request.Builder()
                .url(uri.toString())
                .method(requestMethod, placeholderBody)
                .headers(varyHeaders)
                .build();
        responseBuilder.request(request);

        StatusLine statusLine = StatusLine.parse(extractStatusLine(httpUrlConnection));
        responseBuilder.protocol(statusLine.protocol);
        responseBuilder.code(statusLine.code);
        responseBuilder.message(statusLine.message);

        Response networkResponse = responseBuilder.build();
        responseBuilder.networkResponse(networkResponse);

        Headers headers = extractResponseHeaders(httpUrlConnection, responseBuilder);
        responseBuilder.headers(headers);

        ResponseBody body = createBody(urlConnection);
        responseBuilder.body(body);

        if (httpUrlConnection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) httpUrlConnection;

            Certificate[] peerCertificates;
            try {
                peerCertificates = httpsUrlConnection.getServerCertificates();
            } catch (SSLPeerUnverifiedException e) {
                peerCertificates = null;
            }

            Certificate[] localCertificates = httpsUrlConnection.getLocalCertificates();

            String cipherSuiteString = httpsUrlConnection.getCipherSuite();
            CipherSuite cipherSuite = CipherSuite.forJavaName(cipherSuiteString);
            Handshake handshake = Handshake.get(TlsVersion.SSL_3_0, cipherSuite,
                    nullSafeImmutableList(peerCertificates), nullSafeImmutableList(localCertificates));
            responseBuilder.handshake(handshake);
        }

        return responseBuilder.build();
    }

    private static Headers createHeaders(Map<String, List<String>> headers) {
        Headers.Builder builder = new Headers.Builder();
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            if (header.getKey() == null || header.getValue() == null) {
                continue;
            }
            String name = header.getKey().trim();
            for (String value : header.getValue()) {
                String trimmedValue = value.trim();
                Builder.instance.addLenient(builder, name, trimmedValue);
            }
        }
        return builder.build();
    }

    private static Headers varyHeaders(URLConnection urlConnection, Headers responseHeaders) {
        if (HttpHeaders.hasVaryAll(responseHeaders)) {
            return null;
        }
        Set<String> varyFields = HttpHeaders.varyFields(responseHeaders);
        if (varyFields.isEmpty()) {
            return new Headers.Builder().build();
        }
        if (!(urlConnection instanceof CacheHttpURLConnection
                || urlConnection instanceof CacheHttpsURLConnection)) {
            return null;
        }

        Map<String, List<String>> requestProperties = urlConnection.getRequestProperties();
        Headers.Builder result = new Headers.Builder();
        for (String fieldName : varyFields) {
            List<String> fieldValues = requestProperties.get(fieldName);
            if (fieldValues == null) {
                if (fieldName.equals("Accept-Encoding")) {
                    result.add("Accept-Encoding", "gzip");
                }
            } else {
                for (String fieldValue : fieldValues) {
                    Builder.instance.addLenient(result, fieldName, fieldValue);
                }
            }
        }
        return result.build();
    }

    static Response createResponseForCacheGet(Request request, CacheResponse javaResponse)
            throws IOException {

        Headers responseHeaders = createHeaders(javaResponse.getHeaders());
        Headers varyHeaders;
        if (HttpHeaders.hasVaryAll(responseHeaders)) {
            varyHeaders = new Headers.Builder().build();
        } else {
            varyHeaders = HttpHeaders.varyHeaders(request.headers(), responseHeaders);
        }

        Request cacheRequest = new Request.Builder()
                .url(request.url())
                .method(request.method(), null)
                .headers(varyHeaders)
                .build();

        Response.Builder responseBuilder = new Response.Builder();

        responseBuilder.request(cacheRequest);

        StatusLine statusLine = StatusLine.parse(extractStatusLine(javaResponse));
        responseBuilder.protocol(statusLine.protocol);
        responseBuilder.code(statusLine.code);
        responseBuilder.message(statusLine.message);

        Headers headers = extractHeaders(javaResponse, responseBuilder);
        responseBuilder.headers(headers);

        ResponseBody body = createBody(headers, javaResponse);
        responseBuilder.body(body);

        if (javaResponse instanceof SecureCacheResponse) {
            SecureCacheResponse javaSecureCacheResponse = (SecureCacheResponse) javaResponse;

            List<Certificate> peerCertificates;
            try {
                peerCertificates = javaSecureCacheResponse.getServerCertificateChain();
            } catch (SSLPeerUnverifiedException e) {
                peerCertificates = Collections.emptyList();
            }
            List<Certificate> localCertificates = javaSecureCacheResponse.getLocalCertificateChain();
            if (localCertificates == null) {
                localCertificates = Collections.emptyList();
            }

            String cipherSuiteString = javaSecureCacheResponse.getCipherSuite();
            CipherSuite cipherSuite = CipherSuite.forJavaName(cipherSuiteString);
            Handshake handshake = Handshake.get(
                    TlsVersion.SSL_3_0, cipherSuite, peerCertificates, localCertificates);
            responseBuilder.handshake(handshake);
        }
        return responseBuilder.build();
    }

    public static Request createRequest(
            URI uri, String requestMethod, Map<String, List<String>> requestHeaders) {
        RequestBody placeholderBody = HttpMethod.requiresRequestBody(requestMethod)
                ? RequestBody.create(null, Normal.EMPTY_BYTE_ARRAY)
                : null;

        Request.Builder builder = new Request.Builder()
                .url(uri.toString())
                .method(requestMethod, placeholderBody);

        if (requestHeaders != null) {
            Headers headers = extractHeaders(requestHeaders, null);
            builder.headers(headers);
        }
        return builder.build();
    }

    public static CacheResponse createJavaCacheResponse(final Response response) {
        final Headers headers = withSyntheticHeaders(response);
        final ResponseBody body = response.body();
        if (response.request().isHttps()) {
            final Handshake handshake = response.handshake();
            return new SecureCacheResponse() {
                @Override
                public String getCipherSuite() {
                    return handshake != null ? handshake.cipherSuite().javaName() : null;
                }

                @Override
                public List<Certificate> getLocalCertificateChain() {
                    if (handshake == null) return null;
                    List<Certificate> certificates = handshake.localCertificates();
                    return certificates.size() > 0 ? certificates : null;
                }

                @Override
                public List<Certificate> getServerCertificateChain() {
                    if (handshake == null) return null;
                    List<Certificate> certificates = handshake.peerCertificates();
                    return certificates.size() > 0 ? certificates : null;
                }

                @Override
                public Principal getPeerPrincipal() {
                    if (handshake == null) return null;
                    return handshake.peerPrincipal();
                }

                @Override
                public Principal getLocalPrincipal() {
                    if (handshake == null) return null;
                    return handshake.localPrincipal();
                }

                @Override
                public Map<String, List<String>> getHeaders() {
                    return org.aoju.bus.http.metric.http.HttpURLConnection.toMultimap(headers, StatusLine.get(response).toString());
                }

                @Override
                public InputStream getBody() {
                    if (body == null) return null;
                    return body.byteStream();
                }
            };
        } else {
            return new CacheResponse() {
                @Override
                public Map<String, List<String>> getHeaders() {
                    return org.aoju.bus.http.metric.http.HttpURLConnection.toMultimap(headers, StatusLine.get(response).toString());
                }

                @Override
                public InputStream getBody() {
                    if (body == null) return null;
                    return body.byteStream();
                }
            };
        }
    }

    public static java.net.CacheRequest createJavaCacheRequest(final CacheRequest cacheRequest) {
        return new java.net.CacheRequest() {
            @Override
            public void abort() {
                cacheRequest.abort();
            }

            @Override
            public OutputStream getBody() throws IOException {
                Sink body = cacheRequest.body();
                if (body == null) {
                    return null;
                }
                return IoKit.buffer(body).outputStream();
            }
        };
    }

    static HttpURLConnection createJavaUrlConnectionForCachePut(Response response) {
        response = response.newBuilder()
                .body(null)
                .headers(withSyntheticHeaders(response))
                .build();
        Request request = response.request();
        if (request.isHttps()) {
            return new CacheHttpsURLConnection(new CacheHttpURLConnection(response));
        } else {
            return new CacheHttpURLConnection(response);
        }
    }

    private static Headers withSyntheticHeaders(Response response) {
        return response.headers().newBuilder()
                .add(SENT_MILLIS, Long.toString(response.sentRequestAtMillis()))
                .add(RECEIVED_MILLIS, Long.toString(response.receivedResponseAtMillis()))
                .build();
    }

    static Map<String, List<String>> extractJavaHeaders(Request request) {
        return org.aoju.bus.http.metric.http.HttpURLConnection.toMultimap(request.headers(), null);
    }

    private static Headers extractHeaders(
            CacheResponse javaResponse, Response.Builder responseBuilder) throws IOException {
        Map<String, List<String>> javaResponseHeaders = javaResponse.getHeaders();
        return extractHeaders(javaResponseHeaders, responseBuilder);
    }

    private static Headers extractResponseHeaders(
            HttpURLConnection httpUrlConnection, Response.Builder responseBuilder) {
        Map<String, List<String>> javaResponseHeaders = httpUrlConnection.getHeaderFields();
        return extractHeaders(javaResponseHeaders, responseBuilder);
    }

    static Headers extractHeaders(
            Map<String, List<String>> javaHeaders, Response.Builder responseBuilder) {
        Headers.Builder headersBuilder = new Headers.Builder();
        for (Map.Entry<String, List<String>> javaHeader : javaHeaders.entrySet()) {
            String name = javaHeader.getKey();
            if (name == null) {
                continue;
            }
            if (responseBuilder != null && javaHeader.getValue().size() == 1) {
                if (name.equals(SENT_MILLIS)) {
                    responseBuilder.sentRequestAtMillis(Long.valueOf(javaHeader.getValue().get(0)));
                    continue;
                }
                if (name.equals(RECEIVED_MILLIS)) {
                    responseBuilder.receivedResponseAtMillis(Long.valueOf(javaHeader.getValue().get(0)));
                    continue;
                }
            }
            for (String value : javaHeader.getValue()) {
                Builder.instance.addLenient(headersBuilder, name, value);
            }
        }
        return headersBuilder.build();
    }

    private static String extractStatusLine(HttpURLConnection httpUrlConnection) {
        return httpUrlConnection.getHeaderField(null);
    }

    private static String extractStatusLine(CacheResponse javaResponse) throws IOException {
        Map<String, List<String>> javaResponseHeaders = javaResponse.getHeaders();
        return extractStatusLine(javaResponseHeaders);
    }

    static String extractStatusLine(Map<String, List<String>> javaResponseHeaders)
            throws ProtocolException {
        List<String> values = javaResponseHeaders.get(null);
        if (values == null || values.size() == 0) {
            throw new ProtocolException(
                    "CacheResponse is missing a null header containing the status line. Headers="
                            + javaResponseHeaders);
        }
        return values.get(0);
    }

    private static ResponseBody createBody(final Headers headers,
                                           final CacheResponse cacheResponse) throws IOException {
        final BufferSource body = IoKit.buffer(IoKit.source(cacheResponse.getBody()));
        return new ResponseBody() {
            @Override
            public MediaType contentType() {
                String contentTypeHeader = headers.get(Header.CONTENT_TYPE);
                return contentTypeHeader == null ? null : MediaType.valueOf(contentTypeHeader);
            }

            @Override
            public long contentLength() {
                return HttpHeaders.contentLength(headers);
            }

            @Override
            public BufferSource source() {
                return body;
            }
        };
    }

    private static ResponseBody createBody(final URLConnection urlConnection) throws IOException {
        if (!urlConnection.getDoInput()) {
            return null;
        }

        final BufferSource body = IoKit.buffer(IoKit.source(urlConnection.getInputStream()));
        return new ResponseBody() {
            @Override
            public MediaType contentType() {
                String contentTypeHeader = urlConnection.getContentType();
                return contentTypeHeader == null ? null : MediaType.valueOf(contentTypeHeader);
            }

            @Override
            public long contentLength() {
                String s = urlConnection.getHeaderField("Content-Length");
                return stringToLong(s);
            }

            @Override
            public BufferSource source() {
                return body;
            }
        };
    }

    private static RuntimeException throwRequestModificationException() {
        throw new UnsupportedOperationException("ResponseCache cannot modify the request.");
    }

    private static RuntimeException throwRequestSslAccessException() {
        throw new UnsupportedOperationException("ResponseCache cannot access SSL internals");
    }

    private static RuntimeException throwResponseBodyAccessException() {
        throw new UnsupportedOperationException("ResponseCache cannot access the response body.");
    }

    private static <T> List<T> nullSafeImmutableList(T[] elements) {
        return elements == null ? Collections.emptyList() : Builder.immutableList(elements);
    }

    private static long stringToLong(String s) {
        if (s == null) return -1;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static final class CacheHttpURLConnection extends HttpURLConnection {

        private final Request request;
        private final Response response;

        CacheHttpURLConnection(Response response) {
            super(response.request().url().url());
            this.request = response.request();
            this.response = response;

            this.connected = true;
            this.doOutput = request.body() != null;
            this.doInput = true;
            this.useCaches = true;

            this.method = request.method();
        }

        @Override
        public void connect() {
            throw throwRequestModificationException();
        }

        @Override
        public void disconnect() {
            throw throwRequestModificationException();
        }

        @Override
        public void setRequestProperty(String key, String value) {
            throw throwRequestModificationException();
        }

        @Override
        public void addRequestProperty(String key, String value) {
            throw throwRequestModificationException();
        }

        @Override
        public String getRequestProperty(String key) {
            return request.header(key);
        }

        @Override
        public Map<String, List<String>> getRequestProperties() {
            return org.aoju.bus.http.metric.http.HttpURLConnection.toMultimap(request.headers(), null);
        }

        @Override
        public void setFixedLengthStreamingMode(int contentLength) {
            throw throwRequestModificationException();
        }

        @Override
        public void setFixedLengthStreamingMode(long contentLength) {
            throw throwRequestModificationException();
        }

        @Override
        public void setChunkedStreamingMode(int chunklen) {
            throw throwRequestModificationException();
        }

        @Override
        public boolean getInstanceFollowRedirects() {
            return super.getInstanceFollowRedirects();
        }

        @Override
        public void setInstanceFollowRedirects(boolean followRedirects) {
            throw throwRequestModificationException();
        }

        @Override
        public String getRequestMethod() {
            return request.method();
        }

        @Override
        public void setRequestMethod(String method) {
            throw throwRequestModificationException();
        }

        @Override
        public String getHeaderFieldKey(int position) {
            if (position < 0) {
                throw new IllegalArgumentException("Invalid header index: " + position);
            }
            if (position == 0 || position > response.headers().size()) {
                return null;
            }
            return response.headers().name(position - 1);
        }

        @Override
        public String getHeaderField(int position) {
            if (position < 0) {
                throw new IllegalArgumentException("Invalid header index: " + position);
            }
            if (position == 0) {
                return StatusLine.get(response).toString();
            }
            if (position > response.headers().size()) {
                return null;
            }
            return response.headers().value(position - 1);
        }

        @Override
        public String getHeaderField(String fieldName) {
            return fieldName == null
                    ? StatusLine.get(response).toString()
                    : response.headers().get(fieldName);
        }

        @Override
        public Map<String, List<String>> getHeaderFields() {
            return org.aoju.bus.http.metric.http.HttpURLConnection.toMultimap(response.headers(), StatusLine.get(response).toString());
        }

        @Override
        public int getResponseCode() {
            return response.code();
        }

        @Override
        public String getResponseMessage() {
            return response.message();
        }

        @Override
        public InputStream getErrorStream() {
            return null;
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public int getConnectTimeout() {
            return 0;
        }

        @Override
        public void setConnectTimeout(int timeout) {
            throw throwRequestModificationException();
        }

        @Override
        public int getReadTimeout() {
            return 0;
        }

        @Override
        public void setReadTimeout(int timeout) {
            throw throwRequestModificationException();
        }

        @Override
        public Object getContent() {
            throw throwResponseBodyAccessException();
        }

        @Override
        public Object getContent(Class[] classes) {
            throw throwResponseBodyAccessException();
        }

        @Override
        public InputStream getInputStream() {
            return new InputStream() {
                @Override
                public int read() {
                    throw throwResponseBodyAccessException();
                }
            };
        }

        @Override
        public OutputStream getOutputStream() {
            throw throwRequestModificationException();
        }

        @Override
        public boolean getDoInput() {
            return doInput;
        }

        @Override
        public void setDoInput(boolean doInput) {
            throw throwRequestModificationException();
        }

        @Override
        public boolean getDoOutput() {
            return doOutput;
        }

        @Override
        public void setDoOutput(boolean doOutput) {
            throw throwRequestModificationException();
        }

        @Override
        public boolean getAllowUserInteraction() {
            return false;
        }

        @Override
        public void setAllowUserInteraction(boolean allowUserInteraction) {
            throw throwRequestModificationException();
        }

        @Override
        public boolean getUseCaches() {
            return super.getUseCaches();
        }

        @Override
        public void setUseCaches(boolean useCaches) {
            throw throwRequestModificationException();
        }

        @Override
        public long getIfModifiedSince() {
            return stringToLong(request.headers().get("If-Modified-Since"));
        }

        @Override
        public void setIfModifiedSince(long ifModifiedSince) {
            throw throwRequestModificationException();
        }

        @Override
        public boolean getDefaultUseCaches() {
            return super.getDefaultUseCaches();
        }

        @Override
        public void setDefaultUseCaches(boolean defaultUseCaches) {
            super.setDefaultUseCaches(defaultUseCaches);
        }
    }

    private static final class CacheHttpsURLConnection extends DelegatingConnection {
        private final CacheHttpURLConnection delegate;

        CacheHttpsURLConnection(CacheHttpURLConnection delegate) {
            super(delegate);
            this.delegate = delegate;
        }

        @Override
        protected Handshake handshake() {
            return delegate.response.handshake();
        }

        @Override
        public HostnameVerifier getHostnameVerifier() {
            throw throwRequestSslAccessException();
        }

        @Override
        public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
            throw throwRequestModificationException();
        }

        @Override
        public SSLSocketFactory getSSLSocketFactory() {
            throw throwRequestSslAccessException();
        }

        @Override
        public void setSSLSocketFactory(SSLSocketFactory socketFactory) {
            throw throwRequestModificationException();
        }
    }

}