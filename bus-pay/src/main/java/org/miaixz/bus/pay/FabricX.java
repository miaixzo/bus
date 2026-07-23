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
package org.miaixz.bus.pay;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.context.SslContextFactoryAdapter;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.MultipartBody;

/**
 * Fabric-backed HTTP support for payment providers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class FabricX {

    /**
     * Shared Fabric context for non-certificate payment HTTP calls.
     */
    private static final org.miaixz.bus.fabric.Context CONTEXT = org.miaixz.bus.fabric.Context.create();

    /**
     * Form media used by payment requests.
     */
    private static final MediaType FORM = MediaType.APPLICATION_FORM_URLENCODED_TYPE;

    /**
     * Sends a GET request.
     *
     * @param url URL
     * @return response body
     */
    public static String get(final String url) {
        return get(url, null);
    }

    /**
     * Sends a GET request.
     *
     * @param url   URL
     * @param query query parameters
     * @return response body
     */
    public static String get(final String url, final Map<String, ?> query) {
        return get(url, query, null).body();
    }

    /**
     * Sends a GET request.
     *
     * @param url     URL
     * @param query   query parameters
     * @param headers headers
     * @return response snapshot
     */
    public static Response get(final String url, final Map<String, ?> query, final Map<String, ?> headers) {
        final var builder = Fabric.http(CONTEXT).get(url);
        apply(builder::query, query);
        apply(builder::header, headers);
        return execute(builder::execute);
    }

    /**
     * Sends a POST request with form fields.
     *
     * @param url  URL
     * @param form form fields
     * @return response body
     */
    public static String post(final String url, final Map<String, ?> form) {
        return post(url, null, form).body();
    }

    /**
     * Sends a POST request with raw body.
     *
     * @param url  URL
     * @param data body
     * @return response body
     */
    public static String post(final String url, final String data) {
        return post(url, null, data).body();
    }

    /**
     * Sends a POST request with raw body.
     *
     * @param url     URL
     * @param headers headers
     * @param data    body
     * @return response snapshot
     */
    public static Response post(final String url, final Map<String, ?> headers, final String data) {
        final var builder = Fabric.http(CONTEXT).post(url).body(data == null ? "" : data, media(headers, FORM));
        apply(builder::header, headers);
        return execute(builder::execute);
    }

    /**
     * Sends a POST request with form fields.
     *
     * @param url     URL
     * @param headers headers
     * @param form    form fields
     * @return response snapshot
     */
    public static Response post(final String url, final Map<String, ?> headers, final Map<String, ?> form) {
        final var builder = Fabric.http(CONTEXT).post(url);
        apply(builder::header, headers);
        if (form == null || form.isEmpty()) {
            builder.body(Payload.empty(), FORM);
        } else {
            apply(builder::form, form);
        }
        return execute(builder::execute);
    }

    /**
     * Sends a multipart POST request.
     *
     * @param url     URL
     * @param headers headers
     * @param form    form fields
     * @param file    upload file
     * @return response snapshot
     */
    public static Response post(
            final String url,
            final Map<String, ?> headers,
            final Map<String, ?> form,
            final File file) {
        final var builder = Fabric.http(CONTEXT).post(url).multipart();
        apply(builder::header, headers);
        if (form != null && !form.isEmpty()) {
            form.forEach((name, value) -> {
                if (name != null && value != null) {
                    builder.part(MultipartBody.Part.of(name, Payload.of(value.toString(), Charset.UTF_8)));
                }
            });
        }
        if (file != null) {
            builder.file("file", file.getName(), file);
        }
        return execute(builder::execute);
    }

    /**
     * Sends a certificate-backed POST request.
     *
     * @param url      URL
     * @param data     body
     * @param certPath certificate path
     * @param certPass certificate password
     * @param protocol TLS protocol
     * @return response body
     */
    public static String post(
            final String url,
            final String data,
            final String certPath,
            final String certPass,
            final String protocol) {
        return post(url, data, certPath, null, certPass, protocol);
    }

    /**
     * Sends a certificate-backed POST request.
     *
     * @param url      URL
     * @param data     body
     * @param certFile certificate stream
     * @param certPass certificate password
     * @param protocol TLS protocol
     * @return response body
     */
    public static String post(
            final String url,
            final String data,
            final InputStream certFile,
            final String certPass,
            final String protocol) {
        return post(url, data, null, certFile, certPass, protocol);
    }

    /**
     * Sends a PUT request with raw body.
     *
     * @param url     URL
     * @param headers headers
     * @param data    body
     * @return response snapshot
     */
    public static Response put(final String url, final Map<String, ?> headers, final String data) {
        final var builder = Fabric.http(CONTEXT).put(url).body(data == null ? "" : data, media(headers, FORM));
        apply(builder::header, headers);
        return execute(builder::execute);
    }

    /**
     * Sends a PUT request with form fields.
     *
     * @param url     URL
     * @param headers headers
     * @param form    form fields
     * @return response snapshot
     */
    public static Response put(final String url, final Map<String, ?> headers, final Map<String, ?> form) {
        final var builder = Fabric.http(CONTEXT).put(url);
        apply(builder::header, headers);
        if (form == null || form.isEmpty()) {
            builder.body(Payload.empty(), FORM);
        } else {
            apply(builder::form, form);
        }
        return execute(builder::execute);
    }

    /**
     * Uploads a file through a certificate-backed multipart request.
     *
     * @param url      URL
     * @param data     optional body
     * @param certPath certificate path
     * @param certPass certificate password
     * @param filePath file path
     * @param protocol TLS protocol
     * @return response body
     */
    public static String upload(
            final String url,
            final String data,
            final String certPath,
            final String certPass,
            final String filePath,
            final String protocol) {
        try (org.miaixz.bus.fabric.Context context = certificateContext(certPath, null, certPass, protocol)) {
            final var builder = Fabric.http(context).post(url).multipart();
            if (StringKit.isNotEmpty(data)) {
                builder.part(MultipartBody.Part.of("params", Payload.of(data, Charset.UTF_8)));
            }
            builder.file("file", Path.of(filePath));
            return execute(builder::execute).body();
        }
    }

    /**
     * Executes a certificate-backed POST request.
     *
     * @param url      URL
     * @param data     body
     * @param certPath certificate path
     * @param certFile certificate stream
     * @param certPass certificate password
     * @param protocol TLS protocol
     * @return response body
     */
    private static String post(
            final String url,
            final String data,
            final String certPath,
            final InputStream certFile,
            final String certPass,
            final String protocol) {
        try (org.miaixz.bus.fabric.Context context = certificateContext(certPath, certFile, certPass, protocol)) {
            final var builder = Fabric.http(context).post(url).body(data == null ? "" : data, FORM);
            return execute(builder::execute).body();
        }
    }

    /**
     * Applies values to a Fabric HTTP builder.
     *
     * @param consumer value consumer
     * @param values   values
     */
    private static void apply(final BiConsumer<String, Object> consumer, final Map<String, ?> values) {
        if (values != null && !values.isEmpty()) {
            values.forEach((name, value) -> {
                if (name != null && value != null) {
                    consumer.accept(name, value);
                }
            });
        }
    }

    /**
     * Resolves request media.
     *
     * @param headers  headers
     * @param fallback fallback media
     * @return media type
     */
    private static MediaType media(final Map<String, ?> headers, final MediaType fallback) {
        final Object contentType = header(headers, Http.Header.CONTENT_TYPE);
        return contentType == null ? fallback : media(contentType.toString());
    }

    /**
     * Reads a case-insensitive header.
     *
     * @param headers headers
     * @param name    header name
     * @return header value
     */
    private static Object header(final Map<String, ?> headers, final String name) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        for (final Map.Entry<String, ?> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Parses a valid content type.
     *
     * @param contentType content type
     * @return media type
     */
    private static MediaType media(final String contentType) {
        if (StringKit.isBlank(contentType) || StringKit.containsAny(contentType, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Content-Type must be non-blank and single-line");
        }
        return MediaType.parse(contentType);
    }

    /**
     * Executes a request and snapshots the closeable response.
     *
     * @param executor response executor
     * @return response snapshot
     */
    private static Response execute(final Supplier<HttpResponse> executor) {
        try (HttpResponse response = executor.get()) {
            return new Response(response.code(), response.headers().asMap(), response.text());
        }
    }

    /**
     * Builds a current context carrying certificate TLS settings.
     *
     * @param certPath certificate path
     * @param certFile certificate stream
     * @param certPass certificate password
     * @param protocol TLS protocol
     * @return context
     */
    private static org.miaixz.bus.fabric.Context certificateContext(
            final String certPath,
            final InputStream certFile,
            final String certPass,
            final String protocol) {
        final String selected = StringKit.isEmpty(protocol) ? Protocol.TLSv1.name : protocol;
        final SslContextFactoryAdapter factory = SslContextFactoryAdapter
                .of(() -> sslContext(certPath, certFile, certPass, selected));
        final TlsContext tlsContext = factory.tlsContext();
        final TlsSettings tlsSettings = TlsSettings.builder().versions(List.of(selected)).build();
        return org.miaixz.bus.fabric.Context.builder().tlsContext(tlsContext).tlsSettings(tlsSettings).build();
    }

    /**
     * Creates an SSL context from a PKCS12 client certificate.
     *
     * @param certPath certificate path
     * @param certFile certificate stream
     * @param certPass certificate password
     * @param protocol TLS protocol
     * @return SSL context
     * @throws Exception when the context cannot be built
     */
    private static SSLContext sslContext(
            final String certPath,
            final InputStream certFile,
            final String certPass,
            final String protocol) throws Exception {
        final SSLContext context = SSLContext.getInstance(protocol);
        context.init(keyManagers(certPass, certPath, certFile), null, new SecureRandom());
        return context;
    }

    /**
     * Creates key managers from a PKCS12 certificate source.
     *
     * @param certPass certificate password
     * @param certPath certificate path
     * @param certFile certificate stream
     * @return key managers
     * @throws Exception when loading fails
     */
    private static KeyManager[] keyManagers(final String certPass, final String certPath, final InputStream certFile)
            throws Exception {
        final char[] password = certPass == null ? new char[0] : certPass.toCharArray();
        final KeyStore clientStore = KeyStore.getInstance("PKCS12");
        if (certFile != null) {
            clientStore.load(certFile, password);
        } else {
            try (InputStream input = Files.newInputStream(Path.of(certPath))) {
                clientStore.load(input, password);
            }
        }
        final KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        factory.init(clientStore, password);
        return factory.getKeyManagers();
    }

    /**
     * Immutable response snapshot.
     *
     * @param code    status code
     * @param headers headers
     * @param body    body
     */
    public record Response(int code, Map<String, List<String>> headers, String body) {

        /**
         * Creates a response snapshot.
         *
         * @param code    status code
         * @param headers headers
         * @param body    body
         */
        public Response {
            headers = copy(headers);
            body = body == null ? "" : body;
        }

        /**
         * Returns the total header value count.
         *
         * @return header value count
         */
        public int headerCount() {
            return headers.values().stream().mapToInt(List::size).sum();
        }

        /**
         * Copies headers defensively.
         *
         * @param headers headers
         * @return immutable copy
         */
        private static Map<String, List<String>> copy(final Map<String, List<String>> headers) {
            if (headers == null || headers.isEmpty()) {
                return Map.of();
            }
            final LinkedHashMap<String, List<String>> copy = new LinkedHashMap<>();
            headers.forEach(
                    (name, values) -> copy.put(
                            name,
                            values == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(values))));
            return Collections.unmodifiableMap(copy);
        }

    }

}
