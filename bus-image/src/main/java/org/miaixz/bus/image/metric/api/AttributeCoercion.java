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
package org.miaixz.bus.image.metric.api;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.metric.TransferCapability;

/**
 * Compares related values. Provides DICOM processing details.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AttributeCoercion implements Serializable, Comparable<AttributeCoercion> {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852262397936L;

    /**
     * Provides DICOM processing details.
     */
    private final String commonName;

    /**
     * Provides DICOM processing details.
     */
    private final Condition condition;

    /**
     * Provides DICOM processing details.
     */
    private final String uri;

    /**
     * Creates a new instance.
     *
     * @param commonName the common name.
     * @param sopClasses the sop classes.
     * @param dimse      the dimse.
     * @param role       the role.
     * @param aeTitles   the ae titles.
     * @param uri        the uri.
     * @throws NullPointerException     if the operation cannot be completed.
     * @throws IllegalArgumentException if the operation cannot be completed.
     */
    public AttributeCoercion(String commonName, String[] sopClasses, Dimse dimse, TransferCapability.Role role,
            String[] aeTitles, String uri) {
        if (commonName == null)
            throw new NullPointerException("commonName");
        if (commonName.isEmpty())
            throw new IllegalArgumentException("commonName cannot be empty");
        this.commonName = commonName;
        this.condition = new Condition(Builder.maskNull(sopClasses), dimse, role, Builder.maskNull(aeTitles));
        this.uri = uri;
    }

    /**
     * Adds the related value.
     *
     * @param sb     the sb.
     * @param indent the indent.
     * @param cuids  the cuids.
     */
    private static void promptCUIDsTo(StringBuilder sb, String indent, String[] cuids) {
        if (cuids.length == 0)
            return;
        sb.append(indent).append("cuids: ");
        for (String cuid : cuids)
            UID.promptTo(cuid, sb).append(Symbol.C_COMMA);
        sb.setLength(sb.length() - 1);
        sb.append(Builder.LINE_SEPARATOR);
    }

    /**
     * Adds the related value.
     *
     * @param sb     the sb.
     * @param indent the indent.
     * @param aets   the aets.
     */
    private static void promptAETsTo(StringBuilder sb, String indent, String[] aets) {
        if (aets.length == 0)
            return;
        sb.append(indent).append("aets: ");
        for (String aet : aets)
            sb.append(aet).append(Symbol.C_COMMA);
        sb.setLength(sb.length() - 1);
        sb.append(Builder.LINE_SEPARATOR);
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public final String getCommonName() {
        return commonName;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public final String[] getSOPClasses() {
        return condition.sopClasses;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public final Dimse getDIMSE() {
        return condition.dimse;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public final TransferCapability.Role getRole() {
        return condition.role;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public final String[] getAETitles() {
        return condition.aeTitles;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public final String getURI() {
        return uri;
    }

    /**
     * Determines whether the condition is met.
     *
     * @param sopClass the sop class.
     * @param dimse    the dimse.
     * @param role     the role.
     * @param aeTitle  the ae title.
     * @return true if the condition is met; otherwise false.
     */
    public boolean matchesCondition(String sopClass, Dimse dimse, TransferCapability.Role role, String aeTitle) {
        return condition.matches(sopClass, dimse, role, aeTitle);
    }

    /**
     * Compares related values.
     *
     * @param o the o.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public int compareTo(AttributeCoercion o) {
        return condition.compareTo(o.condition);
    }

    /**
     * Returns the related value.
     *
     * @return the result.
     */
    @Override
    public String toString() {
        return promptTo(new StringBuilder(Normal._64), Normal.EMPTY).toString();
    }

    /**
     * Adds the related value.
     *
     * @param sb     the sb.
     * @param indent the indent.
     * @return the result.
     */
    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + Symbol.SPACE;
        Builder.appendLine(sb, indent, "AttributeCoercion[cn: ", commonName);
        Builder.appendLine(sb, indent2, "dimse: ", condition.dimse);
        Builder.appendLine(sb, indent2, "role: ", condition.role);
        promptCUIDsTo(sb, indent2, condition.sopClasses);
        promptAETsTo(sb, indent2, condition.aeTitles);
        Builder.appendLine(sb, indent2, "cuids: ", Arrays.toString(condition.sopClasses));
        Builder.appendLine(sb, indent2, "aets: ", Arrays.toString(condition.aeTitles));
        Builder.appendLine(sb, indent2, "uri: ", uri);
        return sb.append(indent).append(Symbol.C_BRACKET_RIGHT);
    }

    /**
     * Provides DICOM processing details.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static class Condition implements Serializable, Comparable<Condition> {

        /**
         * The serial version uid value.
         */
        @Serial
        private static final long serialVersionUID = 2852262539097L;

        /**
         * Provides DICOM processing details.
         */
        final String[] sopClasses;

        /**
         * Provides DICOM processing details.
         */
        final Dimse dimse;

        /**
         * Provides DICOM processing details.
         */
        final TransferCapability.Role role;

        /**
         * Provides DICOM processing details.
         */
        final String[] aeTitles;

        /**
         * Compares related values.
         */
        final int weight;

        /**
         * Creates a new instance.
         *
         * @param sopClasses the sop classes.
         * @param dimse      the dimse.
         * @param role       the role.
         * @param aeTitles   the ae titles.
         * @throws NullPointerException if the operation cannot be completed.
         */
        public Condition(String[] sopClasses, Dimse dimse, TransferCapability.Role role, String[] aeTitles) {
            if (dimse == null)
                throw new NullPointerException("dimse");
            if (role == null)
                throw new NullPointerException("role");
            this.sopClasses = sopClasses;
            this.dimse = dimse;
            this.role = role;
            this.aeTitles = aeTitles;
            this.weight = (aeTitles.length != 0 ? 2 : 0) + (sopClasses.length != 0 ? 1 : 0);
        }

        /**
         * Determines whether the condition is met.
         *
         * @param a the a.
         * @param o the o.
         * @return true if the condition is met; otherwise false.
         */
        private static boolean isEmptyOrContains(Object[] a, Object o) {
            if (o == null || a.length == 0)
                return true;
            for (Object object : a)
                if (o.equals(object))
                    return true;
            return false;
        }

        /**
         * Compares related values.
         *
         * @param o the o.
         * @return true if the condition is met; otherwise false.
         */
        @Override
        public int compareTo(Condition o) {
            return o.weight - weight;
        }

        /**
         * Determines whether the condition is met.
         *
         * @param sopClass the sop class.
         * @param dimse    the dimse.
         * @param role     the role.
         * @param aeTitle  the ae title.
         * @return true if the condition is met; otherwise false.
         */
        public boolean matches(String sopClass, Dimse dimse, TransferCapability.Role role, String aeTitle) {
            return this.dimse == dimse && this.role == role && isEmptyOrContains(this.aeTitles, aeTitle)
                    && isEmptyOrContains(this.sopClasses, sopClass);
        }

    }

}
