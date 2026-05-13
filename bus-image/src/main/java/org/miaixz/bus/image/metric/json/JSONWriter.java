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

import java.io.IOException;
import java.util.*;
import java.util.function.LongFunction;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Format;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.*;
import org.miaixz.bus.image.galaxy.data.PersonName.Group;
import org.miaixz.bus.image.galaxy.io.ImageInputHandler;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.logger.Logger;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonGenerator;

/**
 * Allows conversion of DICOM files into JSON format. See
 * <a href="http://dicom.nema.org/medical/dicom/current/output/html/part18.html#sect_F.2">DICOM JSON Model</a>.
 * Implements {@link ImageInputHandler} so it can be attached to a {@link ImageInputStream} to produce the JSON while
 * being read. See sample usage below.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JSONWriter implements ImageInputHandler {

    /**
     * The double max bits value.
     */
    private static final int DOUBLE_MAX_BITS = 53;

    /**
     * The gen value.
     */
    private final JsonGenerator gen;

    /**
     * The has items value.
     */
    private final Deque<Boolean> hasItems = new ArrayDeque<>();

    /**
     * The json type by vr value.
     */
    private final EnumMap<VR, JsonValue.ValueType> jsonTypeByVR = new EnumMap<>(VR.class);

    /**
     * The replace bulk data uri value.
     */
    private String replaceBulkDataURI;

    /**
     * Creates a new instance.
     *
     * @param gen the gen.
     */
    public JSONWriter(JsonGenerator gen) {
        this.gen = gen;
    }

    /**
     * Executes the require is ds sv uv operation.
     *
     * @param vr the vr.
     * @return the operation result.
     */
    private static VR requireIS_DS_SV_UV(VR vr) {
        if (vr != VR.DS && vr != VR.IS && vr != VR.SV && vr != VR.UV)
            throw new IllegalArgumentException("vr:" + vr);
        return vr;
    }

    /**
     * Executes the require number or string operation.
     *
     * @param jsonType the json type.
     * @return the operation result.
     */
    private static JsonValue.ValueType requireNumberOrString(JsonValue.ValueType jsonType) {
        if (jsonType != JsonValue.ValueType.NUMBER && jsonType != JsonValue.ValueType.STRING)
            throw new IllegalArgumentException("jsonType:" + jsonType);
        return jsonType;
    }

    /**
     * Converts this value to strings.
     *
     * @param map the map.
     * @return the operation result.
     */
    private static <T> String[] toStrings(Map<String, T> map) {
        String[] ss = new String[map.size()];
        int i = 0;
        for (Map.Entry<String, T> entry : map.entrySet())
            ss[i++] = entry.getKey() + '=' + entry.getValue();
        return ss;
    }

    /**
     * Compares this instance with another object for equality.
     *
     * @param a  the a.
     * @param a2 the a2.
     * @return true if the condition is met; otherwise false.
     */
    public static <T> boolean equals(T[] a, T[] a2) {
        int length = a.length;
        if (a2.length != length)
            return false;

        outer: for (Object o1 : a) {
            for (Object o2 : a2)
                if (o1.equals(o2))
                    continue outer;
            return false;
        }
        return true;
    }

    /**
     * Sets the json type.
     *
     * @param vr        the vr.
     * @param valueType the value type.
     */
    public void setJsonType(VR vr, JsonValue.ValueType valueType) {
        jsonTypeByVR.put(requireIS_DS_SV_UV(vr), requireNumberOrString(valueType));
    }

    /**
     * Gets the replace bulk data uri.
     *
     * @return the replace bulk data uri.
     */
    public String getReplaceBulkDataURI() {
        return replaceBulkDataURI;
    }

    /**
     * Sets the replace bulk data uri.
     *
     * @param replaceBulkDataURI the replace bulk data uri.
     */
    public void setReplaceBulkDataURI(String replaceBulkDataURI) {
        this.replaceBulkDataURI = replaceBulkDataURI;
    }

    /**
     * Writes the given attributes as a full JSON object. Subsequent calls will generate a new JSON object.
     */
    public void write(Attributes attrs) {
        gen.writeStartObject();
        writeAttributes(attrs);
        gen.writeEnd();
    }

    /**
     * Writes the given attributes to JSON. Can be used to output multiple attributes (e.g. metadata, attributes) to the
     * same JSON object.
     */
    public void writeAttributes(Attributes attrs) {
        final SpecificCharacterSet cs = attrs.getSpecificCharacterSet();
        try {
            attrs.accept(new Visitor() {

                @Override
                public boolean visit(Attributes attrs, int tag, VR vr, Object value) throws Exception {
                    writeAttribute(tag, vr, value, cs, attrs);
                    return true;
                }
            }, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes the attribute.
     *
     * @param tag   the tag.
     * @param vr    the vr.
     * @param value the value.
     * @param cs    the cs.
     * @param attrs the attrs.
     */
    private void writeAttribute(int tag, VR vr, Object value, SpecificCharacterSet cs, Attributes attrs) {
        if (Tag.isGroupLength(tag))
            return;

        gen.writeStartObject(Tag.toHexString(tag));
        gen.write("vr", vr.name());
        if (value instanceof Value)
            writeValue((Value) value, attrs.bigEndian());
        else
            writeValue(vr, value, attrs.bigEndian(), attrs.getSpecificCharacterSet(vr), true);
        gen.writeEnd();
    }

    /**
     * Writes the value.
     *
     * @param value     the value.
     * @param bigEndian the big endian.
     */
    private void writeValue(Value value, boolean bigEndian) {
        if (value.isEmpty())
            return;

        if (value instanceof Sequence) {
            gen.writeStartArray("Value");
            for (Attributes item : (Sequence) value) {
                write(item);
            }
            gen.writeEnd();
        } else if (value instanceof Fragments frags) {
            gen.writeStartArray("DataFragment");
            for (Object frag : frags) {
                if (frag instanceof Value && ((Value) frag).isEmpty())
                    gen.writeNull();
                else {
                    gen.writeStartObject();
                    if (frag instanceof BulkData)
                        writeBulkData((BulkData) frag);
                    else {
                        writeInlineBinary(frags.vr(), (byte[]) frag, bigEndian, true);
                    }
                    gen.writeEnd();
                }
            }
            gen.writeEnd();
        } else if (value instanceof BulkData) {
            writeBulkData((BulkData) value);
        }
    }

    /**
     * Reads the value.
     *
     * @param dis   the dis.
     * @param attrs the attrs.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void readValue(ImageInputStream dis, Attributes attrs) throws IOException {
        int tag = dis.tag();
        VR vr = dis.vr();
        long len = dis.unsignedLength();
        if (Tag.isGroupLength(tag)) {
            dis.readValue(dis, attrs);
        } else if (dis.isExcludeBulkData()) {
            dis.readValue(dis, attrs);
        } else {
            gen.writeStartObject(Tag.toHexString(tag));
            gen.write("vr", vr.name());
            if (vr == VR.SQ || len == -1) {
                hasItems.addLast(false);
                dis.readValue(dis, attrs);
                if (hasItems.removeLast())
                    gen.writeEnd();
            } else if (len > 0) {
                if (dis.isIncludeBulkDataURI()) {
                    writeBulkData(dis.createBulkData(dis));
                } else {
                    byte[] b = dis.readValue();
                    if (tag == Tag.TransferSyntaxUID || tag == Tag.SpecificCharacterSet
                            || tag == Tag.PixelRepresentation || Tag.isPrivateCreator(tag))
                        attrs.setBytes(tag, vr, b);
                    writeValue(vr, b, dis.bigEndian(), attrs.getSpecificCharacterSet(vr), false);
                }
            }
            gen.writeEnd();
        }
    }

    /**
     * Writes the value.
     *
     * @param vr        the vr.
     * @param val       the val.
     * @param bigEndian the big endian.
     * @param cs        the cs.
     * @param preserve  the preserve.
     */
    private void writeValue(VR vr, Object val, boolean bigEndian, SpecificCharacterSet cs, boolean preserve) {
        switch (vr) {
            case AE:
            case AS:
            case AT:
            case CS:
            case DA:
            case DS:
            case DT:
            case IS:
            case LO:
            case LT:
            case PN:
            case SH:
            case ST:
            case TM:
            case UC:
            case UI:
            case UR:
            case UT:
                writeStringValues(vr, val, bigEndian, cs);
                break;

            case FL:
            case FD:
                writeDoubleValues(vr, val, bigEndian);
                break;

            case SL:
            case SS:
            case US:
                writeIntValues(vr, val, bigEndian);
                break;

            case SV:
                writeLongValues(Long::toString, vr, val, bigEndian);
                break;

            case UV:
                writeLongValues(Long::toUnsignedString, vr, val, bigEndian);
                break;

            case UL:
                writeUIntValues(vr, val, bigEndian);
                break;

            case OB:
            case OD:
            case OF:
            case OL:
            case OV:
            case OW:
            case UN:
                writeInlineBinary(vr, (byte[]) val, bigEndian, preserve);
                break;

            case SQ:
                assert true;
        }
    }

    /**
     * Writes the string values.
     *
     * @param vr        the vr.
     * @param val       the val.
     * @param bigEndian the big endian.
     * @param cs        the cs.
     */
    private void writeStringValues(VR vr, Object val, boolean bigEndian, SpecificCharacterSet cs) {
        gen.writeStartArray("Value");
        Object o = vr.toStrings(val, bigEndian, cs);
        String[] ss = (o instanceof String[]) ? (String[]) o : new String[] { (String) o };
        for (String s : ss) {
            if (s == null || s.isEmpty())
                gen.writeNull();
            else
                switch (vr) {
                    case DS:
                        if (jsonTypeByVR.get(VR.DS) == JsonValue.ValueType.NUMBER) {
                            try {
                                gen.write(Builder.parseDS(s));
                            } catch (NumberFormatException e) {
                                Logger.info(
                                        false,
                                        "Image",
                                        "illegal DS value: valueChars={} - encoded as string",
                                        s == null ? 0 : s.length());
                                gen.write(s);
                            }
                        } else {
                            gen.write(s);
                        }
                        break;

                    case IS:
                        if (jsonTypeByVR.get(VR.IS) == JsonValue.ValueType.NUMBER) {
                            writeNumber(s);
                        } else {
                            gen.write(s);
                        }
                        break;

                    case PN:
                        writePersonName(s);
                        break;

                    default:
                        gen.write(s);
                }
        }
        gen.writeEnd();
    }

    /**
     * Writes the number.
     *
     * @param s the s.
     */
    private void writeNumber(String s) {
        try {
            long l = Builder.parseIS(s);
            if ((l < 0 ? -l : l) >> DOUBLE_MAX_BITS == 0) {
                gen.write(l);
                return;
            }
        } catch (NumberFormatException e) {
            Logger.info(
                    false,
                    "Image",
                    "illegal IS value: valueChars={} - encoded as string",
                    s == null ? 0 : s.length());
        }
        gen.write(s);
    }

    /**
     * Writes the double values.
     *
     * @param vr        the vr.
     * @param val       the val.
     * @param bigEndian the big endian.
     */
    private void writeDoubleValues(VR vr, Object val, boolean bigEndian) {
        gen.writeStartArray("Value");
        int vm = vr.vmOf(val);
        for (int i = 0; i < vm; i++) {
            double d = vr.toDouble(val, bigEndian, i, 0);
            if (Double.isNaN(d)) {
                Logger.info(false, "Image", "encode {} NaN as null", vr);
                gen.writeNull();
            } else {
                if (d == Double.POSITIVE_INFINITY) {
                    d = Double.MAX_VALUE;
                    Logger.info(false, "Image", "encode {} Infinity as {}", vr, d);
                } else if (d == Double.NEGATIVE_INFINITY) {
                    d = -Double.MAX_VALUE;
                    Logger.info(false, "Image", "encode {} -Infinity as {}", vr, d);
                }
                gen.write(d);
            }
        }
        gen.writeEnd();
    }

    /**
     * Writes the int values.
     *
     * @param vr        the vr.
     * @param val       the val.
     * @param bigEndian the big endian.
     */
    private void writeIntValues(VR vr, Object val, boolean bigEndian) {
        gen.writeStartArray("Value");
        int vm = vr.vmOf(val);
        for (int i = 0; i < vm; i++) {
            gen.write(vr.toInt(val, bigEndian, i, 0));
        }
        gen.writeEnd();
    }

    /**
     * Writes the u int values.
     *
     * @param vr        the vr.
     * @param val       the val.
     * @param bigEndian the big endian.
     */
    private void writeUIntValues(VR vr, Object val, boolean bigEndian) {
        gen.writeStartArray("Value");
        int vm = vr.vmOf(val);
        for (int i = 0; i < vm; i++) {
            gen.write(vr.toInt(val, bigEndian, i, 0) & 0xffffffffL);
        }
        gen.writeEnd();
    }

    /**
     * Writes the long values.
     *
     * @param toString  the to string.
     * @param vr        the vr.
     * @param val       the val.
     * @param bigEndian the big endian.
     */
    private void writeLongValues(LongFunction<String> toString, VR vr, Object val, boolean bigEndian) {
        gen.writeStartArray("Value");
        boolean asString = jsonTypeByVR.get(vr) != JsonValue.ValueType.NUMBER;
        int vm = vr.vmOf(val);
        for (int i = 0; i < vm; i++) {
            long l = vr.toLong(val, bigEndian, i, 0);
            if (asString || (l < 0 ? (vr == VR.UV || (-l >> DOUBLE_MAX_BITS) > 0) : (l >> DOUBLE_MAX_BITS) > 0)) {
                gen.write(toString.apply(l));
            } else {
                gen.write(l);
            }
        }
        gen.writeEnd();
    }

    /**
     * Writes the person name.
     *
     * @param s the s.
     */
    private void writePersonName(String s) {
        PersonName pn = new PersonName(s, true);
        gen.writeStartObject();
        writePNGroup("Alphabetic", pn, Group.Alphabetic);
        writePNGroup("Ideographic", pn, Group.Ideographic);
        writePNGroup("Phonetic", pn, Group.Phonetic);
        gen.writeEnd();
    }

    /**
     * Writes the pn group.
     *
     * @param name  the name.
     * @param pn    the pn.
     * @param group the group.
     */
    private void writePNGroup(String name, PersonName pn, Group group) {
        if (pn.contains(group))
            gen.write(name, pn.toString(group, true));
    }

    /**
     * Writes the inline binary.
     *
     * @param vr        the vr.
     * @param b         the b.
     * @param bigEndian the big endian.
     * @param preserve  the preserve.
     */
    private void writeInlineBinary(VR vr, byte[] b, boolean bigEndian, boolean preserve) {
        if (bigEndian)
            b = vr.toggleEndian(b, preserve);
        gen.write("InlineBinary", encodeBase64(b));
    }

    /**
     * Executes the encode base64 operation.
     *
     * @param b the b.
     * @return the operation result.
     */
    private String encodeBase64(byte[] b) {
        int len = (b.length * 4 / 3 + 3) & ~3;
        char[] ch = new char[len];
        Builder.encode(b, 0, b.length, ch, 0);
        return new String(ch);
    }

    /**
     * Writes the bulk data.
     *
     * @param blkdata the blkdata.
     */
    private void writeBulkData(BulkData blkdata) {
        gen.write("BulkDataURI", replaceBulkDataURI != null ? replaceBulkDataURI : blkdata.getURI());
    }

    /**
     * Reads the value.
     *
     * @param dis the dis.
     * @param seq the seq.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void readValue(ImageInputStream dis, Sequence seq) throws IOException {
        if (!hasItems.getLast()) {
            gen.writeStartArray("Value");
            hasItems.removeLast();
            hasItems.addLast(true);
        }
        gen.writeStartObject();
        dis.readValue(dis, seq);
        gen.writeEnd();
    }

    /**
     * Reads the value.
     *
     * @param dis   the dis.
     * @param frags the frags.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void readValue(ImageInputStream dis, Fragments frags) throws IOException {
        int len = dis.length();
        if (dis.isExcludeBulkData()) {
            dis.skipFully(len);
            return;
        }
        if (!hasItems.getLast()) {
            gen.writeStartArray("DataFragment");
            hasItems.removeLast();
            hasItems.add(true);
        }

        if (len == 0)
            gen.writeNull();
        else {
            gen.writeStartObject();
            if (dis.isIncludeBulkDataURI()) {
                writeBulkData(dis.createBulkData(dis));
            } else {
                writeInlineBinary(frags.vr(), dis.readValue(), dis.bigEndian(), false);
            }
            gen.writeEnd();
        }
    }

    /**
     * Executes the start dataset operation.
     *
     * @param dis the dis.
     */
    @Override
    public void startDataset(ImageInputStream dis) {
        gen.writeStartObject();
    }

    /**
     * Executes the end dataset operation.
     *
     * @param dis the dis.
     */
    @Override
    public void endDataset(ImageInputStream dis) {
        gen.writeEnd();
    }

    /**
     * Writes the start object.
     *
     * @return the operation result.
     */
    public JsonGenerator writeStartObject() {
        return gen.writeStartObject();
    }

    /**
     * Writes the start object.
     *
     * @param name the name.
     * @return the operation result.
     */
    public JsonGenerator writeStartObject(String name) {
        return gen.writeStartObject(name);
    }

    /**
     * Writes the start array.
     *
     * @return the operation result.
     */
    public JsonGenerator writeStartArray() {
        return gen.writeStartArray();
    }

    /**
     * Writes the start array.
     *
     * @param name the name.
     * @return the operation result.
     */
    public JsonGenerator writeStartArray(String name) {
        return gen.writeStartArray(name);
    }

    /**
     * Executes the write operation.
     *
     * @param name  the name.
     * @param value the value.
     * @return the operation result.
     */
    public JsonGenerator write(String name, int value) {
        return gen.write(name, value);
    }

    /**
     * Executes the write operation.
     *
     * @param name  the name.
     * @param value the value.
     * @return the operation result.
     */
    public JsonGenerator write(String name, boolean value) {
        return gen.write(name, value);
    }

    /**
     * Writes the end.
     *
     * @return the operation result.
     */
    public JsonGenerator writeEnd() {
        return gen.writeEnd();
    }

    /**
     * Executes the write operation.
     *
     * @param value the value.
     * @return the operation result.
     */
    public JsonGenerator write(String value) {
        return gen.write(value);
    }

    /**
     * Writes the not null or def.
     *
     * @param name   the name.
     * @param value  the value.
     * @param defVal the def val.
     */
    public <T> void writeNotNullOrDef(String name, T value, T defVal) {
        if (value != null && !value.equals(defVal))
            gen.write(name, value.toString());
    }

    /**
     * Writes the not null.
     *
     * @param name  the name.
     * @param value the value.
     */
    public void writeNotNull(String name, Boolean value) {
        if (value != null)
            gen.write(name, value.booleanValue());
    }

    /**
     * Writes the not null.
     *
     * @param name  the name.
     * @param value the value.
     */
    public void writeNotNull(String name, Integer value) {
        if (value != null)
            gen.write(name, value.intValue());
    }

    /**
     * Writes the not null.
     *
     * @param name  the name.
     * @param value the value.
     */
    public void writeNotNull(String name, Long value) {
        if (value != null)
            gen.write(name, value.longValue());
    }

    /**
     * Writes the not null or def.
     *
     * @param name   the name.
     * @param value  the value.
     * @param defVal the def val.
     */
    public void writeNotNullOrDef(String name, TimeZone value, TimeZone defVal) {
        if (value != null && !value.equals(defVal))
            gen.write(name, value.getID());
    }

    /**
     * Writes the not null.
     *
     * @param name  the name.
     * @param value the value.
     */
    public void writeNotNull(String name, Date value) {
        if (value != null)
            gen.write(name, Format.formatDT(null, value));
    }

    /**
     * Writes the not empty.
     *
     * @param name    the name.
     * @param values  the values.
     * @param defVals the def vals.
     */
    public <T> void writeNotEmpty(String name, T[] values, T... defVals) {
        if (values.length != 0 && !equals(values, defVals)) {
            gen.writeStartArray(name);
            for (Object value : values)
                gen.write(value.toString());
            gen.writeEnd();
        }
    }

    /**
     * Writes the not empty.
     *
     * @param name the name.
     * @param map  the map.
     */
    public <T> void writeNotEmpty(String name, Map<String, T> map) {
        writeNotEmpty(name, toStrings(map));
    }

    /**
     * Writes the not empty.
     *
     * @param name   the name.
     * @param values the values.
     */
    public void writeNotEmpty(String name, int[] values) {
        if (values.length != 0) {
            gen.writeStartArray(name);
            for (int value : values)
                gen.write(value);
            gen.writeEnd();
        }
    }

    /**
     * Writes the not def.
     *
     * @param name   the name.
     * @param value  the value.
     * @param defVal the def val.
     */
    public void writeNotDef(String name, long value, long defVal) {
        if (value != defVal)
            gen.write(name, value);
    }

    /**
     * Writes the not def.
     *
     * @param name   the name.
     * @param value  the value.
     * @param defVal the def val.
     */
    public void writeNotDef(String name, int value, int defVal) {
        if (value != defVal)
            gen.write(name, value);
    }

    /**
     * Writes the not def.
     *
     * @param name   the name.
     * @param value  the value.
     * @param defVal the def val.
     */
    public void writeNotDef(String name, boolean value, boolean defVal) {
        if (value != defVal)
            gen.write(name, value);
    }

    /**
     * Writes the conn refs.
     *
     * @param conns the conns.
     * @param refs  the refs.
     */
    public void writeConnRefs(List<Connection> conns, List<Connection> refs) {
        writeStartArray("dicomNetworkConnectionReference");
        for (Connection ref : refs)
            write("/dicomNetworkConnection/" + conns.indexOf(ref));
        writeEnd();
    }

}
