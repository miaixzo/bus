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
import org.miaixz.bus.cortex.setting.secret.SecretContext;
import org.miaixz.bus.cortex.setting.secret.SecretManager;
import org.miaixz.bus.cortex.setting.secret.SecretMasker;
import org.miaixz.bus.cortex.setting.secret.SecretMaterial;

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
     * Creates an adapter borrowing the explicitly supplied manager; this adapter does not close it.
     *
     * @param manager required authorized Secret manager
     * @throws NullPointerException if manager is null
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
     * Recognizes the trimmed, case-insensitive SECRET source without validating the descriptor or checking
     * authorization.
     *
     * @param entry item to inspect, or null
     * @return true only when entry is non-null and its source discriminator is SECRET
     */
    @Override
    public boolean supports(Item entry) {
        return ItemNormalizer.isSecretReference(entry);
    }

    /**
     * Checks the reference-only SECRET restrictions and parses the descriptor without backend access. Unchecked
     * contract failures, including a null entry, are converted to false.
     *
     * @param entry item to validate, or null
     * @return true only for a SECRET item with protected category, no inline content, no enabled encryption and a valid
     *         descriptor
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
     * Rejects every context-free resolution request, without reading the entry or contacting a backend.
     *
     * @param entry ignored item, possibly null
     * @return never returns normally
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException always, because authenticated context is required
     */
    @Override
    public String resolve(Item entry) {
        throw new ForbiddenException("Secret access denied");
    }

    /**
     * Requires matching tenant and, when present on the item, application identity before resolving the reference.
     * Returned material and temporary character copies are closed or erased; the returned Java string cannot be erased.
     *
     * @param entry   non-null reference-only SECRET item
     * @param context non-null trusted identity and absolute deadline
     * @return resolved runtime value as a Java string
     * @throws NullPointerException                                  if entry or context is null
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if tenant/application identity or the manager's
     *                                                               access policy denies access
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if descriptor, deadline, lifecycle, backend or
     *                                                               payload validation fails
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
     * Returns the fixed management mask without inspecting the item or contacting a backend.
     *
     * @param entry ignored item, possibly null or not a SECRET item
     * @return the shared four-character mask
     */
    @Override
    public String preview(Item entry) {
        return SecretMasker.MASK;
    }

}
