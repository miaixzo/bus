/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.image.galaxy.data;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.image.Tag;
import org.aoju.bus.image.galaxy.Property;

import java.io.Serializable;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Issuer implements Serializable {

    private String localNamespaceEntityID;
    private String universalEntityID;
    private String universalEntityIDType;

    public Issuer(String localNamespaceEntityID, String universalEntityID,
                  String universalEntityIDType) {
        this.localNamespaceEntityID = localNamespaceEntityID;
        this.universalEntityID = universalEntityID;
        this.universalEntityIDType = universalEntityIDType;
        validate();
    }

    protected Issuer() {

    }

    public Issuer(String s) {
        this(s, Symbol.C_AND);
    }

    public Issuer(String s, char delim) {
        String[] ss = Property.split(s, delim);
        if (ss.length > 3)
            throw new IllegalArgumentException(s);
        this.localNamespaceEntityID = emptyToNull(ss[0]);
        this.universalEntityID = ss.length > 1 ? emptyToNull(ss[1]) : null;
        this.universalEntityIDType = ss.length > 2 ? emptyToNull(ss[2]) : null;
        validate();
    }

    public Issuer(String issuerOfPatientID, Attributes qualifiers) {
        this(issuerOfPatientID,
                null != qualifiers ? qualifiers.getString(Tag.UniversalEntityID) : null,
                null != qualifiers ? qualifiers.getString(Tag.UniversalEntityIDType) : null);
    }

    public Issuer(Attributes issuerItem) {
        this(issuerItem.getString(Tag.LocalNamespaceEntityID),
                issuerItem.getString(Tag.UniversalEntityID),
                issuerItem.getString(Tag.UniversalEntityIDType));
    }

    public Issuer(Issuer other) {
        this(other.getLocalNamespaceEntityID(),
                other.getUniversalEntityID(),
                other.getUniversalEntityIDType());
    }

    public static Issuer fromIssuerOfPatientID(Attributes attrs) {
        String issuerOfPatientID = attrs.getString(Tag.IssuerOfPatientID);
        Attributes qualifiers = attrs.getNestedDataset(Tag.IssuerOfPatientIDQualifiersSequence);
        if (null != qualifiers) {
            String universalEntityID = qualifiers.getString(Tag.UniversalEntityID);
            String universalEntityIDType = qualifiers.getString(Tag.UniversalEntityIDType);
            if (null != universalEntityID && null != universalEntityIDType)
                return new Issuer(issuerOfPatientID, universalEntityID, universalEntityIDType);
        }
        return null != issuerOfPatientID
                ? new Issuer(issuerOfPatientID, null, null)
                : null;
    }

    public static Issuer valueOf(Attributes issuerItem) {
        if (null == issuerItem)
            return null;

        String localNamespaceEntityID = issuerItem.getString(Tag.LocalNamespaceEntityID);
        String universalEntityID = issuerItem.getString(Tag.UniversalEntityID);
        String universalEntityIDType = issuerItem.getString(Tag.UniversalEntityIDType);

        return (null != universalEntityID && null != universalEntityIDType)
                ? new Issuer(localNamespaceEntityID, universalEntityID, universalEntityIDType)
                : null != localNamespaceEntityID
                ? new Issuer(localNamespaceEntityID, null, null)
                : null;
    }

    private void validate() {
        if (null == localNamespaceEntityID && null == universalEntityID)
            throw new IllegalArgumentException(
                    "Missing Local Namespace Entity ID or Universal Entity ID");
        if (null != universalEntityID) {
            if (null == universalEntityIDType)
                throw new IllegalArgumentException("Missing Universal Entity ID Type");
        }
    }

    private String emptyToNull(String s) {
        return s.isEmpty() ? null : s;
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
        if (!matches(other))
            throw new IllegalArgumentException("other=" + other);

        boolean mergeLocalNamespace;
        boolean mergeUniversal;
        if (mergeLocalNamespace = null == this.localNamespaceEntityID
                && null != other.localNamespaceEntityID) {
            this.localNamespaceEntityID = other.localNamespaceEntityID;
        }
        if (mergeUniversal = null == this.universalEntityID
                && null != other.universalEntityID) {
            this.universalEntityID = other.universalEntityID;
            this.universalEntityIDType = other.universalEntityIDType;
        }
        return mergeLocalNamespace || mergeUniversal;
    }

    @Override
    public int hashCode() {
        return 37 * (
                37 * hashCode(localNamespaceEntityID)
                        + hashCode(universalEntityID))
                + hashCode(universalEntityIDType);
    }

    private int hashCode(String s) {
        return null == s ? 0 : s.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Issuer))
            return false;
        Issuer other = (Issuer) o;
        return equals(localNamespaceEntityID, other.localNamespaceEntityID)
                && equals(universalEntityID, other.universalEntityID)
                && equals(universalEntityIDType, other.universalEntityIDType);
    }

    private boolean equals(String s1, String s2) {
        return s1 == s2 || null != s1 && s1.equals(s2);
    }

    public boolean matches(Issuer other) {
        if (this == other || null == other)
            return true;

        boolean matchLocal = null != localNamespaceEntityID
                && null != other.localNamespaceEntityID;
        boolean matchUniversal = null != universalEntityID
                && null != other.universalEntityID;

        return (matchLocal || matchUniversal)
                && (!matchLocal
                || localNamespaceEntityID.equals(other.localNamespaceEntityID))
                && (!matchUniversal
                || universalEntityID.equals(other.universalEntityID)
                && universalEntityIDType.equals(other.universalEntityIDType));
    }

    @Override
    public String toString() {
        return toString(Symbol.C_AND);
    }

    public String toString(char delim) {
        if (null == universalEntityID)
            return localNamespaceEntityID;
        StringBuilder sb = new StringBuilder();
        if (null != localNamespaceEntityID) {
            sb.append(localNamespaceEntityID);
        }
        sb.append(delim);
        sb.append(universalEntityID);
        sb.append(delim);
        sb.append(universalEntityIDType);
        return sb.toString();
    }

    public Attributes toItem() {
        int size = 0;
        if (null != localNamespaceEntityID) {
            size++;
        }

        if (null != universalEntityID) {
            size++;
        }

        if (null != universalEntityIDType) {
            size++;
        }


        Attributes item = new Attributes(size);
        if (null != localNamespaceEntityID) {
            item.setString(Tag.LocalNamespaceEntityID, VR.UT, localNamespaceEntityID);
        }

        if (null != universalEntityID) {
            item.setString(Tag.UniversalEntityID, VR.UT, universalEntityID);
        }

        if (null != universalEntityIDType) {
            item.setString(Tag.UniversalEntityIDType, VR.CS, universalEntityIDType);
        }

        return item;
    }

    public Attributes toIssuerOfPatientID(Attributes attrs) {
        if (null == attrs) {
            attrs = new Attributes(2);
        }

        if (null != localNamespaceEntityID) {
            attrs.setString(Tag.IssuerOfPatientID, VR.LO, localNamespaceEntityID);
        }

        if (null != universalEntityID) {
            Attributes item = new Attributes(2);
            item.setString(Tag.UniversalEntityID, VR.UT, universalEntityID);
            item.setString(Tag.UniversalEntityIDType, VR.CS, universalEntityIDType);
            attrs.newSequence(Tag.IssuerOfPatientIDQualifiersSequence, 1).add(item);
        }
        return attrs;
    }

}
