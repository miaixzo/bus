/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.tracer.transport.jaxb;

import jakarta.xml.bind.annotation.*;
import org.miaixz.bus.tracer.Builder;

import java.util.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
@XmlRootElement(name = Builder.TPIC_HEADER)
@XmlAccessorType(XmlAccessType.NONE)
public final class TpicMap {

    @XmlElement(name = "entry")
    public final List<Entry> entries;

    public TpicMap(List<Entry> entries) {
        this.entries = entries;
    }

    private TpicMap() {
        entries = null;
    }

    public static TpicMap wrap(Map<String, String> map) {
        final List<Entry> values = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            values.add(new Entry(entry.getKey(), entry.getValue()));
        }
        return new TpicMap(values);
    }

    public Map<String, String> unwrapValues() {
        if (null == entries) {
            return Collections.emptyMap();
        }
        final Map<String, String> map = new HashMap<>();
        for (Entry value : this.entries) {
            map.put(value.key, value.value);
        }
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;

        TpicMap tpicMap = (TpicMap) o;

        if (null != entries ? !entries.equals(tpicMap.entries) : null != tpicMap.entries) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return null != entries ? entries.hashCode() : 0;
    }

    public static final class Entry {

        @XmlAttribute(name = "key", required = true)
        public final String key;
        @XmlValue
        public final String value;

        public Entry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        protected Entry() {
            this.key = null;
            this.value = null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (null == o || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (null != key ? !key.equals(entry.key) : null != entry.key) return false;
            if (null != value ? !value.equals(entry.value) : null != entry.value) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = null != key ? key.hashCode() : 0;
            result = 31 * result + (null != value ? value.hashCode() : 0);
            return result;
        }

    }

}
