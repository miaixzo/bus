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

import java.util.Arrays;
import java.util.StringTokenizer;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.logger.Logger;

/**
 * Represents the PersonName type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PersonName {

    /**
     * The fields value.
     */
    private final String[] fields = new String[15];

    /**
     * Creates a new instance.
     */
    public PersonName() {
        // No initialization required.
    }

    /**
     * Creates a new instance.
     *
     * @param s the s.
     */
    public PersonName(String s) {
        this(s, false);
    }

    /**
     * Creates a new instance.
     *
     * @param s       the s.
     * @param lenient the lenient.
     */
    public PersonName(String s, boolean lenient) {
        if (s != null)
            parse(s, lenient);
    }

    /**
     * Executes the trim operation.
     *
     * @param s the s.
     * @return the operation result.
     */
    private static String trim(String s) {
        return s == null || (s = s.trim()).isEmpty() ? null : s;
    }

    /**
     * Executes the parse operation.
     *
     * @param s       the s.
     * @param lenient the lenient.
     */
    private void parse(String s, boolean lenient) {
        int gindex = 0;
        int cindex = 0;
        StringTokenizer stk = new StringTokenizer(s, "^=", true);
        while (stk.hasMoreTokens()) {
            String tk = stk.nextToken();
            switch (tk.charAt(0)) {
                case Symbol.C_EQUAL:
                    if (++gindex > 2)
                        if (lenient) {
                            Logger.info(
                                    false,
                                    "Image",
                                    "illegal PN: valueChars={} - truncate illegal component group(s)",
                                    s == null ? 0 : s.length());
                            return;
                        } else
                            throw new IllegalArgumentException(s);
                    cindex = 0;
                    break;

                case '^':
                    ++cindex;
                    break;

                default:
                    if (cindex <= 4)
                        set(gindex, cindex, tk);
                    else if (lenient) {
                        if ((tk = trim(tk)) != null) {
                            Logger.info(
                                    false,
                                    "Image",
                                    "illegal PN: valueChars={} - subsumes {}th component in suffix",
                                    s == null ? 0 : s.length(),
                                    cindex + 1);
                            set(gindex, 4, Builder.maskNull(get(gindex, 4), "") + ' ' + tk);
                        }
                    } else
                        throw new IllegalArgumentException(s);
            }
        }
    }

    /**
     * Set all components of a component group from encoded component group value.
     *
     * @param g component group
     * @param s encoded component group value
     */
    public void set(Group g, String s) {
        int gindex = g.ordinal();
        if (s.indexOf('=') >= 0)
            throw new IllegalArgumentException(s);

        String[] ss = Builder.split(s, '^');
        if (ss.length > 5)
            throw new IllegalArgumentException(s);

        for (int cindex = 0; cindex < 5; cindex++) {
            fields[gindex * 5 + cindex] = cindex < ss.length ? trim(ss[cindex]) : null;
        }
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    public String toString() {
        int totLen = 0;
        Group lastGroup = Group.Alphabetic;
        for (Group g : Group.values()) {
            Component lastCompOfGroup = Component.FamilyName;
            for (Component c : Component.values()) {
                String s = get(g, c);
                if (s != null) {
                    totLen += s.length();
                    lastGroup = g;
                    lastCompOfGroup = c;
                }
            }
            totLen += lastCompOfGroup.ordinal();
        }
        totLen += lastGroup.ordinal();
        char[] ch = new char[totLen];
        int wpos = 0;
        for (Group g : Group.values()) {
            Component lastCompOfGroup = Component.FamilyName;
            for (Component c : Component.values()) {
                String s = get(g, c);
                if (s != null) {
                    int d = c.ordinal() - lastCompOfGroup.ordinal();
                    while (d-- > 0)
                        ch[wpos++] = Symbol.C_CARET;
                    d = s.length();
                    s.getChars(0, d, ch, wpos);
                    wpos += d;
                    lastCompOfGroup = c;
                }
            }
            if (g == lastGroup)
                break;
            ch[wpos++] = Symbol.C_EQUAL;
        }
        return new String(ch);
    }

    /**
     * Returns the string representation.
     *
     * @param g    the g.
     * @param trim the trim.
     * @return the string representation.
     */
    public String toString(Group g, boolean trim) {
        int totLen = 0;
        Component lastCompOfGroup = Component.FamilyName;
        for (Component c : Component.values()) {
            String s = get(g, c);
            if (s != null) {
                totLen += s.length();
                lastCompOfGroup = c;
            }
        }
        totLen += trim ? lastCompOfGroup.ordinal() : 4;
        char[] ch = new char[totLen];
        int wpos = 0;
        for (Component c : Component.values()) {
            String s = get(g, c);
            if (s != null) {
                int d = s.length();
                s.getChars(0, d, ch, wpos);
                wpos += d;
            }
            if (trim && c == lastCompOfGroup)
                break;
            if (wpos < ch.length)
                ch[wpos++] = '^';
        }
        return new String(ch);
    }

    /**
     * Executes the get operation.
     *
     * @param c the c.
     * @return the operation result.
     */
    public String get(Component c) {
        return get(Group.Alphabetic, c);
    }

    /**
     * Executes the get operation.
     *
     * @param g the g.
     * @param c the c.
     * @return the operation result.
     */
    public String get(Group g, Component c) {
        return get(g.ordinal(), c.ordinal());
    }

    /**
     * Executes the set operation.
     *
     * @param c the c.
     * @param s the s.
     */
    public void set(Component c, String s) {
        set(Group.Alphabetic, c, s);
    }

    /**
     * Executes the set operation.
     *
     * @param g the g.
     * @param c the c.
     * @param s the s.
     */
    public void set(Group g, Component c, String s) {
        set(g.ordinal(), c.ordinal(), s);
    }

    /**
     * Executes the get operation.
     *
     * @param gindex the gindex.
     * @param cindex the cindex.
     * @return the operation result.
     */
    private String get(int gindex, int cindex) {
        return fields[gindex * 5 + cindex];
    }

    /**
     * Executes the set operation.
     *
     * @param gindex the gindex.
     * @param cindex the cindex.
     * @param s      the s.
     */
    private void set(int gindex, int cindex, String s) {
        fields[gindex * 5 + cindex] = trim(s);
    }

    /**
     * Determines whether empty.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isEmpty() {
        for (Group g : Group.values())
            if (contains(g))
                return false;
        return true;
    }

    /**
     * Executes the contains operation.
     *
     * @param g the g.
     * @return true if the condition is met; otherwise false.
     */
    public boolean contains(Group g) {
        for (Component c : Component.values())
            if (contains(g, c))
                return true;
        return false;
    }

    /**
     * Executes the contains operation.
     *
     * @param g the g.
     * @param c the c.
     * @return true if the condition is met; otherwise false.
     */
    public boolean contains(Group g, Component c) {
        return get(g, c) != null;
    }

    /**
     * Executes the contains operation.
     *
     * @param c the c.
     * @return true if the condition is met; otherwise false.
     */
    public boolean contains(Component c) {
        return contains(Group.Alphabetic, c);
    }

    /**
     * Returns the hash code.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(fields);
    }

    /**
     * Compares this instance with another object for equality.
     *
     * @param object the object.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean equals(Object object) {
        if (object == this)
            return true;

        if (!(object instanceof PersonName other))
            return false;

        return Arrays.equals(fields, other.fields);
    }

    /**
     * Defines the Component values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Component {
        /**
         * Constant for the family name value.
         */
        FamilyName,
        /**
         * Constant for the given name value.
         */
        GivenName,
        /**
         * Constant for the middle name value.
         */
        MiddleName,
        /**
         * Constant for the name prefix value.
         */
        NamePrefix,
        /**
         * Constant for the name suffix value.
         */
        NameSuffix

    }

    /**
     * Defines the Group values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Group {
        /**
         * Constant for the alphabetic value.
         */
        Alphabetic,
        /**
         * Constant for the ideographic value.
         */
        Ideographic,
        /**
         * Constant for the phonetic value.
         */
        Phonetic

    }

}
