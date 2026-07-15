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
package org.miaixz.bus.fabric.protocol.http.cache;

import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.cache.CacheEntry;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;

/**
 * Encodes and decodes HTTP cache metadata.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class HttpCacheCodec {

    /**
     * Hidden constructor for HTTP cache metadata encoding helpers.
     */
    private HttpCacheCodec() {
        // No initialization required.
    }

    /**
     * Encodes an HTTP response into a protocol-neutral cache entry.
     *
     * @param request  request
     * @param response response
     * @return cache entry
     */
    static CacheEntry toEntry(final HttpRequest request, final HttpResponse response) {
        return toEntry(request, response, response.body().payload());
    }

    /**
     * Encodes an HTTP response with a supplied payload.
     *
     * @param request  request
     * @param response response
     * @param payload  payload
     * @return cache entry
     */
    static CacheEntry toEntry(final HttpRequest request, final HttpResponse response, final Payload payload) {
        final Headers.Builder metadata = Headers.builder()
                .add(Builder.HTTP_CACHE_CODEC_META_PROTOCOL, Builder.HTTP_CACHE_CODEC_META_PROTOCOL_HTTP)
                .add(Builder.HTTP_CACHE_CODEC_META_METHOD, request.method().name())
                .add(Builder.HTTP_CACHE_CODEC_META_URL, request.url().encoded())
                .add(Builder.HTTP_CACHE_CODEC_META_CODE, Integer.toString(response.code()))
                .add(Builder.HTTP_CACHE_CODEC_META_MESSAGE, response.message())
                .add(Builder.HTTP_CACHE_CODEC_META_RESPONSE_PROTOCOL, response.protocol().name())
                .add(Builder.HTTP_CACHE_CODEC_META_SENT_AT, Long.toString(response.sentRequestAtMillis()))
                .add(Builder.HTTP_CACHE_CODEC_META_RECEIVED_AT, Long.toString(response.receivedResponseAtMillis()))
                .add(Builder.HTTP_CACHE_CODEC_META_MEDIA, response.body().media().value());
        appendHeaders(
                metadata,
                request.headers(),
                Builder.HTTP_CACHE_CODEC_META_REQUEST_HEADER_NAME,
                Builder.HTTP_CACHE_CODEC_META_REQUEST_HEADER_VALUE);
        appendHeaders(
                metadata,
                response.headers(),
                Builder.HTTP_CACHE_CODEC_META_RESPONSE_HEADER_NAME,
                Builder.HTTP_CACHE_CODEC_META_RESPONSE_HEADER_VALUE);
        return CacheEntry.of(metadata.build(), payload);
    }

    /**
     * Decodes a protocol-neutral cache entry into an HTTP response.
     *
     * @param entry cache entry
     * @return response
     */
    static HttpResponse fromEntry(final CacheEntry entry) {
        final Headers metadata = entry.metadata();
        if (!Builder.HTTP_CACHE_CODEC_META_PROTOCOL_HTTP.equals(metadata.get(Builder.HTTP_CACHE_CODEC_META_PROTOCOL))) {
            throw new ProtocolException("Cache entry is not an HTTP entry");
        }
        final HttpRequest request = HttpRequest.builder()
                .method(HTTP.Method.of(requiredMetadata(metadata, Builder.HTTP_CACHE_CODEC_META_METHOD)))
                .url(UnoUrl.parse(requiredMetadata(metadata, Builder.HTTP_CACHE_CODEC_META_URL)))
                .headers(
                        readHeaders(
                                metadata,
                                Builder.HTTP_CACHE_CODEC_META_REQUEST_HEADER_NAME,
                                Builder.HTTP_CACHE_CODEC_META_REQUEST_HEADER_VALUE))
                .build();
        return HttpResponse.builder().request(request).code(readCode(metadata))
                .message(requiredMetadata(metadata, Builder.HTTP_CACHE_CODEC_META_MESSAGE))
                .headers(
                        readHeaders(
                                metadata,
                                Builder.HTTP_CACHE_CODEC_META_RESPONSE_HEADER_NAME,
                                Builder.HTTP_CACHE_CODEC_META_RESPONSE_HEADER_VALUE))
                .body(
                        PayloadBody.of(
                                entry.payload(),
                                MediaType.parse(requiredMetadata(metadata, Builder.HTTP_CACHE_CODEC_META_MEDIA))))
                .protocol(readProtocol(metadata))
                .sentRequestAtMillis(readLong(metadata, Builder.HTTP_CACHE_CODEC_META_SENT_AT))
                .receivedResponseAtMillis(readLong(metadata, Builder.HTTP_CACHE_CODEC_META_RECEIVED_AT)).build();
    }

    /**
     * Copies a response for another request.
     *
     * @param request  request
     * @param response response
     * @return copy
     */
    static HttpResponse copyResponse(final HttpRequest request, final HttpResponse response) {
        return copyResponse(request, response, copyBody(response));
    }

    /**
     * Copies a response for another request with a supplied body.
     *
     * @param request  request
     * @param response response
     * @param body     body
     * @return copy
     */
    static HttpResponse copyResponse(final HttpRequest request, final HttpResponse response, final PayloadBody body) {
        return response.toBuilder().request(request).body(body).build();
    }

    /**
     * Copies response body reference.
     *
     * @param response response
     * @return body
     */
    static PayloadBody copyBody(final HttpResponse response) {
        return response.body();
    }

    /**
     * Merges cached and network headers for a 304 response.
     *
     * @param cached  cached headers
     * @param network network headers
     * @return merged headers
     */
    static Headers mergeHeaders(final Headers cached, final Headers network) {
        final Headers.Builder merged = cached.newBuilder();
        for (final Map.Entry<String, List<String>> entry : network.asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                merged.set(entry.getKey(), value);
            }
        }
        return merged.build();
    }

    /**
     * Stores ordered HTTP headers as paired name/value metadata fields.
     *
     * @param target     metadata target
     * @param headers    headers to encode
     * @param nameField  metadata field used for header names
     * @param valueField metadata field used for header values
     */
    private static void appendHeaders(
            final Headers.Builder target,
            final Headers headers,
            final String nameField,
            final String valueField) {
        for (int i = 0; i < headers.size(); i++) {
            target.add(nameField, headers.name(i));
            target.add(valueField, headers.value(i));
        }
    }

    /**
     * Reconstructs ordered HTTP headers from paired name/value metadata fields.
     *
     * @param metadata   cache entry metadata
     * @param nameField  metadata field used for header names
     * @param valueField metadata field used for header values
     * @return reconstructed headers
     */
    private static Headers readHeaders(final Headers metadata, final String nameField, final String valueField) {
        final Headers.Builder builder = Headers.builder();
        String currentName = null;
        for (int i = 0; i < metadata.size(); i++) {
            final String name = metadata.name(i);
            if (nameField.equalsIgnoreCase(name)) {
                currentName = metadata.value(i);
            } else if (valueField.equalsIgnoreCase(name) && currentName != null) {
                builder.add(currentName, metadata.value(i));
                currentName = null;
            }
        }
        return builder.build();
    }

    /**
     * Reads a required metadata value and fails fast when a cache entry is not decodable as HTTP.
     *
     * @param metadata cache entry metadata
     * @param name     metadata field name
     * @return metadata value
     */
    private static String requiredMetadata(final Headers metadata, final String name) {
        final String value = metadata.get(name);
        return Assert.notNull(value, () -> new ProtocolException("Cache entry missing metadata " + name));
    }

    /**
     * Reads the stored HTTP status code from cache metadata.
     *
     * @param metadata cache entry metadata
     * @return HTTP status code
     */
    private static int readCode(final Headers metadata) {
        try {
            return Integer.parseInt(requiredMetadata(metadata, Builder.HTTP_CACHE_CODEC_META_CODE));
        } catch (final NumberFormatException e) {
            throw new ProtocolException("Invalid cached HTTP status code", e);
        }
    }

    /**
     * Reads the response protocol stored with the cached response.
     *
     * @param metadata cache entry metadata
     * @return protocol, or {@code null} when older metadata did not store one
     */
    private static Protocol readProtocol(final Headers metadata) {
        final String value = metadata.get(Builder.HTTP_CACHE_CODEC_META_RESPONSE_PROTOCOL);
        if (value == null) {
            return null;
        }
        try {
            return Protocol.valueOf(value);
        } catch (final IllegalArgumentException e) {
            throw new ProtocolException("Invalid cached HTTP protocol", e);
        }
    }

    /**
     * Reads a non-negative millisecond timestamp from cache metadata.
     *
     * @param metadata cache entry metadata
     * @param name     metadata field name
     * @return timestamp value, or {@code 0} when absent
     */
    private static long readLong(final Headers metadata, final String name) {
        final String value = metadata.get(name);
        if (value == null) {
            return 0L;
        }
        try {
            final long parsed = Long.parseLong(value);
            Assert.isFalse(parsed < 0, () -> new ProtocolException("Cached HTTP timestamp must be non-negative"));
            return parsed;
        } catch (final NumberFormatException e) {
            throw new ProtocolException("Invalid cached HTTP timestamp", e);
        }
    }

}
