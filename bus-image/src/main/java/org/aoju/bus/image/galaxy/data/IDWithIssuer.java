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
package org.aoju.bus.image.galaxy.data;

import org.aoju.bus.image.Tag;
import org.aoju.bus.image.galaxy.Property;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class IDWithIssuer {

    public static final IDWithIssuer[] EMPTY = {};

    private final String id;
    private String typeOfPatientID;
    private String identifierTypeCode;
    private Issuer issuer;

    public IDWithIssuer(String id, Issuer issuer) {
        if (id.isEmpty())
            throw new IllegalArgumentException("empty id");
        this.id = id;
        this.setIssuer(issuer);
    }

    public IDWithIssuer(String id, String issuer) {
        this.id = id;
        this.setIssuer(issuer != null ? new Issuer(issuer, '&') : null);
    }

    public IDWithIssuer(String cx) {
        String[] ss = Property.split(cx, '^');
        this.id = ss[0];
        if (ss.length > 3) {
            if (!ss[3].isEmpty())
                this.setIssuer(new Issuer(ss[3], '&'));
            if (ss.length > 4 && !ss[4].isEmpty())
                this.setIdentifierTypeCode(ss[4]);
        }
    }

    public static IDWithIssuer valueOf(Attributes attrs, int idTag,
                                       int issuerSeqTag) {
        String id = attrs.getString(idTag);
        if (id == null)
            return null;

        return new IDWithIssuer(id,
                Issuer.valueOf(attrs.getNestedDataset(issuerSeqTag)));
    }

    public static IDWithIssuer pidOf(Attributes attrs) {
        String id = attrs.getString(Tag.PatientID);
        if (id == null)
            return null;

        IDWithIssuer result =
                new IDWithIssuer(id, Issuer.fromIssuerOfPatientID(attrs));
        result.setTypeOfPatientID(attrs.getString(Tag.TypeOfPatientID));
        result.setIdentifierTypeCode(identifierTypeCodeOf(attrs));
        return result;
    }

    private static String identifierTypeCodeOf(Attributes attrs) {
        Attributes qualifiers = attrs.getNestedDataset(Tag.IssuerOfPatientIDQualifiersSequence);
        return qualifiers != null
                ? qualifiers.getString(Tag.IdentifierTypeCode)
                : null;
    }

    public static Set<IDWithIssuer> pidsOf(Attributes attrs) {
        IDWithIssuer pid = IDWithIssuer.pidOf(attrs);
        Sequence opidseq = attrs.getSequence(Tag.OtherPatientIDsSequence);
        if (opidseq == null)
            if (pid == null)
                return Collections.emptySet();
            else
                return Collections.singleton(pid);

        Set<IDWithIssuer> pids =
                new HashSet<IDWithIssuer>((1 + opidseq.size()) << 1);
        if (pid != null)
            pids.add(pid);
        for (Attributes item : opidseq) {
            pid = IDWithIssuer.pidOf(item);
            if (pid != null)
                pids.add(pid);
        }
        return pids;
    }

    public IDWithIssuer withoutIssuer() {
        return issuer == null ? this : new IDWithIssuer(id, (Issuer) null);
    }

    public final String getID() {
        return id;
    }

    public String getTypeOfPatientID() {
        return typeOfPatientID;
    }

    public void setTypeOfPatientID(String typeOfPatientID) {
        this.typeOfPatientID = typeOfPatientID;
    }

    public final String getIdentifierTypeCode() {
        return identifierTypeCode;
    }

    public final void setIdentifierTypeCode(String identifierTypeCode) {
        this.identifierTypeCode = identifierTypeCode;
    }

    public final Issuer getIssuer() {
        return issuer;
    }

    public final void setIssuer(Issuer issuer) {
        this.issuer = issuer;
    }

    @Override
    public String toString() {
        if (issuer == null && identifierTypeCode == null)
            return id;

        StringBuilder sb = new StringBuilder(id);
        sb.append("^^^");
        if (issuer != null)
            sb.append(issuer.toString('&'));
        if (identifierTypeCode != null)
            sb.append('^').append(identifierTypeCode);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        if (typeOfPatientID != null)
            result += typeOfPatientID.hashCode() * 31;
        if (identifierTypeCode != null)
            result += identifierTypeCode.hashCode() * 31;
        if (issuer != null)
            result += issuer.hashCode() * 31;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof IDWithIssuer))
            return false;
        IDWithIssuer other = (IDWithIssuer) obj;
        return id.equals(other.id) &&
                (typeOfPatientID == null
                        ? other.typeOfPatientID == null
                        : typeOfPatientID.equals(typeOfPatientID)) &&
                (identifierTypeCode == null
                        ? other.identifierTypeCode == null
                        : identifierTypeCode.equals(identifierTypeCode)) &&
                (issuer == null
                        ? other.issuer == null
                        : issuer.equals(other.issuer));
    }

    public boolean matches(IDWithIssuer other) {
        return id.equals(other.id) &&
                (issuer == null
                        ? other.issuer == null
                        : issuer.matches(other.issuer));
    }

    public Attributes exportPatientIDWithIssuer(Attributes attrs) {
        if (attrs == null)
            attrs = new Attributes(3);

        attrs.setString(Tag.PatientID, VR.LO, id);
        if (typeOfPatientID != null) {
            attrs.setString(Tag.TypeOfPatientID, VR.CS, typeOfPatientID);
        }
        if (issuer == null && identifierTypeCode == null) {
            return attrs;
        }

        if (issuer != null)
            issuer.toIssuerOfPatientID(attrs);

        if (identifierTypeCode != null) {
            Attributes item = attrs.getNestedDataset(
                    Tag.IssuerOfPatientIDQualifiersSequence);
            if (item == null) {
                item = new Attributes(1);
                attrs.newSequence(Tag.IssuerOfPatientIDQualifiersSequence, 1)
                        .add(item);
            }
            item.setString(Tag.IdentifierTypeCode, VR.CS, identifierTypeCode);
        }
        return attrs;
    }

}
