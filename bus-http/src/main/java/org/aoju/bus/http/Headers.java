/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.http;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.StringKit;

import java.util.*;

/**
 * 单个HTTP消息的头字段。值是未解释的字符串;
 * 使用{@code Request}和{@code Response}解释头信息
 * 该类维护HTTP消息中的头字段的顺序
 * 这个类从值中删除空白。它从不返回带开头或结尾空白的值
 *
 * @author Kimi Liu
 * @version 6.0.1
 * @since JDK 1.8+
 */
public final class Headers {

    private final String[] namesAndValues;

    Headers(Builder builder) {
        this.namesAndValues = builder.namesAndValues.toArray(new String[builder.namesAndValues.size()]);
    }

    private Headers(String[] namesAndValues) {
        this.namesAndValues = namesAndValues;
    }

    private static String get(String[] namesAndValues, String name) {
        for (int i = namesAndValues.length - 2; i >= 0; i -= 2) {
            if (name.equalsIgnoreCase(namesAndValues[i])) {
                return namesAndValues[i + 1];
            }
        }
        return null;
    }

    public static Headers of(String... namesAndValues) {
        if (namesAndValues == null) throw new NullPointerException("namesAndValues == null");
        if (namesAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Expected alternating header names and values");
        }

        namesAndValues = namesAndValues.clone();
        for (int i = 0; i < namesAndValues.length; i++) {
            if (namesAndValues[i] == null) throw new IllegalArgumentException("Headers cannot be null");
            namesAndValues[i] = namesAndValues[i].trim();
        }

        for (int i = 0; i < namesAndValues.length; i += 2) {
            String name = namesAndValues[i];
            String value = namesAndValues[i + 1];
            checkName(name);
            checkValue(value, name);
        }

        return new Headers(namesAndValues);
    }

    public static Headers of(Map<String, String> headers) {
        if (headers == null) throw new NullPointerException("headers == null");

        String[] namesAndValues = new String[headers.size() * 2];
        int i = 0;
        for (Map.Entry<String, String> header : headers.entrySet()) {
            if (header.getKey() == null || header.getValue() == null) {
                throw new IllegalArgumentException("Headers cannot be null");
            }
            String name = header.getKey().trim();
            String value = header.getValue().trim();
            checkName(name);
            checkValue(value, name);
            namesAndValues[i] = name;
            namesAndValues[i + 1] = value;
            i += 2;
        }

        return new Headers(namesAndValues);
    }

    static void checkName(String name) {
        if (name == null) throw new NullPointerException("name == null");
        if (name.isEmpty()) throw new IllegalArgumentException("name is empty");
        for (int i = 0, length = name.length(); i < length; i++) {
            char c = name.charAt(i);
            if (c <= '\u0020' || c >= '\u007f') {
                throw new IllegalArgumentException(StringKit.format(
                        "Unexpected char %#04x at %d in header name: %s", (int) c, i, name));
            }
        }
    }

    static void checkValue(String value, String name) {
        if (value == null) throw new NullPointerException("value for name " + name + " == null");
        for (int i = 0, length = value.length(); i < length; i++) {
            char c = value.charAt(i);
            if ((c <= '\u001f' && c != Symbol.C_HT) || c >= '\u007f') {
                throw new IllegalArgumentException(StringKit.format(
                        "Unexpected char %#04x at %d in %s value: %s", (int) c, i, name, value));
            }
        }
    }

    public String get(String name) {
        return get(namesAndValues, name);
    }

    public Date getDate(String name) {
        String value = get(name);
        return value != null ? org.aoju.bus.http.Builder.parse(value) : null;
    }

    public int size() {
        return namesAndValues.length / 2;
    }

    public String name(int index) {
        return namesAndValues[index * 2];
    }

    public String value(int index) {
        return namesAndValues[index * 2 + 1];
    }

    public Set<String> names() {
        TreeSet<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0, size = size(); i < size; i++) {
            result.add(name(i));
        }
        return Collections.unmodifiableSet(result);
    }

    public List<String> values(String name) {
        List<String> result = null;
        for (int i = 0, size = size(); i < size; i++) {
            if (name.equalsIgnoreCase(name(i))) {
                if (result == null) result = new ArrayList<>(2);
                result.add(value(i));
            }
        }
        return result != null
                ? Collections.unmodifiableList(result)
                : Collections.emptyList();
    }

    public long byteCount() {
        long result = namesAndValues.length * 2;

        for (int i = 0, size = namesAndValues.length; i < size; i++) {
            result += namesAndValues[i].length();
        }

        return result;
    }

    public Builder newBuilder() {
        Builder result = new Builder();
        Collections.addAll(result.namesAndValues, namesAndValues);
        return result;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Headers
                && Arrays.equals(((Headers) other).namesAndValues, namesAndValues);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(namesAndValues);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0, size = size(); i < size; i++) {
            result.append(name(i)).append(": ").append(value(i)).append(Symbol.LF);
        }
        return result.toString();
    }

    public Map<String, List<String>> toMultimap() {
        Map<String, List<String>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0, size = size(); i < size; i++) {
            String name = name(i).toLowerCase(Locale.US);
            List<String> values = result.get(name);
            if (values == null) {
                values = new ArrayList<>(2);
                result.put(name, values);
            }
            values.add(value(i));
        }
        return result;
    }

    public static final class Builder {
        final List<String> namesAndValues = new ArrayList<>(20);

        public Builder addLenient(String line) {
            int index = line.indexOf(Symbol.COLON, 1);
            if (index != -1) {
                return addLenient(line.substring(0, index), line.substring(index + 1));
            } else if (line.startsWith(Symbol.COLON)) {
                return addLenient(Normal.EMPTY, line.substring(1));
            } else {
                return addLenient(Normal.EMPTY, line);
            }
        }

        public Builder add(String line) {
            int index = line.indexOf(Symbol.COLON);
            if (index == -1) {
                throw new IllegalArgumentException("Unexpected header: " + line);
            }
            return add(line.substring(0, index).trim(), line.substring(index + 1));
        }

        public Builder add(String name, String value) {
            checkName(name);
            checkValue(value, name);
            return addLenient(name, value);
        }

        public Builder addUnsafeNonAscii(String name, String value) {
            checkName(name);
            return addLenient(name, value);
        }

        public Builder addAll(Headers headers) {
            int size = headers.size();
            for (int i = 0; i < size; i++) {
                addLenient(headers.name(i), headers.value(i));
            }

            return this;
        }

        public Builder add(String name, Date value) {
            if (value == null) throw new NullPointerException("value for name " + name + " == null");
            add(name, org.aoju.bus.http.Builder.format(value));
            return this;
        }

        public Builder set(String name, Date value) {
            if (value == null) throw new NullPointerException("value for name " + name + " == null");
            set(name, org.aoju.bus.http.Builder.format(value));
            return this;
        }

        Builder addLenient(String name, String value) {
            namesAndValues.add(name);
            namesAndValues.add(value.trim());
            return this;
        }

        public Builder removeAll(String name) {
            for (int i = 0; i < namesAndValues.size(); i += 2) {
                if (name.equalsIgnoreCase(namesAndValues.get(i))) {
                    namesAndValues.remove(i); // name
                    namesAndValues.remove(i); // value
                    i -= 2;
                }
            }
            return this;
        }

        public Builder set(String name, String value) {
            checkName(name);
            checkValue(value, name);
            removeAll(name);
            addLenient(name, value);
            return this;
        }

        public String get(String name) {
            for (int i = namesAndValues.size() - 2; i >= 0; i -= 2) {
                if (name.equalsIgnoreCase(namesAndValues.get(i))) {
                    return namesAndValues.get(i + 1);
                }
            }
            return null;
        }

        public Headers build() {
            return new Headers(this);
        }
    }

}
