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
package org.miaixz.bus.auth.protocol.scim.codec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.protocol.scim.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;

/**
 * Strictly encodes RFC 7644 User/Group search ListResponse representations for a SCIM Service Provider.
 *
 * @author Kimi Liu
 */
public final class ScimListResponseCodec {

    /**
     * Runtime-selected provider-neutral JSON implementation.
     */
    private final JsonProvider jsonProvider;

    /**
     * Maximum encoded response body bytes.
     */
    private final long maximumBytes;

    /**
     * Maximum encoded JSON nesting depth reserved for the shared codec contract.
     */
    private final int maximumDepth;

    /**
     * Creates a strict ListResponse codec with explicit limits.
     *
     * @param jsonProvider externally selected JSON provider
     * @param maximumBytes positive maximum body bytes
     * @param maximumDepth positive maximum JSON container depth
     * @throws IllegalArgumentException if the provider is {@code null}
     * @throws ValidateException        if a limit is not positive
     */
    public ScimListResponseCodec(final JsonProvider jsonProvider, final long maximumBytes, final int maximumDepth) {
        this.jsonProvider = Assert.notNull(jsonProvider, "SCIM ListResponse JSON provider must not be null");
        if (maximumBytes <= 0 || maximumDepth <= 0) {
            throw new ValidateException("SCIM ListResponse JSON limits must be positive");
        }
        this.maximumBytes = maximumBytes;
        this.maximumDepth = maximumDepth;
    }

    /**
     * Creates an exact JSON integer value.
     *
     * @param value integer value
     * @return provider-neutral number
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(java.math.BigDecimal.valueOf(value));
    }

    /**
     * Adds a present optional integer to an ordered JSON object map.
     *
     * @param values target members
     * @param name   exact wire member name
     * @param value  optional integer
     */
    private static void put(final Map<String, JsonValue> values, final String name, final Optional<Integer> value) {
        if (!value.isEmpty()) {
            values.put(name, number(value.getOrThrow()));
        }
    }

    /**
     * Encodes one User/Group search ListResponse as HTTP 200 application/scim+json.
     *
     * @param request  originating Fabric HTTP request
     * @param response standard ListResponse containing only User and Group resources
     * @return complete HTTP response
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if a discovery resource or password-bearing User is present
     */
    public HttpResponse encode(final HttpRequest request, final ListResponse response) {
        final HttpRequest origin = Assert.notNull(request, "SCIM ListResponse origin request must not be null");
        final ListResponse value = Assert.notNull(response, "SCIM ListResponse must not be null");
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        members.put(Scim.Attributes.SCHEMAS, ScimResourceCodec.array(value.schemas()));
        members.put(Scim.Attributes.TOTAL_RESULTS, number(value.totalResults()));
        put(members, Scim.Attributes.START_INDEX, value.startIndex());
        put(members, Scim.Attributes.ITEMS_PER_PAGE, value.itemsPerPage());
        final List<JsonValue> resources = new ArrayList<>(value.resources().size());
        for (Resource resource : value.resources()) {
            if (!(resource instanceof User) && !(resource instanceof Group)) {
                throw new ValidateException("SCIM search ListResponse must contain only User and Group");
            }
            if (resource instanceof User user && !user.password().isEmpty()) {
                throw new ValidateException("SCIM ListResponse must not encode User password");
            }
            resources.add(ScimResourceCodec.encodeResource(resource));
        }
        members.put(Scim.Attributes.RESOURCES, new JsonValue.ArrayValue(resources));
        final byte[] body = ScimResourceCodec
                .bytes(new JsonValue.ObjectValue(members), jsonProvider, maximumBytes, maximumDepth);
        return HttpResponse.builder().request(origin).code(Http.Status.OK).headers(
                Headers.of(Http.Header.CACHE_CONTROL, Http.Cache.NO_STORE, Http.Header.PRAGMA, Http.Cache.NO_CACHE))
                .body(PayloadBody.of(Payload.of(body), MediaType.APPLICATION_SCIM_JSON_TYPE)).build();
    }

}
