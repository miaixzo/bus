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
package org.miaixz.bus.image.metric.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.ToLongFunction;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Format;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.*;
import org.miaixz.bus.image.galaxy.data.PersonName.Group;
import org.miaixz.bus.logger.Logger;

import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;
import jakarta.json.stream.JsonParsingException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class JSONReader {

    private static final Code[] EMPTY_CODES = {};
    private static final int CONN_REF_INDEX_START = "/dicomNetworkConnection/".length();

    private final JsonParser parser;
    private final ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
    private final EnumMap<Group, String> pnGroups = new EnumMap<>(Group.class);
    private boolean skipBulkDataURI;
    private BulkData.Creator bulkDataCreator = BulkData::new;
    private Attributes fmi;
    private Event event;
    private String text;
    private int level = -1;

    public JSONReader(JsonParser parser) {
        this.parser = Objects.requireNonNull(parser);
    }

    public static int toConnectionIndex(String connRef) {
        return Integer.parseInt(connRef.substring(CONN_REF_INDEX_START));
    }

    public boolean isSkipBulkDataURI() {
        return skipBulkDataURI;
    }

    public void setSkipBulkDataURI(boolean skipBulkDataURI) {
        this.skipBulkDataURI = skipBulkDataURI;
    }

    public void setBulkDataCreator(BulkData.Creator bulkDataCreator) {
        this.bulkDataCreator = Objects.requireNonNull(bulkDataCreator);
    }

    public Attributes getFileMetaInformation() {
        return fmi;
    }

    public Event next() {
        text = null;
        return event = parser.next();
    }

    public String getString() {
        if (text == null)
            text = parser.getString();
        return text;
    }

    public void expect(Event expected) {
        if (this.event != expected)
            throw new JsonParsingException("Unexpected " + event + ", expected " + expected, parser.getLocation());
    }

    private String valueString() {
        next();
        expect(Event.VALUE_STRING);
        return getString();
    }

    public Attributes readDataset(Attributes attrs) {
        boolean wrappedInArray = next() == Event.START_ARRAY;
        if (wrappedInArray)
            next();
        expect(Event.START_OBJECT);
        if (attrs == null) {
            attrs = new Attributes();
        }
        fmi = null;
        next();
        doReadDataset(attrs);
        if (wrappedInArray)
            next();
        return attrs;
    }

    public void readDatasets(Callback callback) {
        next();
        expect(Event.START_ARRAY);
        Attributes attrs;
        while (next() == Event.START_OBJECT) {
            fmi = null;
            attrs = new Attributes();
            next();
            doReadDataset(attrs);
            callback.onDataset(fmi, attrs);
        }
        expect(Event.END_ARRAY);
    }

    private Attributes doReadDataset(Attributes attrs) {
        level++;
        while (event == Event.KEY_NAME) {
            readAttribute(attrs);
            next();
        }
        expect(Event.END_OBJECT);
        attrs.trimToSize();
        level--;
        return attrs;
    }

    private void readAttribute(Attributes attrs) {
        int tag = (int) Long.parseLong(getString(), 16);
        if (level == 0 && Tag.isFileMetaInformation(tag)) {
            if (fmi == null)
                fmi = new Attributes();
            attrs = fmi;
        }
        next();
        expect(Event.START_OBJECT);
        Element el = new Element();
        while (next() == Event.KEY_NAME) {
            switch (getString()) {
            case "vr":
                try {
                    el.vr = VR.valueOf(valueString());
                } catch (IllegalArgumentException e) {
                    el.vr = ElementDictionary.getStandardElementDictionary().vrOf(tag);
                    Logger.info("Invalid vr: '{}' at {} - treat as '{}'", getString(), parser.getLocation(), el.vr);
                }
                break;
            case "Value":
                el.values = readValues();
                break;
            case "InlineBinary":
                el.bytes = readInlineBinary();
                break;
            case "BulkDataURI":
                el.bulkDataURI = valueString();
                break;
            case "DataFragment":
                el.values = readDataFragments();
                break;
            default:
                throw new JsonParsingException("Unexpected \"" + getString()
                        + "\", expected \"Value\" or \"InlineBinary\"" + " or \"BulkDataURI\" or  \"DataFragment\"",
                        parser.getLocation());
            }
        }
        expect(Event.END_OBJECT);
        if (el.vr == null) {
            el.vr = ElementDictionary.getStandardElementDictionary().vrOf(tag);
            if (el.vr == null) {
                el.vr = VR.UN;
            }
            Logger.info("Missing property: vr at {} - treat as '{}'", parser.getLocation(), el.vr);
        }
        if (el.isEmpty())
            attrs.setNull(tag, el.vr);
        else if (el.bulkDataURI != null) {
            if (!skipBulkDataURI)
                attrs.setValue(tag, el.vr, bulkDataCreator.create(null, el.bulkDataURI, false));
        } else
            switch (el.vr) {
            case AE:
            case AS:
            case AT:
            case CS:
            case DA:
            case DS:
            case DT:
            case LO:
            case LT:
            case PN:
            case IS:
            case SH:
            case ST:
            case TM:
            case UC:
            case UI:
            case UR:
            case UT:
                attrs.setString(tag, el.vr, el.toStrings());
                break;
            case FL:
            case FD:
                attrs.setDouble(tag, el.vr, el.toDoubles());
                break;
            case SL:
            case SS:
            case UL:
            case US:
                attrs.setInt(tag, el.vr, el.toInts());
                break;
            case SV:
                attrs.setLong(tag, el.vr, el.toLongs(Long::parseLong));
                break;
            case UV:
                attrs.setLong(tag, el.vr, el.toLongs(Long::parseUnsignedLong));
                break;
            case SQ:
                el.toItems(attrs.newSequence(tag, el.values.size()));
                break;
            case OB:
            case OD:
            case OF:
            case OL:
            case OV:
            case OW:
            case UN:
                if (el.bytes != null)
                    attrs.setBytes(tag, el.vr, el.bytes);
                else
                    el.toFragments(attrs.newFragments(tag, el.vr, el.values.size()));
            }
    }

    private List<Object> readValues() {
        ArrayList<Object> list = new ArrayList<>();
        next();
        if (this.event == Event.VALUE_STRING) {
            Logger.info("Missing value array at {} - treat as single value", parser.getLocation());
            list.add(getString());
            return list;
        }
        expect(Event.START_ARRAY);
        while (next() != Event.END_ARRAY) {
            switch (event) {
            case START_OBJECT:
                list.add(readItemOrPersonName());
                break;
            case VALUE_STRING:
                list.add(parser.getString());
                break;
            case VALUE_NUMBER:
                list.add(parser.getBigDecimal());
                break;
            case VALUE_NULL:
                list.add(null);
                break;
            default:
                throw new JsonParsingException("Unexpected " + event, parser.getLocation());
            }
        }
        return list;
    }

    private List<Object> readDataFragments() {
        ArrayList<Object> list = new ArrayList<>();
        next();
        expect(Event.START_ARRAY);
        while (next() != Event.END_ARRAY) {
            switch (event) {
            case START_OBJECT:
                list.add(readDataFragment());
                break;
            case VALUE_NULL:
                list.add(null);
                break;
            default:
                throw new JsonParsingException("Unexpected " + event, parser.getLocation());
            }
        }
        return list;
    }

    private Object readItemOrPersonName() {
        if (next() != Event.KEY_NAME)
            return null;

        return (getString().length() == 8) ? doReadDataset(new Attributes()) : readPersonName();
    }

    private String readPersonName() {
        pnGroups.clear();
        while (event == Event.KEY_NAME) {
            Group key;
            try {
                key = Group.valueOf(getString());
            } catch (IllegalArgumentException e) {
                throw new JsonParsingException("Unexpected \"" + getString()
                        + "\", expected \"Alphabetic\" or \"Ideographic\"" + " or \"Phonetic\"", parser.getLocation());
            }
            pnGroups.put(key, valueString());
            next();
        }
        expect(Event.END_OBJECT);
        String s = pnGroups.get(Group.Alphabetic);
        if (s != null && pnGroups.size() == 1)
            return s;

        StringBuilder sb = new StringBuilder(64);
        if (s != null)
            sb.append(s);

        sb.append('=');
        s = pnGroups.get(Group.Ideographic);
        if (s != null)
            sb.append(s);

        s = pnGroups.get(Group.Phonetic);
        if (s != null)
            sb.append('=').append(s);

        return sb.toString();
    }

    private byte[] readInlineBinary() {
        char[] base64 = valueString().toCharArray();
        bout.reset();
        try {
            Builder.decode(base64, 0, base64.length, bout);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bout.toByteArray();
    }

    private Object readDataFragment() {
        byte[] bytes = null;
        String bulkDataURI = null;
        while (next() == Event.KEY_NAME) {
            switch (getString()) {
            case "BulkDataURI":
                bulkDataURI = valueString();
                break;
            case "InlineBinary":
                bytes = readInlineBinary();
                break;
            default:
                throw new JsonParsingException(
                        "Unexpected \"" + getString() + "\", expected \"InlineBinary\"" + " or \"BulkDataURI\"",
                        parser.getLocation());
            }
        }
        expect(Event.END_OBJECT);
        return bulkDataURI != null && !skipBulkDataURI ? new BulkData(null, bulkDataURI, false) : bytes;
    }

    public JsonParser.Event getEvent() {
        return event;
    }

    public JsonLocation getLocation() {
        return parser.getLocation();
    }

    public String stringValue() {
        next();
        expect(JsonParser.Event.VALUE_STRING);
        return getString();
    }

    public String[] stringArray() {
        next();
        expect(JsonParser.Event.START_ARRAY);
        ArrayList<String> a = new ArrayList<>();
        while (next() == JsonParser.Event.VALUE_STRING)
            a.add(getString());
        expect(JsonParser.Event.END_ARRAY);
        return a.toArray(Normal.EMPTY_STRING_ARRAY);
    }

    public <T extends Enum<T>> T[] enumArray(Class<T> enumType) {
        next();
        expect(JsonParser.Event.START_ARRAY);
        EnumSet<T> a = EnumSet.noneOf(enumType);
        while (next() == JsonParser.Event.VALUE_STRING)
            a.add(T.valueOf(enumType, getString()));
        expect(JsonParser.Event.END_ARRAY);
        return a.toArray((T[]) Array.newInstance(enumType, a.size()));
    }

    public long longValue() {
        next();
        expect(JsonParser.Event.VALUE_NUMBER);
        return Long.parseLong(getString());
    }

    public int intValue() {
        next();
        expect(JsonParser.Event.VALUE_NUMBER);
        return Integer.parseInt(getString());
    }

    public int[] intArray() {
        next();
        expect(JsonParser.Event.START_ARRAY);
        ArrayList<String> a = new ArrayList<>();
        while (next() == JsonParser.Event.VALUE_NUMBER) {
            a.add(getString());
        }
        expect(JsonParser.Event.END_ARRAY);
        int[] is = new int[a.size()];
        for (int i = 0; i < is.length; i++) {
            is[i] = Integer.parseInt(a.get(i));
        }
        return is;
    }

    public boolean booleanValue() {
        switch (next()) {
        case VALUE_FALSE:
            return false;
        case VALUE_TRUE:
            return true;
        }
        throw new JsonParsingException("Unexpected " + event + ", expected VALUE_FALSE or VALUE_TRUE",
                parser.getLocation());
    }

    public Issuer issuerValue() {
        return new Issuer(stringValue());
    }

    public Code[] codeArray() {
        next();
        expect(JsonParser.Event.START_ARRAY);
        ArrayList<Code> a = new ArrayList<>();
        while (next() == JsonParser.Event.VALUE_STRING)
            a.add(new Code(getString()));
        expect(JsonParser.Event.END_ARRAY);
        return a.toArray(EMPTY_CODES);
    }

    public TimeZone timeZoneValue() {
        return TimeZone.getTimeZone(stringValue());
    }

    public Date dateTimeValue() {
        return Format.parseDT(null, stringValue(), new DatePrecision());
    }

    public void skipUnknownProperty() {
        Logger.warn("Skip unknown property: {}", text);
        skipValue();
    }

    private void skipValue() {
        int level = 0;
        do {
            switch (next()) {
            case START_ARRAY:
            case START_OBJECT:
                level++;
                break;
            case END_OBJECT:
            case END_ARRAY:
                level--;
                break;
            }
        } while (level > 0);
    }

    public interface Callback {

        void onDataset(Attributes fmi, Attributes dataset);

    }

    private static class Element {
        VR vr;
        List<Object> values;
        byte[] bytes;
        String bulkDataURI;

        boolean isEmpty() {
            return (values == null || values.isEmpty()) && (bytes == null || bytes.length == 0) && bulkDataURI == null;
        }

        String[] toStrings() {
            String[] ss = new String[values.size()];
            for (int i = 0; i < ss.length; i++) {
                Object value = values.get(i);
                ss[i] = value != null ? value.toString() : null;
            }
            return ss;
        }

        double[] toDoubles() {
            double[] ds = new double[values.size()];
            for (int i = 0; i < ds.length; i++) {
                Number number = (Number) values.get(i);
                double d;
                if (number == null) {
                    Logger.info("decode {} null as NaN", vr);
                    d = Double.NaN;
                } else {
                    d = number.doubleValue();
                    if (d == -Double.MAX_VALUE) {
                        Logger.info("decode {} {} as -Infinity", vr, d);
                        d = Double.NEGATIVE_INFINITY;
                    } else if (d == Double.MAX_VALUE) {
                        Logger.info("decode {} {} as Infinity", vr, d);
                        d = Double.POSITIVE_INFINITY;
                    }
                }
                ds[i] = d;
            }
            return ds;
        }

        int[] toInts() {
            int[] is = new int[values.size()];
            for (int i = 0; i < is.length; i++) {
                is[i] = ((Number) values.get(i)).intValue();
            }
            return is;
        }

        long[] toLongs(ToLongFunction<String> parse) {
            long[] ls = new long[values.size()];
            for (int i = 0; i < ls.length; i++) {
                ls[i] = longValueOf(parse, values.get(i));
            }
            return ls;
        }

        private long longValueOf(ToLongFunction<String> string2long, Object o) {
            return o instanceof Number ? ((Number) o).longValue() : string2long.applyAsLong((String) o);
        }

        void toItems(Sequence seq) {
            for (Object value : values) {
                seq.add(value != null ? (Attributes) value : new Attributes(0));
            }
        }

        void toFragments(Fragments fragments) {
            for (Object value : values) {
                fragments.add(value);
            }
        }

    }

}
