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

import java.util.*;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;

/**
 * Represents a DICOM identifier along with its optional issuer information. This class is commonly used for patient
 * IDs, accession numbers, and other identifiers that may be qualified by an issuing authority.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class IDWithIssuer {

    /**
     * The actual identifier string.
     */
    private final String id;
    /**
     * The Type of Patient ID (0008,0005) if applicable.
     */
    private String typeOfPatientID;
    /**
     * The Identifier Type Code (0040,0032) if applicable.
     */
    private String identifierTypeCode;
    /**
     * The {@link Issuer} of this identifier.
     */
    private Issuer issuer;

    /**
     * Constructs an {@code IDWithIssuer} with the specified ID and {@link Issuer}.
     * 
     * @param id     The identifier string. Must not be empty.
     * @param issuer The {@link Issuer} of the ID. Can be {@code null}.
     * @throws IllegalArgumentException if the ID is empty.
     */
    public IDWithIssuer(String id, Issuer issuer) {
        if (id.isEmpty())
            throw new IllegalArgumentException("empty id");
        this.id = id;
        this.setIssuer(issuer);
    }

    /**
     * Constructs an {@code IDWithIssuer} with the specified ID and issuer string. The issuer string is parsed using the
     * '&amp;' separator.
     * 
     * @param id     The identifier string.
     * @param issuer The issuer string, or {@code null}.
     */
    public IDWithIssuer(String id, String issuer) {
        this.id = id;
        this.setIssuer(issuer != null ? new Issuer(issuer, '&') : null);
    }

    /**
     * Constructs an {@code IDWithIssuer} by parsing a DICOM CX (Composite Person Name) formatted string.
     * 
     * @param cx The CX formatted string.
     */
    public IDWithIssuer(String cx) {
        String[] ss = Builder.split(cx, '^');
        this.id = HL7Separator.unescapeAll(ss[0]);
        if (ss.length > 3) {
            if (!ss[3].isEmpty())
                this.setIssuer(new Issuer(ss[3], '&'));
            if (ss.length > 4 && !ss[4].isEmpty())
                this.setIdentifierTypeCode(HL7Separator.unescapeAll(ss[4]));
        }
    }

    /**
     * Creates an {@code IDWithIssuer} instance from {@link Attributes} containing the ID and Issuer of Patient ID
     * Sequence.
     * 
     * @param attrs        The {@link Attributes} to extract information from.
     * @param idTag        The tag for the identifier (e.g., {@link Tag#PatientID}).
     * @param issuerSeqTag The tag for the Issuer of Patient ID Sequence (e.g., {@link Tag#IssuerOfPatientID}).
     * @return An {@code IDWithIssuer} instance, or {@code null} if the ID is not found.
     */
    public static IDWithIssuer valueOf(Attributes attrs, int idTag, int issuerSeqTag) {
        String id = attrs.getString(idTag);
        if (id == null)
            return null;

        return new IDWithIssuer(id, Issuer.valueOf(attrs.getNestedDataset(issuerSeqTag)));
    }

    /**
     * Creates an {@code IDWithIssuer} instance representing a Patient ID from the given {@link Attributes}. This method
     * extracts Patient ID (0010,0020), Issuer of Patient ID (0010,0021), Type of Patient ID (0008,0005), and Identifier
     * Type Code (0040,0032) from the attributes.
     * 
     * @param attrs The {@link Attributes} to extract patient ID information from.
     * @return An {@code IDWithIssuer} instance for the Patient ID, or {@code null} if Patient ID is not found.
     */
    public static IDWithIssuer pidOf(Attributes attrs) {
        String id = attrs.getString(Tag.PatientID);
        if (id == null)
            return null;

        IDWithIssuer result = new IDWithIssuer(id, Issuer.fromIssuerOfPatientID(attrs));
        result.setTypeOfPatientID(attrs.getString(Tag.TypeOfPatientID));
        result.setIdentifierTypeCode(identifierTypeCodeOf(attrs));
        return result;
    }

    /**
     * Extracts the Identifier Type Code (0040,0032) from the Issuer of Patient ID Qualifiers Sequence (0040,0033).
     * 
     * @param attrs The {@link Attributes} containing the sequence.
     * @return The Identifier Type Code string, or {@code null} if not found.
     */
    private static String identifierTypeCodeOf(Attributes attrs) {
        Attributes qualifiers = attrs.getNestedDataset(Tag.IssuerOfPatientIDQualifiersSequence);
        return qualifiers != null ? qualifiers.getString(Tag.IdentifierTypeCode) : null;
    }

    /**
     * Retrieves all Patient IDs (including Other Patient IDs) from the given {@link Attributes}.
     * 
     * @param attrs The {@link Attributes} to extract patient IDs from.
     * @return A {@link Set} of {@code IDWithIssuer} objects representing all patient IDs.
     */
    public static Set<IDWithIssuer> pidsOf(Attributes attrs) {
        IDWithIssuer pid = IDWithIssuer.pidOf(attrs);
        Sequence opidseq = attrs.getSequence(Tag.OtherPatientIDsSequence);
        if (opidseq == null || opidseq.isEmpty())
            if (pid == null)
                return Collections.emptySet();
            else
                return Collections.singleton(pid);

        Set<IDWithIssuer> pids = new LinkedHashSet<>((1 + opidseq.size()) << 1);
        if (pid != null)
            pids.add(pid);
        for (Attributes item : opidseq)
            addTo(IDWithIssuer.pidOf(item), pids);
        return pids;
    }

    /**
     * Retrieves Other Patient IDs from the given {@link Attributes}.
     * 
     * @param attrs The {@link Attributes} to extract other patient IDs from.
     * @return A {@link Set} of {@code IDWithIssuer} objects representing other patient IDs.
     */
    public static Set<IDWithIssuer> opidsOf(Attributes attrs) {
        Sequence opidseq = attrs.getSequence(Tag.OtherPatientIDsSequence);
        if (opidseq == null || opidseq.isEmpty())
            return Collections.emptySet();

        Set<IDWithIssuer> pids = new LinkedHashSet<>((opidseq.size()) << 1);
        for (Attributes item : opidseq)
            addTo(IDWithIssuer.pidOf(item), pids);
        return pids;
    }

    /**
     * Adds an {@code IDWithIssuer} to a set, handling potential duplicates and qualification. If a less qualified
     * matching ID already exists in the set, it is replaced by the more qualified one.
     * 
     * @param pid  The {@code IDWithIssuer} to add.
     * @param pids The {@link Set} to add to.
     */
    private static void addTo(IDWithIssuer pid, Set<IDWithIssuer> pids) {
        if (pid == null)
            return;

        for (Iterator<IDWithIssuer> itr = pids.iterator(); itr.hasNext();) {
            IDWithIssuer next = itr.next();
            if (next.matches(pid, true, false)) {
                // replace existing matching pid if it is lesser qualified
                if (pid.issuer != null && (next.issuer == null || next.issuer.isLesserQualifiedThan(pid.issuer)))
                    itr.remove();
                else
                    return;
            }
        }
        pids.add(pid);
    }

    /**
     * Returns a new {@code IDWithIssuer} instance with the same ID but without any issuer information.
     * 
     * @return A new {@code IDWithIssuer} instance.
     */
    public IDWithIssuer withoutIssuer() {
        return issuer == null ? this : new IDWithIssuer(id, (Issuer) null);
    }

    /**
     * Returns the identifier string.
     * 
     * @return The ID string.
     */
    public final String getID() {
        return id;
    }

    /**
     * Returns the Type of Patient ID (0008,0005).
     * 
     * @return The Type of Patient ID string, or {@code null}.
     */
    public String getTypeOfPatientID() {
        return typeOfPatientID;
    }

    /**
     * Sets the Type of Patient ID (0008,0005).
     * 
     * @param typeOfPatientID The Type of Patient ID string to set.
     */
    public void setTypeOfPatientID(String typeOfPatientID) {
        this.typeOfPatientID = typeOfPatientID;
    }

    /**
     * Returns the Identifier Type Code (0040,0032).
     * 
     * @return The Identifier Type Code string, or {@code null}.
     */
    public final String getIdentifierTypeCode() {
        return identifierTypeCode;
    }

    /**
     * Sets the Identifier Type Code (0040,0032).
     * 
     * @param identifierTypeCode The Identifier Type Code string to set.
     */
    public final void setIdentifierTypeCode(String identifierTypeCode) {
        this.identifierTypeCode = identifierTypeCode;
    }

    /**
     * Returns the {@link Issuer} of this identifier.
     * 
     * @return The {@link Issuer} object, or {@code null}.
     */
    public final Issuer getIssuer() {
        return issuer;
    }

    /**
     * Sets the {@link Issuer} of this identifier.
     * 
     * @param issuer The {@link Issuer} object to set.
     */
    public final void setIssuer(Issuer issuer) {
        this.issuer = issuer;
    }

    @Override
    public String toString() {
        if (issuer == null && identifierTypeCode == null)
            return HL7Separator.escapeAll(id);

        StringBuilder sb = new StringBuilder(HL7Separator.escapeAll(id));
        sb.append("^^^");
        if (issuer != null)
            sb.append(issuer.toString('&'));
        if (identifierTypeCode != null)
            sb.append('^').append(HL7Separator.escapeAll(identifierTypeCode));
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
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (!(object instanceof IDWithIssuer other))
            return false;
        return (Objects.equals(id, other.getID())) && (Objects.equals(typeOfPatientID, other.getTypeOfPatientID()))
                && (Objects.equals(identifierTypeCode, other.getIdentifierTypeCode()))
                && (Objects.equals(issuer, other.issuer));
    }

    /**
     * Tests if this ID equals another ID and this issuer matches the other issuer. If this ID equals the other ID but
     * only this or the other is qualified by an issuer, the test fails.
     * 
     * @param other The {@code IDWithIssuer} to compare.
     * @return {@code true} if this ID equals the other ID and this issuer matches the other issuer, otherwise
     *         {@code false}.
     */
    public boolean matches(IDWithIssuer other) {
        return matches(other, false, false);
    }

    /**
     * Tests if this ID equals another ID and this issuer matches the other issuer. If this ID equals the other ID but
     * only this or the other is qualified by an issuer, the test returns the value passed by parameter
     * {@code matchNoIssuer}.
     * 
     * @param other             The {@code IDWithIssuer} to compare.
     * @param matchNoIssuer     Value returned if only this or the other is qualified by an issuer.
     * @param matchOnNoMismatch Value returned if the issuer of this and the other includes different types of
     *                          identifiers.
     * @return {@code true} if this ID equals the other ID and this issuer matches the other issuer, otherwise
     *         {@code false}.
     */
    public boolean matches(IDWithIssuer other, boolean matchNoIssuer, boolean matchOnNoMismatch) {
        return id.equals(other.id) && (issuer == null ? (other.issuer == null || matchNoIssuer)
                : issuer.matches(other.issuer, matchNoIssuer, matchOnNoMismatch));
    }

    /**
     * Exports the Patient ID and its associated issuer information into the given {@link Attributes}. If {@code attrs}
     * is {@code null}, a new {@link Attributes} object is created.
     * 
     * @param attrs The {@link Attributes} object to export to. Can be {@code null}.
     * @return The {@link Attributes} object containing the Patient ID and issuer information.
     */
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
            Attributes item = attrs.getNestedDataset(Tag.IssuerOfPatientIDQualifiersSequence);
            if (item == null) {
                item = new Attributes(1);
                attrs.newSequence(Tag.IssuerOfPatientIDQualifiersSequence, 1).add(item);
            }
            item.setString(Tag.IdentifierTypeCode, VR.CS, identifierTypeCode);
        }
        return attrs;
    }

}
