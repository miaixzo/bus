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

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import org.miaixz.bus.image.Tag;

/**
 * DICOM digital signature sequence item.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DigitalSignature extends DicomModule {

    /**
     * Creates a new instance.
     */
    public DigitalSignature() {
        this(new Attributes());
    }

    /**
     * Creates a new instance.
     *
     * @param dcmItems the dcm items.
     */
    public DigitalSignature(Attributes dcmItems) {
        super(dcmItems);
    }

    /**
     * Creates a value from the supplied input.
     *
     * @param sequence the sequence.
     * @return the operation result.
     */
    public static Collection<DigitalSignature> fromSequence(Sequence sequence) {
        return mapSequence(sequence, DigitalSignature::new);
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
     * @param value the value.
     */
    public void setMACIDNumber(int value) {
        dcmItems.setInt(Tag.MACIDNumber, VR.US, value);
    }

    /**
     * Gets the digital signature uid.
     *
     * @return the digital signature uid.
     */
    public String getDigitalSignatureUID() {
        return dcmItems.getString(Tag.DigitalSignatureUID);
    }

    /**
     * Sets the digital signature uid.
     *
     * @param uid the uid.
     */
    public void setDigitalSignatureUID(String uid) {
        dcmItems.setString(Tag.DigitalSignatureUID, VR.UI, uid);
    }

    /**
     * Gets the digital signature date time.
     *
     * @return the digital signature date time.
     */
    public Date getDigitalSignatureDateTime() {
        return dcmItems.getDate(Tag.DigitalSignatureDateTime);
    }

    /**
     * Sets the digital signature date time.
     *
     * @param dateTime the date time.
     */
    public void setDigitalSignatureDateTime(Date dateTime) {
        dcmItems.setDate(Tag.DigitalSignatureDateTime, VR.DT, dateTime);
    }

    /**
     * Gets the certificate type.
     *
     * @return the certificate type.
     */
    public String getCertificateType() {
        return dcmItems.getString(Tag.CertificateType);
    }

    /**
     * Sets the certificate type.
     *
     * @param type the type.
     */
    public void setCertificateType(String type) {
        dcmItems.setString(Tag.CertificateType, VR.CS, type);
    }

    /**
     * Gets the certificate of signer.
     *
     * @return the certificate of signer.
     * @throws IOException if the operation cannot be completed.
     */
    public byte[] getCertificateOfSigner() throws IOException {
        return dcmItems.getBytes(Tag.CertificateOfSigner);
    }

    /**
     * Sets the certificate of signer.
     *
     * @param certificate the certificate.
     */
    public void setCertificateOfSigner(byte[] certificate) {
        dcmItems.setBytes(Tag.CertificateOfSigner, VR.OB, certificate);
    }

    /**
     * Gets the signature.
     *
     * @return the signature.
     * @throws IOException if the operation cannot be completed.
     */
    public byte[] getSignature() throws IOException {
        return dcmItems.getBytes(Tag.Signature);
    }

    /**
     * Sets the signature.
     *
     * @param signature the signature.
     */
    public void setSignature(byte[] signature) {
        dcmItems.setBytes(Tag.Signature, VR.OB, signature);
    }

    /**
     * Gets the certified timestamp type.
     *
     * @return the certified timestamp type.
     */
    public String getCertifiedTimestampType() {
        return dcmItems.getString(Tag.CertifiedTimestampType);
    }

    /**
     * Sets the certified timestamp type.
     *
     * @param type the type.
     */
    public void setCertifiedTimestampType(String type) {
        dcmItems.setString(Tag.CertifiedTimestampType, VR.CS, type);
    }

    /**
     * Gets the certified timestamp.
     *
     * @return the certified timestamp.
     * @throws IOException if the operation cannot be completed.
     */
    public byte[] getCertifiedTimestamp() throws IOException {
        return dcmItems.getBytes(Tag.CertifiedTimestamp);
    }

    /**
     * Sets the certified timestamp.
     *
     * @param timestamp the timestamp.
     */
    public void setCertifiedTimestamp(byte[] timestamp) {
        dcmItems.setBytes(Tag.CertifiedTimestamp, VR.OB, timestamp);
    }

    /**
     * Gets the digital signature purpose code.
     *
     * @return the digital signature purpose code.
     */
    public Code getDigitalSignaturePurposeCode() {
        return nestedCode(dcmItems, Tag.DigitalSignaturePurposeCodeSequence);
    }

    /**
     * Sets the digital signature purpose code.
     *
     * @param code the code.
     */
    public void setDigitalSignaturePurposeCode(Code code) {
        updateCodeSequence(Tag.DigitalSignaturePurposeCodeSequence, code);
    }

}
