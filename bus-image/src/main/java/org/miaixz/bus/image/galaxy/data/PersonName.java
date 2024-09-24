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
package org.miaixz.bus.image.galaxy.data;

import java.util.Arrays;
import java.util.StringTokenizer;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PersonName {

    private final String[] fields = new String[15];

    public PersonName() {
    }

    public PersonName(String s) {
        this(s, false);
    }

    public PersonName(String s, boolean lenient) {
        if (s != null)
            parse(s, lenient);
    }

    private static String trim(String s) {
        return s == null || (s = s.trim()).isEmpty() ? null : s;
    }

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
                        Logger.info("illegal PN: {} - truncate illegal component group(s)", s);
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
                        Logger.info("illegal PN: {} - subsumes {}th component in suffix", s, cindex + 1);
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

    public String get(Component c) {
        return get(Group.Alphabetic, c);
    }

    public String get(Group g, Component c) {
        return get(g.ordinal(), c.ordinal());
    }

    public void set(Component c, String s) {
        set(Group.Alphabetic, c, s);
    }

    public void set(Group g, Component c, String s) {
        set(g.ordinal(), c.ordinal(), s);
    }

    private String get(int gindex, int cindex) {
        return fields[gindex * 5 + cindex];
    }

    private void set(int gindex, int cindex, String s) {
        fields[gindex * 5 + cindex] = trim(s);
    }

    public boolean isEmpty() {
        for (Group g : Group.values())
            if (contains(g))
                return false;
        return true;
    }

    public boolean contains(Group g) {
        for (Component c : Component.values())
            if (contains(g, c))
                return true;
        return false;
    }

    public boolean contains(Group g, Component c) {
        return get(g, c) != null;
    }

    public boolean contains(Component c) {
        return contains(Group.Alphabetic, c);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(fields);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this)
            return true;

        if (!(object instanceof PersonName other))
            return false;

        return Arrays.equals(fields, other.fields);
    }

    public enum Component {
        FamilyName, GivenName, MiddleName, NamePrefix, NameSuffix
    }

    public enum Group {
        Alphabetic, Ideographic, Phonetic
    }

}
