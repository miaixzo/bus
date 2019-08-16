package org.aoju.bus.tracer.transport.jaxb;

import org.aoju.bus.tracer.consts.TraceConsts;

import javax.xml.bind.annotation.*;
import java.util.*;

@XmlRootElement(name = TraceConsts.TPIC_HEADER)
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
        if (entries == null) {
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
        if (o == null || getClass() != o.getClass()) return false;

        TpicMap tpicMap = (TpicMap) o;

        if (entries != null ? !entries.equals(tpicMap.entries) : tpicMap.entries != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return entries != null ? entries.hashCode() : 0;
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
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (key != null ? !key.equals(entry.key) : entry.key != null) return false;
            if (value != null ? !value.equals(entry.value) : entry.value != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = key != null ? key.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "key='" + key + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

}
