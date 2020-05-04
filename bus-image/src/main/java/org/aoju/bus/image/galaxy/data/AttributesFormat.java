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

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.image.Tag;
import org.aoju.bus.image.UID;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class AttributesFormat extends Format {

    private static final long serialVersionUID = 1L;

    private static final char[] CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v'
    };
    private static final int LONG_BYTES = 8;

    private final String pattern;
    private final int[][] tagPaths;
    private final int[] index;
    private final int[] offsets;
    private final Type[] types;
    private final MessageFormat format;

    public AttributesFormat(String pattern) {
        ArrayList<String> tokens = tokenize(pattern);
        int n = tokens.size() / 2;
        this.pattern = pattern;
        this.tagPaths = new int[n][];
        this.index = new int[n];
        this.types = new Type[n];
        this.offsets = new int[n];
        this.format = buildMessageFormat(tokens);
    }

    public static AttributesFormat valueOf(String s) {
        return s != null ? new AttributesFormat(s) : null;
    }

    private ArrayList<String> tokenize(String s) {
        ArrayList<String> result = new ArrayList<String>();
        StringTokenizer stk = new StringTokenizer(s, "{}", true);
        String tk;
        char delim;
        char prevDelim = '}';
        int level = 0;
        StringBuilder sb = new StringBuilder();
        while (stk.hasMoreTokens()) {
            tk = stk.nextToken();
            delim = tk.charAt(0);
            if (delim == '{') {
                if (level++ == 0) {
                    if (prevDelim == '}')
                        result.add("");
                } else {
                    sb.append(delim);
                }
            } else if (delim == '}') {
                if (--level == 0) {
                    result.add(sb.toString());
                    sb.setLength(0);
                } else if (level > 0) {
                    sb.append(delim);
                } else
                    throw new IllegalArgumentException(s);
            } else {
                if (level == 0)
                    result.add(tk);
                else
                    sb.append(tk);
            }
            prevDelim = delim;
        }
        return result;
    }

    private MessageFormat buildMessageFormat(ArrayList<String> tokens) {
        StringBuilder formatBuilder = new StringBuilder(pattern.length());
        int j = 0;
        for (int i = 0; i < tagPaths.length; i++) {
            formatBuilder.append(tokens.get(j++)).append('{').append(i);
            String tagStr = tokens.get(j++);
            int typeStart = tagStr.indexOf(Symbol.C_COMMA) + 1;
            boolean rnd = tagStr.startsWith("rnd");
            if (!rnd && !tagStr.startsWith("now")) {
                int tagStrLen = typeStart != 0
                        ? typeStart - 1
                        : tagStr.length();

                int indexStart = tagStr.charAt(tagStrLen - 1) == ']'
                        ? tagStr.lastIndexOf('[', tagStrLen - 3) + 1
                        : 0;
                try {
                    tagPaths[i] = Tag.parseTagPath(tagStr.substring(0, indexStart != 0 ? indexStart - 1 : tagStrLen));
                    if (indexStart != 0)
                        index[i] = Integer.parseInt(tagStr.substring(indexStart, tagStrLen - 1));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(pattern);
                }
            }
            if (typeStart != 0) {
                int typeEnd = tagStr.indexOf(Symbol.C_COMMA, typeStart);
                try {
                    types[i] = Type.valueOf(tagStr.substring(typeStart,
                            typeEnd < 0 ? tagStr.length() : typeEnd));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(pattern);
                }
                switch (types[i]) {
                    case number:
                    case date:
                    case time:
                    case choice:
                        formatBuilder.append(
                                typeStart > 0 ? tagStr.substring(typeStart - 1) : tagStr);
                        break;
                    case offset:
                        try {
                            offsets[i] = Integer.parseInt(tagStr.substring(typeEnd + 1));
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException(pattern);
                        }
                }
            } else {
                types[i] = Type.none;
            }
            if (rnd) {
                switch (types[i]) {
                    case none:
                        types[i] = Type.rnd;
                    case uuid:
                    case uid:
                        break;
                    default:
                        throw new IllegalArgumentException(pattern);
                }
            }
            formatBuilder.append('}');
        }
        if (j < tokens.size())
            formatBuilder.append(tokens.get(j));
        try {
            return new MessageFormat(formatBuilder.toString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(pattern);
        }
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer result, FieldPosition pos) {
        return format.format(toArgs((Attributes) obj), result, pos);
    }

    private Object[] toArgs(Attributes attrs) {
        Object[] args = new Object[tagPaths.length];
        for (int i = 0; i < args.length; i++) {
            int[] tagPath = tagPaths[i];
            if (tagPath == null) { // now
                args[i] = types[i].toArg(attrs, 0, index[i], offsets[i]);
            } else {
                int last = tagPath.length - 1;
                Attributes item = attrs;
                for (int j = 0; j < last && item != null; j++) {
                    item = item.getNestedDataset(tagPath[j]);
                }
                args[i] = item != null ? types[i].toArg(item, tagPath[last], index[i], offsets[i]) : null;
            }
        }
        return args;
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return pattern;
    }

    private enum Type {
        none {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset) {
                return attrs.getString(tag, index);
            }
        },
        number {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset) {
                return attrs.getDouble(tag, index, 0.);
            }
        },
        offset {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset) {
                return Integer.toString(attrs.getInt(tag, index, 0) + offset);
            }
        },
        date {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset) {
                return tag != 0 ? attrs.getDate(tag, index) : new Date();
            }
        },
        time {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset) {
                return tag != 0 ? attrs.getDate(tag, index) : new Date();
            }
        },
        choice {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset) {
                return attrs.getDouble(tag, index, 0.);
            }
        },
        hash {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset) {
                String s = attrs.getString(tag, index);
                return s != null ? Tag.toHexString(s.hashCode()) : null;
            }
        },
        md5 {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset) {
                String s = attrs.getString(tag, index);
                return s != null ? getMD5String(s) : null;
            }
        },
        urlencoded {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset) {
                String s = attrs.getString(tag, index);
                try {
                    return s != null ? URLEncoder.encode(s, "UTF-8") : null;
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError(e);
                }
            }
        },
        rnd {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset) {
                return Tag.toHexString(ThreadLocalRandom.current().nextInt());
            }
        },
        uuid {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset) {
                return UUID.randomUUID();
            }
        },
        uid {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset) {
                return UID.createUID();
            }
        };

        static String toString32(byte[] ba) {
            long l1 = toLong(ba, 0);
            long l2 = toLong(ba, LONG_BYTES);
            char[] ca = new char[26];
            for (int i = 0; i < 12; i++) {
                ca[i] = CHARS[(int) l1 & 0x1f];
                l1 = l1 >>> 5;
            }
            l1 = l1 | (l2 & 1) << 4;
            ca[12] = CHARS[(int) l1 & 0x1f];
            l2 = l2 >>> 1;
            for (int i = 13; i < 26; i++) {
                ca[i] = CHARS[(int) l2 & 0x1f];
                l2 = l2 >>> 5;
            }

            return new String(ca);
        }

        static long toLong(byte[] ba, int offset) {
            long l = 0;
            for (int i = offset, len = offset + LONG_BYTES; i < len; i++) {
                l |= ba[i] & 0xFF;
                l <<= 8;
            }
            return l;
        }

        abstract Object toArg(Attributes attrs, int tag, int index, int offset);

        String getMD5String(String s) {
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                digest.update(s == null ? new byte[0] : s.getBytes(StandardCharsets.UTF_8));
                return toString32(digest.digest());
            } catch (NoSuchAlgorithmException e) {
                return s;
            }
        }
    }

}
