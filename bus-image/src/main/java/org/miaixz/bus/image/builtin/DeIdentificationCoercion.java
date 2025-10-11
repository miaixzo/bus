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
