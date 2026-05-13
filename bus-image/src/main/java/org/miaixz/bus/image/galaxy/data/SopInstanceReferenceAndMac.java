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
 * SOP instance reference with purpose, MAC parameters and digital signatures.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SopInstanceReferenceAndMac extends SopInstanceReferenceAndPurpose {

    /**
     * Creates a new instance.
     */
    public SopInstanceReferenceAndMac() {
        this(new Attributes());
    }

    /**
     * Creates a new instance.
     *
     * @param dcmItems the dcm items.
     */
    public SopInstanceReferenceAndMac(Attributes dcmItems) {
        super(dcmItems);
    }

    /**
     * Creates a value from the supplied input.
     *
     * @param sequence the sequence.
     * @return the operation result.
     */
    public static Collection<SopInstanceReferenceAndMac> fromMacSequence(Sequence sequence) {
        return mapSequence(sequence, SopInstanceReferenceAndMac::new);
    }

    /**
     * Gets the mac parameters.
     *
     * @return the mac parameters.
     */
    public Collection<MacParameters> getMACParameters() {
        return MacParameters.fromSequence(dcmItems.getSequence(Tag.MACParametersSequence));
    }

    /**
     * Sets the mac parameters.
     *
     * @param macParameters the mac parameters.
     */
    public void setMACParameters(Collection<MacParameters> macParameters) {
        updateSequence(Tag.MACParametersSequence, macParameters);
    }

    /**
     * Gets the digital signatures.
     *
     * @return the digital signatures.
     */
    public Collection<DigitalSignature> getDigitalSignatures() {
        return DigitalSignature.fromSequence(dcmItems.getSequence(Tag.DigitalSignaturesSequence));
    }

    /**
     * Sets the digital signatures.
     *
     * @param digitalSignatures the digital signatures.
     */
    public void setDigitalSignatures(Collection<DigitalSignature> digitalSignatures) {
        updateSequence(Tag.DigitalSignaturesSequence, digitalSignatures);
    }

}
