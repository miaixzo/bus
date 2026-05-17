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
package org.miaixz.bus.image.metric.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.ToLongFunction;

import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;
import jakarta.json.stream.JsonParsingException;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Format;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.*;
import org.miaixz.bus.image.galaxy.data.PersonName.Group;
import org.miaixz.bus.logger.Logger;

/**
 * Represents the JSONReader type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JSONReader {

    /**
     * The empty codes value.
     */
    private static final Code[] EMPTY_CODES = {};

    /**
     * The conn ref index start value.
     */
    private static final int CONN_REF_INDEX_START = "/dicomNetworkConnection/".length();

    /**
     * The parser value.
     */
    private final JsonParser parser;

    /**
     * The bout value.
     */
    private final ByteArrayOutputStream bout = new ByteArrayOutputStream(64);

    /**
     * The pn groups value.
     */
    private final EnumMap<Group, String> pnGroups = new EnumMap<>(Group.class);

    /**
     * The skip bulk data uri value.
     */
    private boolean skipBulkDataURI;

    /**
     * The bulk data creator value.
     */
    private BulkData.Creator bulkDataCreator = BulkData::new;

    /**
     * The fmi value.
     */
    private Attributes fmi;

    /**
     * The event value.
     */
    private Event event;

    /**
     * The text value.
     */
    private String text;

    /**
     * The level value.
     */
    private int level = -1;

    /**
     * Creates a new instance.
     *
     * @param parser the parser.
     */
    public JSONReader(JsonParser parser) {
        this.parser = Objects.requireNonNull(parser);
    }

    /**
     * Converts this value to connection index.
     *
     * @param connRef the conn ref.
     * @return the operation result.
     */
    public static int toConnectionIndex(String connRef) {
        return Integer.parseInt(connRef.substring(CONN_REF_INDEX_START));
    }

    /**
     * Determines whether skip bulk data uri.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isSkipBulkDataURI() {
        return skipBulkDataURI;
    }

    /**
     * Sets the skip bulk data uri.
     *
     * @param skipBulkDataURI the skip bulk data uri.
     */
    public void setSkipBulkDataURI(boolean skipBulkDataURI) {
        this.skipBulkDataURI = skipBulkDataURI;
    }

    /**
     * Sets the bulk data creator.
     *
     * @param bulkDataCreator the bulk data creator.
     */
    public void setBulkDataCreator(BulkData.Creator bulkDataCreator) {
        this.bulkDataCreator = Objects.requireNonNull(bulkDataCreator);
    }

    /**
     * Gets the file meta information.
     *
     * @return the file meta information.
     */
    public Attributes getFileMetaInformation() {
        return fmi;
    }

    /**
     * Executes the next operation.
     *
     * @return the operation result.
     */
    public Event next() {
        text = null;
        return event = parser.next();
    }

    /**
     * Gets the string.
     *
     * @return the string.
     */
    public String getString() {
        if (text == null)
            text = parser.getString();
        return text;
    }

    /**
     * Executes the expect operation.
     *
     * @param expected the expected.
     */
    public void expect(Event expected) {
        if (this.event != expected)
            throw new JsonParsingException("Unexpected " + event + ", expected " + expected, parser.getLocation());
    }

    /**
     * Executes the value string operation.
     *
     * @return the operation result.
     */
    private String valueString() {
        next();
        expect(Event.VALUE_STRING);
        return getString();
    }

    /**
     * Reads the dataset.
     *
     * @param attrs the attrs.
     * @return the operation result.
     */
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

    /**
     * Reads the datasets.
     *
     * @param callback the callback.
     */
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

    /**
     * Executes the do read dataset operation.
     *
     * @param attrs the attrs.
     * @return the operation result.
     */
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

    /**
     * Reads the attribute.
     *
     * @param attrs the attrs.
     */
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
                        Logger.info(
                                false,
                                "Image",
                                "Invalid vr: '{}' at {} - treat as '{}'",
                                getString(),
                                parser.getLocation(),
                                el.vr);
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
            Logger.info(false, "Image", "Missing property: vr at {} - treat as '{}'", parser.getLocation(), el.vr);
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

    /**
     * Reads the values.
     *
     * @return the operation result.
     */
    private List<Object> readValues() {
        ArrayList<Object> list = new ArrayList<>();
        next();
        if (this.event == Event.VALUE_STRING) {
            Logger.info(false, "Image", "Missing value array at {} - treat as single value", parser.getLocation());
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

    /**
     * Reads the data fragments.
     *
     * @return the operation result.
     */
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

    /**
     * Reads the item or person name.
     *
     * @return the operation result.
     */
    private Object readItemOrPersonName() {
        if (next() != Event.KEY_NAME)
            return null;

        return (getString().length() == 8) ? doReadDataset(new Attributes()) : readPersonName();
    }

    /**
     * Reads the person name.
     *
     * @return the operation result.
     */
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

    /**
     * Reads the inline binary.
     *
     * @return the operation result.
     */
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

    /**
     * Reads the data fragment.
     *
     * @return the operation result.
     */
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

    /**
     * Gets the event.
     *
     * @return the event.
     */
    public JsonParser.Event getEvent() {
        return event;
    }

    /**
     * Gets the location.
     *
     * @return the location.
     */
    public JsonLocation getLocation() {
        return parser.getLocation();
    }

    /**
     * Executes the string value operation.
     *
     * @return the operation result.
     */
    public String stringValue() {
        next();
        expect(JsonParser.Event.VALUE_STRING);
        return getString();
    }

    /**
     * Executes the string array operation.
     *
     * @return the operation result.
     */
    public String[] stringArray() {
        next();
        expect(JsonParser.Event.START_ARRAY);
        ArrayList<String> a = new ArrayList<>();
        while (next() == JsonParser.Event.VALUE_STRING)
            a.add(getString());
        expect(JsonParser.Event.END_ARRAY);
        return a.toArray(Normal.EMPTY_STRING_ARRAY);
    }

    /**
     * Executes the enum array operation.
     *
     * @param enumType the enum type.
     * @return the operation result.
     */
    public <T extends Enum<T>> T[] enumArray(Class<T> enumType) {
        next();
        expect(JsonParser.Event.START_ARRAY);
        EnumSet<T> a = EnumSet.noneOf(enumType);
        while (next() == JsonParser.Event.VALUE_STRING)
            a.add(T.valueOf(enumType, getString()));
        expect(JsonParser.Event.END_ARRAY);
        return a.toArray((T[]) Array.newInstance(enumType, a.size()));
    }

    /**
     * Executes the long value operation.
     *
     * @return the operation result.
     */
    public long longValue() {
        next();
        expect(JsonParser.Event.VALUE_NUMBER);
        return Long.parseLong(getString());
    }

    /**
     * Executes the int value operation.
     *
     * @return the operation result.
     */
    public int intValue() {
        next();
        expect(JsonParser.Event.VALUE_NUMBER);
        return Integer.parseInt(getString());
    }

    /**
     * Executes the int array operation.
     *
     * @return the operation result.
     */
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

    /**
     * Executes the boolean value operation.
     *
     * @return true if the condition is met; otherwise false.
     */
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

    /**
     * Determines whether suer value.
     *
     * @return true if the condition is met; otherwise false.
     */
    public Issuer issuerValue() {
        return new Issuer(stringValue());
    }

    /**
     * Executes the code array operation.
     *
     * @return the operation result.
     */
    public Code[] codeArray() {
        next();
        expect(JsonParser.Event.START_ARRAY);
        ArrayList<Code> a = new ArrayList<>();
        while (next() == JsonParser.Event.VALUE_STRING)
            a.add(new Code(getString()));
        expect(JsonParser.Event.END_ARRAY);
        return a.toArray(EMPTY_CODES);
    }

    /**
     * Executes the time zone value operation.
     *
     * @return the operation result.
     */
    public TimeZone timeZoneValue() {
        return TimeZone.getTimeZone(stringValue());
    }

    /**
     * Executes the date time value operation.
     *
     * @return the operation result.
     */
    public Date dateTimeValue() {
        return Format.parseDT(null, stringValue(), new DatePrecision());
    }

    /**
     * Executes the skip unknown property operation.
     */
    public void skipUnknownProperty() {
        Logger.warn(false, "Image", "Skip unknown property: property={}", text);
        skipValue();
    }

    /**
     * Executes the skip value operation.
     */
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

    /**
     * Defines the Callback contract.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public interface Callback {

        /**
         * Executes the on dataset operation.
         *
         * @param fmi     the fmi.
         * @param dataset the dataset.
         */
        void onDataset(Attributes fmi, Attributes dataset);

    }

    /**
     * Represents the Element type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static class Element {

        /**
         * The vr value.
         */
        VR vr;

        /**
         * The values value.
         */
        List<Object> values;

        /**
         * The bytes value.
         */
        byte[] bytes;

        /**
         * The bulk data uri value.
         */
        String bulkDataURI;

        /**
         * Determines whether empty.
         *
         * @return true if the condition is met; otherwise false.
         */
        boolean isEmpty() {
            return (values == null || values.isEmpty()) && (bytes == null || bytes.length == 0) && bulkDataURI == null;
        }

        /**
         * Converts this value to strings.
         *
         * @return the operation result.
         */
        String[] toStrings() {
            String[] ss = new String[values.size()];
            for (int i = 0; i < ss.length; i++) {
                Object value = values.get(i);
                ss[i] = value != null ? value.toString() : null;
            }
            return ss;
        }

        /**
         * Converts this value to doubles.
         *
         * @return the operation result.
         */
        double[] toDoubles() {
            double[] ds = new double[values.size()];
            for (int i = 0; i < ds.length; i++) {
                Number number = (Number) values.get(i);
                double d;
                if (number == null) {
                    Logger.info(false, "Image", "decode {} null as NaN", vr);
                    d = Double.NaN;
                } else {
                    d = number.doubleValue();
                    if (d == -Double.MAX_VALUE) {
                        Logger.info(false, "Image", "decode {} {} as -Infinity", vr, d);
                        d = Double.NEGATIVE_INFINITY;
                    } else if (d == Double.MAX_VALUE) {
                        Logger.info(false, "Image", "decode {} {} as Infinity", vr, d);
                        d = Double.POSITIVE_INFINITY;
                    }
                }
                ds[i] = d;
            }
            return ds;
        }

        /**
         * Converts this value to ints.
         *
         * @return the operation result.
         */
        int[] toInts() {
            int[] is = new int[values.size()];
            for (int i = 0; i < is.length; i++) {
                is[i] = ((Number) values.get(i)).intValue();
            }
            return is;
        }

        /**
         * Converts this value to longs.
         *
         * @param parse the parse.
         * @return the operation result.
         */
        long[] toLongs(ToLongFunction<String> parse) {
            long[] ls = new long[values.size()];
            for (int i = 0; i < ls.length; i++) {
                ls[i] = longValueOf(parse, values.get(i));
            }
            return ls;
        }

        /**
         * Executes the long value of operation.
         *
         * @param string2long the string2long.
         * @param o           the o.
         * @return the operation result.
         */
        private long longValueOf(ToLongFunction<String> string2long, Object o) {
            return o instanceof Number ? ((Number) o).longValue() : string2long.applyAsLong((String) o);
        }

        /**
         * Executes the to items operation.
         *
         * @param seq the seq.
         */
        void toItems(Sequence seq) {
            for (Object value : values) {
                seq.add(value != null ? (Attributes) value : new Attributes(0));
            }
        }

        /**
         * Executes the to fragments operation.
         *
         * @param fragments the fragments.
         */
        void toFragments(Fragments fragments) {
            for (Object value : values) {
                fragments.add(value);
            }
        }

    }

}
