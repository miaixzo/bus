/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.storage;

import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.accord.ConnectionPool;
import org.miaixz.bus.http.bodys.RequestBody;
import org.miaixz.bus.http.metric.Interceptor;
import org.miaixz.bus.http.metric.NewChain;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import software.amazon.awssdk.http.*;
import software.amazon.awssdk.http.async.AsyncExecuteRequest;
import software.amazon.awssdk.http.async.SdkHttpContentPublisher;
import software.amazon.awssdk.utils.AttributeMap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * S3 HTTP client implementation.
 * <p>
 * This client is compatible with the generic S3 protocol and does not depend on a specific storage service type.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ClientX implements SdkHttpClient {

    /**
     * HTTP client instance.
     */
    private final Httpd httpd;
    /**
     * Thread pool executor for asynchronous operations.
     */
    private final ExecutorService executor;

    /**
     * Constructs a {@code ClientX} instance with the provided builder.
     *
     * @param clientBuilder The builder used to configure the client.
     */
    private ClientX(ClientBuilder clientBuilder) {
        this.httpd = clientBuilder.httpdBuilder.build();
        this.executor = clientBuilder.executor != null ? clientBuilder.executor : Executors.newCachedThreadPool();
    }

    /**
     * Prepares an executable HTTP request from an {@link HttpExecuteRequest}.
     *
     * @param request The HTTP execution request to prepare.
     * @return An {@link ExecutableHttpRequest} that can be called.
     */
    @Override
    public ExecutableHttpRequest prepareRequest(HttpExecuteRequest request) {
        return new HttpExecutableHttpRequest(request);
    }

    /**
     * Returns the name of this client.
     *
     * @return The client name.
     */
    @Override
    public String clientName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Closes the client and releases all associated resources. This method shuts down the executor services and evicts
     * connections from the connection pool.
     */
    @Override
    public void close() {
        // Clean up resources
        httpd.dispatcher().executorService().shutdown();
        httpd.connectionPool().evictAll();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Executes an asynchronous request.
     * <p>
     * Note: This is a custom asynchronous request handling method, not an overridden interface method.
     * </p>
     *
     * @param asyncRequest The asynchronous execution request.
     * @return A {@link CompletableFuture} that completes when the request is finished.
     */
    public CompletableFuture<Void> executeAsync(AsyncExecuteRequest asyncRequest) {
        SdkHttpRequest sdkRequest = asyncRequest.request();
        // Build the request
        Request.Builder requestBuilder = new Request.Builder().url(sdkRequest.getUri().toString())
                .method(sdkRequest.method().name(), createRequestBodyFromAsync(asyncRequest));

        // Add request headers
        sdkRequest.headers().forEach((name, values) -> {
            for (String value : values) {
                requestBuilder.addHeader(name, value);
            }
        });

        CompletableFuture<Void> future = new CompletableFuture<>();

        // Execute the request asynchronously
        httpd.newCall(requestBuilder.build()).enqueue(new Callback() {

            @Override
            public void onFailure(NewCall call, IOException e) {
                // Notify the handler of the error
                asyncRequest.responseHandler().onError(e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(NewCall call, Response response) {
                try {
                    // Build AWS SDK response
                    SdkHttpResponse sdkResponse = buildSdkHttpResponse(response);

                    // Notify the response handler of headers
                    asyncRequest.responseHandler().onHeaders(sdkResponse);

                    if (response.body() != null) {
                        // Create ByteBuffer publisher
                        Publisher<ByteBuffer> publisher = new ByteBufferPublisher(response.body().byteStream());
                        asyncRequest.responseHandler().onStream(publisher);
                    }

                    // Request processing completed
                    future.complete(null);
                } catch (Exception e) {
                    // Notify the handler of the error
                    asyncRequest.responseHandler().onError(e);
                    future.completeExceptionally(e);
                } finally {
                    response.close();
                }
            }
        });

        return future;
    }

    /**
     * Builds an AWS SDK HTTP response from an {@link Response}.
     *
     * @param response The HTTP response from the underlying client.
     * @return The constructed {@link SdkHttpResponse}.
     */
    private SdkHttpResponse buildSdkHttpResponse(Response response) {
        // Use SdkHttpResponse.builder() to create a response builder
        SdkHttpResponse.Builder responseBuilder = SdkHttpResponse.builder().statusCode(response.code());

        // Set status text (if present)
        Optional<String> statusText = Optional.ofNullable(response.message());
        statusText.ifPresent(responseBuilder::statusText);

        // Add response headers
        for (Map.Entry<String, java.util.List<String>> entry : response.headers().toMultimap().entrySet()) {
            responseBuilder.putHeader(entry.getKey(), entry.getValue());
        }

        return responseBuilder.build();
    }

    /**
     * Creates a {@link RequestBody} from an {@link AsyncExecuteRequest}.
     *
     * @param asyncRequest The asynchronous execution request.
     * @return The {@link RequestBody} if content is present, otherwise {@code null}.
     */
    private RequestBody createRequestBodyFromAsync(AsyncExecuteRequest asyncRequest) {
        // Use asyncRequest.request() to get SdkHttpRequest
        SdkHttpRequest sdkRequest = asyncRequest.request();

        // Check if there is a request content publisher
        SdkHttpContentPublisher contentPublisher = asyncRequest.requestContentPublisher();

        if (contentPublisher != null) {
            // Create a custom asynchronous RequestBody
            return new AsyncContentPublisherWrapper(contentPublisher, sdkRequest);
        }

        // If no request body, return null
        return null;
    }

    /**
     * Implementation of {@link ExecutableHttpRequest} for this client.
     */
    private class HttpExecutableHttpRequest implements ExecutableHttpRequest {

        /**
         * The HTTP call instance.
         */
        private final NewCall call;
        /**
         * Flag indicating if the request has been aborted.
         */
        private final AtomicBoolean aborted = new AtomicBoolean(false);

        /**
         * Constructs an {@code HttpExecutableHttpRequest}.
         *
         * @param request The HTTP execution request.
         */
        HttpExecutableHttpRequest(HttpExecuteRequest request) {
            // Pre-build the request but do not execute immediately
            SdkHttpRequest sdkRequest = request.httpRequest();
            Request.Builder requestBuilder = new Request.Builder().url(sdkRequest.getUri().toString())
                    .method(sdkRequest.method().name(), createRequestBody(request));

            // Add request headers
            sdkRequest.headers().forEach((name, values) -> {
                for (String value : values) {
                    requestBuilder.addHeader(name, value);
                }
            });

            this.call = httpd.newCall(requestBuilder.build());
        }

        /**
         * Executes the HTTP request.
         *
         * @return The HTTP execution response.
         * @throws IOException If the request execution fails.
         */
        @Override
        public HttpExecuteResponse call() throws IOException {
            if (aborted.get()) {
                throw new IOException("Request has been aborted");
            }

            Response response = call.execute();

            // Build AWS SDK response
            SdkHttpResponse sdkResponse = buildSdkHttpResponse(response);

            // Create response body
            AbortableInputStream responseBody = response.body() != null
                    ? AbortableInputStream.create(response.body().byteStream())
                    : null;

            // Return HttpExecuteResponse
            return HttpExecuteResponse.builder().response(sdkResponse).responseBody(responseBody).build();
        }

        /**
         * Aborts the HTTP request.
         */
        @Override
        public void abort() {
            if (aborted.compareAndSet(false, true)) {
                call.cancel();
            }
        }

        /**
         * Creates a {@link RequestBody} from an {@link HttpExecuteRequest}.
         *
         * @param request The HTTP execution request.
         * @return The {@link RequestBody} if content is present, otherwise {@code null}.
         */
        private RequestBody createRequestBody(HttpExecuteRequest request) {
            // Use request.contentStreamProvider() to get request content
            Optional<ContentStreamProvider> contentStreamProviderOpt = request.contentStreamProvider();
            if (contentStreamProviderOpt.isPresent()) {
                ContentStreamProvider contentStreamProvider = contentStreamProviderOpt.get();
                // Create a custom RequestBody to ensure correct content length handling
                return new CustomRequestBody(contentStreamProvider, request.httpRequest());
            }
            return null;
        }
    }

    /**
     * Custom {@link RequestBody} implementation to ensure correct content length and Content-Type handling.
     * <p>
     * Optimizes performance by reducing memory allocation and using streaming.
     * </p>
     */
    private static class CustomRequestBody extends RequestBody {

        /**
         * The content stream provider.
         */
        private final ContentStreamProvider contentStreamProvider;
        /**
         * The HTTP request.
         */
        private final SdkHttpRequest httpRequest;
        /**
         * The content length, calculated lazily.
         */
        private volatile Long contentLength;

        /**
         * Constructs a {@code CustomRequestBody}.
         *
         * @param contentStreamProvider The content stream provider.
         * @param httpRequest           The HTTP request.
         */
        CustomRequestBody(ContentStreamProvider contentStreamProvider, SdkHttpRequest httpRequest) {
            this.contentStreamProvider = contentStreamProvider;
            this.httpRequest = httpRequest;
            // Content length is calculated lazily, only when needed.
        }

        /**
         * Returns the media type of the request body.
         *
         * @return The {@link MediaType} of the content.
         */
        @Override
        public MediaType contentType() {
            // Get Content-Type from request headers, or use a default value if not present.
            Optional<String> contentTypeOpt = httpRequest.firstMatchingHeader("Content-Type");
            return contentTypeOpt.map(MediaType::valueOf).orElse(MediaType.valueOf("application/octet-stream"));
        }

        /**
         * Writes the request body to the provided {@link BufferSink}.
         *
         * @param sink The buffer sink to write to.
         * @throws IOException If an I/O error occurs during writing.
         */
        @Override
        public void writeTo(BufferSink sink) throws IOException {
            // Use streaming to avoid loading the entire file into memory.
            try (InputStream inputStream = contentStreamProvider.newStream()) {
                byte[] buffer = new byte[8192]; // 8KB buffer
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    sink.write(buffer, 0, bytesRead);
                }
            }
        }

        /**
         * Returns the content length of the request body.
         *
         * @return The content length, or -1 if unknown.
         * @throws IOException If an I/O error occurs while calculating the content length.
         */
        @Override
        public long contentLength() throws IOException {
            if (contentLength == null) {
                synchronized (this) {
                    if (contentLength == null) {
                        // Calculate only once, then cache the result.
                        contentLength = calculateContentLength();
                    }
                }
            }
            return contentLength;
        }

        /**
         * Calculates the content length of the request body.
         *
         * @return The calculated content length.
         * @throws IOException If an I/O error occurs during calculation.
         */
        private long calculateContentLength() throws IOException {
            // Attempt to get content length from request headers
            Optional<String> contentLengthOpt = httpRequest.firstMatchingHeader("Content-Length");
            if (contentLengthOpt.isPresent()) {
                try {
                    return Long.parseLong(contentLengthOpt.get());
                } catch (NumberFormatException e) {
                    // If parsing fails, continue to calculate
                }
            }

            // If unable to get from headers, calculate actual content length
            try (InputStream inputStream = contentStreamProvider.newStream()) {
                long length = 0;
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    length += bytesRead;
                }
                return length;
            }
        }
    }

    /**
     * An asynchronous content publisher wrapper that converts AWS SDK's {@link SdkHttpContentPublisher} to
     * {@link RequestBody}.
     */
    private static class AsyncContentPublisherWrapper extends RequestBody {

        /**
         * The content publisher.
         */
        private final SdkHttpContentPublisher contentPublisher;
        /**
         * The HTTP request.
         */
        private final SdkHttpRequest httpRequest;
        /**
         * The content length, calculated lazily.
         */
        private volatile Long contentLength;

        /**
         * Constructs an {@code AsyncContentPublisherWrapper}.
         *
         * @param contentPublisher The content publisher.
         * @param httpRequest      The HTTP request.
         */
        AsyncContentPublisherWrapper(SdkHttpContentPublisher contentPublisher, SdkHttpRequest httpRequest) {
            this.contentPublisher = contentPublisher;
            this.httpRequest = httpRequest;
        }

        /**
         * Returns the media type of the request body.
         *
         * @return The {@link MediaType} of the content.
         */
        @Override
        public MediaType contentType() {
            // Get Content-Type from request headers, or use a default value if not present.
            Optional<String> contentTypeOpt = httpRequest.firstMatchingHeader("Content-Type");
            return contentTypeOpt.map(MediaType::valueOf).orElse(MediaType.valueOf("application/octet-stream"));
        }

        /**
         * Writes the request body to the provided {@link BufferSink}.
         *
         * @param sink The buffer sink to write to.
         * @throws IOException If an I/O error occurs during writing.
         */
        @Override
        public void writeTo(BufferSink sink) throws IOException {
            try {
                // Write the content from SdkHttpContentPublisher to BufferSink
                CompletableFuture<Void> future = new CompletableFuture<>();

                // Create a subscriber to receive data
                Subscriber<ByteBuffer> subscriber = new Subscriber<>() {

                    /**
                     * The subscription to the publisher.
                     */
                    private Subscription subscription;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.subscription = s;
                        // Request the first data block
                        s.request(1);
                    }

                    @Override
                    public void onNext(ByteBuffer buffer) {
                        try {
                            // Write ByteBuffer to sink
                            if (buffer.hasArray()) {
                                sink.write(buffer.array(), buffer.arrayOffset(), buffer.remaining());
                            } else {
                                byte[] bytes = new byte[buffer.remaining()];
                                buffer.get(bytes);
                                sink.write(bytes);
                            }
                            // Request the next data block
                            subscription.request(1);
                        } catch (IOException e) {
                            subscription.cancel();
                            future.completeExceptionally(e);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }

                    @Override
                    public void onComplete() {
                        future.complete(null);
                    }
                };

                // Subscribe to the SdkHttpContentPublisher
                contentPublisher.subscribe(subscriber);

                // Wait for writing to complete
                future.get();
            } catch (Exception e) {
                if (e instanceof IOException) {
                    throw (IOException) e;
                }
                throw new IOException("Failed to write async request body", e);
            }
        }

        /**
         * Returns the content length of the request body.
         *
         * @return The content length, or -1 if unknown.
         */
        @Override
        public long contentLength() {
            if (contentLength == null) {
                synchronized (this) {
                    if (contentLength == null) {
                        // Attempt to get content length from request headers
                        Optional<String> contentLengthOpt = httpRequest.firstMatchingHeader("Content-Length");
                        if (contentLengthOpt.isPresent()) {
                            try {
                                contentLength = Long.parseLong(contentLengthOpt.get());
                                return contentLength;
                            } catch (NumberFormatException e) {
                                // If parsing fails, continue to calculate
                            }
                        }

                        // If unable to get from headers, try to get from SdkHttpContentPublisher
                        try {
                            Optional<Long> publisherContentLength = contentPublisher.contentLength();
                            if (publisherContentLength.isPresent()) {
                                contentLength = publisherContentLength.get();
                                return contentLength;
                            }
                        } catch (Exception e) {
                            // If fetching fails, return -1 for unknown length
                            contentLength = -1L;
                        }
                    }
                }
            }
            return contentLength != null ? contentLength : -1L;
        }
    }

    /**
     * {@link Publisher} implementation for {@link ByteBuffer} that converts an {@link InputStream} into a stream of
     * {@link ByteBuffer}s.
     * <p>
     * Optimizes performance by reducing memory allocation and using efficient buffer management.
     * </p>
     */
    private static class ByteBufferPublisher implements Publisher<ByteBuffer> {

        /**
         * The input stream to read from.
         */
        private final InputStream inputStream;
        /**
         * The size of the buffer to use for reading.
         */
        private final int bufferSize;
        /**
         * The executor service for emitting data.
         */
        private final ExecutorService executorService;

        /**
         * Constructs a {@code ByteBufferPublisher} with a default buffer size.
         *
         * @param inputStream The input stream.
         */
        ByteBufferPublisher(InputStream inputStream) {
            this(inputStream, 8192);
        }

        /**
         * Constructs a {@code ByteBufferPublisher} with a specified buffer size.
         *
         * @param inputStream The input stream.
         * @param bufferSize  The size of the buffer.
         */
        ByteBufferPublisher(InputStream inputStream, int bufferSize) {
            this.inputStream = inputStream;
            this.bufferSize = bufferSize;
            this.executorService = Executors.newSingleThreadExecutor();
        }

        /**
         * Subscribes the given {@link Subscriber} to this publisher.
         *
         * @param subscriber The subscriber to subscribe.
         */
        @Override
        public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
            subscriber.onSubscribe(new Subscription() {

                /** Flag indicating if the subscription has been cancelled. */
                private volatile boolean cancelled = false;
                /** The amount of data requested by the subscriber. */
                private volatile long requested = 0;
                /** Lock object for synchronizing the requested count. */
                private final Object lock = new Object();

                @Override
                public void request(long n) {
                    if (n <= 0 || cancelled) {
                        return;
                    }

                    synchronized (lock) {
                        requested += n;
                        if (requested > 0) {
                            // Execute data emission in a separate thread to avoid blocking the calling thread.
                            executorService.submit(this::emitData);
                        }
                    }
                }

                @Override
                public void cancel() {
                    if (!cancelled) {
                        cancelled = true;
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            // Ignore close exceptions
                        }
                        executorService.shutdown();
                    }
                }

                /**
                 * Emits data from the input stream to the subscriber.
                 */
                private void emitData() {
                    try {
                        byte[] buffer = new byte[bufferSize];
                        while (!cancelled && requested > 0) {
                            int bytesRead = inputStream.read(buffer);
                            if (bytesRead == -1) {
                                // End of stream, notify completion
                                subscriber.onComplete();
                                executorService.shutdown();
                                return;
                            }

                            synchronized (lock) {
                                if (requested > 0) {
                                    requested--;
                                    subscriber.onNext(ByteBuffer.wrap(buffer, 0, bytesRead));
                                } else {
                                    // If no more data is requested, stop sending
                                    break;
                                }
                            }
                        }
                    } catch (IOException e) {
                        subscriber.onError(e);
                        executorService.shutdown();
                    }
                }
            });
        }
    }

    /**
     * Builder pattern for {@code ClientX}.
     */
    public static class ClientBuilder implements SdkHttpClient.Builder<ClientBuilder> {

        /**
         * The HTTP client builder.
         */
        private Httpd.Builder httpdBuilder;
        /**
         * The executor service for callbacks.
         */
        private ExecutorService executor;

        /**
         * Constructs a new {@code ClientBuilder} with default settings.
         */
        public ClientBuilder() {
            this.httpdBuilder = new Httpd.Builder().connectTimeout(Duration.ofSeconds(10))
                    .readTimeout(Duration.ofSeconds(30)).writeTimeout(Duration.ofSeconds(30))
                    .retryOnConnectionFailure(true)
                    // Optimize connection pool configuration
                    .connectionPool(new ConnectionPool(20, 5, TimeUnit.MINUTES))
                    // Add generic S3 protocol compatibility configuration
                    .addInterceptor(new S3CompatibilityInterceptor());
        }

        /**
         * Sets the connection timeout for the HTTP client.
         *
         * @param timeout The connection timeout duration.
         * @return This builder instance.
         */
        public ClientBuilder connectTimeout(Duration timeout) {
            httpdBuilder.connectTimeout(timeout);
            return this;
        }

        /**
         * Sets the read timeout for the HTTP client.
         *
         * @param timeout The read timeout duration.
         * @return This builder instance.
         */
        public ClientBuilder readTimeout(Duration timeout) {
            httpdBuilder.readTimeout(timeout);
            return this;
        }

        /**
         * Sets the write timeout for the HTTP client.
         *
         * @param timeout The write timeout duration.
         * @return This builder instance.
         */
        public ClientBuilder writeTimeout(Duration timeout) {
            httpdBuilder.writeTimeout(timeout);
            return this;
        }

        /**
         * Sets the call timeout for the HTTP client.
         *
         * @param timeout The call timeout duration.
         * @return This builder instance.
         */
        public ClientBuilder callTimeout(Duration timeout) {
            httpdBuilder.callTimeout(timeout);
            return this;
        }

        /**
         * Adds an interceptor to the HTTP client.
         *
         * @param interceptor The interceptor to add.
         * @return This builder instance.
         */
        public ClientBuilder addInterceptor(Interceptor interceptor) {
            httpdBuilder.addInterceptor(interceptor);
            return this;
        }

        /**
         * Adds a network interceptor to the HTTP client.
         *
         * @param interceptor The network interceptor to add.
         * @return This builder instance.
         */
        public ClientBuilder addNetworkInterceptor(Interceptor interceptor) {
            httpdBuilder.addNetworkInterceptor(interceptor);
            return this;
        }

        /**
         * Sets the connection pool for the HTTP client.
         *
         * @param connectionPool The connection pool to use.
         * @return This builder instance.
         */
        public ClientBuilder connectionPool(ConnectionPool connectionPool) {
            httpdBuilder.connectionPool(connectionPool);
            return this;
        }

        /**
         * Sets whether the HTTP client should retry on connection failure.
         *
         * @param retry {@code true} to retry on connection failure, {@code false} otherwise.
         * @return This builder instance.
         */
        public ClientBuilder retryOnConnectionFailure(boolean retry) {
            httpdBuilder.retryOnConnectionFailure(retry);
            return this;
        }

        /**
         * Sets the executor service for callbacks.
         *
         * @param callbackExecutor The executor service for callbacks.
         * @return This builder instance.
         */
        public ClientBuilder callbackExecutor(ExecutorService callbackExecutor) {
            this.executor = callbackExecutor;
            return this;
        }

        /**
         * Builds a {@code ClientX} instance with default attributes.
         *
         * @param attributeMap The attribute map.
         * @return A new {@code ClientX} instance.
         */
        @Override
        public ClientX buildWithDefaults(AttributeMap attributeMap) {
            return new ClientX(this);
        }

        /**
         * Builds a {@code ClientX} instance.
         *
         * @return A new {@code ClientX} instance.
         */
        public ClientX build() {
            return new ClientX(this);
        }
    }

    /**
     * S3 protocol compatibility interceptor.
     * <p>
     * This interceptor ensures generic S3 protocol compatibility and does not depend on specific storage service type
     * detection.
     * </p>
     */
    private static class S3CompatibilityInterceptor implements Interceptor {

        /**
         * Intercepts the request to add S3 protocol compatibility headers.
         *
         * @param chain The request chain.
         * @return The response from the next interceptor in the chain.
         * @throws IOException If an I/O error occurs during request processing.
         */
        @Override
        public Response intercept(NewChain chain) throws IOException {
            Request request = chain.request();

            // Get the original request builder
            Request.Builder requestBuilder = request.newBuilder();

            // Ensure Content-Type header exists
            if (request.header("Content-Type") == null) {
                requestBuilder.addHeader("Content-Type", "application/octet-stream");
            }

            // Ensure Content-Length header exists
            if (request.body() != null && request.header("Content-Length") == null) {
                try {
                    // For custom RequestBody, we have already implemented the contentLength() method
                    long length = request.body().contentLength();
                    if (length != -1) {
                        requestBuilder.addHeader("Content-Length", String.valueOf(length));
                    }
                } catch (IOException e) {
                    // If content length cannot be obtained, ignore the error
                }
            }

            // Add generic S3 protocol headers
            addS3ProtocolHeaders(requestBuilder, request);

            // Build the new request
            Request newRequest = requestBuilder.build();

            // Proceed with the request chain
            return chain.proceed(newRequest);
        }

        /**
         * Adds generic S3 protocol headers to the request.
         *
         * @param requestBuilder The request builder.
         * @param request        The original request.
         */
        private void addS3ProtocolHeaders(Request.Builder requestBuilder, Request request) {
            // Ensure Host header is correct
            if (request.header("Host") == null) {
                requestBuilder.addHeader("Host", request.url().host());
            }

            // Ensure Date header exists
            if (request.header("Date") == null) {
                requestBuilder.addHeader(
                        "Date",
                        DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)));
            }

            // Add generic Content-SHA256 header
            // AWS S3 format is used as default here, as most S3 compatible services support this format
            if (request.header("x-amz-content-sha256") == null) {
                requestBuilder.addHeader("x-amz-content-sha256", "UNSIGNED-PAYLOAD");
            }

            // Check if file overwrite is allowed
            boolean shouldOverwrite = shouldAllowOverwrite(request);

            // If overwrite is not allowed, add a header to forbid it
            // Note: Not all S3 compatible services support this header, but it is useful for those that do.
            if (!shouldOverwrite && request.header("x-oss-forbid-overwrite") == null) {
                requestBuilder.addHeader("x-oss-forbid-overwrite", "true");
            }
        }

        /**
         * Determines whether overwriting a file is allowed based on the request.
         *
         * @param request The HTTP request.
         * @return {@code true} if overwriting is allowed, {@code false} otherwise.
         */
        private boolean shouldAllowOverwrite(Request request) {
            // If the request method is PUT, it usually indicates an upload or update operation, so overwriting should
            // be allowed.
            if ("PUT".equals(request.method())) {
                return true;
            }

            // Check if the URL contains specific parameters or path patterns
            String url = request.url().toString();

            // If the URL contains "overwrite=true" parameter, allow overwriting
            if (url.contains("overwrite=true")) {
                return true;
            }

            // If the URL contains specific path patterns, allow overwriting
            if (url.contains("/update/") || url.contains("/replace/")) {
                return true;
            }

            // By default, overwriting is not allowed
            return false;
        }
    }

}
