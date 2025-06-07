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
package org.miaixz.bus.image.galaxy.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Issuer implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852263505712L;

    private String localNamespaceEntityID;
    private String universalEntityID;
    private String universalEntityIDType;

    public Issuer(String localNamespaceEntityID, String universalEntityID, String universalEntityIDType) {
        this.localNamespaceEntityID = localNamespaceEntityID;
        this.universalEntityID = universalEntityID;
        this.universalEntityIDType = universalEntityIDType;
        validate();
    }

    public Issuer(String s) {
        this(s, Symbol.C_AND);
    }

    public Issuer(String s, char delim) {
        String[] ss = Builder.split(s, delim);
        if (ss.length > 3)
            throw new IllegalArgumentException(s);
        this.localNamespaceEntityID = unescapeHL7Separators(ss[0]);
        this.universalEntityID = ss.length > 1 ? unescapeHL7Separators(ss[1]) : null;
        this.universalEntityIDType = ss.length > 2 ? unescapeHL7Separators(ss[2]) : null;
        validate();
    }

    public Issuer(String issuerOfPatientID, Attributes qualifiers) {
        this(issuerOfPatientID, qualifiers != null ? qualifiers.getString(Tag.UniversalEntityID) : null,
                qualifiers != null ? qualifiers.getString(Tag.UniversalEntityIDType) : null);
    }

    public Issuer(Attributes issuerItem) {
        this(issuerItem.getString(Tag.LocalNamespaceEntityID), issuerItem.getString(Tag.UniversalEntityID),
                issuerItem.getString(Tag.UniversalEntityIDType));
    }

    public Issuer(Issuer other) {
        this(other.getLocalNamespaceEntityID(), other.getUniversalEntityID(), other.getUniversalEntityIDType());
    }

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

    private static String unescapeHL7Separators(String s) {
        return s.isEmpty() ? null : HL7Separator.unescapeAll(s);
    }

    private void validate() {
        if (localNamespaceEntityID == null && universalEntityID == null)
            throw new IllegalArgumentException("Missing Local Namespace Entity ID or Universal Entity ID");
        if (universalEntityID != null) {
            if (universalEntityIDType == null)
                throw new IllegalArgumentException("Missing Universal Entity ID Type");
        }
    }

    public final String getLocalNamespaceEntityID() {
        return localNamespaceEntityID;
    }

    public final String getUniversalEntityID() {
        return universalEntityID;
    }

    public final String getUniversalEntityIDType() {
        return universalEntityIDType;
    }

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

    @Override
    public int hashCode() {
        return 37 * (37 * hashCode(localNamespaceEntityID) + hashCode(universalEntityID))
                + hashCode(universalEntityIDType);
    }

    private int hashCode(String s) {
        return s == null ? 0 : s.hashCode();
    }

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

    private boolean equals(String s1, String s2) {
        return Objects.equals(s1, s2);
    }

    public boolean matches(Issuer other) {
        return matches(other, true, false);
    }

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

    @Override
    public String toString() {
        return toString(Symbol.C_AND);
    }

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

    public boolean isLesserQualifiedThan(Issuer other) {
        return other.universalEntityID != null && (universalEntityID == null
                || other.localNamespaceEntityID != null && localNamespaceEntityID == null);
    }

}
