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
 * DICOM MAC parameters sequence item.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MacParameters extends DicomModule {

    /**
     * Creates a new instance.
     */
    public MacParameters() {
        this(new Attributes());
    }

    /**
     * Creates a new instance.
     *
     * @param dcmItems the dcm items.
     */
    public MacParameters(Attributes dcmItems) {
        super(dcmItems);
    }

    /**
     * Creates a value from the supplied input.
     *
     * @param sequence the sequence.
     * @return the operation result.
     */
    public static Collection<MacParameters> fromSequence(Sequence sequence) {
        return mapSequence(sequence, MacParameters::new);
    }

    /**
     * Gets the macid number.
     *
     * @return the macid number.
     */
    public int getMACIDNumber() {
        return dcmItems.getInt(Tag.MACIDNumber, -1);
    }

    /**
     * Sets the macid number.
     *
     * @param macId the mac id.
     */
    public void setMACIDNumber(int macId) {
        dcmItems.setInt(Tag.MACIDNumber, VR.US, macId);
    }

    /**
     * Gets the mac calculation transfer syntax uid.
     *
     * @return the mac calculation transfer syntax uid.
     */
    public String getMACCalculationTransferSyntaxUID() {
        return dcmItems.getString(Tag.MACCalculationTransferSyntaxUID);
    }

    /**
     * Sets the mac calculation transfer syntax uid.
     *
     * @param transferSyntaxUID the transfer syntax uid.
     */
    public void setMACCalculationTransferSyntaxUID(String transferSyntaxUID) {
        dcmItems.setString(Tag.MACCalculationTransferSyntaxUID, VR.UI, transferSyntaxUID);
    }

    /**
     * Gets the mac algorithm.
     *
     * @return the mac algorithm.
     */
    public String getMACAlgorithm() {
        return dcmItems.getString(Tag.MACAlgorithm);
    }

    /**
     * Sets the mac algorithm.
     *
     * @param algorithm the algorithm.
     */
    public void setMACAlgorithm(String algorithm) {
        dcmItems.setString(Tag.MACAlgorithm, VR.CS, algorithm);
    }

    /**
     * Gets the data elements signed.
     *
     * @return the data elements signed.
     */
    public int[] getDataElementsSigned() {
        return dcmItems.getInts(Tag.DataElementsSigned);
    }

    /**
     * Sets the data elements signed.
     *
     * @param signedTags the signed tags.
     */
    public void setDataElementsSigned(int[] signedTags) {
        dcmItems.setInt(Tag.DataElementsSigned, VR.AT, signedTags);
    }

}
