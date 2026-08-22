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
import java.util.List;
import java.util.Locale;

import org.miaixz.bus.auth.FabricX.Body;
import org.miaixz.bus.auth.protocol.scim.PatchOp;
import org.miaixz.bus.auth.protocol.scim.PatchOperation;
import org.miaixz.bus.auth.protocol.scim.Scim;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonRecordVerifier;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Strictly decodes an RFC 7644 PATCH request for a SCIM Service Provider.
 * <p>
 * The codec owns and closes the inbound request body. A successfully returned {@link PatchOp} owns every password
 * {@link SecretLease}; the caller must close that model after the asynchronous patch operation completes.
 * </p>
 *
 * @author Kimi Liu
 */
public class ScimPatchCodec {

    /**
     * Verifies the exact top-level RFC 7644 PatchOp members.
     */
    private static final JsonRecordVerifier<PatchDocument> DOCUMENT_VERIFIER = JsonRecordVerifier
            .of(PatchDocument.class);

    /**
     * Verifies the exact RFC 7644 members accepted for one patch operation.
     */
    private static final JsonRecordVerifier<OperationDocument> OPERATION_VERIFIER = JsonRecordVerifier
            .of(OperationDocument.class);

    /**
     * Runtime-selected provider-neutral JSON implementation.
     */
    private final JsonProvider jsonProvider;

    /**
     * Maximum accepted encoded request body bytes.
     */
    private final long maximumBytes;

    /**
     * Maximum accepted JSON object/array nesting depth.
     */
    private final int maximumDepth;

    /**
     * Creates a strict PATCH request decoder with explicit limits.
     *
     * @param jsonProvider externally selected provider-neutral JSON implementation
     * @param maximumBytes positive maximum encoded request body bytes
     * @param maximumDepth positive maximum JSON container depth
     * @throws IllegalArgumentException if {@code jsonProvider} is {@code null}
     * @throws ValidateException        if a limit is not positive
     */
    public ScimPatchCodec(final JsonProvider jsonProvider, final long maximumBytes, final int maximumDepth) {
        this.jsonProvider = Assert.notNull(jsonProvider, "SCIM PATCH JSON provider must not be null");
        if (maximumBytes <= 0 || maximumDepth <= 0) {
            throw new ValidateException("SCIM PATCH JSON limits must be positive");
        }
        this.maximumBytes = maximumBytes;
        this.maximumDepth = maximumDepth;
    }

    /**
     * Decodes one already bounded PatchOp JSON object for the SCIM Bulk request codec.
     * <p>
     * This package-private entry preserves a single implementation of PatchOp member, operation, and secret-lifetime
     * rules without exposing a second public decoding contract.
     * </p>
     *
     * @param object strictly parsed PatchOp JSON object
     * @return typed PatchOp owning all decoded password leases
     */
    static PatchOp decode(final JsonValue.ObjectValue object) {
        Assert.notNull(object, "SCIM PatchOp JSON object must not be null");
        final List<PatchOperation> operations = new ArrayList<>();
        try {
            DOCUMENT_VERIFIER.validate(object);
            final List<String> schemas = ScimResourceCodec.strings(
                    ScimResourceCodec.required(object.values(), Scim.Attributes.SCHEMAS),
                    Scim.Attributes.SCHEMAS);
            if (!schemas.equals(List.of(Scim.PATCH_OP_SCHEMA))) {
                throw new ValidateException("SCIM PatchOp schemas must contain only the standard schema URI");
            }
            final JsonValue encodedOperations = ScimResourceCodec.required(object.values(), Scim.Attributes.OPERATIONS);
            if (!(encodedOperations instanceof JsonValue.ArrayValue array) || array.values().isEmpty()) {
                throw new ValidateException("SCIM PatchOp Operations must be a non-empty array");
            }
            for (JsonValue item : array.values()) {
                if (!(item instanceof JsonValue.ObjectValue operation)) {
                    throw new ValidateException("SCIM PatchOp operation must be an object");
                }
                operations.add(operation(operation));
            }
            return new PatchOp(schemas, operations);
        } catch (RuntimeException exception) {
            close(operations);
            throw exception;
        }
    }

    /**
     * Decodes one operation while transferring password ownership to an erasable lease.
     *
     * @param object parsed operation object
     * @return validated typed patch operation
     */
    private static PatchOperation operation(final JsonValue.ObjectValue object) {
        OPERATION_VERIFIER.validate(object);
        final PatchOperation.Op op = operationName(
                ScimResourceCodec.requiredString(object.values(), Scim.Attributes.OP));
        final String path = ScimResourceCodec.optionalString(object.values(), Scim.Attributes.PATH);
        final JsonValue encodedValue = object.values().get(Scim.Attributes.VALUE);
        final PatchOperation.Value value;
        if (encodedValue == null) {
            value = null;
        } else if (encodedValue instanceof JsonValue.NullValue) {
            throw new ValidateException("SCIM patch operation value must not be null");
        } else if (path != null && passwordPath(path)) {
            if (!(encodedValue instanceof JsonValue.StringValue password)) {
                throw new ValidateException("SCIM password patch value must be a JSON string");
            }
            value = new PatchOperation.SecretData(new SecretLease(password.value().toCharArray()));
        } else {
            value = new PatchOperation.JsonData(encodedValue);
        }
        try {
            if (path != null) {
                validatePatchPathFilter(path);
            }
            return new PatchOperation(op, Optional.ofNullable(path == null ? null : new PatchOperation.Path(path)),
                    Optional.ofNullable(value));
        } catch (RuntimeException exception) {
            if (value != null) {
                value.close();
            }
            throw exception;
        }
    }

    /**
     * Parses the filter embedded in a PATCH valuePath before constructing the protocol value.
     *
     * @param path candidate PATCH path
     */
    private static void validatePatchPathFilter(final String path) {
        final int open = path.indexOf(Symbol.C_BRACKET_LEFT);
        if (open >= 0) {
            final int close = path.lastIndexOf(Symbol.C_BRACKET_RIGHT);
            if (close > open) {
                new ScimFilterParser(path.substring(open + 1, close), path.length(), 32).parse();
            }
        }
    }

    /**
     * Converts a case-insensitive standard operation token to its canonical model constant.
     *
     * @param value decoded operation name
     * @return matching add, remove, or replace operation
     */
    private static PatchOperation.Op operationName(final String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case Scim.Operations.ADD -> PatchOperation.Op.ADD;
            case Scim.Operations.REMOVE -> PatchOperation.Op.REMOVE;
            case Scim.Operations.REPLACE -> PatchOperation.Op.REPLACE;
            default -> throw new ValidateException("SCIM patch operation op must be add, remove, or replace");
        };
    }

    /**
     * Tests whether a validated patch path directly identifies the core password attribute.
     *
     * @param value patch path lexical value
     * @return whether the path selects password
     */
    private static boolean passwordPath(final String value) {
        final String normalized = value.toLowerCase(Locale.ROOT);
        return "password".equals(normalized) || normalized.endsWith(":password");
    }

    /**
     * Erases secret values owned by operations accumulated before a decode failure.
     *
     * @param operations partially decoded operation sequence
     */
    private static void close(final List<PatchOperation> operations) {
        for (PatchOperation operation : operations) {
            operation.close();
        }
    }

    /**
     * Decodes one bounded PatchOp body without interpreting route or header state.
     *
     * @param body owned request body closed by this method
     * @return typed PatchOp owning all decoded password leases
     * @throws IllegalArgumentException if {@code body} is {@code null}
     * @throws ValidateException        if the media type, JSON, schema, or operation is invalid
     */
    public PatchOp decode(final Body body) {
        final Body encoded = Assert.notNull(body, "SCIM PatchOp body must not be null");
        try (encoded) {
            final JsonValue.ObjectValue object = ScimResourceCodec
                    .object(encoded, jsonProvider, maximumBytes, maximumDepth);
            return decode(object);
        }
    }

    /**
     * Associates the exact RFC 7644 PatchOp object vocabulary with one Java record.
     *
     * @param schemas    standard schema array
     * @param Operations ordered patch operations
     */
    private record PatchDocument(JsonValue schemas, JsonValue Operations) {

    }

    /**
     * Associates the exact RFC 7644 patch operation vocabulary with one Java record.
     *
     * @param op    operation name
     * @param path  optional attribute path
     * @param value optional operation value
     */
    private record OperationDocument(JsonValue op, JsonValue path, JsonValue value) {

    }

}
