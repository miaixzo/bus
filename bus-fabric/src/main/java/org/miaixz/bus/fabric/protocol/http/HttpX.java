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
package org.miaixz.bus.fabric.protocol.http;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Callback;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Filter;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.Wiring;
import org.miaixz.bus.fabric.codec.DataCodec;
import org.miaixz.bus.fabric.codec.body.RequestBody;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.network.proxy.ProxyPlan;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.Itinerary;
import org.miaixz.bus.fabric.protocol.http.auth.HttpAuth;
import org.miaixz.bus.fabric.protocol.http.body.FileBody;
import org.miaixz.bus.fabric.protocol.http.body.FormBody;
import org.miaixz.bus.fabric.protocol.http.body.HttpBody;
import org.miaixz.bus.fabric.protocol.http.body.MultipartBody;
import org.miaixz.bus.fabric.protocol.http.body.SoapBody;
import org.miaixz.bus.fabric.protocol.http.calls.HttpCall;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Immutable HTTP exchange.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpX {

    /**
     * Immutable execution snapshot.
     */
    private final HttpSnapshot snapshot;

    /**
     * Execution runner.
     */
    private final HttpRunner runner;

    /**
     * Creates an HTTP exchange.
     *
     * @param context  shared context
     * @param request  request snapshot
     * @param callback callback
     * @param observer observer
     * @param filter   filter
     * @param guard    guard
     */
    private HttpX(final Context context, final HttpRequest request, final Callback<HttpResponse> callback,
            final EventObserver observer, final Filter filter, final GuardRule guard) {
        this.snapshot = new HttpSnapshot(context, request, callback, observer, filter, guard);
        this.runner = new HttpRunner(snapshot);
    }

    /**
     * Creates an HTTP builder.
     *
     * @param context shared context
     * @return builder
     */
    public static Builder builder(final Context context) {
        return new Builder(require(context, "Context"));
    }

    /**
     * Returns request snapshot.
     *
     * @return request
     */
    public HttpRequest request() {
        return snapshot.request();
    }

    /**
     * Executes this exchange synchronously.
     *
     * @return response
     */
    public HttpResponse execute() {
        return runner.execute(Cancellation.create());
    }

    /**
     * Executes this exchange with a shared cancellation scope.
     *
     * @param cancellation cancellation scope
     * @return response
     */
    public HttpResponse execute(final Cancellation cancellation) {
        return runner.execute(cancellation);
    }

    /**
     * Creates a single-use call for this exchange.
     *
     * @return response call
     */
    public Call<HttpResponse> call() {
        return HttpCall.create(this, snapshot.context().reactor().dispatcher());
    }

    /**
     * Enqueues this exchange asynchronously.
     *
     * @return response call
     */
    public Call<HttpResponse> enqueue() {
        return call().enqueue();
    }

    /**
     * Returns bus-core protocol.
     *
     * @return protocol
     */
    public Protocol protocol() {
        return request().url().address().protocol();
    }

    /**
     * Returns request address.
     *
     * @return address
     */
    public Address address() {
        return request().url().address();
    }

    /**
     * Returns request execution path.
     *
     * @return itinerary
     */
    public Itinerary itinerary() {
        return Itinerary.of(protocol(), address());
    }

    /**
     * Returns request headers.
     *
     * @return headers
     */
    public Headers headers() {
        return request().headers();
    }

    /**
     * Returns request timeout.
     *
     * @return timeout
     */
    public Timeout timeout() {
        return request().timeout();
    }

    /**
     * Returns request tag.
     *
     * @return tag
     */
    public Object tag() {
        return request().tag();
    }

    /**
     * Creates a protocol-neutral message from this HTTP exchange and payload.
     *
     * @param payload payload
     * @return message
     */
    public Message message(final Payload payload) {
        return Message.of(protocol(), address(), headers(), payload, tag());
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  name
     * @param <T>   type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Body mode.
     */
    private enum BodyMode {

        /**
         * No body.
         */
        NONE,

        /**
         * Direct body.
         */
        BODY,

        /**
         * Form body.
         */
        FORM,

        /**
         * Multipart body.
         */
        MULTIPART

    }

    /**
     * HTTP exchange builder.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Shared context.
         */
        private final Context context;

        /**
         * Target URL.
         */
        private String url;

        /**
         * Base URL.
         */
        private String baseUrl;

        /**
         * HTTP method.
         */
        private HTTP.Method method = HTTP.Method.GET;

        /**
         * Header builder.
         */
        private final Headers.Builder headers = Headers.builder();

        /**
         * Query entries.
         */
        private final List<QueryEntry> query = new ArrayList<>();

        /**
         * Path segments to append.
         */
        private final List<String> pathSegments = new ArrayList<>();

        /**
         * Path variable replacements.
         */
        private final Map<String, String> pathVariables = new LinkedHashMap<>();

        /**
         * Form builder.
         */
        private FormBody.Builder form;

        /**
         * Multipart parts.
         */
        private final List<MultipartBody.Part> parts = new ArrayList<>();

        /**
         * Body payload.
         */
        private Payload body = Payload.empty();

        /**
         * Body media.
         */
        private MediaType media = MediaType.APPLICATION_OCTET_STREAM_TYPE;

        /**
         * Optional codec.
         */
        private DataCodec<?> codec;

        /**
         * Body mode.
         */
        private BodyMode bodyMode = BodyMode.NONE;

        /**
         * Timeout.
         */
        private Timeout timeout;

        /**
         * Progress listener.
         */
        private BiConsumer<Long, Long> progress;

        /**
         * Tag.
         */
        private Object tag;

        /**
         * Proxy plan.
         */
        private ProxyPlan proxy;

        /**
         * Optional filter.
         */
        private Filter filter;

        /**
         * Optional guard.
         */
        private GuardRule guard;

        /**
         * Observer.
         */
        private EventObserver observer = EventObserver.noop();

        /**
         * Callback.
         */
        private Callback<HttpResponse> callback = Wiring.callback();

        /**
         * Creates a builder.
         *
         * @param context shared context
         */
        private Builder(final Context context) {
            this.context = context;
            final Timeout configured = context.options().get("timeout", Timeout.class);
            this.timeout = configured == null ? Timeout.defaults() : configured;
            this.proxy = configuredProxy();
        }

        /**
         * Returns configured proxy plan.
         *
         * @return proxy plan
         */
        private ProxyPlan configuredProxy() {
            if (context.options().contains("http.proxy")) {
                final ProxyPlan value = context.options().get("http.proxy", ProxyPlan.class);
                return value == null ? ProxyPlan.direct() : value;
            }
            if (context.options().contains("proxy")) {
                final ProxyPlan value = context.options().get("proxy", ProxyPlan.class);
                return value == null ? ProxyPlan.direct() : value;
            }
            return ProxyPlan.direct();
        }

        /**
         * Sets target URL.
         *
         * @param url target URL
         * @return this builder
         */
        public Builder to(final String url) {
            this.url = validateText(url, "URL");
            return this;
        }

        /**
         * Sets target URL.
         *
         * @param url target URL
         * @return this builder
         */
        public Builder url(final String url) {
            return to(url);
        }

        /**
         * Sets base URL.
         *
         * @param url base URL
         * @return this builder
         */
        public Builder base(final String url) {
            this.baseUrl = validateText(url, "Base URL");
            return this;
        }

        /**
         * Sets GET method.
         *
         * @return this builder
         */
        public Builder get() {
            this.method = HTTP.Method.GET;
            return this;
        }

        /**
         * Sets target URL and GET method.
         *
         * @param url target URL
         * @return this builder
         */
        public Builder get(final String url) {
            return to(url).get();
        }

        /**
         * Sets POST method.
         *
         * @return this builder
         */
        public Builder post() {
            this.method = HTTP.Method.POST;
            return this;
        }

        /**
         * Sets target URL and POST method.
         *
         * @param url target URL
         * @return this builder
         */
        public Builder post(final String url) {
            return to(url).post();
        }

        /**
         * Sets PUT method.
         *
         * @return this builder
         */
        public Builder put() {
            this.method = HTTP.Method.PUT;
            return this;
        }

        /**
         * Sets target URL and PUT method.
         *
         * @param url target URL
         * @return this builder
         */
        public Builder put(final String url) {
            return to(url).put();
        }

        /**
         * Sets PATCH method.
         *
         * @return this builder
         */
        public Builder patch() {
            this.method = HTTP.Method.PATCH;
            return this;
        }

        /**
         * Sets target URL and PATCH method.
         *
         * @param url target URL
         * @return this builder
         */
        public Builder patch(final String url) {
            return to(url).patch();
        }

        /**
         * Sets DELETE method.
         *
         * @return this builder
         */
        public Builder delete() {
            this.method = HTTP.Method.DELETE;
            return this;
        }

        /**
         * Sets target URL and DELETE method.
         *
         * @param url target URL
         * @return this builder
         */
        public Builder delete(final String url) {
            return to(url).delete();
        }

        /**
         * Sets HEAD method.
         *
         * @return this builder
         */
        public Builder head() {
            this.method = HTTP.Method.HEAD;
            return this;
        }

        /**
         * Sets target URL and HEAD method.
         *
         * @param url target URL
         * @return this builder
         */
        public Builder head(final String url) {
            return to(url).head();
        }

        /**
         * Sets method.
         *
         * @param method method
         * @return this builder
         */
        public Builder method(final HTTP.Method method) {
            this.method = require(method, "HTTP method");
            return this;
        }

        /**
         * Sets method.
         *
         * @param method method token
         * @return this builder
         */
        public Builder method(final String method) {
            this.method = HTTP.Method.of(validateText(method, "HTTP method"));
            return this;
        }

        /**
         * Appends a header.
         *
         * @param name  header name
         * @param value header value
         * @return this builder
         */
        public Builder header(final String name, final String value) {
            headers.add(name, value);
            return this;
        }

        /**
         * Appends a header.
         *
         * @param name  header name
         * @param value header value
         * @return this builder
         */
        public Builder header(final String name, final Object value) {
            return header(name, stringValue(value, "Header value"));
        }

        /**
         * Replaces a header.
         *
         * @param name  header name
         * @param value header value
         * @return this builder
         */
        public Builder replaceHeader(final String name, final Object value) {
            headers.set(name, stringValue(value, "Header value"));
            return this;
        }

        /**
         * Sets the User-Agent header.
         *
         * @param value User-Agent value
         * @return this builder
         */
        public Builder userAgent(final String value) {
            return replaceHeader("User-Agent", value);
        }

        /**
         * Merges headers.
         *
         * @param headers headers
         * @return this builder
         */
        public Builder headers(final Headers headers) {
            mergeHeaders(require(headers, "Headers"));
            return this;
        }

        /**
         * Merges single-value headers.
         *
         * @param headers headers
         * @return this builder
         */
        public Builder headers(final Map<String, ?> headers) {
            require(headers, "Headers").forEach((name, value) -> header(name, value));
            return this;
        }

        /**
         * Appends a decoded query value.
         *
         * @param name  query name
         * @param value query value
         * @return this builder
         */
        public Builder query(final String name, final String value) {
            query.add(new QueryEntry(validateText(name, "Query name"), validateText(value, "Query value"), false));
            return this;
        }

        /**
         * Appends a decoded query value.
         *
         * @param name  query name
         * @param value query value
         * @return this builder
         */
        public Builder query(final String name, final Object value) {
            return query(name, stringValue(value, "Query value"));
        }

        /**
         * Appends decoded query values.
         *
         * @param query query values
         * @return this builder
         */
        public Builder query(final Map<String, ?> query) {
            require(query, "Query").forEach((name, value) -> query(name, value));
            return this;
        }

        /**
         * Appends an encoded query value.
         *
         * @param name  query name
         * @param value query value
         * @return this builder
         */
        public Builder encoded(final String name, final String value) {
            final String checkedName = validateText(name, "Encoded query name");
            final String checkedValue = validateText(value, "Encoded query value");
            validatePercent(checkedName);
            validatePercent(checkedValue);
            query.add(new QueryEntry(checkedName, checkedValue, true));
            return this;
        }

        /**
         * Appends an encoded query value.
         *
         * @param name  query name
         * @param value query value
         * @return this builder
         */
        public Builder encoded(final String name, final Object value) {
            return encoded(name, stringValue(value, "Encoded query value"));
        }

        /**
         * Appends encoded query values.
         *
         * @param query encoded query values
         * @return this builder
         */
        public Builder encoded(final Map<String, ?> query) {
            require(query, "Encoded query").forEach((name, value) -> encoded(name, value));
            return this;
        }

        /**
         * Appends a single path segment. Slashes in the segment are encoded.
         *
         * @param segment path segment
         * @return this builder
         */
        public Builder path(final String segment) {
            pathSegments.add(validateText(segment, "Path segment"));
            return this;
        }

        /**
         * Adds a path variable replacement for {name} placeholders.
         *
         * @param name  variable name without braces
         * @param value variable value
         * @return this builder
         */
        public Builder path(final String name, final Object value) {
            pathVariables.put(validateText(name, "Path variable name"), stringValue(value, "Path variable value"));
            return this;
        }

        /**
         * Adds path variable replacements.
         *
         * @param variables path variables
         * @return this builder
         */
        public Builder path(final Map<String, ?> variables) {
            require(variables, "Path variables").forEach((name, value) -> path(name, value));
            return this;
        }

        /**
         * Appends a parameter using the active HTTP method.
         *
         * @param name  parameter name
         * @param value parameter value
         * @return this builder
         */
        public Builder param(final String name, final Object value) {
            if (method == HTTP.Method.GET || method == HTTP.Method.HEAD || method == HTTP.Method.DELETE) {
                return query(name, value);
            }
            return form(name, value);
        }

        /**
         * Appends parameters using the active HTTP method.
         *
         * @param params parameter values
         * @return this builder
         */
        public Builder param(final Map<String, ?> params) {
            require(params, "Params").forEach((name, value) -> param(name, value));
            return this;
        }

        /**
         * Appends a form field.
         *
         * @param name  field name
         * @param value field value
         * @return this builder
         */
        public Builder form(final String name, final String value) {
            ensureBodyMode(BodyMode.FORM);
            if (form == null) {
                form = FormBody.builder();
            }
            form.add(name, value);
            return this;
        }

        /**
         * Appends a form field.
         *
         * @param name  field name
         * @param value field value
         * @return this builder
         */
        public Builder form(final String name, final Object value) {
            return form(name, stringValue(value, "Form field value"));
        }

        /**
         * Appends form fields.
         *
         * @param form form values
         * @return this builder
         */
        public Builder form(final Map<String, ?> form) {
            require(form, "Form").forEach((name, value) -> form(name, value));
            return this;
        }

        /**
         * Appends an already encoded form field.
         *
         * @param name  encoded field name
         * @param value encoded field value
         * @return this builder
         */
        public Builder encodedForm(final String name, final String value) {
            ensureBodyMode(BodyMode.FORM);
            if (form == null) {
                form = FormBody.builder();
            }
            form.encoded(name, value);
            return this;
        }

        /**
         * Appends an already encoded form field.
         *
         * @param name  encoded field name
         * @param value encoded field value
         * @return this builder
         */
        public Builder encodedForm(final String name, final Object value) {
            return encodedForm(name, stringValue(value, "Encoded form field value"));
        }

        /**
         * Appends a file part.
         *
         * @param name part name
         * @param path file path
         * @return this builder
         */
        public Builder file(final String name, final Path path) {
            ensureBodyMode(BodyMode.MULTIPART);
            parts.add(
                    MultipartBody.Part.file(
                            name == null || name.isBlank() ? "file" : name,
                            path,
                            MediaType.APPLICATION_OCTET_STREAM_TYPE));
            return this;
        }

        /**
         * Appends a file part with the default part name.
         *
         * @param path file path
         * @return this builder
         */
        public Builder file(final Path path) {
            return file("file", path);
        }

        /**
         * Appends a file part.
         *
         * @param name part name
         * @param file file
         * @return this builder
         */
        public Builder file(final String name, final File file) {
            return file(name, require(file, "File").toPath());
        }

        /**
         * Appends a file part.
         *
         * @param name     file part name
         * @param filename filename hint
         * @param file     file
         * @return this builder
         */
        public Builder file(final String name, final String filename, final File file) {
            ensureBodyMode(BodyMode.MULTIPART);
            final File current = require(file, "File");
            final Path path = current.toPath();
            final Path fileName = path.getFileName();
            final String selected = filename == null || filename.isBlank()
                    ? fileName == null ? "file" : fileName.toString()
                    : stringValue(filename, "File name");
            parts.add(
                    MultipartBody.Part.file(
                            name == null || name.isBlank() ? "file" : name,
                            selected,
                            path,
                            MediaType.APPLICATION_OCTET_STREAM_TYPE));
            return this;
        }

        /**
         * Appends a file part from bytes.
         *
         * @param name     file part name
         * @param filename filename
         * @param content  file content
         * @return this builder
         */
        public Builder file(final String name, final String filename, final byte[] content) {
            ensureBodyMode(BodyMode.MULTIPART);
            parts.add(
                    MultipartBody.Part.file(
                            name == null || name.isBlank() ? "file" : name,
                            stringValue(filename, "File name"),
                            Payload.of(require(content, "File content")),
                            MediaType.APPLICATION_OCTET_STREAM_TYPE));
            return this;
        }

        /**
         * Appends a file part from bytes.
         *
         * @param name        file part name
         * @param filename    filename
         * @param content     file content
         * @param charsetName ignored charset name
         * @return this builder
         */
        public Builder file(final String name, final String filename, final byte[] content, final String charsetName) {
            return file(name, filename, content);
        }

        /**
         * Appends a file part from a stream.
         *
         * @param name     file part name
         * @param filename filename
         * @param input    file input
         * @return this builder
         */
        public Builder file(final String name, final String filename, final InputStream input) {
            ensureBodyMode(BodyMode.MULTIPART);
            parts.add(
                    MultipartBody.Part.file(
                            name == null || name.isBlank() ? "file" : name,
                            stringValue(filename, "File name"),
                            Payload.stream(require(input, "File input"), -1),
                            MediaType.APPLICATION_OCTET_STREAM_TYPE));
            return this;
        }

        /**
         * Appends a file part from text using UTF-8.
         *
         * @param name     file part name
         * @param filename filename
         * @param content  file content
         * @return this builder
         */
        public Builder file(final String name, final String filename, final String content) {
            return file(name, filename, content, org.miaixz.bus.core.lang.Charset.UTF_8.name());
        }

        /**
         * Appends a file part from text.
         *
         * @param name        file part name
         * @param filename    filename
         * @param content     file content
         * @param charsetName charset name
         * @return this builder
         */
        public Builder file(final String name, final String filename, final String content, final String charsetName) {
            final Charset charset = Charset.forName(validateText(charsetName, "Charset name"));
            if (content == null) {
                throw new ValidateException("File content must not be null");
            }
            return file(name, filename, content.getBytes(charset));
        }

        /**
         * Appends a multipart part.
         *
         * @param part part
         * @return this builder
         */
        public Builder part(final MultipartBody.Part part) {
            ensureBodyMode(BodyMode.MULTIPART);
            parts.add(require(part, "Part"));
            return this;
        }

        /**
         * Enables multipart body.
         *
         * @return this builder
         */
        public Builder multipart() {
            ensureBodyMode(BodyMode.MULTIPART);
            return this;
        }

        /**
         * Sets a text body.
         *
         * @param value body text
         * @return this builder
         */
        public Builder body(final String value) {
            ensureBodyMode(BodyMode.BODY);
            this.body = Payload.of(value == null ? "" : value, org.miaixz.bus.core.lang.Charset.UTF_8);
            this.media = MediaType.TEXT_PLAIN_TYPE.withCharset(org.miaixz.bus.core.lang.Charset.UTF_8);
            return this;
        }

        /**
         * Sets a text body with explicit media.
         *
         * @param value body text
         * @param media media
         * @return this builder
         */
        public Builder body(final String value, final MediaType media) {
            return body(value).media(media);
        }

        /**
         * Sets a byte body.
         *
         * @param value body bytes
         * @return this builder
         */
        public Builder body(final byte[] value) {
            ensureBodyMode(BodyMode.BODY);
            this.body = Payload.of(require(value, "Body bytes"));
            this.media = MediaType.APPLICATION_OCTET_STREAM_TYPE;
            return this;
        }

        /**
         * Sets a byte body with explicit media.
         *
         * @param value body bytes
         * @param media media
         * @return this builder
         */
        public Builder body(final byte[] value, final MediaType media) {
            return body(value).media(media);
        }

        /**
         * Sets a streaming body.
         *
         * @param input  body stream
         * @param length declared length, or -1 when unknown
         * @return this builder
         */
        public Builder body(final InputStream input, final long length) {
            ensureBodyMode(BodyMode.BODY);
            this.body = Payload.stream(require(input, "Body stream"), length);
            this.media = MediaType.APPLICATION_OCTET_STREAM_TYPE;
            return this;
        }

        /**
         * Sets a streaming body with explicit media.
         *
         * @param input  body stream
         * @param length declared length, or -1 when unknown
         * @param media  media
         * @return this builder
         */
        public Builder body(final InputStream input, final long length, final MediaType media) {
            return body(input, length).media(media);
        }

        /**
         * Sets a file as the complete request body.
         *
         * @param path file path
         * @return this builder
         */
        public Builder body(final Path path) {
            return body(path, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        }

        /**
         * Sets a file as the complete request body.
         *
         * @param path  file path
         * @param media media
         * @return this builder
         */
        public Builder body(final Path path, final MediaType media) {
            return body(FileBody.of(path, media));
        }

        /**
         * Sets a file as the complete request body.
         *
         * @param file file
         * @return this builder
         */
        public Builder body(final File file) {
            return body(file, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        }

        /**
         * Sets a file as the complete request body.
         *
         * @param file  file
         * @param media media
         * @return this builder
         */
        public Builder body(final File file, final MediaType media) {
            return body(require(file, "Body file").toPath(), media);
        }

        /**
         * Sets a text body.
         *
         * @param value body text
         * @return this builder
         */
        public Builder text(final String value) {
            return body(value);
        }

        /**
         * Sets a JSON body.
         *
         * @param value JSON text
         * @return this builder
         */
        public Builder json(final String value) {
            return body(value)
                    .media(MediaType.APPLICATION_JSON_TYPE.withCharset(org.miaixz.bus.core.lang.Charset.UTF_8));
        }

        /**
         * Sets a payload body.
         *
         * @param payload payload
         * @return this builder
         */
        public Builder body(final Payload payload) {
            ensureBodyMode(BodyMode.BODY);
            this.body = require(payload, "Payload");
            return this;
        }

        /**
         * Sets a payload body with explicit media.
         *
         * @param payload payload
         * @param media   media
         * @return this builder
         */
        public Builder body(final Payload payload, final MediaType media) {
            return body(payload).media(media);
        }

        /**
         * Encodes a value with the supplied codec and sets it as body.
         *
         * @param value value
         * @param codec codec
         * @param <T>   value type
         * @return this builder
         */
        public <T> Builder body(final T value, final DataCodec<? super T> codec) {
            final DataCodec<? super T> current = require(codec, "Codec");
            ensureBodyMode(BodyMode.BODY);
            this.body = require(current.encode(value), "Encoded payload");
            this.media = current.media();
            return this;
        }

        /**
         * Sets a prepared HTTP body.
         *
         * @param body HTTP body
         * @return this builder
         */
        public Builder body(final HttpBody body) {
            final HttpBody current = require(body, "HTTP body");
            ensureBodyMode(BodyMode.BODY);
            this.body = current.payload();
            this.media = current.media();
            return this;
        }

        /**
         * Sets a prepared request body.
         *
         * @param body request body
         * @return this builder
         */
        public Builder body(final RequestBody body) {
            final RequestBody current = require(body, "Request body");
            return body(HttpBody.of(current.payload(), current.media()));
        }

        /**
         * Sets the body using a generic object.
         *
         * @param value body value
         * @return this builder
         */
        public Builder body(final Object value) {
            if (value instanceof RequestBody requestBody) {
                return body(requestBody);
            }
            if (value instanceof HttpBody httpBody) {
                return body(httpBody);
            }
            if (value instanceof Payload payload) {
                return body(payload);
            }
            if (value instanceof Path path) {
                return body(path);
            }
            if (value instanceof File file) {
                return body(file);
            }
            if (value instanceof byte[] bytes) {
                return body(bytes);
            }
            if (value instanceof CharSequence text) {
                return body(text.toString());
            }
            if (codec != null) {
                return body(value, (DataCodec) codec);
            }
            return body(value == null ? "" : value.toString());
        }

        /**
         * Sets a SOAP envelope body and SOAPAction header.
         *
         * @param namespace method namespace
         * @param method    method name
         * @param params    method parameters
         * @return this builder
         */
        public Builder soap(final String namespace, final String method, final Map<String, ?> params) {
            final SoapBody soap = SoapBody.envelope().namespace(namespace).method(method).params(params).build();
            headers.set("SOAPAction", soap.action());
            return body(soap.body());
        }

        /**
         * Sets body media.
         *
         * @param media media
         * @return this builder
         */
        public Builder media(final MediaType media) {
            this.media = require(media, "MediaType");
            return this;
        }

        /**
         * Sets codec.
         *
         * @param codec codec
         * @return this builder
         */
        public Builder codec(final DataCodec<?> codec) {
            this.codec = require(codec, "Codec");
            return this;
        }

        /**
         * Sets range header.
         *
         * @param start range start
         * @param end   range end
         * @return this builder
         */
        public Builder range(final long start, final long end) {
            if (start < 0 || end < 0 || end < start) {
                throw new ValidateException("Range bounds must be non-negative and ordered");
            }
            headers.set("Range", "bytes=" + start + Symbol.C_MINUS + end);
            return this;
        }

        /**
         * Sets progress listener.
         *
         * @param listener listener
         * @return this builder
         */
        public Builder progress(final BiConsumer<Long, Long> listener) {
            this.progress = listener;
            return this;
        }

        /**
         * Sets tag.
         *
         * @param tag tag
         * @return this builder
         */
        public Builder tag(final Object tag) {
            this.tag = tag;
            return this;
        }

        /**
         * Sets request id as the tag.
         *
         * @param id request id
         * @return this builder
         */
        public Builder id(final Object id) {
            return tag(id);
        }

        /**
         * Sets proxy plan.
         *
         * @param proxy proxy plan
         * @return this builder
         */
        public Builder proxy(final ProxyPlan proxy) {
            this.proxy = require(proxy, "Proxy plan");
            return this;
        }

        /**
         * Sets an HTTP proxy address.
         *
         * @param proxy proxy address
         * @return this builder
         */
        public Builder proxy(final Address proxy) {
            return proxy(ProxyPlan.http(require(proxy, "Proxy address")));
        }

        /**
         * Sets an HTTP proxy URL.
         *
         * @param proxy proxy URL
         * @return this builder
         */
        public Builder proxy(final String proxy) {
            return proxy(Address.parse(validateText(proxy, "Proxy URL")));
        }

        /**
         * Disables proxy for this request.
         *
         * @return this builder
         */
        public Builder directProxy() {
            this.proxy = ProxyPlan.direct();
            return this;
        }

        /**
         * Sets proxy authorization headers for the current HTTP proxy.
         *
         * @param authorization authorization headers
         * @return this builder
         */
        public Builder proxyAuthorization(final Headers authorization) {
            this.proxy = proxy.withAuthorization(require(authorization, "Proxy authorization"));
            return this;
        }

        /**
         * Sets Basic credentials for the current HTTP proxy.
         *
         * @param username username
         * @param password password
         * @return this builder
         */
        public Builder proxyCredentials(final String username, final String password) {
            return proxyAuthorization(HttpAuth.basic(username, password).applyProxy(Headers.empty()));
        }

        /**
         * Sets filter.
         *
         * @param filter filter
         * @return this builder
         */
        public Builder filter(final Filter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Sets guard.
         *
         * @param guard guard
         * @return this builder
         */
        public Builder guard(final GuardRule guard) {
            this.guard = guard;
            return this;
        }

        /**
         * Sets observer.
         *
         * @param observer observer
         * @return this builder
         */
        public Builder observe(final EventObserver observer) {
            this.observer = observer == null ? EventObserver.noop() : observer;
            return this;
        }

        /**
         * Sets callback.
         *
         * @param callback callback
         * @return this builder
         */
        public Builder callback(final Callback<HttpResponse> callback) {
            this.callback = callback == null ? Wiring.callback() : callback;
            return this;
        }

        /**
         * Sets all timeout values.
         *
         * @param timeout timeout
         * @return this builder
         */
        public Builder timeout(final Duration timeout) {
            if (timeout == null || timeout.isNegative()) {
                throw new ValidateException("Timeout must be non-null and non-negative");
            }
            this.timeout = Timeout.of(timeout);
            return this;
        }

        /**
         * Sets timeout.
         *
         * @param timeout timeout
         * @return this builder
         */
        public Builder timeout(final Timeout timeout) {
            this.timeout = require(timeout, "Timeout");
            return this;
        }

        /**
         * Builds an HTTP exchange snapshot.
         *
         * @return HTTP exchange
         */
        public HttpX build() {
            final UnoUrl target = buildUrl();
            final HttpBody httpBody = buildBody();
            final Headers requestHeaders = buildHeaders(httpBody);
            final HttpRequest request = HttpRequest.builder().method(method).url(target).headers(requestHeaders)
                    .body(httpBody).tag(tag).proxy(proxy).timeout(timeout).build();
            final Protocol protocol = request.url().address().protocol();
            if (protocol != Protocol.HTTP && protocol != Protocol.HTTPS) {
                throw new ProtocolException("HTTP exchange requires http or https URL");
            }
            return new HttpX(context, request, callback, observer, filter, guard);
        }

        /**
         * Builds a current HTTP request snapshot.
         *
         * @return request
         */
        public HttpRequest request() {
            return build().request();
        }

        /**
         * Creates a call for a built exchange.
         *
         * @return response call
         */
        public Call<HttpResponse> call() {
            return build().call();
        }

        /**
         * Executes a built exchange.
         *
         * @return response
         */
        public HttpResponse execute() {
            return build().execute();
        }

        /**
         * Executes a built exchange and reads response text.
         *
         * @return response text
         */
        public String executeText() {
            return execute().text();
        }

        /**
         * Enqueues a built exchange.
         *
         * @return response call
         */
        public Call<HttpResponse> enqueue() {
            return build().enqueue();
        }

        /**
         * Merges headers.
         *
         * @param source source headers
         */
        private void mergeHeaders(final Headers source) {
            source.asMap().forEach((name, values) -> values.forEach(value -> headers.add(name, value)));
        }

        /**
         * Builds headers with body metadata.
         *
         * @param body body
         * @return headers
         */
        private Headers buildHeaders(final HttpBody body) {
            final Headers current = headers.build();
            final Headers.Builder builder = Headers.builder();
            current.asMap().forEach((name, values) -> values.forEach(value -> builder.add(name, value)));
            if (body.length() > 0 && !current.contains("Content-Length")) {
                builder.set("Content-Length", Long.toString(body.length()));
            }
            if (!current.contains("Content-Type") && body.length() != 0) {
                builder.set("Content-Type", body.media().value());
            }
            return builder.build();
        }

        /**
         * Builds request body.
         *
         * @return HTTP body
         */
        private HttpBody buildBody() {
            Payload payload = body;
            MediaType selectedMedia = media;
            if (bodyMode == BodyMode.FORM && form != null) {
                final FormBody formBody = form.build();
                payload = formBody.payload();
                selectedMedia = formBody.media();
            } else if (bodyMode == BodyMode.MULTIPART) {
                final MultipartBody.Builder multipart = MultipartBody.builder();
                for (final MultipartBody.Part part : parts) {
                    multipart.part(part);
                }
                final MultipartBody multipartBody = multipart.build();
                payload = multipartBody.payload();
                selectedMedia = multipartBody.media();
            }
            if (codec != null && bodyMode == BodyMode.NONE) {
                selectedMedia = codec.media();
            }
            final HttpBody httpBody = HttpBody.of(payload, selectedMedia, context.options().materializeMaxBytes());
            return progress == null ? httpBody : httpBody.progress(progress);
        }

        /**
         * Builds target URL.
         *
         * @return URL
         */
        private UnoUrl buildUrl() {
            final String resolved = resolveTarget();
            return UnoUrl.parse(appendQuery(resolved));
        }

        /**
         * Resolves target text.
         *
         * @return target URL text
         */
        private String resolveTarget() {
            final String resolved;
            if (url == null && baseUrl == null) {
                throw new ValidateException("HTTP target URL must be set");
            }
            if (url == null) {
                resolved = baseUrl;
            } else if (absolute(url) || baseUrl == null) {
                resolved = url;
            } else {
                try {
                    resolved = restorePathPlaceholders(
                            new URI(protectPathPlaceholders(baseUrl)).resolve(protectPathPlaceholders(url)).toString());
                } catch (final URISyntaxException | IllegalArgumentException e) {
                    throw new ProtocolException("Invalid base URL", e);
                }
            }
            return appendPathSegments(applyPath(resolved));
        }

        /**
         * Applies path variable replacements.
         *
         * @param target target URL
         * @return URL with path variables replaced
         */
        private String applyPath(final String target) {
            String current = target;
            for (final Map.Entry<String, String> entry : pathVariables.entrySet()) {
                final String token = Symbol.BRACE_LEFT + entry.getKey() + Symbol.BRACE_RIGHT;
                if (!current.contains(token)) {
                    throw new ValidateException("Path variable " + entry.getKey() + " does not exist in URL");
                }
                current = current.replace(token, entry.getValue());
            }
            if (hasPathPlaceholder(current)) {
                throw new ValidateException("Path variable is missing for URL");
            }
            return current;
        }

        /**
         * Appends path segments before query and fragment text.
         *
         * @param target target URL
         * @return URL with appended path segments
         */
        private String appendPathSegments(final String target) {
            if (pathSegments.isEmpty()) {
                return target;
            }
            final int split = pathSplit(target);
            final StringBuilder builder = new StringBuilder(target.substring(0, split));
            for (final String segment : pathSegments) {
                if (builder.isEmpty() || builder.charAt(builder.length() - 1) != Symbol.C_SLASH) {
                    builder.append(Symbol.C_SLASH);
                }
                builder.append(encode(segment));
            }
            return builder.append(target.substring(split)).toString();
        }

        /**
         * Appends query entries.
         *
         * @param target target URL
         * @return URL with query
         */
        private String appendQuery(final String target) {
            if (query.isEmpty()) {
                return target;
            }
            final int fragment = target.indexOf(Symbol.C_HASH);
            final String head = fragment < 0 ? target : target.substring(0, fragment);
            final String tail = fragment < 0 ? "" : target.substring(fragment);
            final StringBuilder builder = new StringBuilder(head);
            builder.append(head.indexOf(Symbol.C_QUESTION_MARK) >= 0 ? Symbol.C_AND : Symbol.C_QUESTION_MARK);
            for (int i = 0; i < query.size(); i++) {
                if (i > 0) {
                    builder.append(Symbol.C_AND);
                }
                final QueryEntry entry = query.get(i);
                builder.append(entry.encoded ? entry.name : encode(entry.name)).append(Symbol.C_EQUAL)
                        .append(entry.encoded ? entry.value : encode(entry.value));
            }
            return builder.append(tail).toString();
        }

        /**
         * Ensures the request body mode is valid for the selected method.
         *
         * @param mode requested mode
         */
        private void ensureBodyMode(final BodyMode mode) {
            if (bodyMode != BodyMode.NONE && bodyMode != mode) {
                throw new ValidateException("HTTP body modes are mutually exclusive");
            }
            bodyMode = mode;
        }

        /**
         * Returns whether URL text is absolute.
         *
         * @param value URL text
         * @return true when absolute
         */
        private static boolean absolute(final String value) {
            return value.startsWith("http://") || value.startsWith("https://");
        }

        /**
         * Returns whether URL text still contains a {name} path placeholder.
         *
         * @param value URL text
         * @return true when a placeholder remains
         */
        private static boolean hasPathPlaceholder(final String value) {
            final int left = value.indexOf(Symbol.C_BRACE_LEFT);
            final int right = value.indexOf(Symbol.C_BRACE_RIGHT, left + 1);
            return left >= 0 && right > left + 1;
        }

        /**
         * Returns the index where query or fragment text starts.
         *
         * @param value URL text
         * @return split index
         */
        private static int pathSplit(final String value) {
            final int query = value.indexOf(Symbol.C_QUESTION_MARK);
            final int fragment = value.indexOf(Symbol.C_HASH);
            if (query < 0) {
                return fragment < 0 ? value.length() : fragment;
            }
            if (fragment < 0) {
                return query;
            }
            return Math.min(query, fragment);
        }

        /**
         * Temporarily encodes path placeholder braces so URI resolution can preserve them.
         *
         * @param value URL text
         * @return protected URL text
         */
        private static String protectPathPlaceholders(final String value) {
            return value.replace(Symbol.BRACE_LEFT, "%7B").replace(Symbol.BRACE_RIGHT, "%7D");
        }

        /**
         * Restores protected path placeholder braces after URI resolution.
         *
         * @param value URL text
         * @return restored URL text
         */
        private static String restorePathPlaceholders(final String value) {
            return value.replace("%7B", Symbol.BRACE_LEFT).replace("%7D", Symbol.BRACE_RIGHT);
        }

        /**
         * Encodes a URL component.
         *
         * @param value value
         * @return encoded value
         */
        private static String encode(final String value) {
            return UrlEncoder.encodeAll(value, org.miaixz.bus.core.lang.Charset.UTF_8);
        }

        /**
         * Validates text input.
         *
         * @param value value
         * @param name  name
         * @return value
         */
        private static String validateText(final String value, final String name) {
            if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
                throw new ValidateException(name + " must be non-blank and single-line");
            }
            return value;
        }

        /**
         * Converts a non-null value to a single-line string.
         *
         * @param value value
         * @param name  value name
         * @return string value
         */
        private static String stringValue(final Object value, final String name) {
            return validateText(value == null ? null : value.toString(), name);
        }

        /**
         * Validates percent encoding.
         *
         * @param value encoded text
         */
        private static void validatePercent(final String value) {
            for (int i = 0; i < value.length(); i++) {
                if (value.charAt(i) == Symbol.C_PERCENT) {
                    if (i + 2 >= value.length() || !CharKit.isHexChar(value.charAt(i + 1))
                            || !CharKit.isHexChar(value.charAt(i + 2))) {
                        throw new ProtocolException("Invalid percent-encoded query value");
                    }
                    i += 2;
                }
            }
        }

    }

    /**
     * Query entry.
     *
     * @param name    name
     * @param value   value
     * @param encoded encoded flag
     */
    private record QueryEntry(String name, String value, boolean encoded) {

    }

}
