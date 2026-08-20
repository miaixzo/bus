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
package org.miaixz.bus.auth.protocol.scim;

import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models exactly the RFC 7644 PatchOp JSON request body.
 *
 * @param schemas    singleton standard PatchOp schema URI
 * @param operations non-empty ordered patch operation sequence
 * @author Kimi Liu
 */
public record PatchOp(List<String> schemas, List<PatchOperation> operations) implements AutoCloseable {

    /**
     * Enforces the PatchOp schema and non-empty operation sequence.
     *
     * @throws IllegalArgumentException if a required value, collection, or operation is {@code null}
     * @throws ValidateException        if the schema or operation sequence is invalid
     */
    public PatchOp {
        Assert.notNull(schemas, "SCIM PatchOp schemas must not be null");
        schemas = List.copyOf(schemas);
        if (!schemas.equals(List.of(Scim.PATCH_OP_SCHEMA))) {
            throw new ValidateException("SCIM PatchOp schemas must contain only the standard schema URI");
        }
        Assert.notNull(operations, "SCIM PatchOp Operations must not be null");
        if (operations.isEmpty()) {
            throw new ValidateException("SCIM PatchOp Operations must not be empty");
        }
        for (PatchOperation operation : operations) {
            Assert.notNull(operation, "SCIM PatchOp operation must not be null");
        }
        operations = List.copyOf(operations);
    }

    /**
     * Idempotently closes every operation so password SecretLease values are erased.
     */
    @Override
    public void close() {
        for (PatchOperation operation : operations) {
            operation.close();
        }
    }

    /**
     * Returns a redacted representation that never exposes patch values.
     *
     * @return fixed redacted PatchOp representation
     */
    @Override
    public String toString() {
        return "PatchOp[operations=<redacted>]";
    }

}
