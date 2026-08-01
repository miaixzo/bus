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
package org.miaixz.bus.tempus.temporal.payload;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

import org.miaixz.bus.extra.json.JsonProvider;

import io.temporal.common.converter.DataConverterException;

/**
 * Creates Temporal's JSON payload converter while keeping service-client generated types out of the {@code bus.tempus}
 * module descriptor.
 * <p>
 * Temporal SDK 1.35 splits internal packages across its SDK and service-client JARs, so the latter cannot safely be
 * added as a named JPMS dependency. The proxy implements Temporal's public converter contract at runtime, while all
 * JSON processing remains delegated to the application-wide {@link JsonProvider}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class JsonPayloadConverter implements InvocationHandler {

    /**
     * Temporal payload encoding identifier for ordinary JSON documents.
     */
    private static final String ENCODING = "json/plain";

    /**
     * Standard Temporal metadata key containing the payload encoding identifier.
     */
    private static final String ENCODING_METADATA = "encoding";

    /**
     * Bus metadata key recording which application JSON provider produced the payload.
     */
    private static final String PROVIDER_METADATA = "bus-json-provider";

    /**
     * Application-selected provider responsible for JSON serialization and deserialization.
     */
    private final JsonProvider provider;

    /**
     * Runtime Temporal payload class loaded reflectively to avoid a JPMS dependency on service-client.
     */
    private final Class<?> payloadType;

    /**
     * Runtime protobuf byte-string class loaded reflectively for Temporal payload construction.
     */
    private final Class<?> byteStringType;

    /**
     * Creates a provider-backed Temporal converter adapter.
     *
     * @param provider application JSON provider
     */
    public JsonPayloadConverter(JsonProvider provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
        this.payloadType = load("io.temporal.api.common.v1.Payload");
        this.byteStringType = load("com.google.protobuf.ByteString");
    }

    /**
     * Returns the Temporal converter proxy.
     *
     * @return Temporal payload converter
     */
    public io.temporal.common.converter.PayloadConverter converter() {
        Class<io.temporal.common.converter.PayloadConverter> contract = io.temporal.common.converter.PayloadConverter.class;
        return (io.temporal.common.converter.PayloadConverter) Proxy
                .newProxyInstance(contract.getClassLoader(), new Class<?>[] { contract }, this);
    }

    /**
     * Dispatches calls from Temporal's {@code PayloadConverter} proxy to the Bus provider-backed implementation.
     *
     * @param proxy     generated Temporal converter proxy
     * @param method    invoked converter method
     * @param arguments invocation arguments supplied by Temporal
     * @return result required by the invoked converter method
     * @throws DataConverterException        if reflective payload access or JSON conversion fails
     * @throws UnsupportedOperationException if the installed Temporal SDK exposes an unsupported converter method
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] arguments) {
        try {
            return switch (method.getName()) {
                case "getEncodingType" -> ENCODING;
                case "toData" -> toData(arguments[0]);
                case "fromData" -> fromData(arguments);
                case "withContext" -> proxy;
                case "toString" -> "BusJsonPayloadConverter[" + provider.name() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == arguments[0];
                default -> throw new UnsupportedOperationException("Unsupported Temporal converter method: " + method);
            };
        } catch (DataConverterException e) {
            throw e;
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new DataConverterException(unwrap(e));
        }
    }

    /**
     * Serializes a Java value and constructs a Temporal payload with encoding and provider metadata.
     *
     * @param value Java value to serialize
     * @return populated Temporal payload
     * @throws ReflectiveOperationException if the installed Temporal or protobuf runtime is incompatible
     */
    private Optional<?> toData(Object value) throws ReflectiveOperationException {
        Object builder = payloadType.getMethod("newBuilder").invoke(null);
        Method copyFromUtf8 = byteStringType.getMethod("copyFromUtf8", String.class);
        Method copyFrom = byteStringType.getMethod("copyFrom", byte[].class);
        Method putMetadata = builder.getClass().getMethod("putMetadata", String.class, byteStringType);
        Method setData = builder.getClass().getMethod("setData", byteStringType);

        putMetadata.invoke(builder, ENCODING_METADATA, copyFromUtf8.invoke(null, ENCODING));
        putMetadata.invoke(builder, PROVIDER_METADATA, copyFromUtf8.invoke(null, provider.name()));
        setData.invoke(builder, copyFrom.invoke(null, (Object) provider.write(value)));
        return Optional.of(builder.getClass().getMethod("build").invoke(builder));
    }

    /**
     * Extracts JSON bytes from a Temporal payload and deserializes them into the requested Java type.
     *
     * @param arguments Temporal {@code fromData} arguments: payload, concrete class, and generic type
     * @return deserialized value, or {@code null} for an empty payload body
     * @throws ReflectiveOperationException if the installed Temporal or protobuf runtime is incompatible
     */
    private Object fromData(Object[] arguments) throws ReflectiveOperationException {
        Object payload = payloadType.cast(arguments[0]);
        Class<?> valueClass = (Class<?>) arguments[1];
        Type valueType = (Type) arguments[2];
        Object data = payloadType.getMethod("getData").invoke(payload);
        byte[] bytes = (byte[]) byteStringType.getMethod("toByteArray").invoke(data);
        if (bytes.length == 0) {
            return null;
        }
        return provider.read(bytes, valueType == null ? valueClass : valueType);
    }

    /**
     * Loads a required runtime type without introducing a static module dependency.
     *
     * @param className fully qualified runtime class name
     * @return loaded class
     * @throws IllegalStateException if the required runtime dependency is missing
     */
    private static Class<?> load(String className) {
        try {
            return Class.forName(className, false, JsonPayloadConverter.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Temporal runtime dependency is missing: " + className, e);
        }
    }

    /**
     * Unwraps a reflective invocation failure so Temporal receives the original conversion cause.
     *
     * @param throwable caught reflective or runtime failure
     * @return underlying invocation cause when present; otherwise the supplied failure
     */
    private static Throwable unwrap(Throwable throwable) {
        if (throwable instanceof InvocationTargetException invocation && invocation.getCause() != null) {
            return invocation.getCause();
        }
        return throwable;
    }

}
