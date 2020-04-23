/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.image.metric;

import org.aoju.bus.image.UID;
import org.aoju.bus.image.galaxy.Property;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
public class TransferCapability implements Serializable {

    private ApplicationEntity ae;
    private String commonName;
    private String sopClass;
    private Role role;
    private String[] transferSyntaxes;
    private String[] prefTransferSyntaxes = {};
    private EnumSet<QueryOption> queryOptions;
    private StorageOptions storageOptions;

    public TransferCapability() {
        this(null, UID.VerificationSOPClass, Role.SCU, UID.ImplicitVRLittleEndian);
    }

    public TransferCapability(String commonName, String sopClass, Role role,
                              String... transferSyntaxes) {
        setCommonName(commonName);
        setSopClass(sopClass);
        setRole(role);
        setTransferSyntaxes(transferSyntaxes);
    }

    public void setApplicationEntity(ApplicationEntity ae) {
        if (ae != null) {
            if (this.ae != null)
                throw new IllegalStateException("already owned by AE " +
                        this.ae.getAETitle());
        }
        this.ae = ae;
    }

    /**
     * get the name of the Transfer Capability object. Can be a meaningful name
     * or any unique sequence of characters.
     *
     * @return A String containing the common name.
     */
    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * Get the role for this TransferCapability instance.
     *
     * @return Role (SCU or SCP) for this TransferCapability instance
     */
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        if (role == null)
            throw new NullPointerException();

        if (this.role == role)
            return;

        ApplicationEntity ae = this.ae;
        if (ae != null)
            ae.removeTransferCapabilityFor(sopClass, this.role);

        this.role = role;

        if (ae != null)
            ae.addTransferCapability(this);
    }

    /**
     * Get the SOP Class of this Transfer Capability object.
     *
     * @return A String containing the SOP Class UID.
     */
    public String getSopClass() {
        return sopClass;
    }

    public void setSopClass(String sopClass) {
        if (sopClass.isEmpty())
            throw new IllegalArgumentException("empty sopClass");

        if (sopClass.equals(this.sopClass))
            return;

        ApplicationEntity ae = this.ae;
        if (ae != null)
            ae.removeTransferCapabilityFor(sopClass, this.role);

        this.sopClass = sopClass;

        if (ae != null)
            ae.addTransferCapability(this);
    }

    /**
     * Get the transfer syntax(es) that may be requested as an SCU or that are
     * offered as an SCP.
     *
     * @return list of transfer syntaxes.
     */
    public String[] getTransferSyntaxes() {
        return transferSyntaxes;
    }

    public void setTransferSyntaxes(String... transferSyntaxes) {
        this.transferSyntaxes = Property.requireContainsNoEmpty(
                Property.requireNotEmpty(transferSyntaxes, "missing transferSyntax"),
                "empty transferSyntax");
    }

    public String[] getPreferredTransferSyntaxes() {
        return prefTransferSyntaxes;
    }

    public void setPreferredTransferSyntaxes(String... transferSyntaxes) {
        this.prefTransferSyntaxes =
                Property.requireContainsNoEmpty(transferSyntaxes, "empty transferSyntax");
    }

    public boolean containsTransferSyntax(String ts) {
        return "*".equals(transferSyntaxes[0]) || Property.contains(transferSyntaxes, ts);
    }

    public String selectTransferSyntax(String... transferSyntaxes) {
        if (transferSyntaxes.length == 1)
            return containsTransferSyntax(transferSyntaxes[0]) ? transferSyntaxes[0] : null;

        List<String> acceptable = retainAcceptable(transferSyntaxes);
        if (acceptable.isEmpty())
            return null;

        for (String prefTransferSyntax : prefTransferSyntaxes.length > 0
                ? prefTransferSyntaxes
                : ae.getPreferredTransferSyntaxes())
            if (acceptable.contains(prefTransferSyntax))
                return prefTransferSyntax;

        return acceptable.get(0);
    }

    private List<String> retainAcceptable(String[] transferSyntaxes) {
        List<String> acceptable = new ArrayList<>(transferSyntaxes.length);
        for (String transferSyntax : transferSyntaxes) {
            if (containsTransferSyntax(transferSyntax))
                acceptable.add(transferSyntax);
        }
        return acceptable;
    }

    public EnumSet<QueryOption> getQueryOptions() {
        return queryOptions;
    }

    public void setQueryOptions(EnumSet<QueryOption> queryOptions) {
        this.queryOptions = queryOptions;
    }

    public StorageOptions getStorageOptions() {
        return storageOptions;
    }

    public void setStorageOptions(StorageOptions storageOptions) {
        this.storageOptions = storageOptions;
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder(512), "").toString();
    }

    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + "  ";
        Property.appendLine(sb, indent, "TransferCapability[cn: ", commonName);
        Property.appendLine(sb, indent2, "role: ", role);
        sb.append(indent2).append("as: ");
        UID.promptTo(sopClass, sb).append(Property.LINE_SEPARATOR);
        for (String ts : transferSyntaxes) {
            sb.append(indent2).append("ts: ");
            UID.promptTo(ts, sb).append(Property.LINE_SEPARATOR);
        }
        if (queryOptions != null)
            sb.append(indent2).append("QueryOptions").append(queryOptions)
                    .append(Property.LINE_SEPARATOR);
        if (storageOptions != null)
            sb.append(indent2).append(storageOptions)
                    .append(Property.LINE_SEPARATOR);
        return sb.append(indent).append(']');
    }

    public enum Role {SCU, SCP}

}
