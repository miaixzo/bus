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
package org.miaixz.bus.image.builtin;

import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.AttributesCoercion;

/**
 * Implements {@link AttributesCoercion} to perform de-identification on DICOM attributes. This class delegates
 * de-identification tasks to a {@link DeIdentifier} instance and can chain with another {@link AttributesCoercion} for
 * further processing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DeIdentificationCoercion implements AttributesCoercion {

    /**
     * The de-identifier instance used to perform de-identification operations.
     */
    private final DeIdentifier deIdentifier;
    /**
     * The next {@link AttributesCoercion} in the chain, or {@code null} if this is the last one.
     */
    private final AttributesCoercion next;

    /**
     * Constructs a new {@code DeIdentificationCoercion} with the specified de-identifier and next coercion.
     *
     * @param deIdentifier The {@link DeIdentifier} to use for de-identification.
     * @param next         The next {@link AttributesCoercion} in the chain, or {@code null}.
     */
    public DeIdentificationCoercion(DeIdentifier deIdentifier, AttributesCoercion next) {
        this.deIdentifier = deIdentifier;
        this.next = next;
    }

    /**
     * Creates a {@code DeIdentificationCoercion} instance if de-identification options are provided, otherwise returns
     * the next coercion directly.
     *
     * @param options An array of {@link DeIdentifier.Option} to configure the de-identifier.
     * @param next    The next {@link AttributesCoercion} in the chain, or {@code null}.
     * @return A new {@code DeIdentificationCoercion} instance or the provided {@code next} coercion.
     */
    public static AttributesCoercion valueOf(DeIdentifier.Option[] options, AttributesCoercion next) {
        return options != null && options.length > 0 ? new DeIdentificationCoercion(new DeIdentifier(options), next)
                : next;
    }

    /**
     * Remaps a given UID using the internal {@link DeIdentifier} and then passes the result to the next coercion in the
     * chain, if one exists.
     *
     * @param uid The UID to remap.
     * @return The remapped UID.
     */
    @Override
    public String remapUID(String uid) {
        String remappedUID = deIdentifier.remapUID(uid);
        return next != null ? next.remapUID(remappedUID) : remappedUID;
    }

    /**
     * Coerces the given DICOM attributes by applying de-identification and then delegates to the next coercion in the
     * chain, if one exists.
     *
     * @param attrs    The original DICOM attributes to be de-identified.
     * @param modified The attributes where modified values should be stored (may be the same as {@code attrs}).
     * @throws Exception if an error occurs during de-identification or subsequent coercion.
     */
    @Override
    public void coerce(Attributes attrs, Attributes modified) throws Exception {
        deIdentifier.deidentify(attrs);
        if (next != null)
            next.coerce(attrs, modified);
    }

}
