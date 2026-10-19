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
package org.miaixz.bus.cortex.setting.curator;

import java.util.Arrays;
import java.util.Objects;

import org.miaixz.bus.core.lang.exception.ForbiddenException;
import org.miaixz.bus.core.lang.exception.UncheckedException;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemNormalizer;
import org.miaixz.bus.cortex.setting.secret.*;

/**
 * Connects the existing setting-source SPI to trusted SecretManager reads. The context-free overload always denies
 * access; management preview returns a fixed mask. Tenant/application matching is checked before resolution, and
 * callers own the returned immutable Java string.
 */
public final class SecretSourceAdapter implements ItemSourceAdapter {

    /**
     * Authorized Secret entry point; the adapter borrows rather than closes it.
     */
    private final SecretManager manager;

    /**
     * Creates SecretSourceAdapter with explicitly bound collaborators. Dependencies are not discovered implicitly.
     *
     * @param manager input manager used by this operation
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    public SecretSourceAdapter(SecretManager manager) {
        this.manager = Objects.requireNonNull(manager);
    }

    /**
     * Returns the source discriminator used to select this adapter.
     *
     * @return SECRET source discriminator
     */
    @Override
    public String source() {
        return ItemNormalizer.SECRET_SOURCE;
    }

    /**
     * Tests whether the supplied source or protection mode is supported without performing any remote resolution.
     *
     * @param entry setting entry or already selected snapshot; null is handled as documented
     * @return true when the input is supported
     */
    @Override
    public boolean supports(Item entry) {
        return ItemNormalizer.isSecret(entry);
    }

    /**
     * Validates only the reference descriptor and does not acquire credentials or contact a cloud service.
     *
     * @param entry setting entry or already selected snapshot; null is handled as documented
     * @return true if the SECRET descriptor can be parsed
     */
    @Override
    public boolean validate(Item entry) {
        try {
            ItemNormalizer.secretReference(entry);
            return true;
        } catch (UncheckedException failure) {
            return false;
        }
    }

    /**
     * Rejects access without an authenticated context.
     *
     * @param entry setting entry or already selected snapshot; null is handled as documented
     * @return no value; this overload always denies access
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    @Override
    public String resolve(Item entry) {
        throw new ForbiddenException("Secret access denied");
    }

    /**
     * Checks tenant/application identity, parses the reference and resolves through SecretManager. Temporary character
     * copies are erased; the returned Java string cannot be erased.
     *
     * @param entry   setting entry or already selected snapshot; null is handled as documented
     * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                reference
     * @return runtime value as a Java string
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    @Override
    public String resolve(Item entry, SecretContext context) {
        Objects.requireNonNull(context);
        if (!context.tenantId().equals(entry.getTenant_id())
                || entry.getApp_id() != null && !context.appId().equals(entry.getApp_id())) {
            throw new ForbiddenException("Secret access denied");
        }
        try (SecretMaterial value = manager.resolve(ItemNormalizer.secretReference(entry), context)) {
            char[] copy = value.copy();
            try {
                return new String(copy);
            } finally {
                Arrays.fill(copy, '\0');
            }
        }
    }

    /**
     * Returns the fixed management-safe mask for SECRET entries, without resolving the backend value.
     *
     * @param entry setting entry or already selected snapshot; null is handled as documented
     * @return management-safe preview
     */
    @Override
    public String preview(Item entry) {
        return SecretMasker.MASK;
    }

}
