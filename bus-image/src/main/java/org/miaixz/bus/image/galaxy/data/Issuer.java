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

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;

/**
 * Represents the Issuer type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Issuer implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852263505712L;

    /**
     * The local namespace entity id value.
     */
    private String localNamespaceEntityID;

    /**
     * The universal entity id value.
     */
    private String universalEntityID;

    /**
     * The universal entity id type value.
     */
    private String universalEntityIDType;

    /**
     * Constructor for persistence frameworks.
     */
    protected Issuer() {
        // No initialization required.
    }

    /**
     * Creates a new instance.
     *
     * @param s the s.
     */
    public Issuer(String s) {
        this(s, Symbol.C_AND);
    }

    /**
     * Creates a new instance.
     *
     * @param s     the s.
     * @param delim the delim.
     */
    public Issuer(String s, char delim) {
        String[] ss = Builder.split(s, delim);
        if (ss.length > 3)
            throw new IllegalArgumentException(s);
        this.localNamespaceEntityID = unescapeHL7Separators(ss[0]);
        this.universalEntityID = ss.length > 1 ? unescapeHL7Separators(ss[1]) : null;
        this.universalEntityIDType = ss.length > 2 ? unescapeHL7Separators(ss[2]) : null;
        validate();
    }

    /**
     * Creates a new instance.
     *
     * @param localNamespaceEntityID the local namespace entity id.
     * @param universalEntityID      the universal entity id.
     * @param universalEntityIDType  the universal entity id type.
     */
    public Issuer(String localNamespaceEntityID, String universalEntityID, String universalEntityIDType) {
        this.localNamespaceEntityID = localNamespaceEntityID;
        this.universalEntityID = universalEntityID;
        this.universalEntityIDType = universalEntityIDType;
        validate();
    }

    /**
     * Creates a new instance.
     *
     * @param issuerOfPatientID the issuer of patient id.
     * @param qualifiers        the qualifiers.
     */
    public Issuer(String issuerOfPatientID, Attributes qualifiers) {
        this(issuerOfPatientID, qualifiers != null ? qualifiers.getString(Tag.UniversalEntityID) : null,
                qualifiers != null ? qualifiers.getString(Tag.UniversalEntityIDType) : null);
    }

    /**
     * Creates a new instance.
     *
     * @param issuerItem the issuer item.
     */
    public Issuer(Attributes issuerItem) {
        this(issuerItem.getString(Tag.LocalNamespaceEntityID), issuerItem.getString(Tag.UniversalEntityID),
                issuerItem.getString(Tag.UniversalEntityIDType));
    }

    /**
     * Creates a new instance.
     *
     * @param other the other.
     */
    public Issuer(Issuer other) {
        this(other.getLocalNamespaceEntityID(), other.getUniversalEntityID(), other.getUniversalEntityIDType());
    }

    /**
     * Executes the from issuer of patient id operation.
     *
     * @param attrs the attrs.
     * @return the operation result.
     */
    public static Issuer fromIssuerOfPatientID(Attributes attrs) {
        String issuerOfPatientID = attrs.getString(Tag.IssuerOfPatientID);
        Attributes qualifiers = attrs.getNestedDataset(Tag.IssuerOfPatientIDQualifiersSequence);
        if (qualifiers != null) {
            String universalEntityID = qualifiers.getString(Tag.UniversalEntityID);
            String universalEntityIDType = qualifiers.getString(Tag.UniversalEntityIDType);
            if (universalEntityID != null && universalEntityIDType != null)
                return new Issuer(issuerOfPatientID, universalEntityID, universalEntityIDType);
        }
        return (issuerOfPatientID != null) ? new Issuer(issuerOfPatientID, null, null) : null;
    }

    /**
     * Executes the value of operation.
     *
     * @param issuerItem the issuer item.
     * @return the operation result.
     */
    public static Issuer valueOf(Attributes issuerItem) {
        if (issuerItem == null)
            return null;

        String localNamespaceEntityID = issuerItem.getString(Tag.LocalNamespaceEntityID);
        String universalEntityID = issuerItem.getString(Tag.UniversalEntityID);
        String universalEntityIDType = issuerItem.getString(Tag.UniversalEntityIDType);

        return (universalEntityID != null && universalEntityIDType != null)
                ? new Issuer(localNamespaceEntityID, universalEntityID, universalEntityIDType)
                : localNamespaceEntityID != null ? new Issuer(localNamespaceEntityID, null, null) : null;
    }

    /**
     * Executes the unescape hl7 separators operation.
     *
     * @param s the s.
     * @return the operation result.
     */
    private static String unescapeHL7Separators(String s) {
        return s.isEmpty() ? null : HL7Separator.unescapeAll(s);
    }

    /**
     * Executes the validate operation.
     */
    private void validate() {
        if (localNamespaceEntityID == null && universalEntityID == null)
            throw new IllegalArgumentException("Missing Local Namespace Entity ID or Universal Entity ID");
        if (universalEntityID != null) {
            if (universalEntityIDType == null)
                throw new IllegalArgumentException("Missing Universal Entity ID Type");
        }
    }

    /**
     * Gets the local namespace entity id.
     *
     * @return the local namespace entity id.
     */
    public final String getLocalNamespaceEntityID() {
        return localNamespaceEntityID;
    }

    /**
     * Gets the universal entity id.
     *
     * @return the universal entity id.
     */
    public final String getUniversalEntityID() {
        return universalEntityID;
    }

    /**
     * Gets the universal entity id type.
     *
     * @return the universal entity id type.
     */
    public final String getUniversalEntityIDType() {
        return universalEntityIDType;
    }

    /**
     * Executes the merge operation.
     *
     * @param other the other.
     * @return true if the condition is met; otherwise false.
     */
    public boolean merge(Issuer other) {
        if (!matches(other, true, true))
            throw new IllegalArgumentException("other=" + other);

        boolean mergeLocalNamespace;
        boolean mergeUniversal;
        if (mergeLocalNamespace = this.localNamespaceEntityID == null && other.localNamespaceEntityID != null) {
            this.localNamespaceEntityID = other.localNamespaceEntityID;
        }
        if (mergeUniversal = this.universalEntityID == null && other.universalEntityID != null) {
            this.universalEntityID = other.universalEntityID;
            this.universalEntityIDType = other.universalEntityIDType;
        }
        return mergeLocalNamespace || mergeUniversal;
    }

    /**
     * Returns the hash code.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public int hashCode() {
        return 37 * (37 * hashCode(localNamespaceEntityID) + hashCode(universalEntityID))
                + hashCode(universalEntityIDType);
    }

    /**
     * Returns the hash code.
     *
     * @param s the s.
     * @return true if the condition is met; otherwise false.
     */
    private int hashCode(String s) {
        return s == null ? 0 : s.hashCode();
    }

    /**
     * Compares this instance with another object for equality.
     *
     * @param o the o.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Issuer other))
            return false;
        return equals(localNamespaceEntityID, other.getLocalNamespaceEntityID())
                && equals(universalEntityID, other.getUniversalEntityID())
                && equals(universalEntityIDType, other.getUniversalEntityIDType());
    }

    /**
     * Compares this instance with another object for equality.
     *
     * @param s1 the s1.
     * @param s2 the s2.
     * @return true if the condition is met; otherwise false.
     */
    private boolean equals(String s1, String s2) {
        return Objects.equals(s1, s2);
    }

    /**
     * Executes the matches operation.
     *
     * @param other the other.
     * @return true if the condition is met; otherwise false.
     */
    public boolean matches(Issuer other) {
        return matches(other, true, false);
    }

    /**
     * Executes the matches operation.
     *
     * @param other             the other.
     * @param matchNoIssuer     the match no issuer.
     * @param matchOnNoMismatch the match on no mismatch.
     * @return true if the condition is met; otherwise false.
     */
    public boolean matches(Issuer other, boolean matchNoIssuer, boolean matchOnNoMismatch) {
        if (this == other)
            return true;

        if (other == null)
            return matchNoIssuer;

        boolean matchLocal = localNamespaceEntityID != null && other.getLocalNamespaceEntityID() != null;
        boolean matchUniversal = universalEntityID != null && other.getUniversalEntityID() != null;

        return !matchLocal && !matchUniversal ? matchOnNoMismatch
                : (!matchLocal || localNamespaceEntityID.equals(other.getLocalNamespaceEntityID()))
                        && (!matchUniversal || universalEntityID.equals(other.getUniversalEntityID())
                                && universalEntityIDType.equals(other.getUniversalEntityIDType()));
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return toString(Symbol.C_AND);
    }

    /**
     * Returns the string representation.
     *
     * @param delim the delim.
     * @return the string representation.
     */
    public String toString(char delim) {
        if (universalEntityID == null)
            return HL7Separator.escapeAll(localNamespaceEntityID);
        StringBuilder sb = new StringBuilder();
        if (localNamespaceEntityID != null)
            sb.append(HL7Separator.escapeAll(localNamespaceEntityID));
        sb.append(delim);
        sb.append(HL7Separator.escapeAll(universalEntityID));
        sb.append(delim);
        sb.append(HL7Separator.escapeAll(universalEntityIDType));
        return sb.toString();
    }

    /**
     * Converts this value to item.
     *
     * @return the operation result.
     */
    public Attributes toItem() {
        int size = 0;
        if (localNamespaceEntityID != null)
            size++;
        if (universalEntityID != null)
            size++;
        if (universalEntityIDType != null)
            size++;

        Attributes item = new Attributes(size);
        if (localNamespaceEntityID != null)
            item.setString(Tag.LocalNamespaceEntityID, VR.UT, localNamespaceEntityID);
        if (universalEntityID != null)
            item.setString(Tag.UniversalEntityID, VR.UT, universalEntityID);
        if (universalEntityIDType != null)
            item.setString(Tag.UniversalEntityIDType, VR.CS, universalEntityIDType);
        return item;
    }

    /**
     * Converts this value to issuer of patient id.
     *
     * @param attrs the attrs.
     * @return the operation result.
     */
    public Attributes toIssuerOfPatientID(Attributes attrs) {
        if (attrs == null)
            attrs = new Attributes(2);
        if (localNamespaceEntityID != null)
            attrs.setString(Tag.IssuerOfPatientID, VR.LO, localNamespaceEntityID);
        if (universalEntityID != null) {
            Attributes item = new Attributes(2);
            item.setString(Tag.UniversalEntityID, VR.UT, universalEntityID);
            item.setString(Tag.UniversalEntityIDType, VR.CS, universalEntityIDType);
            attrs.newSequence(Tag.IssuerOfPatientIDQualifiersSequence, 1).add(item);
        }
        return attrs;
    }

    /**
     * Determines whether lesser qualified than.
     *
     * @param other the other.
     * @return true if the condition is met; otherwise false.
     */
    public boolean isLesserQualifiedThan(Issuer other) {
        return other.universalEntityID != null && (universalEntityID == null
                || other.localNamespaceEntityID != null && localNamespaceEntityID == null);
    }

}
