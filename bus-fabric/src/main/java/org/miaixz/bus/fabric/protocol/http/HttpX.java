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

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
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
import org.miaixz.bus.fabric.codec.DataCodec;
import org.miaixz.bus.fabric.codec.body.RequestBody;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.network.proxy.ProxyPlan;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.Itinerary;
import org.miaixz.bus.fabric.protocol.Mediator;
import org.miaixz.bus.fabric.protocol.Mediator.Type;
import org.miaixz.bus.fabric.protocol.http.auth.HttpAuth;
import org.miaixz.bus.fabric.protocol.http.body.FileBody;
import org.miaixz.bus.fabric.protocol.http.body.FormBody;
import org.miaixz.bus.fabric.protocol.http.body.MultipartBody;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;
import org.miaixz.bus.fabric.protocol.http.body.SoapBody;
import org.miaixz.bus.fabric.protocol.http.body.TextBody;
import org.miaixz.bus.fabric.protocol.http.calls.HttpCall;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Immutable HTTP exchange.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpX {

    /** Most recent safe immutable synchronous request shape. */
    private static volatile RequestCache requestCache;

    /**
     * Immutable execution snapshot.
     */
    private final HttpSnapshot snapshot;

    /**
     * Execution runner.
     */
    private final HttpRunner runner;

    /**
     * Callback managed by the shared call lifecycle.
     */
    private final Callback<HttpResponse> callback;

    /**
     * Creates an HTTP exchange.
     *
     * @param context  shared context
     * @param request  request snapshot
     * @param callback terminal callback receiving the HTTP result
     * @param observer observer receiving exchange lifecycle events
     * @param filter   filter applied to protocol-neutral messages
     * @param guard    guard validating the exchange, or {@code null}
     */
    private HttpX(final Context context, final HttpRequest request, final Callback<HttpResponse> callback,
            final EventObserver observer, final Filter filter, final GuardRule guard) {
        this.snapshot = new HttpSnapshot(context, request, observer, filter, guard);
        this.runner = new HttpRunner(snapshot, observer != EventObserver.noop());
        this.callback = callback;
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
        if (callback == null) {
            return execute(Cancellation.none());
        }
        return call().execute();
    }

    /**
     * Creates a single-use call for this exchange.
     *
     * @return response call
     */
    public Call<HttpResponse> call() {
        return HttpCall.create(snapshot.request(), snapshot.context().reactor().dispatcher(), callback, this::execute);
    }

    /**
     * Executes the frozen exchange with a caller-owned cancellation scope.
     *
     * @param cancellation cancellation scope
     * @return response
     */
    private HttpResponse execute(final Cancellation cancellation) {
        return Mediator.execute(Type.HTTP, cancellation, runner::run);
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
     * @param payload HTTP payload represented by the message
     * @return message
     */
    public Message message(final Payload payload) {
        return Message.of(protocol(), address(), headers(), payload, tag());
    }

    /**
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  diagnostic parameter name
     * @param <T>   type
     * @return the validated reference
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
        private Http.Method method = Http.Method.GET;

        /**
         * Header builder.
         */
        private final Headers.Builder headers = Headers.builder();

        /**
         * Query entries.
         */
        private List<QueryEntry> query;

        /**
         * Path segments to append.
         */
        private List<String> pathSegments;

        /**
         * Path variable replacements.
         */
        private Map<String, String> pathVariables;

        /**
         * Form builder.
         */
        private FormBody.Builder form;

        /**
         * Multipart parts.
         */
        private List<MultipartBody.Part> parts;

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
        private Callback<HttpResponse> callback;

        /**
         * Materialization limit frozen with the builder's shared context.
         */
        private final long materializeMaxBytes;

        /**
         * Creates a builder.
         *
         * @param context shared context
         */
        private Builder(final Context context) {
            this.context = context;
            final Timeout configured = context.options().get(org.miaixz.bus.fabric.Builder.OPTION_TIMEOUT);
            this.timeout = configured == null ? Timeout.defaults() : configured;
            this.proxy = configuredProxy();
            this.materializeMaxBytes = context.options().materializeMaxBytes();
        }

        private List<QueryEntry> queryEntries() {
            if (query == null)
                query = new ArrayList<>(2);
            return query;
        }

        private List<String> pathSegments() {
            if (pathSegments == null)
                pathSegments = new ArrayList<>(2);
            return pathSegments;
        }

        private Map<String, String> pathVariables() {
            if (pathVariables == null)
                pathVariables = new LinkedHashMap<>(2);
            return pathVariables;
        }

        private List<MultipartBody.Part> parts() {
            if (parts == null)
                parts = new ArrayList<>(2);
            return parts;
        }

        /**
         * Returns configured proxy plan.
         *
         * @return proxy plan
         */
        private ProxyPlan configuredProxy() {
            if (context.options().contains(org.miaixz.bus.fabric.Builder.OPTION_HTTP_PROXY)) {
                final ProxyPlan value = context.options().get(org.miaixz.bus.fabric.Builder.OPTION_HTTP_PROXY);
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
            this.method = Http.Method.GET;
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
            this.method = Http.Method.POST;
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
            this.method = Http.Method.PUT;
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
            this.method = Http.Method.PATCH;
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
            this.method = Http.Method.DELETE;
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
            this.method = Http.Method.HEAD;
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
         * @param method HTTP method token to use
         * @return this builder
         */
        public Builder method(final Http.Method method) {
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
            this.method = Http.Method.of(validateText(method, "HTTP method"));
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
            return replaceHeader(Http.Header.USER_AGENT, value);
        }

        /**
         * Merges headers.
         *
         * @param headers multi-value headers to merge
         * @return this builder
         */
        public Builder headers(final Headers headers) {
            mergeHeaders(require(headers, "Headers"));
            return this;
        }

        /**
         * Merges single-value headers.
         *
         * @param headers single-value headers to merge
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
            queryEntries()
                    .add(new QueryEntry(validateText(name, "Query name"), validateText(value, "Query value"), false));
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
            queryEntries().add(new QueryEntry(checkedName, checkedValue, true));
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
            pathSegments().add(validateText(segment, "Path segment"));
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
            pathVariables().put(validateText(name, "Path variable name"), stringValue(value, "Path variable value"));
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
            if (method == Http.Method.GET || method == Http.Method.HEAD || method == Http.Method.DELETE) {
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
            parts().add(
                    MultipartBody.Part.file(
                            StringKit.isBlank(name) ? "file" : name,
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
         * @param file file whose contents form the multipart part
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
         * @param file     file whose contents form the multipart part
         * @return this builder
         */
        public Builder file(final String name, final String filename, final File file) {
            ensureBodyMode(BodyMode.MULTIPART);
            final File current = require(file, "File");
            final Path path = current.toPath();
            final Path fileName = path.getFileName();
            final String selected = StringKit.isBlank(filename) ? fileName == null ? "file" : fileName.toString()
                    : stringValue(filename, "File name");
            parts().add(
                    MultipartBody.Part.file(
                            StringKit.isBlank(name) ? "file" : name,
                            selected,
                            path,
                            MediaType.APPLICATION_OCTET_STREAM_TYPE));
            return this;
        }

        /**
         * Appends a file part from bytes.
         *
         * @param name     file part name
         * @param filename filename reported in Content-Disposition
         * @param content  file content
         * @return this builder
         */
        public Builder file(final String name, final String filename, final byte[] content) {
            ensureBodyMode(BodyMode.MULTIPART);
            parts().add(
                    MultipartBody.Part.file(
                            StringKit.isBlank(name) ? "file" : name,
                            stringValue(filename, "File name"),
                            Payload.of(require(content, "File content")),
                            MediaType.APPLICATION_OCTET_STREAM_TYPE));
            return this;
        }

        /**
         * Appends a file part from bytes.
         *
         * @param name        file part name
         * @param filename    filename reported in Content-Disposition
         * @param content     file content
         * @param charsetName ignored charset name
         * @return this builder
         */
        public Builder file(final String name, final String filename, final byte[] content, final String charsetName) {
            return file(name, filename, content);
        }

        /**
         * Appends a file part from a source.
         *
         * @param name     file part name
         * @param filename filename reported in Content-Disposition
         * @param input    file source
         * @param length   declared length, or -1 when unknown
         * @return this builder
         */
        public Builder file(final String name, final String filename, final Source input, final long length) {
            ensureBodyMode(BodyMode.MULTIPART);
            parts().add(
                    MultipartBody.Part.file(
                            StringKit.isBlank(name) ? "file" : name,
                            stringValue(filename, "File name"),
                            Payload.source(require(input, "File source"), length),
                            MediaType.APPLICATION_OCTET_STREAM_TYPE));
            return this;
        }

        /**
         * Appends a file part from text using UTF-8.
         *
         * @param name     file part name
         * @param filename filename reported in Content-Disposition
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
         * @param filename    filename reported in Content-Disposition
         * @param content     file content
         * @param charsetName charset name
         * @return this builder
         */
        public Builder file(final String name, final String filename, final String content, final String charsetName) {
            final Charset charset = Charset.forName(validateText(charsetName, "Charset name"));
            return file(
                    name,
                    filename,
                    ByteString.encodeString(require(content, "File content"), charset).toByteArray());
        }

        /**
         * Appends a multipart part.
         *
         * @param part multipart part to append
         * @return this builder
         */
        public Builder part(final MultipartBody.Part part) {
            ensureBodyMode(BodyMode.MULTIPART);
            parts().add(require(part, "Part"));
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
            return body(TextBody.of(value));
        }

        /**
         * Sets a text body with explicit media.
         *
         * @param value body text
         * @param media content type assigned to the text body
         * @return this builder
         */
        public Builder body(final String value, final MediaType media) {
            return body(TextBody.of(value, media));
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
         * @param media content type assigned to the byte body
         * @return this builder
         */
        public Builder body(final byte[] value, final MediaType media) {
            return body(value).media(media);
        }

        /**
         * Sets a streaming body.
         *
         * @param input  body source
         * @param length declared length, or -1 when unknown
         * @return this builder
         */
        public Builder body(final Source input, final long length) {
            ensureBodyMode(BodyMode.BODY);
            this.body = Payload.source(require(input, "Body source"), length);
            this.media = MediaType.APPLICATION_OCTET_STREAM_TYPE;
            return this;
        }

        /**
         * Sets a streaming body with explicit media.
         *
         * @param input  body source
         * @param length declared length, or -1 when unknown
         * @param media  content type assigned to the streaming body
         * @return this builder
         */
        public Builder body(final Source input, final long length, final MediaType media) {
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
         * @param media content type assigned to the file body
         * @return this builder
         */
        public Builder body(final Path path, final MediaType media) {
            return body(FileBody.of(path, media));
        }

        /**
         * Sets a file as the complete request body.
         *
         * @param file file used as the complete request body
         * @return this builder
         */
        public Builder body(final File file) {
            return body(file, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        }

        /**
         * Sets a file as the complete request body.
         *
         * @param file  file used as the complete request body
         * @param media content type assigned to the file body
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
            return body(TextBody.of(value));
        }

        /**
         * Sets a JSON body.
         *
         * @param value JSON text
         * @return this builder
         */
        public Builder json(final String value) {
            return body(
                    TextBody.of(
                            value,
                            MediaType.APPLICATION_JSON_TYPE.withCharset(org.miaixz.bus.core.lang.Charset.UTF_8)));
        }

        /**
         * Sets a payload body.
         *
         * @param payload payload used as the complete request body
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
         * @param payload payload used as the complete request body
         * @param media   content type overriding the payload media type
         * @return this builder
         */
        public Builder body(final Payload payload, final MediaType media) {
            return body(payload).media(media);
        }

        /**
         * Encodes a value with the supplied codec and sets it as body.
         *
         * @param value object to encode as the request body
         * @param codec codec used to encode the object
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
         * Sets a prepared payload body.
         *
         * @param body payload body
         * @return this builder
         */
        public Builder body(final PayloadBody body) {
            final PayloadBody current = require(body, "Payload body");
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
            return body(PayloadBody.of(current.payload(), current.media()));
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
            if (value instanceof PayloadBody payloadBody) {
                return body(payloadBody);
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
            headers.set(Http.Header.SOAP_ACTION, soap.action());
            return body(soap.body());
        }

        /**
         * Sets body media.
         *
         * @param media content type assigned to the request body
         * @return this builder
         */
        public Builder media(final MediaType media) {
            this.media = require(media, "MediaType");
            return this;
        }

        /**
         * Sets codec.
         *
         * @param codec codec used for object request and response bodies
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
            Assert.isTrue(
                    start >= 0 && end >= 0 && end >= start,
                    () -> new ValidateException("Range bounds must be non-negative and ordered"));
            headers.set(Http.Header.RANGE, "bytes=" + start + Symbol.C_MINUS + end);
            return this;
        }

        /**
         * Sets progress listener.
         *
         * @param listener progress listener notified during body transfer
         * @return this builder
         */
        public Builder progress(final BiConsumer<Long, Long> listener) {
            this.progress = listener;
            return this;
        }

        /**
         * Sets tag.
         *
         * @param tag application tag attached to the request
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
         * @param username proxy authentication user name
         * @param password proxy authentication password
         * @return this builder
         */
        public Builder proxyCredentials(final String username, final String password) {
            return proxyAuthorization(HttpAuth.basic(username, password).applyProxy(Headers.empty()));
        }

        /**
         * Sets filter.
         *
         * @param filter message filter applied to the exchange
         * @return this builder
         */
        public Builder filter(final Filter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Sets guard.
         *
         * @param guard request guard applied before transport
         * @return this builder
         */
        public Builder guard(final GuardRule guard) {
            this.guard = guard;
            return this;
        }

        /**
         * Sets observer.
         *
         * @param observer event observer receiving exchange lifecycle events
         * @return this builder
         */
        public Builder observe(final EventObserver observer) {
            this.observer = observer == null ? EventObserver.noop() : observer;
            return this;
        }

        /**
         * Sets callback.
         *
         * @param callback terminal callback receiving the HTTP result
         * @return this builder
         */
        public Builder callback(final Callback<HttpResponse> callback) {
            this.callback = callback;
            return this;
        }

        /**
         * Sets all timeout values.
         *
         * @param timeout duration assigned to every timeout phase
         * @return this builder
         */
        public Builder timeout(final Duration timeout) {
            Assert.isTrue(
                    timeout != null && !timeout.isNegative(),
                    () -> new ValidateException("Timeout must be non-null and non-negative"));
            this.timeout = Timeout.of(timeout);
            return this;
        }

        /**
         * Sets timeout.
         *
         * @param timeout complete timeout policy to use
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
            return new HttpX(context, buildRequest(), callback, observer, filter, guard);
        }

        /**
         * Freezes the current request fields exactly once.
         *
         * @return request snapshot
         */
        private HttpRequest buildRequest() {
            final PayloadBody payloadBody = buildBody();
            final RequestCache cached = requestCache;
            final Headers reusableHeaders = cached == null ? null : cached.request.headers();
            final Headers requestHeaders = buildHeaders(payloadBody, reusableHeaders);
            if (cached != null && cached.context == context && cached.request.method() == method
                    && simpleTargetMatches(cached.request) && cached.request.headers() == requestHeaders
                    && cached.request.body() == payloadBody && cached.request.tag() == tag
                    && cached.request.proxy().equals(proxy) && cached.request.timeout().equals(timeout)) {
                return cached.request;
            }
            final UnoUrl target = buildUrl();
            if (cached != null && cached.context == context && cached.request.method() == method
                    && cached.request.url().toString().equals(target.toString())
                    && cached.request.headers() == requestHeaders && cached.request.body() == payloadBody
                    && cached.request.tag() == tag && cached.request.proxy().equals(proxy)
                    && cached.request.timeout().equals(timeout)) {
                return cached.request;
            }
            final HttpRequest request = HttpRequest.builder().method(method).url(target).headers(requestHeaders)
                    .body(payloadBody).tag(tag).proxy(proxy).timeout(timeout).build();
            final Protocol protocol = request.url().address().protocol();
            if (protocol != Protocol.HTTP && protocol != Protocol.HTTPS) {
                throw new ProtocolException("HTTP exchange requires http or https URL");
            }
            if (payloadBody == PayloadBody.empty() && tag == null && !requestHeaders.contains(Http.Header.AUTHORIZATION)
                    && !requestHeaders.contains(Http.Header.PROXY_AUTHORIZATION)
                    && !requestHeaders.contains(Http.Header.COOKIE)) {
                requestCache = new RequestCache(context, request);
            }
            return request;
        }

        /** Matches an unmodified absolute URL without reparsing it on repeated builder calls. */
        private boolean simpleTargetMatches(final HttpRequest request) {
            return url != null && baseUrl == null && (query == null || query.isEmpty())
                    && (pathSegments == null || pathSegments.isEmpty())
                    && (pathVariables == null || pathVariables.isEmpty()) && request.url().toString().equals(url);
        }

        /**
         * Builds a current HTTP request snapshot.
         *
         * @return request
         */
        public HttpRequest request() {
            return buildRequest();
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
            if (callback == null) {
                return HttpRunner.executeSync(context, buildRequest(), observer, filter, guard);
            }
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
         * @param body request body supplying content headers
         * @return headers
         */
        private Headers buildHeaders(final PayloadBody body, final Headers reusable) {
            final Headers current = headers.buildOrReuse(reusable);
            final long length = body.length();
            final boolean needsLength = length > 0 && !current.contains(Http.Header.CONTENT_LENGTH);
            final boolean needsType = length != 0 && !current.contains(Http.Header.CONTENT_TYPE);
            if (!needsLength && !needsType) {
                return current;
            }
            final Headers.Builder builder = current.newBuilder();
            if (needsLength) {
                builder.set(Http.Header.CONTENT_LENGTH, Long.toString(body.length()));
            }
            if (needsType) {
                builder.set(Http.Header.CONTENT_TYPE, body.media().value());
            }
            return builder.build();
        }

        /**
         * Builds request body.
         *
         * @return payload body
         */
        private PayloadBody buildBody() {
            Payload payload = body;
            MediaType selectedMedia = media;
            if (bodyMode == BodyMode.FORM && form != null) {
                final FormBody formBody = form.build();
                payload = formBody.payload();
                selectedMedia = formBody.media();
            } else if (bodyMode == BodyMode.MULTIPART) {
                final MultipartBody.Builder multipart = MultipartBody.builder();
                for (final MultipartBody.Part part : parts == null ? List.<MultipartBody.Part>of() : parts) {
                    multipart.part(part);
                }
                final MultipartBody multipartBody = multipart.build();
                payload = multipartBody.payload();
                selectedMedia = multipartBody.media();
            }
            if (codec != null && bodyMode == BodyMode.NONE) {
                selectedMedia = codec.media();
            }
            final PayloadBody payloadBody = PayloadBody.of(payload, selectedMedia, materializeMaxBytes);
            return progress == null ? payloadBody : payloadBody.progress(progress);
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
            if (pathVariables == null) {
                return target;
            }
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
            if (pathSegments == null || pathSegments.isEmpty()) {
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
            if (query == null || query.isEmpty()) {
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
         * @param value raw component text to percent-encode
         * @return encoded value
         */
        private static String encode(final String value) {
            return UrlEncoder.encodeAll(value, org.miaixz.bus.core.lang.Charset.UTF_8);
        }

        /**
         * Validates text input.
         *
         * @param value text to validate
         * @param name  diagnostic parameter name
         * @return the validated text
         */
        private static String validateText(final String value, final String name) {
            if (value == null || value.isEmpty()) {
                throw new ValidateException(name + " must be non-blank and single-line");
            }
            boolean nonWhitespace = false;
            for (int index = 0; index < value.length(); index++) {
                final char current = value.charAt(index);
                if (current == Symbol.C_CR || current == Symbol.C_LF) {
                    throw new ValidateException(name + " must be non-blank and single-line");
                }
                nonWhitespace |= !Character.isWhitespace(current);
            }
            if (!nonWhitespace) {
                throw new ValidateException(name + " must be non-blank and single-line");
            }
            return value;
        }

        /**
         * Converts a non-null value to a single-line string.
         *
         * @param value object to convert
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
     * Safe last-request cache scoped by context identity.
     *
     * @param context context identity that owns the request
     * @param request immutable request snapshot
     */
    private record RequestCache(Context context, HttpRequest request) {
    }

    /**
     * Query entry.
     *
     * @param name    query parameter name
     * @param value   query parameter value
     * @param encoded encoded flag
     */
    private record QueryEntry(String name, String value, boolean encoded) {

    }

}
