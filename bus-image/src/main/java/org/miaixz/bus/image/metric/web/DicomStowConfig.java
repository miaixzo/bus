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
package org.miaixz.bus.image.metric.web;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.core.lang.MediaType;

/**
 * Configuration for DICOM STOW-RS operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DicomStowConfig {

    /**
     * The default user agent value.
     */
    private static final String DEFAULT_USER_AGENT = "bus-image STOW-RS Client";

    /**
     * The default thread pool size value.
     */
    private static final int DEFAULT_THREAD_POOL_SIZE = 5;

    /**
     * The default connect timeout value.
     */
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);

    /**
     * The request url value.
     */
    private final String requestUrl;

    /**
     * The content type value.
     */
    private final String contentType;

    /**
     * The user agent value.
     */
    private final String userAgent;

    /**
     * The headers value.
     */
    private final Map<String, String> headers;

    /**
     * The thread pool size value.
     */
    private final int threadPoolSize;

    /**
     * The connect timeout value.
     */
    private final Duration connectTimeout;

    /**
     * Creates a new instance.
     *
     * @param builder the builder.
     */
    private DicomStowConfig(Builder builder) {
        this.requestUrl = normalizeUrl(builder.requestUrl);
        this.contentType = builder.contentType;
        this.userAgent = builder.userAgent;
        this.headers = Map.copyOf(builder.headers);
        this.threadPoolSize = builder.threadPoolSize;
        this.connectTimeout = builder.connectTimeout;
    }

    /**
     * Builds the er.
     *
     * @return the operation result.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the request url.
     *
     * @return the request url.
     */
    public String getRequestUrl() {
        return requestUrl;
    }

    /**
     * Gets the content type.
     *
     * @return the content type.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the user agent.
     *
     * @return the user agent.
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Gets the headers.
     *
     * @return the headers.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Gets the thread pool size.
     *
     * @return the thread pool size.
     */
    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    /**
     * Gets the connect timeout.
     *
     * @return the connect timeout.
     */
    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Executes the normalize url operation.
     *
     * @param url the url.
     * @return the operation result.
     */
    private String normalizeUrl(String url) {
        Objects.requireNonNull(url, "Request URL cannot be null");
        String normalized = url.trim();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (!normalized.endsWith("/studies")) {
            normalized += "/studies";
        }
        return normalized;
    }

    /**
     * Represents the Builder type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * The request url value.
         */
        private String requestUrl;

        /**
         * The content type value.
         */
        private String contentType = MediaType.APPLICATION_DICOM;

        /**
         * The user agent value.
         */
        private String userAgent = DEFAULT_USER_AGENT;

        /**
         * The headers value.
         */
        private final Map<String, String> headers = new HashMap<>();

        /**
         * The thread pool size value.
         */
        private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;

        /**
         * The connect timeout value.
         */
        private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;

        /**
         * Creates a new instance.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Executes the request url operation.
         *
         * @param requestUrl the request url.
         * @return the operation result.
         */
        public Builder requestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
            return this;
        }

        /**
         * Executes the content type operation.
         *
         * @param contentType the content type.
         * @return the operation result.
         */
        public Builder contentType(String contentType) {
            this.contentType = Objects.requireNonNull(contentType, "Content type cannot be null");
            return this;
        }

        /**
         * Executes the user agent operation.
         *
         * @param userAgent the user agent.
         * @return the operation result.
         */
        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent != null ? userAgent : DEFAULT_USER_AGENT;
            return this;
        }

        /**
         * Executes the header operation.
         *
         * @param name  the name.
         * @param value the value.
         * @return the operation result.
         */
        public Builder header(String name, String value) {
            Objects.requireNonNull(name, "Header name cannot be null");
            if (value != null) {
                headers.put(name, value);
            }
            return this;
        }

        /**
         * Executes the headers operation.
         *
         * @param headers the headers.
         * @return the operation result.
         */
        public Builder headers(Map<String, String> headers) {
            if (headers != null) {
                this.headers.putAll(headers);
            }
            return this;
        }

        /**
         * Executes the thread pool size operation.
         *
         * @param threadPoolSize the thread pool size.
         * @return the operation result.
         */
        public Builder threadPoolSize(int threadPoolSize) {
            if (threadPoolSize <= 0) {
                throw new IllegalArgumentException("Thread pool size must be positive");
            }
            this.threadPoolSize = threadPoolSize;
            return this;
        }

        /**
         * Executes the connect timeout operation.
         *
         * @param connectTimeout the connect timeout.
         * @return the operation result.
         */
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = Objects.requireNonNull(connectTimeout);
            return this;
        }

        /**
         * Executes the build operation.
         *
         * @return the operation result.
         */
        public DicomStowConfig build() {
            Objects.requireNonNull(requestUrl, "Request URL is required");
            return new DicomStowConfig(this);
        }

    }

}
