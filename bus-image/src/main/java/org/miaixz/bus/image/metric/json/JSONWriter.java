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
package org.miaixz.bus.image.metric.json;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonGenerator;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Format;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.*;
import org.miaixz.bus.image.galaxy.data.PersonName.Group;
import org.miaixz.bus.image.galaxy.io.ImageInputHandler;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.logger.Logger;

import java.io.IOException;
import java.util.*;
import java.util.function.LongFunction;

/**
 * Allows conversion of DICOM files into JSON format.
 * See <a href="http://dicom.nema.org/medical/dicom/current/output/html/part18.html#sect_F.2">DICOM JSON Model</a>.
 * Implements {@link ImageInputHandler} so it can be attached to a
 * {@link ImageInputStream} to produce the JSON while being read. See sample usage below.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JSONWriter implements ImageInputHandler {

    private static final int DOUBLE_MAX_BITS = 53;

    private final JsonGenerator gen;
    private final Deque<Boolean> hasItems = new ArrayDeque<>();
    private final EnumMap<VR, JsonValue.ValueType> jsonTypeByVR = new EnumMap<>(VR.class);
    private String replaceBulkDataURI;

    public JSONWriter(JsonGenerator gen) {
        this.gen = gen;
    }

    private static VR requireIS_DS_SV_UV(VR vr) {
        if (vr != VR.DS && vr != VR.IS && vr != VR.SV && vr != VR.UV)
            throw new IllegalArgumentException("vr:" + vr);
        return vr;
    }

    private static JsonValue.ValueType requireNumberOrString(JsonValue.ValueType jsonType) {
        if (jsonType != JsonValue.ValueType.NUMBER && jsonType != JsonValue.ValueType.STRING)
            throw new IllegalArgumentException("jsonType:" + jsonType);
        return jsonType;
    }

    private static <T> String[] toStrings(Map<String, T> map) {
        String[] ss = new String[map.size()];
        int i = 0;
        for (Map.Entry<String, T> entry : map.entrySet())
            ss[i++] = entry.getKey() + '=' + entry.getValue();
        return ss;
    }

    public static <T> boolean equals(T[] a, T[] a2) {
        int length = a.length;
        if (a2.length != length)
            return false;

        outer:
        for (Object o1 : a) {
            for (Object o2 : a2)
                if (o1.equals(o2))
                    continue outer;
            return false;
        }
        return true;
    }

    public void setJsonType(VR vr, JsonValue.ValueType valueType) {
        jsonTypeByVR.put(requireIS_DS_SV_UV(vr), requireNumberOrString(valueType));
    }

    public String getReplaceBulkDataURI() {
        return replaceBulkDataURI;
    }

    public void setReplaceBulkDataURI(String replaceBulkDataURI) {
        this.replaceBulkDataURI = replaceBulkDataURI;
    }

    /**
     * Writes the given attributes as a full JSON object. Subsequent calls will generate a new JSON
     * object.
     */
    public void write(Attributes attrs) {
        gen.writeStartObject();
        writeAttributes(attrs);
        gen.writeEnd();
    }

    /**
     * Writes the given attributes to JSON. Can be used to output multiple attributes (e.g. metadata,
     * attributes) to the same JSON object.
     */
    public void writeAttributes(Attributes attrs) {
        final SpecificCharacterSet cs = attrs.getSpecificCharacterSet();
        try {
            attrs.accept(new Visitor() {
                             @Override
                             public boolean visit(Attributes attrs, int tag, VR vr, Object value)
                                     throws Exception {
                                 writeAttribute(tag, vr, value, cs, attrs);
                                 return true;
                             }
                         },
                    false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeAttribute(int tag, VR vr, Object value,
                                SpecificCharacterSet cs, Attributes attrs) {
        if (Tag.isGroupLength(tag))
            return;

        gen.writeStartObject(Tag.toHexString(tag));
        gen.write("vr", vr.name());
        if (value instanceof Value)
            writeValue((Value) value, attrs.bigEndian());
        else
            writeValue(vr, value, attrs.bigEndian(),
                    attrs.getSpecificCharacterSet(vr), true);
        gen.writeEnd();
    }

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

    @Override
    public void readValue(ImageInputStream dis, Attributes attrs)
            throws IOException {
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
                    if (tag == Tag.TransferSyntaxUID
                            || tag == Tag.SpecificCharacterSet
                            || tag == Tag.PixelRepresentation
                            || Tag.isPrivateCreator(tag))
                        attrs.setBytes(tag, vr, b);
                    writeValue(vr, b, dis.bigEndian(),
                            attrs.getSpecificCharacterSet(vr), false);
                }
            }
            gen.writeEnd();
        }
    }

    private void writeValue(VR vr, Object val, boolean bigEndian,
                            SpecificCharacterSet cs, boolean preserve) {
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

    private void writeStringValues(VR vr, Object val, boolean bigEndian,
                                   SpecificCharacterSet cs) {
        gen.writeStartArray("Value");
        Object o = vr.toStrings(val, bigEndian, cs);
        String[] ss = (o instanceof String[])
                ? (String[]) o
                : new String[]{(String) o};
        for (String s : ss) {
            if (s == null || s.isEmpty())
                gen.writeNull();
            else switch (vr) {
                case DS:
                    if (jsonTypeByVR.get(VR.DS) == JsonValue.ValueType.NUMBER) {
                        try {
                            gen.write(Builder.parseDS(s));
                        } catch (NumberFormatException e) {
                            Logger.info("illegal DS value: {} - encoded as string", s);
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

    private void writeNumber(String s) {
        try {
            long l = Builder.parseIS(s);
            if ((l < 0 ? -l : l) >> DOUBLE_MAX_BITS == 0) {
                gen.write(l);
                return;
            }
        } catch (NumberFormatException e) {
            Logger.info("illegal IS value: {} - encoded as string", s);
        }
        gen.write(s);
    }

    private void writeDoubleValues(VR vr, Object val, boolean bigEndian) {
        gen.writeStartArray("Value");
        int vm = vr.vmOf(val);
        for (int i = 0; i < vm; i++) {
            double d = vr.toDouble(val, bigEndian, i, 0);
            if (Double.isNaN(d)) {
                Logger.info("encode {} NaN as null", vr);
                gen.writeNull();
            } else {
                if (d == Double.POSITIVE_INFINITY) {
                    d = Double.MAX_VALUE;
                    Logger.info("encode {} Infinity as {}", vr, d);
                } else if (d == Double.NEGATIVE_INFINITY) {
                    d = -Double.MAX_VALUE;
                    Logger.info("encode {} -Infinity as {}", vr, d);
                }
                gen.write(d);
            }
        }
        gen.writeEnd();
    }

    private void writeIntValues(VR vr, Object val, boolean bigEndian) {
        gen.writeStartArray("Value");
        int vm = vr.vmOf(val);
        for (int i = 0; i < vm; i++) {
            gen.write(vr.toInt(val, bigEndian, i, 0));
        }
        gen.writeEnd();
    }

    private void writeUIntValues(VR vr, Object val, boolean bigEndian) {
        gen.writeStartArray("Value");
        int vm = vr.vmOf(val);
        for (int i = 0; i < vm; i++) {
            gen.write(vr.toInt(val, bigEndian, i, 0) & 0xffffffffL);
        }
        gen.writeEnd();
    }

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

    private void writePersonName(String s) {
        PersonName pn = new PersonName(s, true);
        gen.writeStartObject();
        writePNGroup("Alphabetic", pn, Group.Alphabetic);
        writePNGroup("Ideographic", pn, Group.Ideographic);
        writePNGroup("Phonetic", pn, Group.Phonetic);
        gen.writeEnd();
    }

    private void writePNGroup(String name, PersonName pn, Group group) {
        if (pn.contains(group))
            gen.write(name, pn.toString(group, true));
    }

    private void writeInlineBinary(VR vr, byte[] b, boolean bigEndian,
                                   boolean preserve) {
        if (bigEndian)
            b = vr.toggleEndian(b, preserve);
        gen.write("InlineBinary", encodeBase64(b));
    }

    private String encodeBase64(byte[] b) {
        int len = (b.length * 4 / 3 + 3) & ~3;
        char[] ch = new char[len];
        Builder.encode(b, 0, b.length, ch, 0);
        return new String(ch);
    }

    private void writeBulkData(BulkData blkdata) {
        gen.write("BulkDataURI", replaceBulkDataURI != null ? replaceBulkDataURI : blkdata.getURI());
    }

    @Override
    public void readValue(ImageInputStream dis, Sequence seq)
            throws IOException {
        if (!hasItems.getLast()) {
            gen.writeStartArray("Value");
            hasItems.removeLast();
            hasItems.addLast(true);
        }
        gen.writeStartObject();
        dis.readValue(dis, seq);
        gen.writeEnd();
    }

    @Override
    public void readValue(ImageInputStream dis, Fragments frags)
            throws IOException {
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
                writeInlineBinary(frags.vr(), dis.readValue(),
                        dis.bigEndian(), false);
            }
            gen.writeEnd();
        }
    }

    @Override
    public void startDataset(ImageInputStream dis) {
        gen.writeStartObject();
    }

    @Override
    public void endDataset(ImageInputStream dis) {
        gen.writeEnd();
    }

    public JsonGenerator writeStartObject() {
        return gen.writeStartObject();
    }

    public JsonGenerator writeStartObject(String name) {
        return gen.writeStartObject(name);
    }

    public JsonGenerator writeStartArray() {
        return gen.writeStartArray();
    }

    public JsonGenerator writeStartArray(String name) {
        return gen.writeStartArray(name);
    }

    public JsonGenerator write(String name, int value) {
        return gen.write(name, value);
    }

    public JsonGenerator write(String name, boolean value) {
        return gen.write(name, value);
    }

    public JsonGenerator writeEnd() {
        return gen.writeEnd();
    }

    public JsonGenerator write(String value) {
        return gen.write(value);
    }

    public <T> void writeNotNullOrDef(String name, T value, T defVal) {
        if (value != null && !value.equals(defVal))
            gen.write(name, value.toString());
    }

    public void writeNotNull(String name, Boolean value) {
        if (value != null)
            gen.write(name, value.booleanValue());
    }

    public void writeNotNull(String name, Integer value) {
        if (value != null)
            gen.write(name, value.intValue());
    }

    public void writeNotNull(String name, Long value) {
        if (value != null)
            gen.write(name, value.longValue());
    }

    public void writeNotNullOrDef(String name, TimeZone value, TimeZone defVal) {
        if (value != null && !value.equals(defVal))
            gen.write(name, value.getID());
    }

    public void writeNotNull(String name, Date value) {
        if (value != null)
            gen.write(name, Format.formatDT(null, value));
    }

    public <T> void writeNotEmpty(String name, T[] values, T... defVals) {
        if (values.length != 0 && !equals(values, defVals)) {
            gen.writeStartArray(name);
            for (Object value : values)
                gen.write(value.toString());
            gen.writeEnd();
        }
    }

    public <T> void writeNotEmpty(String name, Map<String, T> map) {
        writeNotEmpty(name, toStrings(map));
    }

    public void writeNotEmpty(String name, int[] values) {
        if (values.length != 0) {
            gen.writeStartArray(name);
            for (int value : values)
                gen.write(value);
            gen.writeEnd();
        }
    }

    public void writeNotDef(String name, long value, long defVal) {
        if (value != defVal)
            gen.write(name, value);
    }

    public void writeNotDef(String name, int value, int defVal) {
        if (value != defVal)
            gen.write(name, value);
    }

    public void writeNotDef(String name, boolean value, boolean defVal) {
        if (value != defVal)
            gen.write(name, value);
    }

    public void writeConnRefs(List<Connection> conns, List<Connection> refs) {
        writeStartArray("dicomNetworkConnectionReference");
        for (Connection ref : refs)
            write("/dicomNetworkConnection/" + conns.indexOf(ref));
        writeEnd();
    }

}
