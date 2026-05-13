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
package org.miaixz.bus.image.galaxy.data;

import java.util.Collection;

import org.miaixz.bus.image.Tag;

/**
 * SOP instance reference with a coded purpose.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SopInstanceReferenceAndPurpose extends SopInstanceReference {

    /**
     * Creates a new instance.
     */
    public SopInstanceReferenceAndPurpose() {
        this(new Attributes());
    }

    /**
     * Creates a new instance.
     *
     * @param dcmItems the dcm items.
     */
    public SopInstanceReferenceAndPurpose(Attributes dcmItems) {
        super(dcmItems);
    }

    /**
     * Creates a value from the supplied input.
     *
     * @param sequence the sequence.
     * @return the operation result.
     */
    public static Collection<SopInstanceReferenceAndPurpose> fromPurposeSequence(Sequence sequence) {
        return mapSequence(sequence, SopInstanceReferenceAndPurpose::new);
    }

    /**
     * Gets the purpose of reference code.
     *
     * @return the purpose of reference code.
     */
    public Code getPurposeOfReferenceCode() {
        return nestedCode(dcmItems, Tag.PurposeOfReferenceCodeSequence);
    }

    /**
     * Sets the purpose of reference code.
     *
     * @param purposeCode the purpose code.
     */
    public void setPurposeOfReferenceCode(Code purposeCode) {
        updateCodeSequence(Tag.PurposeOfReferenceCodeSequence, purposeCode);
    }

}
