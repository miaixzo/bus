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
package org.miaixz.bus.image.metric;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.metric.net.ApplicationEntity;

/**
 * DICOM Standard, Part 15, Annex H: Transfer Capability - The description of the SOP classes and syntaxes supported by
 * a Network AE. An instance of the <code>TransferCapability</code> class describes the DICOM transfer capabilities of
 * an SCU or SCP in terms of a single presentation syntax. This includes the role selection (SCU or SCP), the acceptable
 * transfer syntaxes for a given SOP Class, and any extra information.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TransferCapability implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852261961377L;

    /**
     * The ae value.
     */
    private ApplicationEntity ae;

    /**
     * The common name value.
     */
    private String commonName;

    /**
     * The sop class value.
     */
    private String sopClass;

    /**
     * The role value.
     */
    private Role role;

    /**
     * The transfer syntaxes value.
     */
    private String[] transferSyntaxes;

    /**
     * The pref transfer syntaxes value.
     */
    private String[] prefTransferSyntaxes = {};

    /**
     * The query options value.
     */
    private EnumSet<QueryOption> queryOptions;

    /**
     * The storage options value.
     */
    private StorageOptions storageOptions;

    /**
     * Creates a new instance.
     */
    public TransferCapability() {
        this(null, UID.Verification.uid, Role.SCU, UID.ImplicitVRLittleEndian.uid);
    }

    /**
     * Creates a new instance.
     *
     * @param commonName       the common name.
     * @param sopClass         the sop class.
     * @param role             the role.
     * @param transferSyntaxes the transfer syntaxes.
     */
    public TransferCapability(String commonName, String sopClass, Role role, String... transferSyntaxes) {
        setCommonName(commonName);
        setSopClass(sopClass);
        setRole(role);
        setTransferSyntaxes(transferSyntaxes);
    }

    /**
     * Sets the application entity.
     *
     * @param ae the ae.
     */
    public void setApplicationEntity(ApplicationEntity ae) {
        if (ae != null) {
            if (this.ae != null)
                throw new IllegalStateException("already owned by AE " + this.ae.getAETitle());
        }
        this.ae = ae;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public String getCommonName() {
        return commonName;
    }

    /**
     * Sets the common name.
     *
     * @param commonName the common name.
     */
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Sets the role.
     *
     * @param role the role.
     */
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
     * Gets the related value.
     *
     * @return the result.
     */
    public String getSopClass() {
        return sopClass;
    }

    /**
     * Sets the sop class.
     *
     * @param sopClass the sop class.
     */
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
     * Gets the related value.
     *
     * @return the result.
     */
    public String[] getTransferSyntaxes() {
        return transferSyntaxes;
    }

    /**
     * Sets the transfer syntaxes.
     *
     * @param transferSyntaxes the transfer syntaxes.
     */
    public void setTransferSyntaxes(String... transferSyntaxes) {
        this.transferSyntaxes = Builder.requireContainsNoEmpty(
                Builder.requireNotEmpty(transferSyntaxes, "missing transferSyntax"),
                "empty transferSyntax");
    }

    /**
     * Gets the preferred transfer syntaxes.
     *
     * @return the preferred transfer syntaxes.
     */
    public String[] getPreferredTransferSyntaxes() {
        return prefTransferSyntaxes;
    }

    /**
     * Sets the preferred transfer syntaxes.
     *
     * @param transferSyntaxes the transfer syntaxes.
     */
    public void setPreferredTransferSyntaxes(String... transferSyntaxes) {
        this.prefTransferSyntaxes = Builder.requireContainsNoEmpty(transferSyntaxes, "empty transferSyntax");
    }

    /**
     * Gets the effective preferred transfer syntaxes.
     *
     * @return the effective preferred transfer syntaxes.
     */
    public String[] preferredTransferSyntaxes() {
        return prefTransferSyntaxes.length > 0 ? prefTransferSyntaxes : ae.getPreferredTransferSyntaxes();
    }

    /**
     * Determines whether transfer syntax.
     *
     * @param ts the ts.
     * @return true if the condition is met; otherwise false.
     */
    public boolean containsTransferSyntax(String ts) {
        return Symbol.STAR.equals(transferSyntaxes[0]) || Builder.contains(transferSyntaxes, ts);
    }

    /**
     * Executes the select transfer syntax operation.
     *
     * @param transferSyntaxes the transfer syntaxes.
     * @return the operation result.
     */
    public String selectTransferSyntax(String... transferSyntaxes) {
        if (transferSyntaxes.length == 1)
            return containsTransferSyntax(transferSyntaxes[0]) ? transferSyntaxes[0] : null;

        List<String> acceptable = retainAcceptable(transferSyntaxes);
        if (acceptable.isEmpty())
            return null;

        for (String prefTransferSyntax : preferredTransferSyntaxes())
            if (acceptable.contains(prefTransferSyntax))
                return prefTransferSyntax;

        return acceptable.get(0);
    }

    /**
     * Executes the retain acceptable operation.
     *
     * @param transferSyntaxes the transfer syntaxes.
     * @return the operation result.
     */
    private List<String> retainAcceptable(String[] transferSyntaxes) {
        List<String> acceptable = new ArrayList<>(transferSyntaxes.length);
        for (String transferSyntax : transferSyntaxes) {
            if (containsTransferSyntax(transferSyntax))
                acceptable.add(transferSyntax);
        }
        return acceptable;
    }

    /**
     * Gets the query options.
     *
     * @return the query options.
     */
    public EnumSet<QueryOption> getQueryOptions() {
        return queryOptions;
    }

    /**
     * Sets the query options.
     *
     * @param queryOptions the query options.
     */
    public void setQueryOptions(EnumSet<QueryOption> queryOptions) {
        this.queryOptions = queryOptions;
    }

    /**
     * Gets the storage options.
     *
     * @return the storage options.
     */
    public StorageOptions getStorageOptions() {
        return storageOptions;
    }

    /**
     * Sets the storage options.
     *
     * @param storageOptions the storage options.
     */
    public void setStorageOptions(StorageOptions storageOptions) {
        this.storageOptions = storageOptions;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return promptTo(new StringBuilder(Normal._512), Normal.EMPTY).toString();
    }

    /**
     * Executes the prompt to operation.
     *
     * @param sb     the sb.
     * @param indent the indent.
     * @return the operation result.
     */
    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + Symbol.SPACE;
        Builder.appendLine(sb, indent, "TransferCapability[cn: ", commonName);
        Builder.appendLine(sb, indent2, "role: ", role);
        sb.append(indent2).append("as: ");
        UID.promptTo(sopClass, sb).append(Builder.LINE_SEPARATOR);
        for (String ts : transferSyntaxes) {
            sb.append(indent2).append("ts: ");
            UID.promptTo(ts, sb).append(Builder.LINE_SEPARATOR);
        }
        if (queryOptions != null)
            sb.append(indent2).append("QueryOptions").append(queryOptions).append(Builder.LINE_SEPARATOR);
        if (storageOptions != null)
            sb.append(indent2).append(storageOptions).append(Builder.LINE_SEPARATOR);
        return sb.append(indent).append(Symbol.C_BRACKET_RIGHT);
    }

    /**
     * Defines the Role values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Role {
        /**
         * Constant for the scu value.
         */
        SCU,
        /**
         * Constant for the scp value.
         */
        SCP

    }

}
