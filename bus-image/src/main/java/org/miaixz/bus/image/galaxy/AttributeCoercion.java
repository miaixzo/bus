/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.metric.TransferCapability;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class AttributeCoercion implements Serializable, Comparable<AttributeCoercion> {

    private final String commonName;
    private final Condition condition;
    private final String uri;

    public AttributeCoercion(String commonName, String[] sopClasses,
                             Dimse dimse, TransferCapability.Role role, String[] aeTitles, String uri) {
        if (null == commonName)
            throw new NullPointerException("commonName");
        if (commonName.isEmpty())
            throw new IllegalArgumentException("commonName cannot be empty");

        this.commonName = commonName;
        this.condition = new Condition(
                Material.maskNull(sopClasses),
                dimse,
                role,
                Material.maskNull(aeTitles));
        this.uri = uri;
    }

    private static void promptCUIDsTo(StringBuilder sb, String indent,
                                      String[] cuids) {
        if (cuids.length == 0)
            return;
        sb.append(indent).append("cuids: ");
        for (String cuid : cuids)
            UID.promptTo(cuid, sb).append(Symbol.C_COMMA);
        sb.setLength(sb.length() - 1);
        sb.append(Material.LINE_SEPARATOR);
    }

    private static void promptAETsTo(StringBuilder sb, String indent,
                                     String[] aets) {
        if (aets.length == 0)
            return;
        sb.append(indent).append("aets: ");
        for (String aet : aets)
            sb.append(aet).append(Symbol.C_COMMA);
        sb.setLength(sb.length() - 1);
        sb.append(Material.LINE_SEPARATOR);
    }

    public final String getCommonName() {
        return commonName;
    }

    public final String[] getSOPClasses() {
        return condition.sopClasses;
    }

    public final Dimse getDIMSE() {
        return condition.dimse;
    }

    public final TransferCapability.Role getRole() {
        return condition.role;
    }

    public final String[] getAETitles() {
        return condition.aeTitles;
    }

    public final String getURI() {
        return uri;
    }

    public boolean matchesCondition(String sopClass, Dimse dimse, TransferCapability.Role role,
                                    String aeTitle) {
        return condition.matches(sopClass, dimse, role, aeTitle);
    }

    @Override
    public int compareTo(AttributeCoercion o) {
        return condition.compareTo(o.condition);
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder(Normal._64), Normal.EMPTY).toString();
    }

    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + Symbol.SPACE;
        Material.appendLine(sb, indent,
                "AttributeCoercion[cn: ", commonName);
        Material.appendLine(sb, indent2, "dimse: ", condition.dimse);
        Material.appendLine(sb, indent2, "role: ", condition.role);
        promptCUIDsTo(sb, indent2, condition.sopClasses);
        promptAETsTo(sb, indent2, condition.aeTitles);
        Material.appendLine(sb, indent2, "cuids: ",
                Arrays.toString(condition.sopClasses));
        Material.appendLine(sb, indent2, "aets: ",
                Arrays.toString(condition.aeTitles));
        Material.appendLine(sb, indent2, "uri: ", uri);
        return sb.append(indent).append(Symbol.C_BRACKET_RIGHT);
    }

    private static class Condition implements Serializable, Comparable<Condition> {

        final String[] sopClasses;
        final Dimse dimse;
        final TransferCapability.Role role;
        final String[] aeTitles;
        final int weight;

        public Condition(String[] sopClasses, Dimse dimse, TransferCapability.Role role,
                         String[] aeTitles) {
            if (null == dimse)
                throw new NullPointerException("dimse");
            if (null == role)
                throw new NullPointerException("role");

            this.sopClasses = sopClasses;
            this.dimse = dimse;
            this.role = role;
            this.aeTitles = aeTitles;
            this.weight = (aeTitles.length != 0 ? 2 : 0)
                    + (sopClasses.length != 0 ? 1 : 0);
        }

        private static boolean isEmptyOrContains(Object[] a, Object o) {
            if (null == o || a.length == 0)
                return true;

            for (int i = 0; i < a.length; i++)
                if (o.equals(a[i]))
                    return true;

            return false;
        }

        @Override
        public int compareTo(Condition o) {
            return o.weight - weight;
        }

        public boolean matches(String sopClass, Dimse dimse, TransferCapability.Role role,
                               String aeTitle) {
            return this.dimse == dimse
                    && this.role == role
                    && isEmptyOrContains(this.aeTitles, aeTitle)
                    && isEmptyOrContains(this.sopClasses, sopClass);
        }

    }

}
