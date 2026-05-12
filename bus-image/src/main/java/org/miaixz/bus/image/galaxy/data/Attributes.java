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

import java.io.*;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.io.ImageEncodingOptions;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.logger.Logger;

/**
 * DICOM attribute set used to store and manipulate DICOM data elements.
 * <p>
 * This class provides operations for DICOM attributes, including standard and private attributes. It handles string,
 * numeric, date, sequence, and related DICOM data element values.
 * </p>
 * <p>
 * This class implements serialization and can be serialized and deserialized.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Attributes implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852260209995L;

    /**
     * Coercion mode constant
     */
    public static final String COERCE = "COERCE";

    /**
     * Correction mode constant
     */
    public static final String CORRECT = "CORRECT";

    /**
     * Initial capacity
     */
    private static final int INIT_CAPACITY = 16;

    /**
     * Method limit value
     */
    private static final int TO_STRING_LIMIT = 50;

    /**
     * Method width value
     */
    private static final int TO_STRING_WIDTH = 78;

    /**
     * Parent attribute set
     */
    private transient Attributes parent;

    /**
     * Parent sequence private creator
     */
    private transient String parentSequencePrivateCreator;

    /**
     * Parent sequence tag
     */
    private transient int parentSequenceTag;

    /**
     * Tag array
     */
    private transient int[] tags;

    /**
     * VR array
     */
    private transient VR[] vrs;

    /**
     * Value array
     */
    private transient Object[] values;

    /**
     * Size.
     */
    private transient int size;

    /**
     * Specific character set.
     */
    private transient SpecificCharacterSet cs;

    /**
     * Time zone.
     */
    private transient TimeZone tz;

    /**
     * Length.
     */
    private transient int length = -1;

    /**
     * Group length array
     */
    private transient int[] groupLengths;

    /**
     * Group length index zero
     */
    private transient int groupLengthIndex0;

    /**
     * Whether big endian
     */
    private volatile boolean bigEndian;

    /**
     * Item position
     */
    private long itemPosition = -1;

    /**
     * Whether a specific character set is present
     */
    private boolean containsSpecificCharacterSet;

    /**
     * Whether UTC time zone offset is present
     */
    private boolean containsTimezoneOffsetFromUTC;

    /**
     * Property map
     */
    private Map<String, Object> properties;

    /**
     * Default time zone
     */
    private TimeZone defaultTimeZone;

    /**
     * Read-only flag.
     */
    private volatile boolean readOnly;

    /**
     * Default constructor
     */
    public Attributes() {
        this(false, INIT_CAPACITY);
    }

    /**
     * Constructor
     *
     * @param bigEndian Whether big endian
     */
    public Attributes(boolean bigEndian) {
        this(bigEndian, INIT_CAPACITY);
    }

    /**
     * Constructor
     *
     * @param initialCapacity Initial capacity
     */
    public Attributes(int initialCapacity) {
        this(false, initialCapacity);
    }

    /**
     * Constructor
     *
     * @param bigEndian       Whether big endian
     * @param initialCapacity Initial capacity
     */
    public Attributes(boolean bigEndian, int initialCapacity) {
        this.bigEndian = bigEndian;
        init(initialCapacity);
    }

    /**
     * Constructor
     *
     * @param other other attribute set
     */
    public Attributes(Attributes other) {
        this(other, other.bigEndian);
    }

    /**
     * Constructor
     *
     * @param other     other attribute set
     * @param bigEndian Whether big endian
     */
    public Attributes(Attributes other, boolean bigEndian) {
        this(bigEndian, other.size);
        if (other.properties != null)
            properties = new HashMap<String, Object>(other.properties);
        addAll(other);
    }

    /**
     * Constructor
     *
     * @param other     other attribute set
     * @param selection selected tags
     */
    public Attributes(Attributes other, int... selection) {
        this(other, other.bigEndian, selection);
    }

    /**
     * Constructor
     *
     * @param other     other attribute set
     * @param bigEndian Whether big endian
     * @param selection selected tags
     */
    public Attributes(Attributes other, boolean bigEndian, int... selection) {
        this(bigEndian, selection.length);
        if (other.properties != null)
            properties = new HashMap<>(other.properties);
        addSelected(other, selection);
    }

    /**
     * Constructor
     *
     * @param other     other attribute set
     * @param bigEndian Whether big endian
     * @param selection selected attributes
     */
    public Attributes(Attributes other, boolean bigEndian, Attributes selection) {
        this(bigEndian, selection.size());
        if (other.properties != null)
            properties = new HashMap<>(other.properties);
        addSelected(other, selection);
    }

    /**
     * Clears the attribute set
     */
    public void clear() {
        size = 0;
        Arrays.fill(tags, 0);
        Arrays.fill(vrs, null);
        Arrays.fill(values, null);
    }

    /**
     * Initialization method
     *
     * @param initialCapacity Initial capacity
     */
    private void init(int initialCapacity) {
        this.tags = new int[initialCapacity];
        this.vrs = new VR[initialCapacity];
        this.values = new Object[initialCapacity];
    }

    /**
     * Tests whether this attribute set is read-only.
     *
     * @return {@code true} if this attribute set is read-only
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Marks as read-only
     */
    public void setReadOnly() {
        this.readOnly = true;
        for (int i = 0, n = size; i < n; i++) {
            Object value = values[i];
            if (value instanceof Sequence)
                ((Sequence) value).setReadOnly();
            else if (value instanceof Fragments)
                ((Fragments) value).setReadOnly();
        }
    }

    /**
     * Ensures the instance is modifiable
     */
    private void ensureModifiable() {
        if (readOnly) {
            throw new UnsupportedOperationException("read-only");
        }
    }

    /**
     * Gets the property map.
     *
     * @return Property map
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Sets the property map.
     *
     * @param properties Property map
     */
    public void setProperties(Map<String, Object> properties) {
        ensureModifiable();
        this.properties = properties;
    }

    /**
     * Gets the property value
     *
     * @param key    key
     * @param defVal default value
     * @return result
     */
    public Object getProperty(String key, Object defVal) {
        if (properties == null)
            return defVal;
        Object val = properties.get(key);
        return val != null ? val : defVal;
    }

    /**
     * Sets the property value
     *
     * @param key   key
     * @param value value
     * @return previous value
     */
    public Object setProperty(String key, Object value) {
        ensureModifiable();
        if (properties == null)
            properties = new HashMap<>();
        return properties.put(key, value);
    }

    /**
     * Clears the property
     *
     * @param key key
     * @return previous value
     */
    public Object clearProperty(String key) {
        ensureModifiable();
        return properties != null ? properties.remove(key) : null;
    }

    /**
     * Whether this is the root attribute set
     *
     * @return Whether this is the root attribute set
     */
    public final boolean isRoot() {
        return parent == null;
    }

    /**
     * Gets the root attribute set
     *
     * @return result
     */
    public final Attributes getRoot() {
        return isRoot() ? this : parent.getRoot();
    }

    /**
     * Gets the level
     *
     * @return level
     */
    public final int getLevel() {
        return isRoot() ? 0 : 1 + parent.getLevel();
    }

    /**
     * Whether big endian
     *
     * @return Whether big endian
     */
    public final boolean bigEndian() {
        return bigEndian;
    }

    /**
     * Gets the parent attribute set.
     *
     * @return Parent attribute set
     */
    public final Attributes getParent() {
        return parent;
    }

    /**
     * Gets the parent sequence private creator.
     *
     * @return Parent sequence private creator
     */
    public String getParentSequencePrivateCreator() {
        return parentSequencePrivateCreator;
    }

    /**
     * Gets the parent sequence tag.
     *
     * @return Parent sequence tag
     */
    public int getParentSequenceTag() {
        return parentSequenceTag;
    }

    /**
     * Gets the length
     *
     * @return length
     */
    public final int getLength() {
        return length;
    }

    /**
     * Sets the parent
     *
     * @param parent                       Parent attribute set
     * @param parentSequencePrivateCreator Parent sequence private creator
     * @param parentSequenceTag            Parent sequence tag
     * @return current attribute set
     */
    Attributes setParent(Attributes parent, String parentSequencePrivateCreator, int parentSequenceTag) {
        if (parent != null) {
            if (this.parent != null)
                throw new IllegalArgumentException("Item already contained by Sequence");
            if (!containsSpecificCharacterSet)
                cs = null;
            if (!containsTimezoneOffsetFromUTC)
                tz = null;
            if (parent.bigEndian != bigEndian)
                toggleEndian();
        }
        this.parent = parent;
        this.parentSequencePrivateCreator = parentSequencePrivateCreator;
        this.parentSequenceTag = parentSequenceTag;
        return this;
    }

    /**
     * Toggles endian order
     */
    private void toggleEndian() {
        for (int i = 0; i < size; i++) {
            Object value = values[i];
            if (value instanceof byte[]) {
                vrs[i].toggleEndian((byte[]) value, false);
            } else if (value instanceof Sequence) {
                for (Attributes item : (Sequence) value) {
                    item.toggleEndian();
                }
            }
            bigEndian = !bigEndian;
        }
    }

    /**
     * Gets the item position.
     *
     * @return Item position
     */
    public final long getItemPosition() {
        return itemPosition;
    }

    /**
     * Sets the item position.
     *
     * @param itemPosition Item position
     */
    public final void setItemPosition(long itemPosition) {
        this.itemPosition = itemPosition;
    }

    /**
     * Whether empty
     *
     * @return Whether empty
     */
    public final boolean isEmpty() {
        return size == 0;
    }

    /**
     * Gets the number of attributes.
     *
     * @return size
     */
    public final int size() {
        return size;
    }

    /**
     * Gets the item pointers.
     *
     * @return item pointer array
     */
    public ItemPointer[] itemPointers() {
        return itemPointers(0);
    }

    /**
     * Gets the item pointers.
     *
     * @param n offset
     * @return item pointer array
     */
    private ItemPointer[] itemPointers(int n) {
        if (parent == null)
            return new ItemPointer[n];
        ItemPointer[] itemPointers = parent.itemPointers(n + 1);
        itemPointers[itemPointers.length - n - 1] = new ItemPointer(parentSequencePrivateCreator, parentSequenceTag,
                itemIndex());
        return itemPointers;
    }

    /**
     * Gets the item index.
     *
     * @return item index
     */
    public int itemIndex() {
        if (parent == null)
            return -1;
        Sequence seq = parent.getSequence(parentSequencePrivateCreator, parentSequenceTag);
        if (seq == null)
            return -1;
        return seq.indexOf(this);
    }

    /**
     * Gets a copy of the tag array.
     *
     * @return Tag array
     */
    public int[] tags() {
        return Arrays.copyOf(tags, size);
    }

    /**
     * Resizes
     */
    public void trimToSize() {
        trimToSize(false);
    }

    /**
     * Resizes
     *
     * @param recursive whether recursive
     */
    public void trimToSize(boolean recursive) {
        ensureModifiable();
        int oldCapacity = tags.length;
        if (size < oldCapacity) {
            tags = Arrays.copyOf(tags, size);
            vrs = Arrays.copyOf(vrs, size);
            values = Arrays.copyOf(values, size);
        }
        if (recursive)
            for (Object value : values) {
                if (value instanceof Sequence) {
                    ((Sequence) value).trimToSize(recursive);
                } else if (value instanceof Fragments)
                    ((Fragments) value).trimToSize();
            }
    }

    /**
     * Interns string values
     *
     * @param decode whether to decode
     */
    public void internalizeStringValues(boolean decode) {
        ensureModifiable();
        SpecificCharacterSet cs = getSpecificCharacterSet();
        for (int i = 0; i < values.length; i++) {
            VR vr = vrs[i];
            Object value = values[i];
            if (vr.isStringType()) {
                if (value instanceof byte[]) {
                    if (!decode)
                        continue;
                    value = vr.toStrings((byte[]) value, bigEndian, cs);
                }
                if (value instanceof String)
                    values[i] = ((String) value).intern();
                else if (value instanceof String[]) {
                    String[] ss = (String[]) value;
                    for (int j = 0; j < ss.length; j++)
                        ss[j] = ss[j].intern();
                }
            } else if (value instanceof Sequence)
                for (Attributes item : (Sequence) value)
                    item.internalizeStringValues(decode);
        }
    }

    /**
     * Decodes string values with the specific character set
     */
    private void decodeStringValuesUsingSpecificCharacterSet() {
        Object value;
        VR vr;
        SpecificCharacterSet cs = getSpecificCharacterSet();
        for (int i = 0; i < size; i++) {
            value = values[i];
            if (value instanceof Sequence) {
                for (Attributes item : (Sequence) value)
                    item.decodeStringValuesUsingSpecificCharacterSet();
            } else if ((vr = vrs[i]).useSpecificCharacterSet())
                if (value instanceof byte[])
                    values[i] = vr.toStrings((byte[]) value, bigEndian, cs);
        }
    }

    /**
     * Ensures capacity
     *
     * @param minCapacity parameter
     */
    private void ensureCapacity(int minCapacity) {
        int oldCapacity = tags.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = Math.max(minCapacity, oldCapacity << 1);
            tags = Arrays.copyOf(tags, newCapacity);
            vrs = Arrays.copyOf(vrs, newCapacity);
            values = Arrays.copyOf(values, newCapacity);
        }
    }

    /**
     * Gets the nested dataset
     *
     * @param sequenceTag sequencetag
     * @return result
     */
    public Attributes getNestedDataset(int sequenceTag) {
        return getNestedDataset(null, sequenceTag, 0);
    }

    /**
     * Gets the nested dataset
     *
     * @param sequenceTag sequencetag
     * @param itemIndex   item index
     * @return result
     */
    public Attributes getNestedDataset(int sequenceTag, int itemIndex) {
        return getNestedDataset(null, sequenceTag, itemIndex);
    }

    /**
     * Gets the nested dataset
     *
     * @param privateCreator private creator
     * @param sequenceTag    sequencetag
     * @return result
     */
    public Attributes getNestedDataset(String privateCreator, int sequenceTag) {
        return getNestedDataset(privateCreator, sequenceTag, 0);
    }

    /**
     * Gets the nested dataset
     *
     * @param privateCreator private creator
     * @param sequenceTag    sequencetag
     * @param itemIndex      item index
     * @return result
     */
    public Attributes getNestedDataset(String privateCreator, int sequenceTag, int itemIndex) {
        Object value = getSequence(privateCreator, sequenceTag);
        if (value == null)
            return null;
        Sequence sq = (Sequence) value;
        if (itemIndex >= sq.size())
            return null;
        return sq.get(itemIndex);
    }

    /**
     * Gets the nested dataset
     *
     * @param itemPointers item pointer
     * @return result
     */
    public Attributes getNestedDataset(ItemPointer... itemPointers) {
        return getNestedDataset(Arrays.asList(itemPointers));
    }

    /**
     * Gets the nested dataset
     *
     * @param itemPointers item pointer list
     * @return result
     */
    public Attributes getNestedDataset(List<ItemPointer> itemPointers) {
        Attributes item = this;
        for (ItemPointer ip : itemPointers) {
            Object value = item.getValue(ip.privateCreator, ip.sequenceTag);
            if (!(value instanceof Sequence))
                return null;
            Sequence sq = (Sequence) value;
            if (ip.itemIndex >= sq.size())
                return null;
            item = sq.get(ip.itemIndex);
        }
        return item;
    }

    /**
     * Gets the functional group
     *
     * @param sequenceTag sequencetag
     * @param frameIndex  parameter
     * @return result
     */
    public Attributes getFunctionGroup(int sequenceTag, int frameIndex) {
        Attributes sfgs = getNestedDataset(Tag.SharedFunctionalGroupsSequence);
        if (sfgs == null)
            return null;
        Attributes item = sfgs.getNestedDataset(sequenceTag);
        if (item != null)
            return item;
        Attributes fgs = getNestedDataset(Tag.PerFrameFunctionalGroupsSequence, frameIndex);
        if (fgs == null)
            return null;
        return fgs.getNestedDataset(sequenceTag);
    }

    /**
     * Gets the insertion index
     *
     * @param tag tag
     * @return result
     */
    private int indexForInsertOf(int tag) {
        return size == 0 ? -1 : tags[size - 1] < tag ? -(size + 1) : indexOf(tag);
    }

    /**
     * Gets the index
     *
     * @param tag tag
     * @return result
     */
    private int indexOf(int tag) {
        return Arrays.binarySearch(tags, 0, size, tag);
    }

    /**
     * Gets the index
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return result
     */
    private int indexOf(String privateCreator, int tag) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(privateCreator, tag, false);
            if (creatorTag == -1)
                return -1;
            tag = Tag.toPrivateTag(creatorTag, tag);
        }
        return indexOf(tag);
    }

    /**
     * Resolves an actual private tag from a private tag with a placeholder, such as 0011,xx13.
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return result
     */
    public int tagOf(String privateCreator, int tag) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(privateCreator, tag, false);
            if (creatorTag == -1)
                return -1;
            tag = Tag.toPrivateTag(creatorTag, tag);
        }
        return tag;
    }

    /**
     * Gets the creator tag
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param reserve        whether to reserve
     * @return creator tag
     */
    private int creatorTagOf(String privateCreator, int tag, boolean reserve) {
        if (!Tag.isPrivateGroup(tag))
            throw new IllegalArgumentException(Tag.toString(tag) + " is not a private Data Element");
        int group = tag & 0xffff0000;
        int creatorTag = group | 0x10;
        int index = indexOf(creatorTag);
        if (index < 0)
            index = -index - 1;
        while (index < size && (tags[index] & 0xffffff00) == group) {
            creatorTag = tags[index];
            if (vrs[index].isStringType()) {
                Object creatorID = decodeStringValue(index);
                if (privateCreator.equals(creatorID))
                    return creatorTag;
            }
            index++;
            creatorTag++;
        }
        if (!reserve)
            return -1;
        if ((creatorTag & 0xff00) != 0)
            throw new IllegalStateException("No free block for Private Element " + Tag.toString(tag));
        setString(creatorTag, VR.LO, privateCreator);
        return creatorTag;
    }

    /**
     * Decodes string values
     *
     * @param index parameter
     * @return result
     */
    private Object decodeStringValue(int index) {
        Object value = loadBulkData(vrs[index], values[index]);
        return decodeStringValue(index, value);
    }

    /**
     * Decodes string values
     *
     * @param index parameter
     * @param value value
     * @return result
     */
    private Object decodeStringValue(int index, Object value) {
        if (value instanceof byte[]) {
            value = vrs[index].toStrings((byte[]) value, bigEndian, getSpecificCharacterSet(vrs[index]));
            if (value instanceof String && ((String) value).isEmpty())
                value = Value.NULL;
            values[index] = value;
        }
        return value;
    }

    /**
     * Loads and stores bulk data
     *
     * @param index parameter
     * @return result
     */
    private Object loadAndStoreBulkData(int index) {
        return values[index] = loadBulkData(vrs[index], values[index]);
    }

    /**
     * Loads bulk data
     *
     * @param vr    VR
     * @param value value
     * @return result
     */
    private Object loadBulkData(VR vr, Object value) {
        try {
            return (value instanceof BulkData) ? ((BulkData) value).toBytes(vr, bigEndian) : value;
        } catch (Exception e) {
            Logger.info(false, "Image", "Failed to load {}", value);
            return Value.NULL;
        }
    }

    /**
     * Gets the effective specific character set for the VR.
     *
     * @param vr VR
     * @return result
     */
    public SpecificCharacterSet getSpecificCharacterSet(VR vr) {
        return vr.useSpecificCharacterSet() ? getSpecificCharacterSet() : SpecificCharacterSet.ASCII;
    }

    /**
     * Decodes DS values
     *
     * @param index parameter
     * @return result
     */
    private double[] decodeDSValue(int index) {
        Object value = index < 0 ? Value.NULL : values[index];
        if (value == Value.NULL)
            return new double[] {};
        if (value instanceof double[])
            return (double[]) value;
        double[] ds;
        if (value instanceof byte[])
            value = vrs[index].toStrings(value, bigEndian, SpecificCharacterSet.ASCII);
        if (value instanceof String) {
            String s = (String) value;
            if (s.isEmpty()) {
                values[index] = Value.NULL;
                return new double[] {};
            }
            ds = new double[] { Builder.parseDS(s) };
        } else { // value instanceof String[]
            String[] ss = (String[]) value;
            ds = new double[ss.length];
            for (int i = 0; i < ds.length; i++) {
                String s = ss[i];
                ds[i] = (s != null && !s.isEmpty()) ? Builder.parseDS(s) : Double.NaN;
            }
        }
        values[index] = ds;
        return ds;
    }

    /**
     * Decodes IS values
     *
     * @param index parameter
     * @return result
     */
    private long[] decodeISValue(int index) {
        Object value = index < 0 ? Value.NULL : values[index];
        if (value == Value.NULL)
            return new long[] {};
        if (value instanceof long[])
            return (long[]) value;
        long[] ls;
        if (value instanceof byte[])
            value = vrs[index].toStrings(value, bigEndian, SpecificCharacterSet.ASCII);
        if (value instanceof String) {
            String s = (String) value;
            if (s.isEmpty()) {
                values[index] = Value.NULL;
                return new long[] {};
            }
            ls = new long[] { Builder.parseIS(s) };
        } else { // value instanceof String[]
            String[] ss = (String[]) value;
            ls = new long[ss.length];
            for (int i = 0; i < ls.length; i++) {
                String s = ss[i];
                ls[i] = (s != null && !s.isEmpty()) ? Builder.parseIS(s) : Long.MIN_VALUE;
            }
        }
        values[index] = ls;
        return ls;
    }

    /**
     * Updates the VR
     *
     * @param index parameter
     * @param vr    VR
     * @return result
     */
    private VR updateVR(int index, VR vr) {
        VR prev = vrs[index];
        if (vr == null || vr == prev)
            return prev;
        Object value = values[index];
        if (!(value == Value.NULL || value instanceof byte[]
                || vr.isStringType() && (value instanceof String || value instanceof String[])))
            throw new IllegalStateException("value instanceof " + value.getClass());
        return vrs[index] = vr;
    }

    /**
     * Whether the value is empty
     *
     * @param value value
     * @return Whether the value is empty
     */
    private static boolean isEmpty(Object value) {
        return (value instanceof Value) && ((Value) value).isEmpty();
    }

    /**
     * Whether the specified tag is present
     *
     * @param tag tag
     * @return whether present
     */
    public boolean contains(int tag) {
        return indexOf(tag) >= 0;
    }

    /**
     * Whether the specified tag is present
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return whether present
     */
    public boolean contains(String privateCreator, int tag) {
        return indexOf(privateCreator, tag) >= 0;
    }

    /**
     * Whether the specified tag has a value
     *
     * @param tag tag
     * @return whether present
     */
    public boolean containsValue(int tag) {
        return containsValue(null, tag);
    }

    /**
     * Whether the specified tag has a value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return whether present
     */
    public boolean containsValue(String privateCreator, int tag) {
        int index = indexOf(privateCreator, tag);
        return index >= 0 && !isEmpty(vrs[index].isStringType() ? decodeStringValue(index) : values[index]);
    }

    /**
     * Tests whether at least one tag is present in the given range
     *
     * @param firstTag parameter
     * @param lastTag  parameter
     * @return result
     */
    public boolean containsTagInRange(int firstTag, int lastTag) {
        final int indexFirstTag = indexForInsertOf(firstTag);
        if (indexFirstTag >= 0) {
            return true;
        }
        int insertIndex = -indexFirstTag - 1;
        return insertIndex < size && tags[insertIndex] <= lastTag;
    }

    /**
     * Gets the private creator for the specified tag
     *
     * @param tag tag
     * @return private creator
     */
    public String privateCreatorOf(int tag) {
        return Tag.isPrivateTag(tag) ? privateCreatorAt(indexOf(Tag.creatorTagOf(tag))) : null;
    }

    /**
     * Gets the private creator at the specified index
     *
     * @param index parameter
     * @return private creator
     */
    private String privateCreatorAt(int index) {
        Object value;
        return (index < 0 || !vrs[index].isStringType() || (value = decodeStringValue(index)) == Value.NULL) ? null
                : VR.LO.toString(value, false, 0, null);
    }

    /**
     * Gets the value
     *
     * @param tag tag
     * @return value
     */
    public Object getValue(int tag) {
        return getValue(null, tag, null);
    }

    /**
     * Gets the value
     *
     * @param tag tag
     * @param vr  VR holder
     * @return value
     */
    public Object getValue(int tag, VR.Holder vr) {
        return getValue(null, tag, vr);
    }

    /**
     * Gets the value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return value
     */
    public Object getValue(String privateCreator, int tag) {
        return getValue(privateCreator, tag, null);
    }

    /**
     * Gets the value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR holder
     * @return value
     */
    public Object getValue(String privateCreator, int tag, VR.Holder vr) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;
        if (vr != null)
            vr.vr = vrs[index];
        return values[index];
    }

    /**
     * Gets the VR
     *
     * @param tag tag
     * @return VR
     */
    public VR getVR(int tag) {
        return getVR(null, tag);
    }

    /**
     * Gets the VR
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return VR
     */
    public VR getVR(String privateCreator, int tag) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;
        return vrs[index];
    }

    /**
     * Gets the sequence
     *
     * @param tag tag
     * @return sequence
     */
    public Sequence getSequence(int tag) {
        return getSequence(null, tag);
    }

    /**
     * Gets the sequence
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return sequence
     */
    public Sequence getSequence(String privateCreator, int tag) {
        int sqtag = tag;
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(privateCreator, tag, false);
            if (creatorTag == -1)
                return null;
            sqtag = Tag.toPrivateTag(creatorTag, tag);
        }
        int index = indexOf(sqtag);
        if (index < 0)
            return null;
        VR vr = vrs[index];
        if (vr != VR.SQ && vr != VR.UN)
            return null;
        Object value = values[index];
        if (value instanceof Sequence)
            return (Sequence) value;
        if (value == Value.NULL) {
            vrs[index] = VR.SQ;
            values[index] = new Sequence(this, privateCreator, tag, 0);
        } else {
            try {
                ImageInputStream.parseUNSequence((byte[]) value, this, sqtag);
            } catch (IOException e) {
                return null;
            }
        }
        return (Sequence) values[index];
    }

    /**
     * Gets the byte array
     *
     * @param tag tag
     * @return byte array
     * @throws IOException IOerror
     */
    public byte[] getBytes(int tag) throws IOException {
        return getBytes(null, tag);
    }

    /**
     * Gets the byte array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return byte array
     * @throws IOException IOerror
     */
    public byte[] getBytes(String privateCreator, int tag) throws IOException {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;
        Object value = values[index];
        VR vr = vrs[index];
        try {
            if (value instanceof Value)
                return ((Value) value).toBytes(vr, bigEndian);
            return vr.toBytes(value, getSpecificCharacterSet(vr));
        } catch (UnsupportedOperationException e) {
            Logger.info(false, "Image", "Attempt to access {} {} as bytes", Tag.toString(tag), vr);
            return null;
        }
    }

    /**
     * Safely gets the byte array.
     *
     * @param tag tag
     * @return byte array
     */
    public byte[] getSafeBytes(int tag) {
        return getSafeBytes(null, tag);
    }

    /**
     * Safely gets the byte array.
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return byte array
     */
    public byte[] getSafeBytes(String privateCreator, int tag) {
        try {
            return getBytes(privateCreator, tag);
        } catch (IOException e) {
            Logger.warn(
                    false,
                    "Image",
                    e,
                    "DICOM attribute access failed: tag={}, exception={}",
                    Tag.toString(tag),
                    e.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * Gets the string
     *
     * @param tag tag
     * @return result
     */
    public String getString(int tag) {
        return getString(null, tag, null, 0, null);
    }

    /**
     * Gets the string
     *
     * @param tag    tag
     * @param defVal default value
     * @return result
     */
    public String getString(int tag, String defVal) {
        return getString(null, tag, null, 0, defVal);
    }

    /**
     * Gets the string
     *
     * @param tag        tag
     * @param valueIndex value index
     * @return result
     */
    public String getString(int tag, int valueIndex) {
        return getString(null, tag, null, valueIndex, null);
    }

    /**
     * Gets the string
     *
     * @param tag        tag
     * @param valueIndex value index
     * @param defVal     default value
     * @return result
     */
    public String getString(int tag, int valueIndex, String defVal) {
        return getString(null, tag, null, valueIndex, defVal);
    }

    /**
     * Gets the string
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return result
     */
    public String getString(String privateCreator, int tag) {
        return getString(privateCreator, tag, null, 0, null);
    }

    /**
     * Gets the string
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param defVal         default value
     * @return result
     */
    public String getString(String privateCreator, int tag, String defVal) {
        return getString(privateCreator, tag, null, 0, defVal);
    }

    /**
     * Gets the string
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @return result
     */
    public String getString(String privateCreator, int tag, VR vr) {
        return getString(privateCreator, tag, vr, 0, null);
    }

    /**
     * Gets the string
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param defVal         default value
     * @return result
     */
    public String getString(String privateCreator, int tag, VR vr, String defVal) {
        return getString(privateCreator, tag, vr, 0, defVal);
    }

    /**
     * Gets the string
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param valueIndex     value index
     * @return result
     */
    public String getString(String privateCreator, int tag, int valueIndex) {
        return getString(privateCreator, tag, null, valueIndex, null);
    }

    /**
     * Gets the string
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param valueIndex     value index
     * @param defVal         default value
     * @return result
     */
    public String getString(String privateCreator, int tag, int valueIndex, String defVal) {
        return getString(privateCreator, tag, null, valueIndex, defVal);
    }

    /**
     * Gets the string
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param valueIndex     value index
     * @return result
     */
    public String getString(String privateCreator, int tag, VR vr, int valueIndex) {
        return getString(privateCreator, tag, vr, valueIndex, null);
    }

    /**
     * Gets the string
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param valueIndex     value index
     * @param defVal         default value
     * @return result
     */
    public String getString(String privateCreator, int tag, VR vr, int valueIndex, String defVal) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return defVal;
        Object value = values[index];
        if (value == Value.NULL)
            return defVal;
        vr = updateVR(index, vr);
        value = loadBulkData(vr, value);
        if (vr.isStringType()) {
            value = decodeStringValue(index, value);
        }
        if (value == Value.NULL)
            return defVal;
        try {
            return vr.toString(value, bigEndian, valueIndex, defVal);
        } catch (UnsupportedOperationException e) {
            Logger.info(false, "Image", "Attempt to access {} {} as string", Tag.toString(tag), vr);
            return defVal;
        }
    }

    /**
     * Gets the string array
     *
     * @param tag tag
     * @return string array
     */
    public String[] getStrings(int tag) {
        return getStrings(null, tag, null);
    }

    /**
     * Gets the string array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return string array
     */
    public String[] getStrings(String privateCreator, int tag) {
        return getStrings(privateCreator, tag, null);
    }

    /**
     * Gets the string array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @return string array
     */
    public String[] getStrings(String privateCreator, int tag, VR vr) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;
        Object value = values[index];
        if (value == Value.NULL)
            return Normal.EMPTY_STRING_ARRAY;
        vr = updateVR(index, vr);
        value = loadBulkData(vr, value);
        if (vr.isStringType()) {
            value = decodeStringValue(index, value);
        }
        if (value == Value.NULL)
            return Normal.EMPTY_STRING_ARRAY;
        try {
            return toStrings(vr.toStrings(value, bigEndian, getSpecificCharacterSet(vr)));
        } catch (UnsupportedOperationException e) {
            Logger.info(false, "Image", "Attempt to access {} {} as string", Tag.toString(tag), vr);
            return null;
        }
    }

    /**
     * Converts to a string array
     *
     * @param val value
     * @return string array
     */
    private static String[] toStrings(Object val) {
        return (val instanceof String) ? new String[] { (String) val } : (String[]) val;
    }

    /**
     * Gets the integer value
     *
     * @param tag    tag
     * @param defVal default value
     * @return result
     */
    public int getInt(int tag, int defVal) {
        return getInt(null, tag, null, 0, defVal);
    }

    /**
     * Gets the integer value
     *
     * @param tag        tag
     * @param valueIndex value index
     * @param defVal     default value
     * @return result
     */
    public int getInt(int tag, int valueIndex, int defVal) {
        return getInt(null, tag, null, valueIndex, defVal);
    }

    /**
     * Gets the integer value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param defVal         default value
     * @return result
     */
    public int getInt(String privateCreator, int tag, int defVal) {
        return getInt(privateCreator, tag, null, 0, defVal);
    }

    /**
     * Gets the integer value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param defVal         default value
     * @return result
     */
    public int getInt(String privateCreator, int tag, VR vr, int defVal) {
        return getInt(privateCreator, tag, vr, 0, defVal);
    }

    /**
     * Gets the integer value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param valueIndex     value index
     * @param defVal         default value
     * @return result
     */
    public int getInt(String privateCreator, int tag, int valueIndex, int defVal) {
        return getInt(privateCreator, tag, null, valueIndex, defVal);
    }

    /**
     * Gets the integer value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param valueIndex     value index
     * @param defVal         default value
     * @return result
     */
    public int getInt(String privateCreator, int tag, VR vr, int valueIndex, int defVal) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return defVal;
        Object value = values[index];
        if (value == Value.NULL)
            return defVal;
        vr = updateVR(index, vr);
        try {
            value = loadAndStoreBulkData(index);
            if (vr == VR.IS)
                value = decodeISValue(index);
            return vr.toInt(value, bigEndian, valueIndex, defVal);
        } catch (UnsupportedOperationException e) {
            Logger.info(false, "Image", "Attempt to access {} {} as int", Tag.toString(tag), vr);
            return defVal;
        } catch (IllegalArgumentException e) {
            Logger.info(false, "Image", "Invalid value of {} {}", Tag.toString(tag), vr);
            return defVal;
        }
    }

    /**
     * Gets the integer array
     *
     * @param tag tag
     * @return integer array
     */
    public int[] getInts(int tag) {
        return getInts(null, tag, null);
    }

    /**
     * Gets the integer array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return integer array
     */
    public int[] getInts(String privateCreator, int tag) {
        return getInts(privateCreator, tag, null);
    }

    /**
     * Gets the integer array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @return integer array
     */
    public int[] getInts(String privateCreator, int tag, VR vr) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;
        Object value = values[index];
        if (value == Value.NULL)
            return new int[] {};
        vr = updateVR(index, vr);
        try {
            value = loadAndStoreBulkData(index);
            if (vr == VR.IS)
                value = decodeISValue(index);
            return vr.toInts(value, bigEndian);
        } catch (UnsupportedOperationException e) {
            Logger.info(false, "Image", "Attempt to access {} {} as int", Tag.toString(tag), vr);
            return null;
        } catch (IllegalArgumentException e) {
            Logger.info(false, "Image", "Invalid value of {} {}", Tag.toString(tag), vr);
            return null;
        }
    }

    /**
     * Gets the long value
     *
     * @param tag    tag
     * @param defVal default value
     * @return result
     */
    public long getLong(int tag, long defVal) {
        return getLong(null, tag, null, 0, defVal);
    }

    /**
     * Gets the long value
     *
     * @param tag        tag
     * @param valueIndex value index
     * @param defVal     default value
     * @return result
     */
    public long getLong(int tag, int valueIndex, long defVal) {
        return getLong(null, tag, null, valueIndex, defVal);
    }

    /**
     * Gets the long value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param defVal         default value
     * @return result
     */
    public long getLong(String privateCreator, int tag, long defVal) {
        return getLong(privateCreator, tag, null, 0, defVal);
    }

    /**
     * Gets the long value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param defVal         default value
     * @return result
     */
    public long getLong(String privateCreator, int tag, VR vr, long defVal) {
        return getLong(privateCreator, tag, vr, 0, defVal);
    }

    /**
     * Gets the long value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param valueIndex     value index
     * @param defVal         default value
     * @return result
     */
    public long getLong(String privateCreator, int tag, int valueIndex, long defVal) {
        return getLong(privateCreator, tag, null, valueIndex, defVal);
    }

    /**
     * Gets the long value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param valueIndex     value index
     * @param defVal         default value
     * @return result
     */
    public long getLong(String privateCreator, int tag, VR vr, int valueIndex, long defVal) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return defVal;
        Object value = values[index];
        if (value == Value.NULL)
            return defVal;
        vr = updateVR(index, vr);
        try {
            value = loadAndStoreBulkData(index);
            if (vr == VR.IS)
                value = decodeISValue(index);
            return vr.toLong(value, bigEndian, valueIndex, defVal);
        } catch (UnsupportedOperationException e) {
            Logger.info(false, "Image", "Attempt to access {} {} as int", Tag.toString(tag), vr);
            return defVal;
        } catch (IllegalArgumentException e) {
            Logger.info(false, "Image", "Invalid value of {} {}", Tag.toString(tag), vr);
            return defVal;
        }
    }

    /**
     * Gets the long array
     *
     * @param tag tag
     * @return result
     */
    public long[] getLongs(int tag) {
        return getLongs(null, tag, null);
    }

    /**
     * Gets the long array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return result
     */
    public long[] getLongs(String privateCreator, int tag) {
        return getLongs(privateCreator, tag, null);
    }

    /**
     * Gets the long array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @return result
     */
    public long[] getLongs(String privateCreator, int tag, VR vr) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;
        Object value = values[index];
        if (value == Value.NULL)
            return new long[] {};
        vr = updateVR(index, vr);
        try {
            value = loadAndStoreBulkData(index);
            if (vr == VR.IS)
                value = decodeISValue(index);
            return vr.toLongs(value, bigEndian);
        } catch (UnsupportedOperationException e) {
            Logger.info(false, "Image", "Attempt to access {} {} as long", Tag.toString(tag), vr);
            return null;
        } catch (IllegalArgumentException e) {
            Logger.info(false, "Image", "Invalid value of {} {}", Tag.toString(tag), vr);
            return null;
        }
    }

    /**
     * Gets the float value
     *
     * @param tag    tag
     * @param defVal default value
     * @return result
     */
    public float getFloat(int tag, float defVal) {
        return getFloat(null, tag, null, 0, defVal);
    }

    /**
     * Gets the float value
     *
     * @param tag        tag
     * @param valueIndex value index
     * @param defVal     default value
     * @return result
     */
    public float getFloat(int tag, int valueIndex, float defVal) {
        return getFloat(null, tag, null, valueIndex, defVal);
    }

    /**
     * Gets the float value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param defVal         default value
     * @return result
     */
    public float getFloat(String privateCreator, int tag, float defVal) {
        return getFloat(privateCreator, tag, null, 0, defVal);
    }

    /**
     * Gets the float value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param defVal         default value
     * @return result
     */
    public float getFloat(String privateCreator, int tag, VR vr, float defVal) {
        return getFloat(privateCreator, tag, vr, 0, defVal);
    }

    /**
     * Gets the float value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param valueIndex     value index
     * @param defVal         default value
     * @return result
     */
    public float getFloat(String privateCreator, int tag, int valueIndex, float defVal) {
        return getFloat(privateCreator, tag, null, valueIndex, defVal);
    }

    /**
     * Gets the float value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param valueIndex     value index
     * @param defVal         default value
     * @return result
     */
    public float getFloat(String privateCreator, int tag, VR vr, int valueIndex, float defVal) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return defVal;
        Object value = values[index];
        if (value == Value.NULL)
            return defVal;
        vr = updateVR(index, vr);
        try {
            value = loadAndStoreBulkData(index);
            if (vr == VR.DS)
                value = decodeDSValue(index);
            return vr.toFloat(value, bigEndian, valueIndex, defVal);
        } catch (UnsupportedOperationException e) {
            Logger.info(false, "Image", "Attempt to access {} {} as float", Tag.toString(tag), vr);
            return defVal;
        } catch (IllegalArgumentException e) {
            Logger.info(false, "Image", "Invalid value of {} {}", Tag.toString(tag), vr);
            return defVal;
        }
    }

    /**
     * Gets the float array
     *
     * @param tag tag
     * @return float array
     */
    public float[] getFloats(int tag) {
        return getFloats(null, tag, null);
    }

    /**
     * Gets the float array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return float array
     */
    public float[] getFloats(String privateCreator, int tag) {
        return getFloats(privateCreator, tag, null);
    }

    /**
     * Gets the float array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @return float array
     */
    public float[] getFloats(String privateCreator, int tag, VR vr) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;
        Object value = values[index];
        if (value == Value.NULL)
            return new float[] {};
        vr = updateVR(index, vr);
        try {
            value = loadAndStoreBulkData(index);
            if (vr == VR.DS)
                value = decodeDSValue(index);
            return vr.toFloats(value, bigEndian);
        } catch (UnsupportedOperationException e) {
            Logger.info(false, "Image", "Attempt to access {} {} as float", Tag.toString(tag), vr);
            return null;
        } catch (IllegalArgumentException e) {
            Logger.info(false, "Image", "Invalid value of {} {}", Tag.toString(tag), vr);
            return null;
        }
    }

    /**
     * Gets the double value
     *
     * @param tag    tag
     * @param defVal default value
     * @return result
     */
    public double getDouble(int tag, double defVal) {
        return getDouble(null, tag, null, 0, defVal);
    }

    /**
     * Gets the double value
     *
     * @param tag        tag
     * @param valueIndex value index
     * @param defVal     default value
     * @return result
     */
    public double getDouble(int tag, int valueIndex, double defVal) {
        return getDouble(null, tag, null, valueIndex, defVal);
    }

    /**
     * Gets the double value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param defVal         default value
     * @return result
     */
    public double getDouble(String privateCreator, int tag, double defVal) {
        return getDouble(privateCreator, tag, null, 0, defVal);
    }

    /**
     * Gets the double value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param defVal         default value
     * @return result
     */
    public double getDouble(String privateCreator, int tag, VR vr, double defVal) {
        return getDouble(privateCreator, tag, vr, 0, defVal);
    }

    /**
     * Gets the double value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param valueIndex     value index
     * @param defVal         default value
     * @return result
     */
    public double getDouble(String privateCreator, int tag, int valueIndex, double defVal) {
        return getDouble(privateCreator, tag, null, valueIndex, defVal);
    }

    /**
     * Gets the double value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param valueIndex     value index
     * @param defVal         default value
     * @return result
     */
    public double getDouble(String privateCreator, int tag, VR vr, int valueIndex, double defVal) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return defVal;
        Object value = values[index];
        if (value == Value.NULL)
            return defVal;
        vr = updateVR(index, vr);
        try {
            value = loadAndStoreBulkData(index);
            if (vr == VR.DS)
                value = decodeDSValue(index);
            return vr.toDouble(value, bigEndian, valueIndex, defVal);
        } catch (UnsupportedOperationException e) {
            Logger.info(false, "Image", "Attempt to access {} {} as double", Tag.toString(tag), vr);
            return defVal;
        } catch (IllegalArgumentException e) {
            Logger.info(false, "Image", "Invalid value of {} {}", Tag.toString(tag), vr);
            return defVal;
        }
    }

    /**
     * Gets the double array
     *
     * @param tag tag
     * @return result
     */
    public double[] getDoubles(int tag) {
        return getDoubles(null, tag, null);
    }

    /**
     * Gets the double array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return result
     */
    public double[] getDoubles(String privateCreator, int tag) {
        return getDoubles(privateCreator, tag, null);
    }

    /**
     * Gets the double array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @return result
     */
    public double[] getDoubles(String privateCreator, int tag, VR vr) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;
        Object value = values[index];
        if (value == Value.NULL)
            return new double[] {};
        vr = updateVR(index, vr);
        try {
            value = loadAndStoreBulkData(index);
            if (vr == VR.DS)
                value = decodeDSValue(index);
            return vr.toDoubles(value, bigEndian);
        } catch (UnsupportedOperationException e) {
            Logger.info(false, "Image", "Attempt to access {} {} as double", Tag.toString(tag), vr);
            return null;
        } catch (IllegalArgumentException e) {
            Logger.info(false, "Image", "Invalid value of {} {}", Tag.toString(tag), vr);
            return null;
        }
    }

    /**
     * Gets the most precise temporal type. For the given tag, returns the most precise temporal type. A
     * {@link ZonedDateTime} instance is returned in the following cases:
     * <ul>
     * <li>A tag with {@link VR#DT} whose value defines a time zone offset.</li>
     * <li>A tag with {@link VR#DT} whose value does not define a time zone offset, while
     * {@link Tag#TimezoneOffsetFromUTC} is defined here or in a parent attribute, or a default time zone is configured
     * here or in a parent attribute (see {@link #setDefaultTimeZone(TimeZone)}).</li>
     * </ul>
     * If no time zone information is available, a {@link LocalDateTime} instance is returned for {@link VR#DT} tags.
     * For {@link VR#DA} or {@link VR#TM} tags, a {@link LocalDate} or {@link LocalTime} instance is returned. If the
     * tag value is not set or empty, this method returns <code>null</code> or the supplied default value
     * <code>defVal</code>.
     *
     * @param tag tag number
     * @return temporal value or {@code null}
     */
    public Temporal getTemporal(int tag) {
        return getTemporal(null, tag, null, 0, null, new DatePrecision());
    }

    /**
     * Gets the temporal value
     * <p>
     * See {@link #getTemporal(int)}.
     * </p>
     *
     * @param privateCreator private creator
     * @param tag            tag number
     * @param vr             VR
     * @param valueIndex     value index
     * @param defVal         default value
     * @param precision      used as an output value containing date/time precision and whether the tag value itself
     *                       contains time zone information, only for {@link VR#DT} tags.
     * @return result
     */
    public Temporal getTemporal(
            String privateCreator,
            int tag,
            VR vr,
            int valueIndex,
            Temporal defVal,
            DatePrecision precision) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return defVal;
        Object value = values[index];
        if (value == Value.NULL)
            return defVal;
        vr = updateVR(index, vr);
        if (!vr.isTemporalType()) {
            Logger.info(false, "Image", "Attempt to access {} {} as date/time", Tag.toString(tag), vr);
            return defVal;
        }
        value = decodeStringValue(index);
        if (value == Value.NULL) {
            return defVal;
        }
        Temporal t;
        try {
            t = vr.toTemporal(value, valueIndex, precision);
        } catch (IllegalArgumentException e) {
            Logger.info(false, "Image", "Invalid value of {} {}", Tag.toString(tag), vr);
            return defVal;
        }
        if (t == null) {
            return defVal;
        } else if (t instanceof OffsetDateTime) {
            return ((OffsetDateTime) t).toZonedDateTime();
        } else if (t instanceof LocalDateTime) {
            ZoneId zoneId = getZoneId();
            if (zoneId != null) {
                return ((LocalDateTime) t).atZone(zoneId);
            }
        }
        return t;
    }

    /**
     * Gets the date
     *
     * @param tag tag
     * @return date
     */
    public Date getDate(int tag) {
        return getDate(null, tag, null, 0, null, new DatePrecision());
    }

    /**
     * Gets the date
     *
     * @param tag       tag
     * @param precision precision
     * @return date
     */
    public Date getDate(int tag, DatePrecision precision) {
        return getDate(null, tag, null, 0, null, precision);
    }

    /**
     * Gets the date
     *
     * @param tag    tag
     * @param defVal default value
     * @return date
     */
    public Date getDate(int tag, Date defVal) {
        return getDate(null, tag, null, 0, defVal, new DatePrecision());
    }

    /**
     * Gets the date
     *
     * @param tag       tag
     * @param defVal    default value
     * @param precision precision
     * @return date
     */
    public Date getDate(int tag, Date defVal, DatePrecision precision) {
        return getDate(null, tag, null, 0, defVal, precision);
    }

    /**
     * Gets the date
     *
     * @param tag        tag
     * @param valueIndex value index
     * @return date
     */
    public Date getDate(int tag, int valueIndex) {
        return getDate(null, tag, null, valueIndex, null, new DatePrecision());
    }

    /**
     * Gets the date
     *
     * @param tag        tag
     * @param valueIndex value index
     * @param precision  precision
     * @return date
     */
    public Date getDate(int tag, int valueIndex, DatePrecision precision) {
        return getDate(null, tag, null, valueIndex, null, precision);
    }

    /**
     * Gets the date
     *
     * @param tag        tag
     * @param valueIndex value index
     * @param defVal     default value
     * @return date
     */
    public Date getDate(int tag, int valueIndex, Date defVal) {
        return getDate(null, tag, null, valueIndex, defVal, new DatePrecision());
    }

    /**
     * Gets the date
     *
     * @param tag        tag
     * @param valueIndex value index
     * @param defVal     default value
     * @param precision  precision
     * @return date
     */
    public Date getDate(int tag, int valueIndex, Date defVal, DatePrecision precision) {
        return getDate(null, tag, null, valueIndex, defVal, precision);
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return date
     */
    public Date getDate(String privateCreator, int tag) {
        return getDate(privateCreator, tag, null, 0, null, new DatePrecision());
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param precision      precision
     * @return date
     */
    public Date getDate(String privateCreator, int tag, DatePrecision precision) {
        return getDate(privateCreator, tag, null, 0, null, precision);
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param defVal         default value
     * @param precision      precision
     * @return date
     */
    public Date getDate(String privateCreator, int tag, Date defVal, DatePrecision precision) {
        return getDate(privateCreator, tag, null, 0, defVal, precision);
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @return date
     */
    public Date getDate(String privateCreator, int tag, VR vr) {
        return getDate(privateCreator, tag, vr, 0, null, new DatePrecision());
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param precision      precision
     * @return date
     */
    public Date getDate(String privateCreator, int tag, VR vr, DatePrecision precision) {
        return getDate(privateCreator, tag, vr, 0, null, precision);
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param defVal         default value
     * @return date
     */
    public Date getDate(String privateCreator, int tag, VR vr, Date defVal) {
        return getDate(privateCreator, tag, vr, 0, defVal, new DatePrecision());
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param defVal         default value
     * @param precision      precision
     * @return date
     */
    public Date getDate(String privateCreator, int tag, VR vr, Date defVal, DatePrecision precision) {
        return getDate(privateCreator, tag, vr, 0, defVal, precision);
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param valueIndex     value index
     * @return date
     */
    public Date getDate(String privateCreator, int tag, int valueIndex) {
        return getDate(privateCreator, tag, null, valueIndex, null, new DatePrecision());
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param valueIndex     value index
     * @param precision      precision
     * @return date
     */
    public Date getDate(String privateCreator, int tag, int valueIndex, DatePrecision precision) {
        return getDate(privateCreator, tag, null, valueIndex, null, precision);
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param valueIndex     value index
     * @param defVal         default value
     * @return date
     */
    public Date getDate(String privateCreator, int tag, int valueIndex, Date defVal) {
        return getDate(privateCreator, tag, null, valueIndex, defVal, new DatePrecision());
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param valueIndex     value index
     * @param defVal         default value
     * @param precision      precision
     * @return date
     */
    public Date getDate(String privateCreator, int tag, int valueIndex, Date defVal, DatePrecision precision) {
        return getDate(privateCreator, tag, null, valueIndex, defVal, precision);
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param valueIndex     value index
     * @return date
     */
    public Date getDate(String privateCreator, int tag, VR vr, int valueIndex) {
        return getDate(privateCreator, tag, vr, valueIndex, null, new DatePrecision());
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param valueIndex     value index
     * @param precision      precision
     * @return date
     */
    public Date getDate(String privateCreator, int tag, VR vr, int valueIndex, DatePrecision precision) {
        return getDate(privateCreator, tag, vr, valueIndex, null, precision);
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param valueIndex     value index
     * @param defVal         default value
     * @return date
     */
    public Date getDate(String privateCreator, int tag, VR vr, int valueIndex, Date defVal) {
        return getDate(privateCreator, tag, vr, valueIndex, defVal, new DatePrecision());
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param valueIndex     value index
     * @param defVal         default value
     * @param precision      precision
     * @return date
     */
    public Date getDate(String privateCreator, int tag, VR vr, int valueIndex, Date defVal, DatePrecision precision) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return defVal;
        Object value = values[index];
        if (value == Value.NULL)
            return defVal;
        vr = updateVR(index, vr);
        if (!vr.isTemporalType()) {
            Logger.info(false, "Image", "Attempt to access {} {} as date", Tag.toString(tag), vr);
            return defVal;
        }
        value = decodeStringValue(index);
        if (value == Value.NULL)
            return defVal;
        try {
            return vr.toDate(value, getTimeZone(), valueIndex, false, defVal, precision);
        } catch (IllegalArgumentException e) {
            Logger.info(false, "Image", "Invalid value of {} {}", Tag.toString(tag), vr);
            return defVal;
        }
    }

    /**
     * Gets the date
     *
     * @param tag tag
     * @return date
     */
    public Date getDate(long tag) {
        return getDate(null, tag, null, new DatePrecision());
    }

    /**
     * Gets the date
     *
     * @param tag       tag
     * @param precision precision
     * @return date
     */
    public Date getDate(long tag, DatePrecision precision) {
        return getDate(null, tag, null, precision);
    }

    /**
     * Gets the date
     *
     * @param tag    tag
     * @param defVal default value
     * @return date
     */
    public Date getDate(long tag, Date defVal) {
        return getDate(null, tag, defVal, new DatePrecision());
    }

    /**
     * Gets the date
     *
     * @param tag       tag
     * @param defVal    default value
     * @param precision precision
     * @return date
     */
    public Date getDate(long tag, Date defVal, DatePrecision precision) {
        return getDate(null, tag, defVal, precision);
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return date
     */
    public Date getDate(String privateCreator, long tag) {
        return getDate(privateCreator, tag, null, new DatePrecision());
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param precision      precision
     * @return date
     */
    public Date getDate(String privateCreator, long tag, DatePrecision precision) {
        return getDate(privateCreator, tag, null, precision);
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param defVal         default value
     * @return date
     */
    public Date getDate(String privateCreator, long tag, Date defVal) {
        return getDate(privateCreator, tag, defVal, new DatePrecision());
    }

    /**
     * Gets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param defVal         default value
     * @param precision      precision
     * @return date
     */
    public Date getDate(String privateCreator, long tag, Date defVal, DatePrecision precision) {
        int daTag = (int) (tag >>> 32);
        int tmTag = (int) tag;
        String tm = getString(privateCreator, tmTag, VR.TM, null);
        if (tm == null)
            return getDate(daTag, defVal, precision);
        String da = getString(privateCreator, daTag, VR.DA, null);
        if (da == null)
            return defVal;
        try {
            return VR.DT.toDate(da + tm, getTimeZone(), 0, false, null, precision);
        } catch (IllegalArgumentException e) {
            Logger.info(false, "Image", "Invalid value of {} DA or {} TM", Tag.toString(daTag), Tag.toString(tmTag));
            return defVal;
        }
    }

    /**
     * Gets the temporal value
     *
     * @param tag tag
     * @return temporal value
     */
    public Temporal getTemporal(long tag) {
        return getTemporal(null, tag, null, new DatePrecision());
    }

    /**
     * Gets the temporal value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param defVal         default value
     * @param precision      precision
     * @return temporal value
     */
    public Temporal getTemporal(String privateCreator, long tag, Temporal defVal, DatePrecision precision) {
        int daTag = (int) (tag >>> 32);
        int tmTag = (int) tag;
        LocalDate date = (LocalDate) getTemporal(privateCreator, daTag, VR.DA, 0, null, precision);
        LocalTime time = (LocalTime) getTemporal(privateCreator, tmTag, VR.TM, 0, null, precision);
        if (date != null && time != null) {
            LocalDateTime localDateTime = LocalDateTime.of(date, time);
            ZoneId zoneId = getZoneId();
            if (zoneId != null) {
                return localDateTime.atZone(zoneId);
            } else {
                return localDateTime;
            }
        } else if (date != null) {
            return date;
        } else if (time != null) {
            return time;
        }
        return defVal;
    }

    /**
     * Gets the date array
     *
     * @param tag tag
     * @return date array
     */
    public Date[] getDates(int tag) {
        return getDates(null, tag, null, new DatePrecision());
    }

    /**
     * Gets the date array
     *
     * @param tag        tag
     * @param precisions precision array
     * @return date array
     */
    public Date[] getDates(int tag, DatePrecision precisions) {
        return getDates(null, tag, null, precisions);
    }

    /**
     * Gets the date array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return date array
     */
    public Date[] getDates(String privateCreator, int tag) {
        return getDates(privateCreator, tag, null, new DatePrecision());
    }

    /**
     * Gets the date array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param precision      precision
     * @return date array
     */
    public Date[] getDates(String privateCreator, int tag, DatePrecision precision) {
        return getDates(privateCreator, tag, null, precision);
    }

    /**
     * Gets the date array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @return date array
     */
    public Date[] getDates(String privateCreator, int tag, VR vr) {
        return getDates(privateCreator, tag, vr, new DatePrecision());
    }

    /**
     * Gets the date array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param precisions     precision array
     * @return date array
     */
    public Date[] getDates(String privateCreator, int tag, VR vr, DatePrecision precisions) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;
        Object value = values[index];
        if (value == Value.NULL)
            return Normal.EMPTY_DATE_OBJECT_ARRAY;
        vr = updateVR(index, vr);
        if (!vr.isTemporalType()) {
            Logger.info(false, "Image", "Attempt to access {} {} as date", Tag.toString(tag), vr);
            return Normal.EMPTY_DATE_OBJECT_ARRAY;
        }
        value = decodeStringValue(index);
        if (value == Value.NULL)
            return Normal.EMPTY_DATE_OBJECT_ARRAY;
        try {
            return vr.toDates(value, getTimeZone(), false, precisions);
        } catch (IllegalArgumentException e) {
            Logger.info(false, "Image", "Invalid value of {} {}", Tag.toString(tag), vr);
            return Normal.EMPTY_DATE_OBJECT_ARRAY;
        }
    }

    /**
     * Gets the date array
     *
     * @param tag tag
     * @return date array
     */
    public Date[] getDates(long tag) {
        return getDates(null, tag, new DatePrecision());
    }

    /**
     * Gets the date array
     *
     * @param tag        tag
     * @param precisions precision array
     * @return date array
     */
    public Date[] getDates(long tag, DatePrecision precisions) {
        return getDates(null, tag, precisions);
    }

    /**
     * Gets the date array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return date array
     */
    public Date[] getDates(String privateCreator, long tag) {
        return getDates(privateCreator, tag, new DatePrecision());
    }

    /**
     * Gets the date array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param precision      precision
     * @return date array
     */
    public Date[] getDates(String privateCreator, long tag, DatePrecision precision) {
        int daTag = (int) (tag >>> 32);
        int tmTag = (int) tag;
        String[] tm = getStrings(privateCreator, tmTag);
        if (tm == null || tm.length == 0)
            return getDates(daTag, precision);
        String[] da = getStrings(privateCreator, daTag);
        if (da == null || da.length == 0)
            return Normal.EMPTY_DATE_OBJECT_ARRAY;
        Date[] dates = new Date[da.length];
        precision.precisions = new DatePrecision[da.length];
        int i = 0;
        try {
            TimeZone tz = getTimeZone();
            while (i < tm.length)
                dates[i++] = VR.DT
                        .toDate(da[i] + tm[i], tz, 0, false, null, precision.precisions[i] = new DatePrecision());
            while (i < da.length)
                dates[i++] = VR.DA.toDate(da[i], tz, 0, false, null, precision.precisions[i] = new DatePrecision());
        } catch (IllegalArgumentException e) {
            Logger.info(false, "Image", "Invalid value of {} DA or {} TM", Tag.toString(daTag), Tag.toString(tmTag));
            dates = Arrays.copyOf(dates, i);
        }
        return dates;
    }

    /**
     * Gets the date range
     *
     * @param tag tag
     * @return date range
     */
    public DateRange getDateRange(int tag) {
        return getDateRange(null, tag, null, null);
    }

    /**
     * Gets the date range
     *
     * @param tag    tag
     * @param defVal default value
     * @return date range
     */
    public DateRange getDateRange(int tag, DateRange defVal) {
        return getDateRange(null, tag, null, defVal);
    }

    /**
     * Gets the date range
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return date range
     */
    public DateRange getDateRange(String privateCreator, int tag) {
        return getDateRange(privateCreator, tag, null, null);
    }

    /**
     * Gets the date range
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param defVal         default value
     * @return date range
     */
    public DateRange getDateRange(String privateCreator, int tag, DateRange defVal) {
        return getDateRange(privateCreator, tag, null, defVal);
    }

    /**
     * Gets the date range
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @return date range
     */
    public DateRange getDateRange(String privateCreator, int tag, VR vr) {
        return getDateRange(privateCreator, tag, vr, null);
    }

    /**
     * Gets the date range
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param defVal         default value
     * @return date range
     */
    public DateRange getDateRange(String privateCreator, int tag, VR vr, DateRange defVal) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return defVal;
        Object value = values[index];
        if (value == Value.NULL)
            return defVal;
        vr = updateVR(index, vr);
        if (!vr.isTemporalType()) {
            Logger.info(false, "Image", "Attempt to access {} {} as date", Tag.toString(tag), vr);
            return defVal;
        }
        value = decodeStringValue(index);
        if (value == Value.NULL)
            return defVal;
        try {
            return toDateRange((value instanceof String) ? (String) value : ((String[]) value)[0], vr);
        } catch (IllegalArgumentException e) {
            Logger.info(false, "Image", "Invalid value of {} {}", Tag.toString(tag), vr);
            return defVal;
        }
    }

    /**
     * Converts to a date range
     *
     * @param s  parameter
     * @param vr VR
     * @return date range
     */
    private DateRange toDateRange(String s, VR vr) {
        String[] range = splitRange(s);
        TimeZone tz = getTimeZone();
        DatePrecision precision = new DatePrecision();
        Date start = range[0] == null ? null : vr.toDate(range[0], tz, 0, false, null, precision);
        Date end = range[1] == null ? null : vr.toDate(range[1], tz, 0, true, null, precision);
        return new DateRange(start, end);
    }

    /**
     * Splits the range
     *
     * @param s parameter
     * @return range array
     */
    private static String[] splitRange(String s) {
        String[] range = new String[2];
        int delim = s.indexOf('-');
        if (delim == -1)
            range[0] = range[1] = s;
        else {
            if (delim > 0)
                range[0] = s.substring(0, delim);
            if (delim < s.length() - 1)
                range[1] = s.substring(delim + 1);
        }
        return range;
    }

    /**
     * Gets the date range
     *
     * @param tag tag
     * @return date range
     */
    public DateRange getDateRange(long tag) {
        return getDateRange(null, tag, null);
    }

    /**
     * Gets the date range
     *
     * @param tag    tag
     * @param defVal default value
     * @return date range
     */
    public DateRange getDateRange(long tag, DateRange defVal) {
        return getDateRange(null, tag, defVal);
    }

    /**
     * Gets the date range
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return date range
     */
    public DateRange getDateRange(String privateCreator, long tag) {
        return getDateRange(privateCreator, tag, null);
    }

    /**
     * Gets the date range
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param defVal         default value
     * @return date range
     */
    public DateRange getDateRange(String privateCreator, long tag, DateRange defVal) {
        int daTag = (int) (tag >>> 32);
        int tmTag = (int) tag;
        String tm = getString(privateCreator, tmTag, VR.TM, null);
        if (tm == null)
            return getDateRange(daTag, defVal);
        String da = getString(privateCreator, daTag, VR.DA, null);
        if (da == null)
            return defVal;
        try {
            return toDateRange(da, tm);
        } catch (IllegalArgumentException e) {
            Logger.info(false, "Image", "Invalid value of {} TM", Tag.toString((int) tag));
            return defVal;
        }
    }

    /**
     * Converts to a date range
     *
     * @param da date string
     * @param tm time string
     * @return date range
     */
    private DateRange toDateRange(String da, String tm) {
        String[] darange = splitRange(da);
        String[] tmrange = splitRange(tm);
        DatePrecision precision = new DatePrecision();
        return new DateRange(
                darange[0] == null ? null
                        : VR.DT.toDate(
                                tmrange[0] == null ? darange[0] : darange[0] + tmrange[0],
                                tz,
                                0,
                                false,
                                null,
                                precision),
                darange[1] == null ? null
                        : VR.DT.toDate(
                                tmrange[1] == null ? darange[1] : darange[1] + tmrange[1],
                                tz,
                                0,
                                true,
                                null,
                                precision));
    }

    /**
     * Sets Specific Character Set (0008,0005) to the specified codes and re-encodes contained LO, LT, PN, SH, ST, and
     * UT attributes accordingly.
     *
     * @param codes new values of Specific Character Set (0008,0005)
     */
    public void setSpecificCharacterSet(String... codes) {
        ensureModifiable();
        decodeStringValuesUsingSpecificCharacterSet();
        setString(Tag.SpecificCharacterSet, VR.CS, codes);
    }

    /**
     * Gets the effective specific character set.
     *
     * @return specific character set
     */
    public SpecificCharacterSet getSpecificCharacterSet() {
        if (cs != null)
            return cs;
        if (containsSpecificCharacterSet)
            cs = SpecificCharacterSet.valueOf(getStrings(null, Tag.SpecificCharacterSet, VR.CS));
        else if (parent != null)
            return parent.getSpecificCharacterSet();
        else
            cs = SpecificCharacterSet.getDefaultCharacterSet();
        return cs;
    }

    /**
     * Whether UTC time zone offset is present
     *
     * @return Whether UTC time zone offset is present
     */
    public boolean containsTimezoneOffsetFromUTC() {
        return containsTimezoneOffsetFromUTC;
    }

    /**
     * Sets the default time zone.
     *
     * @param tz time zone
     */
    public void setDefaultTimeZone(TimeZone tz) {
        ensureModifiable();
        defaultTimeZone = tz;
    }

    /**
     * Gets the default zone ID
     *
     * @return default zone ID
     */
    public ZoneId getDefaultZoneId() {
        if (defaultTimeZone != null)
            return defaultTimeZone.toZoneId();
        if (parent != null)
            return parent.getDefaultZoneId();
        return null;
    }

    /**
     * Gets the default time zone.
     *
     * @return Default time zone
     */
    public TimeZone getDefaultTimeZone() {
        if (defaultTimeZone != null)
            return defaultTimeZone;
        if (parent != null)
            return parent.getDefaultTimeZone();
        return TimeZone.getDefault();
    }

    /**
     * Gets the time zone
     *
     * @return time zone
     */
    public TimeZone getTimeZone() {
        if (tz != null)
            return tz;
        if (containsTimezoneOffsetFromUTC) {
            String s = getString(Tag.TimezoneOffsetFromUTC);
            if (s != null)
                try {
                    tz = Format.timeZone(s);
                } catch (IllegalArgumentException e) {
                    Logger.warn(
                            false,
                            "Image",
                            e,
                            "DICOM timezone parse failed: tag={}, valueChars={}, exception={}",
                            Tag.toString(Tag.TimezoneOffsetFromUTC),
                            s.length(),
                            e.getClass().getSimpleName());
                }
        } else if (parent != null)
            return parent.getTimeZone();
        else
            tz = getDefaultTimeZone();
        return tz;
    }

    /**
     * Gets the zone ID
     *
     * @return zone ID
     */
    public ZoneId getZoneId() {
        if (tz != null)
            return tz.toZoneId();
        if (containsTimezoneOffsetFromUTC) {
            String s = getString(Tag.TimezoneOffsetFromUTC);
            if (s == null) {
                return null;
            }
            try {
                tz = Format.timeZone(s);
            } catch (IllegalArgumentException e) {
                Logger.warn(
                        false,
                        "Image",
                        e,
                        "DICOM zone parse failed: tag={}, valueChars={}, exception={}",
                        Tag.toString(Tag.TimezoneOffsetFromUTC),
                        s.length(),
                        e.getClass().getSimpleName());
                return null;
            }
            return tz.toZoneId();
        } else if (parent != null) {
            return parent.getZoneId();
        } else {
            return getDefaultZoneId();
        }
    }

    /**
     * Sets UTC offset (0008,0201) to the specified value and adjusts contained DA, DT, and TM attributes accordingly.
     *
     * @param utcOffset UTC offset in (+|-)HHMM format
     */
    public void setTimezoneOffsetFromUTC(String utcOffset) {
        ensureModifiable();
        TimeZone tz = Format.timeZone(utcOffset);
        updateTimezone(getTimeZone(), tz);
        setString(Tag.TimezoneOffsetFromUTC, VR.SH, utcOffset);
        this.tz = tz;
    }

    /**
     * Sets the default time zone to the specified value and adjusts contained DA, DT, and TM attributes accordingly. If
     * the time zone does not use daylight saving time, Timezone Offset From UTC (0008,0201) is set accordingly. If it
     * uses daylight saving time, any previous Timezone Offset From UTC (0008,0201) attribute is removed.
     *
     * @param tz time zone
     * @see #setDefaultTimeZone(TimeZone)
     * @see #setTimezoneOffsetFromUTC(String)
     */
    public void setTimezone(TimeZone tz) {
        ensureModifiable();
        updateTimezone(getTimeZone(), tz);
        if (tz.useDaylightTime()) {
            remove(Tag.TimezoneOffsetFromUTC);
            setDefaultTimeZone(tz);
        } else {
            setString(Tag.TimezoneOffsetFromUTC, VR.SH, Format.formatTimezoneOffsetFromUTC(tz));
        }
        this.tz = tz;
    }

    /**
     * Updates the time zone
     *
     * @param from source time zone
     * @param to   target time zone
     */
    private void updateTimezone(TimeZone from, TimeZone to) {
        if (from.hasSameRules(to))
            return;
        for (int i = 0; i < size; i++) {
            Object val = values[i];
            if (val instanceof Sequence) {
                Sequence new_name = (Sequence) val;
                for (Attributes item : new_name) {
                    item.updateTimezone(item.getTimeZone(), to);
                    item.remove(Tag.TimezoneOffsetFromUTC);
                }
            } else if (vrs[i] == VR.TM && tags[i] != Tag.PatientBirthTime
                    || vrs[i] == VR.DT && tags[i] != Tag.ContextGroupVersion && tags[i] != Tag.ContextGroupLocalVersion)
                updateTimezone(from, to, i);
        }
    }

    /**
     * Updates the time zone
     *
     * @param from    source time zone
     * @param to      target time zone
     * @param tmIndex time index
     */
    private void updateTimezone(TimeZone from, TimeZone to, int tmIndex) {
        Object tm = decodeStringValue(tmIndex);
        if (tm == Value.NULL)
            return;
        int tmTag = tags[tmIndex];
        if (vrs[tmIndex] == VR.DT) {
            if (tm instanceof String[]) {
                String[] tms = (String[]) tm;
                for (int i = 0; i < tms.length; i++) {
                    tms[i] = updateTimeZoneDT(from, to, tms[i]);
                }
            } else
                values[tmIndex] = updateTimeZoneDT(from, to, (String) tm);
        } else {
            int daTag = ElementDictionary.getElementDictionary(privateCreatorOf(tmTag)).daTagOf(tmTag);
            int daIndex = daTag != 0 ? indexOf(daTag) : -1;
            Object da = daIndex >= 0 ? decodeStringValue(daIndex) : Value.NULL;
            if (tm instanceof String[]) {
                String[] tms = (String[]) tm;
                if (da instanceof String[]) {
                    String[] das = (String[]) da;
                    for (int i = 0; i < tms.length; i++) {
                        if (i < das.length) {
                            String dt = updateTimeZoneDT(from, to, das[i] + tms[i]);
                            das[i] = dt.substring(0, 8);
                            tms[i] = dt.substring(8);
                        } else {
                            tms[i] = updateTimeZoneTM(from, to, tms[i]);
                        }
                    }
                } else {
                    if (da == Value.NULL) {
                        tms[0] = updateTimeZoneTM(from, to, tms[0]);
                    } else {
                        String dt = updateTimeZoneDT(from, to, da + tms[0]);
                        values[daIndex] = dt.substring(0, 8);
                        tms[0] = dt.substring(8);
                    }
                    for (int i = 1; i < tms.length; i++) {
                        tms[i] = updateTimeZoneTM(from, to, tms[i]);
                    }
                }
            } else {
                if (da instanceof String[]) {
                    String[] das = (String[]) da;
                    String dt = updateTimeZoneDT(from, to, das[0] + tm);
                    das[0] = dt.substring(0, 8);
                    values[tmIndex] = dt.substring(8);
                } else {
                    String[] tmRange = null;
                    if (isRange((String) tm)) {
                        tmRange = splitRange((String) tm);
                        if (tmRange[0] == null)
                            tmRange[0] = "000000.000";
                        if (tmRange[1] == null)
                            tmRange[1] = "235959.999";
                    }
                    if (da == Value.NULL) {
                        if (tmRange != null) {
                            tmRange[0] = updateTimeZoneTM(from, to, tmRange[0]);
                            tmRange[1] = updateTimeZoneTM(from, to, tmRange[1]);
                            values[tmIndex] = toDateRangeString(tmRange[0], tmRange[1]);
                        } else {
                            values[tmIndex] = updateTimeZoneTM(from, to, (String) tm);
                        }
                    } else {
                        if (tmRange != null) {
                            String[] daRange = splitRange((String) da);
                            if (daRange[0] == null) {
                                daRange[0] = "";
                                tmRange[0] = updateTimeZoneTM(from, to, tmRange[0]);
                            } else {
                                String dt = updateTimeZoneDT(from, to, daRange[0] + tmRange[0]);
                                daRange[0] = dt.substring(0, 8);
                                tmRange[0] = dt.substring(8);
                            }
                            if (daRange[1] == null) {
                                daRange[1] = "";
                                tmRange[1] = updateTimeZoneTM(from, to, tmRange[1]);
                            } else {
                                String dt = updateTimeZoneDT(from, to, daRange[1] + tmRange[1]);
                                daRange[1] = dt.substring(0, 8);
                                tmRange[1] = dt.substring(8);
                            }
                            values[daIndex] = toDateRangeString(daRange[0], daRange[1]);
                            values[tmIndex] = toDateRangeString(tmRange[0], tmRange[1]);
                        } else {
                            String dt = updateTimeZoneDT(from, to, da + (String) tm);
                            values[daIndex] = dt.substring(0, 8);
                            values[tmIndex] = dt.substring(8);
                        }
                    }
                }
            }
        }
    }

    /**
     * Whether the value is a range
     *
     * @param s parameter
     * @return Whether the value is a range
     */
    private static boolean isRange(String s) {
        return s.indexOf('-') >= 0;
    }

    /**
     * Updates the DT time zone
     *
     * @param from source time zone
     * @param to   target time zone
     * @param dt   date time values
     * @return result
     */
    private String updateTimeZoneDT(TimeZone from, TimeZone to, String dt) {
        int dtlen = dt.length();
        if (dtlen > 8) {
            char ch = dt.charAt(dtlen - 5);
            if (ch == '+' || ch == '-')
                return dt;
        }
        try {
            DatePrecision precision = new DatePrecision();
            Date date = Format.parseDT(from, dt, false, precision);
            dt = Format.formatDT(to, date, precision);
        } catch (IllegalArgumentException e) {
        }
        return dt;
    }

    /**
     * Updates the TM time zone
     *
     * @param from source time zone
     * @param to   target time zone
     * @param tm   parameter
     * @return result
     */
    private String updateTimeZoneTM(TimeZone from, TimeZone to, String tm) {
        try {
            DatePrecision precision = new DatePrecision();
            Date date = Format.parseTM(from, tm, false, precision);
            tm = Format.formatTM(to, date, precision);
        } catch (IllegalArgumentException e) {
        }
        return tm;
    }

    /**
     * Gets the private creator
     *
     * @param tag tag
     * @return private creator
     */
    public String getPrivateCreator(int tag) {
        return Tag.isPrivateTag(tag) ? getString(Tag.creatorTagOf(tag), null) : null;
    }

    /**
     * Removes the attribute
     *
     * @param tag tag
     * @return result
     */
    public Object remove(int tag) {
        return remove(null, tag);
    }

    /**
     * Removes the attribute
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return result
     */
    public Object remove(String privateCreator, int tag) {
        ensureModifiable();
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;
        Object value = values[index];
        if (value instanceof Sequence) {
            for (Attributes attrs : ((Sequence) value)) {
                attrs.setParent(null, null, 0);
            }
        }
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(tags, index + 1, tags, index, numMoved);
            System.arraycopy(vrs, index + 1, vrs, index, numMoved);
            System.arraycopy(values, index + 1, values, index, numMoved);
        }
        values[--size] = null;
        if (tag == Tag.SpecificCharacterSet) {
            containsSpecificCharacterSet = false;
            cs = null;
        } else if (tag == Tag.TimezoneOffsetFromUTC) {
            containsTimezoneOffsetFromUTC = false;
            tz = null;
        }
        return value;
    }

    /**
     * Sets a null value
     *
     * @param tag tag
     * @param vr  VR
     * @return previous value
     */
    public Object setNull(int tag, VR vr) {
        return setNull(null, tag, vr);
    }

    /**
     * Sets a null value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @return previous value
     */
    public Object setNull(String privateCreator, int tag, VR vr) {
        ensureModifiable();
        return set(privateCreator, tag, vr, Value.NULL);
    }

    /**
     * Sets the byte array
     *
     * @param tag tag
     * @param vr  VR
     * @param b   byte array
     * @return previous value
     */
    public Object setBytes(int tag, VR vr, byte[] b) {
        return setBytes(null, tag, vr, b);
    }

    /**
     * Sets the byte array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param b              byte array
     * @return previous value
     */
    public Object setBytes(String privateCreator, int tag, VR vr, byte[] b) {
        ensureModifiable();
        return set(privateCreator, tag, vr, vr.toValue(b));
    }

    /**
     * Sets the string
     *
     * @param tag tag
     * @param vr  VR
     * @param s   parameter
     * @return previous value
     */
    public Object setString(int tag, VR vr, String s) {
        return setString(null, tag, vr, s);
    }

    /**
     * Sets the string
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param s              parameter
     * @return previous value
     */
    public Object setString(String privateCreator, int tag, VR vr, String s) {
        ensureModifiable();
        return set(privateCreator, tag, vr, vr.toValue(s, bigEndian));
    }

    /**
     * Sets the string array
     *
     * @param tag tag
     * @param vr  VR
     * @param ss  string array
     * @return previous value
     */
    public Object setString(int tag, VR vr, String... ss) {
        return setString(null, tag, vr, ss);
    }

    /**
     * Sets the string array
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param ss             string array
     * @return previous value
     */
    public Object setString(String privateCreator, int tag, VR vr, String... ss) {
        ensureModifiable();
        return set(privateCreator, tag, vr, vr.toValue(ss, bigEndian));
    }

    /**
     * Sets the integer values
     *
     * @param tag tag
     * @param vr  VR
     * @param is  integer values
     * @return previous value
     */
    public Object setInt(int tag, VR vr, int... is) {
        return setInt(null, tag, vr, is);
    }

    /**
     * Sets the integer values
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param is             integer values
     * @return previous value
     */
    public Object setInt(String privateCreator, int tag, VR vr, int... is) {
        ensureModifiable();
        return set(privateCreator, tag, vr, vr.toValue(is, bigEndian));
    }

    /**
     * Sets the long values
     *
     * @param tag tag
     * @param vr  VR
     * @param ls  parameter
     * @return previous value
     */
    public Object setLong(int tag, VR vr, long... ls) {
        return setLong(null, tag, vr, ls);
    }

    /**
     * Sets the long values
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param ls             parameter
     * @return previous value
     */
    public Object setLong(String privateCreator, int tag, VR vr, long... ls) {
        ensureModifiable();
        return set(privateCreator, tag, vr, vr.toValue(ls, bigEndian));
    }

    /**
     * Sets the float values
     *
     * @param tag tag
     * @param vr  VR
     * @param fs  parameter
     * @return previous value
     */
    public Object setFloat(int tag, VR vr, float... fs) {
        return setFloat(null, tag, vr, fs);
    }

    /**
     * Sets the float values
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param fs             parameter
     * @return previous value
     */
    public Object setFloat(String privateCreator, int tag, VR vr, float... fs) {
        ensureModifiable();
        return set(privateCreator, tag, vr, vr.toValue(fs, bigEndian));
    }

    /**
     * Sets the double values
     *
     * @param tag tag
     * @param vr  VR
     * @param ds  date values
     * @return previous value
     */
    public Object setDouble(int tag, VR vr, double... ds) {
        return setDouble(null, tag, vr, ds);
    }

    /**
     * Sets the double values
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param ds             date values
     * @return previous value
     */
    public Object setDouble(String privateCreator, int tag, VR vr, double... ds) {
        ensureModifiable();
        return set(privateCreator, tag, vr, vr.toValue(ds, bigEndian));
    }

    /**
     * Sets the date
     *
     * @param tag tag
     * @param vr  VR
     * @param ds  date array
     * @return previous value
     */
    public Object setDate(int tag, VR vr, Date... ds) {
        return setDate(null, tag, vr, ds);
    }

    /**
     * Sets the date
     *
     * @param tag       tag
     * @param vr        VR
     * @param precision precision
     * @param ds        date array
     * @return previous value
     */
    public Object setDate(int tag, VR vr, DatePrecision precision, Date... ds) {
        return setDate(null, tag, vr, precision, ds);
    }

    /**
     * Sets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param ds             date array
     * @return previous value
     */
    public Object setDate(String privateCreator, int tag, VR vr, Date... ds) {
        return setDate(privateCreator, tag, vr, new DatePrecision(), ds);
    }

    /**
     * Sets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param precision      precision
     * @param ds             date array
     * @return previous value
     */
    public Object setDate(String privateCreator, int tag, VR vr, DatePrecision precision, Date... ds) {
        return setDate(privateCreator, tag, vr, vr == VR.DT, precision, ds);
    }

    /**
     * Sets the date
     *
     * @param tag                 tag
     * @param vr                  VR
     * @param applyTimezoneOffset whether to apply time zone offset
     * @param ds                  date array
     * @return previous value
     */
    public Object setDate(int tag, VR vr, boolean applyTimezoneOffset, Date... ds) {
        return setDate(null, tag, vr, applyTimezoneOffset, ds);
    }

    /**
     * Sets the date
     *
     * @param tag                 tag
     * @param vr                  VR
     * @param applyTimezoneOffset whether to apply time zone offset
     * @param precision           precision
     * @param ds                  date array
     * @return previous value
     */
    public Object setDate(int tag, VR vr, boolean applyTimezoneOffset, DatePrecision precision, Date... ds) {
        return setDate(null, tag, vr, applyTimezoneOffset, precision, ds);
    }

    /**
     * Sets the date
     *
     * @param privateCreator      private creator
     * @param tag                 tag
     * @param vr                  VR
     * @param applyTimezoneOffset whether to apply time zone offset
     * @param ds                  date array
     * @return previous value
     */
    public Object setDate(String privateCreator, int tag, VR vr, boolean applyTimezoneOffset, Date... ds) {
        return setDate(privateCreator, tag, vr, applyTimezoneOffset, new DatePrecision(), ds);
    }

    /**
     * Sets the date
     *
     * @param privateCreator      private creator
     * @param tag                 tag
     * @param vr                  VR
     * @param applyTimezoneOffset whether to apply time zone offset
     * @param precision           precision
     * @param ds                  date array
     * @return previous value
     */
    public Object setDate(
            String privateCreator,
            int tag,
            VR vr,
            boolean applyTimezoneOffset,
            DatePrecision precision,
            Date... ds) {
        ensureModifiable();
        return set(privateCreator, tag, vr, vr.toValue(ds, applyTimezoneOffset ? getTimeZone() : null, precision));
    }

    /**
     * Sets the date
     *
     * @param tag tag
     * @param dt  date array
     */
    public void setDate(long tag, Date... dt) {
        setDate(null, tag, dt);
    }

    /**
     * Sets the date
     *
     * @param tag       tag
     * @param precision precision
     * @param dt        date array
     */
    public void setDate(long tag, DatePrecision precision, Date... dt) {
        setDate(null, tag, precision, dt);
    }

    /**
     * Sets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param dt             date array
     */
    public void setDate(String privateCreator, long tag, Date... dt) {
        setDate(privateCreator, tag, new DatePrecision(), dt);
    }

    /**
     * Sets the date
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param precision      precision
     * @param dt             date array
     */
    public void setDate(String privateCreator, long tag, DatePrecision precision, Date... dt) {
        int daTag = (int) (tag >>> 32);
        int tmTag = (int) tag;
        setDate(privateCreator, daTag, VR.DA, true, precision, dt);
        setDate(privateCreator, tmTag, VR.TM, true, precision, dt);
    }

    /**
     * Sets the date range
     *
     * @param tag   tag
     * @param vr    VR
     * @param range date range
     * @return previous value
     */
    public Object setDateRange(int tag, VR vr, DateRange range) {
        return setDateRange(null, tag, vr, range);
    }

    /**
     * Sets the date range
     *
     * @param tag       tag
     * @param vr        VR
     * @param precision precision
     * @param range     date range
     * @return previous value
     */
    public Object setDateRange(int tag, VR vr, DatePrecision precision, DateRange range) {
        return setDateRange(null, tag, vr, precision, range);
    }

    /**
     * Sets the date range
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param range          date range
     * @return previous value
     */
    public Object setDateRange(String privateCreator, int tag, VR vr, DateRange range) {
        return setDateRange(privateCreator, tag, vr, new DatePrecision(), range);
    }

    /**
     * Sets the date range
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param precision      precision
     * @param range          date range
     * @return previous value
     */
    public Object setDateRange(String privateCreator, int tag, VR vr, DatePrecision precision, DateRange range) {
        ensureModifiable();
        return set(privateCreator, tag, vr, toString(range, vr, getTimeZone(), precision));
    }

    /**
     * Converts to a string
     *
     * @param range     date range
     * @param vr        VR
     * @param tz        time zone
     * @param precision precision
     * @return result
     */
    private static String toString(DateRange range, VR vr, TimeZone tz, DatePrecision precision) {
        String start = range.getStartDate() != null
                ? (String) vr.toValue(new Date[] { range.getStartDate() }, tz, precision)
                : "";
        String end = range.getEndDate() != null ? (String) vr.toValue(new Date[] { range.getEndDate() }, tz, precision)
                : "";
        return toDateRangeString(start, end);
    }

    /**
     * Converts to a date range string.
     *
     * @param start start string
     * @param end   end string
     * @return result
     */
    private static String toDateRangeString(String start, String end) {
        return start.equals(end) ? start : (start + '-' + end);
    }

    /**
     * Sets the date range
     *
     * @param tag tag
     * @param dr  date range
     */
    public void setDateRange(long tag, DateRange dr) {
        setDateRange(null, tag, dr);
    }

    /**
     * Sets the date range
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param range          date range
     */
    public void setDateRange(String privateCreator, long tag, DateRange range) {
        int daTag = (int) (tag >>> 32);
        int tmTag = (int) tag;
        setDateRange(privateCreator, daTag, VR.DA, range);
        setDateRange(privateCreator, tmTag, VR.TM, range);
    }

    /**
     * Sets the value
     *
     * @param tag   tag
     * @param vr    VR
     * @param value value
     * @return previous value
     */
    public Object setValue(int tag, VR vr, Object value) {
        return setValue(null, tag, vr, value);
    }

    /**
     * Sets the value
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param value          value
     * @return previous value
     */
    public Object setValue(String privateCreator, int tag, VR vr, Object value) {
        ensureModifiable();
        return set(privateCreator, tag, vr, value != null ? value : Value.NULL);
    }

    /**
     * Creates a new sequence
     *
     * @param tag             tag
     * @param initialCapacity Initial capacity
     * @return sequence
     */
    public Sequence newSequence(int tag, int initialCapacity) {
        return newSequence(null, tag, initialCapacity);
    }

    /**
     * Creates a new sequence
     *
     * @param privateCreator  private creator
     * @param tag             tag
     * @param initialCapacity Initial capacity
     * @return sequence
     */
    public Sequence newSequence(String privateCreator, int tag, int initialCapacity) {
        ensureModifiable();
        Sequence seq = new Sequence(this, privateCreator, tag, initialCapacity);
        set(privateCreator, tag, VR.SQ, seq);
        return seq;
    }

    /**
     * Ensures the sequence exists
     *
     * @param tag             tag
     * @param initialCapacity Initial capacity
     * @return sequence
     */
    public Sequence ensureSequence(int tag, int initialCapacity) {
        return ensureSequence(null, tag, initialCapacity);
    }

    /**
     * Ensures the sequence exists
     *
     * @param privateCreator  private creator
     * @param tag             tag
     * @param initialCapacity Initial capacity
     * @return sequence
     */
    public Sequence ensureSequence(String privateCreator, int tag, int initialCapacity) {
        ensureModifiable();
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(privateCreator, tag, true);
            tag = Tag.toPrivateTag(creatorTag, tag);
        }
        Sequence seq;
        int index = indexOf(tag);
        if (index >= 0) {
            Object oldValue = values[index];
            if (oldValue instanceof Sequence)
                seq = (Sequence) oldValue;
            else
                values[index] = seq = new Sequence(this, null, 0, initialCapacity);
        } else {
            seq = new Sequence(this, null, 0, initialCapacity);
            insert(-index - 1, tag, VR.SQ, seq);
        }
        return seq;
    }

    /**
     * Creates new fragments
     *
     * @param tag             tag
     * @param vr              VR
     * @param initialCapacity Initial capacity
     * @return fragments
     */
    public Fragments newFragments(int tag, VR vr, int initialCapacity) {
        return newFragments(null, tag, vr, initialCapacity);
    }

    /**
     * Creates new fragments
     *
     * @param privateCreator  private creator
     * @param tag             tag
     * @param vr              VR
     * @param initialCapacity Initial capacity
     * @return fragments
     */
    public Fragments newFragments(String privateCreator, int tag, VR vr, int initialCapacity) {
        ensureModifiable();
        Fragments frags = new Fragments(vr, bigEndian, initialCapacity);
        set(privateCreator, tag, vr, frags);
        return frags;
    }

    /**
     * Sets the attribute
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param value          value
     * @return previous value
     */
    private Object set(String privateCreator, int tag, VR vr, Object value) {
        if (vr == null)
            throw new NullPointerException("vr");
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(privateCreator, tag, true);
            tag = Tag.toPrivateTag(creatorTag, tag);
        }
        if (Tag.isGroupLength(tag))
            return null;
        Object oldValue = set(tag, vr, value);
        if (tag == Tag.SpecificCharacterSet) {
            containsSpecificCharacterSet = true;
            cs = null;
        } else if (tag == Tag.TimezoneOffsetFromUTC) {
            containsTimezoneOffsetFromUTC = value != Value.NULL;
            tz = null;
        }
        return oldValue;
    }

    /**
     * Sets the attribute
     *
     * @param tag   tag
     * @param vr    VR
     * @param value value
     * @return previous value
     */
    private Object set(int tag, VR vr, Object value) {
        int index = indexForInsertOf(tag);
        if (index >= 0) {
            Object oldValue = values[index];
            vrs[index] = vr;
            values[index] = value;
            return oldValue;
        }
        insert(-index - 1, tag, vr, value);
        return null;
    }

    /**
     * Inserts the attribute
     *
     * @param index parameter
     * @param tag   tag
     * @param vr    VR
     * @param value value
     */
    private void insert(int index, int tag, VR vr, Object value) {
        ensureCapacity(size + 1);
        int numMoved = size - index;
        if (numMoved > 0) {
            System.arraycopy(tags, index, tags, index + 1, numMoved);
            System.arraycopy(vrs, index, vrs, index + 1, numMoved);
            System.arraycopy(values, index, values, index + 1, numMoved);
        }
        tags[index] = tag;
        vrs[index] = vr;
        values[index] = value;
        size++;
    }

    /**
     * Adds all attributes
     *
     * @param other other attribute set
     * @return result
     */
    public boolean addAll(Attributes other) {
        ensureModifiable();
        return add(other, null, null, 0, 0, null, null, false, false, null);
    }

    /**
     * Adds all attributes
     *
     * @param other                           other attribute set
     * @param mergeOriginalAttributesSequence whether to mergeOriginal Attributes Sequence
     * @return result
     */
    public boolean addAll(Attributes other, boolean mergeOriginalAttributesSequence) {
        ensureModifiable();
        return add(other, null, null, 0, 0, null, null, mergeOriginalAttributesSequence, false, null);
    }

    /**
     * Adds selected attributes
     *
     * @param other     other attribute set
     * @param selection selected attributes
     * @return result
     */
    public boolean addSelected(Attributes other, Attributes selection) {
        ensureModifiable();
        return add(other, null, null, 0, 0, selection, null, false, false, null);
    }

    /**
     * Adds selected attributes
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @return result
     */
    public boolean addSelected(Attributes other, String privateCreator, int tag) {
        ensureModifiable();
        int index = other.indexOf(privateCreator, tag);
        if (index < 0)
            return false;
        VR vr = other.vrs[index];
        Object value = other.values[index];
        if (!getSpecificCharacterSet().contains(other.getSpecificCharacterSet())
                && containsNonASCIIStringValues(value, vr)) {
            if (!(getSpecificCharacterSet()).isUTF8()) {
                throw new InternalException(
                        "Specific Character Sets " + Arrays.toString(getSpecificCharacterSet().toCodes()) + " and "
                                + Arrays.toString(other.getSpecificCharacterSet().toCodes()) + " not compatible");
            }
            if (vr.useSpecificCharacterSet()) {
                value = other.loadBulkData(vr, value);
                if (value instanceof byte[])
                    value = vr.toStrings(value, other.bigEndian(), other.getSpecificCharacterSet());
            }
        }
        if (value instanceof Sequence) {
            set(privateCreator, tag, (Sequence) value, null);
        } else if (value instanceof Fragments) {
            set(privateCreator, tag, (Fragments) value);
        } else {
            set(privateCreator, tag, vr, toggleEndian(vr, value, bigEndian != other.bigEndian));
        }
        return true;
    }

    /**
     * Tests whether the value contains non-ASCII string data.
     *
     * @param val value
     * @param vr  VR
     * @return {@code true} if non-ASCII string data is present
     */
    private static boolean containsNonASCIIStringValues(Object val, VR vr) {
        if (val instanceof Sequence) {
            for (Attributes item : ((Sequence) val)) {
                if (item.containsNonASCIIStringValues(null, null, 0, 0, null)) {
                    return true;
                }
            }
        } else if (val != Value.NULL && vr.useSpecificCharacterSet()) {
            return true;
        }
        return false;
    }

    /**
     * Adds selected attributes from another attribute object. The specified tag array must be sorted before this call,
     * for example by {@link java.util.Arrays#sort(int[])}.
     *
     * @param other     other attribute object
     * @param selection sorted tag values
     * @return {@code true} if one or more attributes were added or overwritten
     */
    public boolean addSelected(Attributes other, int... selection) {
        return addSelected(other, selection, 0, selection.length);
    }

    /**
     * Adds selected attributes from another attribute object. The specified tag array must be sorted before this call,
     * for example by {@link java.util.Arrays#sort(int[], int, int)}.
     *
     * @param other     other attribute object
     * @param selection sorted tag values
     * @param fromIndex index of the first tag, inclusive
     * @param toIndex   index of the last tag, exclusive
     * @return {@code true} if one or more attributes were added or overwritten
     */
    public boolean addSelected(Attributes other, int[] selection, int fromIndex, int toIndex) {
        ensureModifiable();
        return add(other, selection, null, fromIndex, toIndex, null, null, false, false, null);
    }

    /**
     * Adds non-selected attributes from another attribute object. The specified tag array must be sorted before this
     * call, for example by {@link java.util.Arrays#sort(int[])}.
     *
     * @param other     other attribute object
     * @param selection sorted tag values
     * @return {@code true} if one or more attributes were added or overwritten
     */
    public boolean addNotSelected(Attributes other, int... selection) {
        return addNotSelected(other, selection, 0, selection.length);
    }

    /**
     * Adds non-selected attributes from another attribute object. The specified tag array must be sorted before this
     * call, for example by {@link java.util.Arrays#sort(int[])}.
     *
     * @param other     other attribute object
     * @param selection sorted tag values
     * @param fromIndex index of the first tag, inclusive
     * @param toIndex   index of the last tag, exclusive
     * @return {@code true} if one or more attributes were added or overwritten
     */
    public boolean addNotSelected(Attributes other, int[] selection, int fromIndex, int toIndex) {
        ensureModifiable();
        return add(other, null, selection, fromIndex, toIndex, null, null, false, false, null);
    }

    /**
     * Supplements empty attributes from the selection.
     *
     * @param selection selected attributes
     */
    public void supplementEmpty(Attributes selection) {
        ensureModifiable();
        final int[] otherTags = selection.tags;
        final VR[] otherVRs = selection.vrs;
        final Object[] otherValues = selection.values;
        for (int i = 0; i < selection.size; i++) {
            int index = indexOf(otherTags[i]);
            if (index < 0) {
                insert(-index - 1, otherTags[i], otherVRs[i], Value.NULL);
            } else if (otherValues[i] instanceof Sequence && values[index] instanceof Sequence) {
                Sequence otherSeq = (Sequence) otherValues[i];
                Attributes otherItem;
                if (!otherSeq.isEmpty() && !(otherItem = otherSeq.get(0)).isEmpty()) {
                    for (Attributes item : (Sequence) values[index]) {
                        item.supplementEmpty(otherItem);
                    }
                }
            }
        }
    }

    /**
     * Adds attributes
     *
     * @param other                           other attribute set
     * @param include                         included tags
     * @param exclude                         excluded tags
     * @param fromIndex                       start index
     * @param toIndex                         end index
     * @param selection                       selected attributes
     * @param updatePolicy                    update policy
     * @param mergeOriginalAttributesSequence whether to mergeOriginal Attributes Sequence
     * @param simulate                        whether simulation is enabled
     * @param modified                        modified attribute set
     * @return result
     */
    private boolean add(
            Attributes other,
            int[] include,
            int[] exclude,
            int fromIndex,
            int toIndex,
            Attributes selection,
            UpdatePolicy updatePolicy,
            boolean mergeOriginalAttributesSequence,
            boolean simulate,
            Attributes modified) {
        if (updatePolicy == UpdatePolicy.REPLACE)
            throw new IllegalArgumentException("updatePolicy:" + updatePolicy);
        boolean decodeStringValue = false;
        if (updatePolicy != UpdatePolicy.PRESERVE && !isEmpty()) {
            boolean updateSpecificCharacterSet = isUpdateSpecificCharacterSet(
                    other,
                    include,
                    exclude,
                    fromIndex,
                    toIndex,
                    selection,
                    updatePolicy);
            if (!(updateSpecificCharacterSet
                    ? other.getSpecificCharacterSet().contains(getSpecificCharacterSet())
                            || !containsNonASCIIStringValues(null, null, 0, 0, null)
                    : getSpecificCharacterSet().contains(other.getSpecificCharacterSet())
                            || !other.containsNonASCIIStringValues(include, exclude, fromIndex, toIndex, selection))) {
                if (!(updateSpecificCharacterSet ? other.getSpecificCharacterSet() : getSpecificCharacterSet())
                        .isUTF8()) {
                    throw new InternalException(
                            "Specific Character Sets " + Arrays.toString(getSpecificCharacterSet().toCodes()) + " and "
                                    + Arrays.toString(other.getSpecificCharacterSet().toCodes()) + " not compatible");
                }
                if (updateSpecificCharacterSet) {
                    decodeStringValuesUsingSpecificCharacterSet();
                } else {
                    decodeStringValue = true;
                }
            }
        }
        boolean toggleEndian = bigEndian != other.bigEndian;
        boolean modifiedToggleEndian = modified != null && bigEndian != modified.bigEndian;
        final int[] otherTags = other.tags;
        final VR[] srcVRs = other.vrs;
        final Object[] srcValues = other.values;
        final int otherSize = other.size;
        int numAdd = 0;
        String privateCreator = null;
        String privateCreator0 = null;
        int creatorTag = 0;
        for (int i = 0; i < otherSize; i++) {
            int tag = otherTags[i];
            VR vr = srcVRs[i];
            Object value = srcValues[i];
            if (include != null && Arrays.binarySearch(include, fromIndex, toIndex, tag) < 0)
                continue;
            if (exclude != null && Arrays.binarySearch(exclude, fromIndex, toIndex, tag) >= 0)
                continue;
            if (Tag.isPrivateCreator(tag) && (privateCreator = other.privateCreatorAt(i)) != null) {
                if ((selection == null || selection.creatorTagOf(privateCreator, tag, false) > 0) && !contains(tag)
                        && (creatorTagOf(privateCreator, tag, false) < 0
                                || other.creatorTagOf(privateCreator, tag, false) != tag)) { // preserve non-conflicting
                    // Private Creator ID tag
                    // positions
                    setString(tag, VR.LO, privateCreator);
                }
                continue;
            }
            if (Tag.isPrivateTag(tag)) {
                int tmp = Tag.creatorTagOf(tag);
                if (creatorTag != tmp) {
                    creatorTag = tmp;
                    privateCreator = other.privateCreatorAt(other.indexOf(creatorTag));
                    privateCreator0 = privateCreator == null
                            || privateCreator.equals(privateCreatorAt(indexOf(creatorTag))) // preserve private tag
                                    ? null
                                    : privateCreator;
                }
            } else {
                creatorTag = 0;
                privateCreator = null;
                privateCreator0 = null;
            }
            if (selection != null && !selection.contains(privateCreator, tag))
                continue;
            if (updatePolicy != null) {
                if (updatePolicy != UpdatePolicy.OVERWRITE && isEmpty(value))
                    continue;
                int j = indexOf(tag);
                if (j < 0) {
                    if (updatePolicy == UpdatePolicy.PRESERVE)
                        value = Value.NULL;
                } else {
                    if (updatePolicy == UpdatePolicy.PRESERVE)
                        continue;
                    Object origValue = vrs[j].isStringType() ? decodeStringValue(j) : values[j];
                    if (updatePolicy == UpdatePolicy.SUPPLEMENT ? !isEmpty(origValue) : equalValues(other, j, i))
                        continue;
                    if (modified != null && !isEmpty(origValue) && !modified.contains(privateCreator, tag)) {
                        if (origValue instanceof Sequence) {
                            modified.set(privateCreator, tag, (Sequence) origValue, null);
                        } else if (origValue instanceof Fragments) {
                            modified.set(privateCreator, tag, (Fragments) origValue);
                        } else {
                            modified.set(
                                    privateCreator,
                                    tag,
                                    vrs[j],
                                    toggleEndian(vrs[j], origValue, modifiedToggleEndian));
                        }
                    }
                }
            }
            if (!simulate) {
                if (value instanceof Sequence) {
                    Sequence dest;
                    if (mergeOriginalAttributesSequence && tag == Tag.OriginalAttributesSequence
                            && (dest = getSequence(tag)) != null)
                        mergeOriginalAttributesSequence((Sequence) value, dest);
                    else
                        set(
                                privateCreator0,
                                tag,
                                (Sequence) value,
                                selection != null ? selection.getNestedDataset(privateCreator, tag) : null);
                } else if (value instanceof Fragments) {
                    set(privateCreator0, tag, (Fragments) value);
                } else {
                    if (decodeStringValue && vr.useSpecificCharacterSet()) {
                        value = other.loadBulkData(vr, value);
                        if (value instanceof byte[])
                            value = vr.toStrings(value, other.bigEndian(), other.getSpecificCharacterSet());
                    }
                    set(privateCreator0, tag, vr, toggleEndian(vr, value, toggleEndian));
                }
            }
            numAdd++;
        }
        return numAdd != 0;
    }

    /**
     * Tests whether this attribute range contains non-ASCII string values.
     *
     * @param include   included tags
     * @param exclude   excluded tags
     * @param fromIndex start index
     * @param toIndex   end index
     * @param selection selected attributes
     * @return {@code true} if a non-ASCII string value is present
     */
    private boolean containsNonASCIIStringValues(
            int[] include,
            int[] exclude,
            int fromIndex,
            int toIndex,
            Attributes selection) {
        for (int i = 0; i < size; i++) {
            int tag = tags[i];
            Object val = values[i];
            if ((include == null || Arrays.binarySearch(include, fromIndex, toIndex, tag) >= 0)
                    && (exclude == null || Arrays.binarySearch(exclude, fromIndex, toIndex, tag) < 0)
                    && (selection == null || selection.contains(tag))) {
                if (val instanceof Sequence) {
                    Attributes nestedSelection = selection != null ? selection.getNestedDataset(tag) : null;
                    for (Attributes item : ((Sequence) val)) {
                        if (item.containsNonASCIIStringValues(null, null, 0, 0, nestedSelection)) {
                            return true;
                        }
                    }
                } else if (val != Value.NULL && vrs[i].useSpecificCharacterSet()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tests whether the specific character set must be updated.
     *
     * @param other        other attributes
     * @param include      included tags
     * @param exclude      excluded tags
     * @param fromIndex    start index
     * @param toIndex      end index
     * @param selection    selected attributes
     * @param updatePolicy update policy
     * @return {@code true} if the specific character set must be updated
     */
    private boolean isUpdateSpecificCharacterSet(
            Attributes other,
            int[] include,
            int[] exclude,
            int fromIndex,
            int toIndex,
            Attributes selection,
            UpdatePolicy updatePolicy) {
        String[] oscs = other.getStrings(Tag.SpecificCharacterSet);
        if (oscs == null)
            return false;
        if (updatePolicy != null)
            switch (updatePolicy) {
                case PRESERVE:
                    return false;

                case SUPPLEMENT:
                    if (containsValue(Tag.SpecificCharacterSet))
                        return false;
                case MERGE:
                    if (oscs.length == 0)
                        return false;
            }
        return (include == null || Arrays.binarySearch(include, fromIndex, toIndex, Tag.SpecificCharacterSet) >= 0)
                && (exclude == null || Arrays.binarySearch(exclude, fromIndex, toIndex, Tag.SpecificCharacterSet) < 0)
                && (selection == null || selection.contains(Tag.SpecificCharacterSet));
    }

    /**
     * Merges the original attributes sequence.
     *
     * @param src  source sequence
     * @param dest destination sequence
     */
    private void mergeOriginalAttributesSequence(Sequence src, Sequence dest) {
        Map<String, Attributes> sort = new TreeMap<>();
        for (Attributes destItem : dest) {
            sort.put(destItem.getString(Tag.AttributeModificationDateTime, ""), destItem);
        }
        dest.clear();
        for (Attributes srcItem : src) {
            String dt = srcItem.getString(Tag.AttributeModificationDateTime, "");
            Attributes destItem = sort.get(dt);
            Attributes destModified;
            if (destItem != null
                    && (destModified = destItem.getNestedDataset(Tag.ModifiedAttributesSequence)) != null) {
                try {
                    Attributes srcModified;
                    if ((srcModified = srcItem.getNestedDataset(Tag.ModifiedAttributesSequence)) != null) {
                        destModified.addAll(srcModified);
                    }
                } catch (InternalException e) {
                    Logger.warn(
                            false,
                            "Image",
                            e,
                            "DICOM original attributes merge failed: modifiedAtPresent={}, exception={}",
                            dt != null,
                            e.getClass().getSimpleName());
                }
            } else {
                sort.put(dt, new Attributes(srcItem));
            }
        }
        for (Attributes destItem : sort.values()) {
            dest.add(destItem);
        }
    }

    /**
     * Updates attributes
     *
     * @param updatePolicy update policy
     * @param newAttrs     new attribute set
     * @param modified     modified attribute set
     * @return result
     */
    public boolean update(UpdatePolicy updatePolicy, Attributes newAttrs, Attributes modified) {
        ensureModifiable();
        return add(newAttrs, null, null, 0, 0, null, updatePolicy, false, false, modified);
    }

    /**
     * Updates attributes
     *
     * @param updatePolicy                    update policy
     * @param mergeOriginalAttributesSequence whether to mergeOriginal Attributes Sequence
     * @param newAttrs                        new attribute set
     * @param modified                        modified attribute set
     * @return result
     */
    public boolean update(
            UpdatePolicy updatePolicy,
            boolean mergeOriginalAttributesSequence,
            Attributes newAttrs,
            Attributes modified) {
        ensureModifiable();
        return add(newAttrs, null, null, 0, 0, null, updatePolicy, mergeOriginalAttributesSequence, false, modified);
    }

    /**
     * Tests update
     *
     * @param updatePolicy update policy
     * @param newAttrs     new attribute set
     * @param modified     modified attribute set
     * @return result
     */
    public boolean testUpdate(UpdatePolicy updatePolicy, Attributes newAttrs, Attributes modified) {
        return add(newAttrs, null, null, 0, 0, null, updatePolicy, false, true, modified);
    }

    /**
     * Adds selected attributes from another attribute object. Optionally saves original values of overwritten existing
     * non-empty attributes in another attribute object. The specified tag array must be sorted before this call, for
     * example by {@link java.util.Arrays#sort(int[])} method).
     *
     * @param newAttrs  other attribute object
     * @param modified  attribute object used to collectoverwritten non-empty attributes and their original values, or
     *                  null
     * @param selection sorted tag values
     * @return {@code true} if one or more attributes were added or overwritten
     */
    public boolean updateSelected(
            UpdatePolicy updatePolicy,
            Attributes newAttrs,
            Attributes modified,
            int... selection) {
        ensureModifiable();
        return add(newAttrs, selection, null, 0, selection.length, null, updatePolicy, false, false, modified);
    }

    /**
     * Tests whether {@link #updateSelected} would modify attributes without modifying this instance.
     *
     * @param newAttrs  other attribute object
     * @param modified  attribute object used to collectoverwritten non-empty attributes and their original values, or
     *                  null
     * @param selection sorted tag values
     * @return {@code true} if one or more attributes would be added or overwritten
     */
    public boolean testUpdateSelected(
            UpdatePolicy updatePolicy,
            Attributes newAttrs,
            Attributes modified,
            int... selection) {
        return add(newAttrs, selection, null, 0, selection.length, null, updatePolicy, false, true, modified);
    }

    /**
     * Adds non-selected attributes from another attribute object. Optionally saves original values of overwritten
     * existing non-empty attributes in another attribute object. The specified tag array must be sorted before this
     * call, for example by {@link java.util.Arrays#sort(int[])} method).
     *
     * @param newAttrs  other attribute object
     * @param modified  attribute object used to collectoverwritten non-empty attributes and their original values, or
     *                  null
     * @param selection sorted tag values
     * @return {@code true} if one or more attributes were added or overwritten
     */
    public boolean updateNotSelected(
            UpdatePolicy updatePolicy,
            Attributes newAttrs,
            Attributes modified,
            int... selection) {
        ensureModifiable();
        return add(newAttrs, null, selection, 0, selection.length, null, updatePolicy, false, false, modified);
    }

    /**
     * Tests whether {@link #updateNotSelected} would modify attributes without modifying this instance.
     *
     * @param newAttrs  other attribute object
     * @param modified  attribute object used to collectoverwritten non-empty attributes and their original values, or
     *                  null
     * @param selection sorted tag values
     * @return {@code true} if one or more attributes would be added or overwritten
     */
    public boolean testUpdateNotSelected(
            UpdatePolicy updatePolicy,
            Attributes newAttrs,
            Attributes modified,
            int... selection) {
        return add(newAttrs, null, selection, 0, selection.length, null, updatePolicy, false, true, modified);
    }

    /**
     * Appends an item to an existing or newly added (0400,0561) Original Attributes Sequence.
     *
     * @param sourceOfPreviousValues source of previous values
     * @param modificationDateTime   modification date time
     * @param reasonForModification  modification reason
     * @param modifyingSystem        modifying system
     * @param originalAttributes     original attributes
     * @return the same attribute instance
     */
    public Attributes addOriginalAttributes(
            String sourceOfPreviousValues,
            Date modificationDateTime,
            String reasonForModification,
            String modifyingSystem,
            Attributes originalAttributes) {
        ensureModifiable();
        if (originalAttributes.isEmpty())
            return this;
        Attributes item = new Attributes(bigEndian, 5);
        item.ensureSequence(Tag.ModifiedAttributesSequence, 1).add(originalAttributes);
        item.setDate(Tag.AttributeModificationDateTime, VR.DT, modificationDateTime);
        item.setString(Tag.ModifyingSystem, VR.LO, modifyingSystem);
        item.setString(Tag.SourceOfPreviousValues, VR.LO, sourceOfPreviousValues);
        item.setString(Tag.ReasonForTheAttributeModification, VR.CS, reasonForModification);
        ensureSequence(Tag.OriginalAttributesSequence, 1).add(item);
        return this;
    }

    /**
     * Toggles endian order
     *
     * @param vr           VR
     * @param value        value
     * @param toggleEndian parameter
     * @return result
     */
    private static Object toggleEndian(VR vr, Object value, boolean toggleEndian) {
        return (toggleEndian && value instanceof byte[]) ? vr.toggleEndian((byte[]) value, true) : value;
    }

    /**
     * Compares objects for equality
     *
     * @param o object
     * @return result
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Attributes))
            return false;
        final Attributes other = (Attributes) o;
        if (size != other.size)
            return false;
        String privateCreator = null;
        int creatorTag = 0;
        int otherCreatorTag = 0;
        for (int i = 0; i < size; i++) {
            int tag = tags[i];
            switch (Tag.Type.typeOf(tag)) {
                case PRIVATE_CREATOR:
                    continue;

                case PRIVATE:
                    int tmp = Tag.creatorTagOf(tag);
                    if (creatorTag != tmp) {
                        creatorTag = tmp;
                        privateCreator = privateCreatorAt(indexOf(tmp));
                        if (privateCreator != null) {
                            otherCreatorTag = other.creatorTagOf(privateCreator, tag, false);
                            if (otherCreatorTag == -1)
                                return false; // other has no matching private creator
                        } else {
                            if (other.privateCreatorAt(other.indexOf(tmp)) != null)
                                return false; // other attribute has associated private creator
                        }
                    }
                    if (privateCreator != null) {
                        int j = other.indexOf(Tag.toPrivateTag(otherCreatorTag, tag));
                        if (j < 0 || !equalValues(other, i, j))
                            return false;
                        continue;
                    }
                    // fall through: treat private attributes without associated private creator like standard
                    // attributes
                case STANDARD:
                    if (tag != other.tags[i] || !equalValues(other, i, i))
                        return false;
            }
        }
        return true;
    }

    /**
     * Compares values for equality
     *
     * @param other other attribute set
     * @param tag   tag
     * @return result
     */
    public boolean equalValues(Attributes other, int tag) {
        return equalValues(other, null, tag);
    }

    /**
     * Compares values for equality
     *
     * @param other          other attribute set
     * @param privateCreator private creator
     * @param tag            tag
     * @return result
     */
    public boolean equalValues(Attributes other, String privateCreator, int tag) {
        return equalValues(other, indexOf(privateCreator, tag), other.indexOf(privateCreator, tag));
    }

    /**
     * Compares values for equality
     *
     * @param other      other attribute set
     * @param index      parameter
     * @param otherIndex parameter
     * @return result
     */
    private boolean equalValues(Attributes other, int index, int otherIndex) {
        if (index < 0 && otherIndex < 0)
            return true;
        VR vr = index < 0 ? other.vrs[otherIndex] : vrs[index];
        if (otherIndex >= 0 && vr != other.vrs[otherIndex])
            return false;
        if (vr.isStringType()) {
            try {
                if (vr == VR.IS)
                    return Arrays.equals(decodeISValue(index), other.decodeISValue(otherIndex));
                else if (vr == VR.DS)
                    return Arrays.equals(decodeDSValue(index), other.decodeDSValue(otherIndex));
            } catch (NumberFormatException e) {
            }
            Object v1 = index < 0 ? Value.NULL : decodeStringValue(index);
            Object v2 = otherIndex < 0 ? Value.NULL : other.decodeStringValue(otherIndex);
            return (v1 instanceof String[])
                    ? (v2 instanceof String[]) && (vr == VR.PN ? equalPNValues((String[]) v1, (String[]) v2)
                            : Arrays.equals((String[]) v1, (String[]) v2))
                    : !(v2 instanceof String[]) && (vr == VR.PN ? equalPNValues(v1, v2) : v1.equals(v2));
        }
        Object v1 = index < 0 ? Value.NULL : values[index];
        Object v2 = otherIndex < 0 ? Value.NULL : other.values[otherIndex];
        if (v1 instanceof byte[]) {
            if (v2 instanceof byte[] && ((byte[]) v1).length == ((byte[]) v2).length) {
                if (bigEndian != other.bigEndian)
                    v2 = vr.toggleEndian((byte[]) v2, true);
                return Arrays.equals((byte[]) v1, (byte[]) v2);
            }
        } else
            return v1.equals(v2);
        return false;
    }

    /**
     * Compares PN values for equality
     *
     * @param v1 value1
     * @param v2 value2
     * @return result
     */
    private boolean equalPNValues(Object v1, Object v2) {
        return v1 == Value.NULL ? !containsPNValue(v2)
                : v2 == Value.NULL ? !containsPNValue(v1) : equalPNValues((String) v1, (String) v2);
    }

    /**
     * Whether PN value is present
     *
     * @param v value
     * @return Whether PN value is present
     */
    private static boolean containsPNValue(Object v) {
        return v != Value.NULL && !new PersonName((String) v, true).isEmpty();
    }

    /**
     * Compares PN values for equality
     *
     * @param v1 value1
     * @param v2 value2
     * @return result
     */
    private static boolean equalPNValues(String[] v1, String[] v2) {
        if (v1.length != v2.length)
            return false;
        for (int i = 0; i < v1.length; i++)
            if (!equalPNValues(v1[i], v2[i]))
                return false;
        return true;
    }

    /**
     * Compares PN values for equality
     *
     * @param v1 value1
     * @param v2 value2
     * @return result
     */
    private static boolean equalPNValues(String v1, String v2) {
        return new PersonName(v1, true).equals(new PersonName(v2, true));
    }

    /**
     * Computes the hash code
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        int h = 0;
        for (int i = 0; i < size; i++) {
            int tag = tags[i];
            if (!Tag.isPrivateGroup(tag))
                h = 31 * h + tag;
        }
        return h;
    }

    /**
     * Sets the sequence
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param src            source sequence
     * @param selection      selected attributes
     */
    private void set(String privateCreator, int tag, Sequence src, Attributes selection) {
        Sequence dst = newSequence(privateCreator, tag, src.size());
        for (Attributes item : src)
            dst.add(
                    selection != null && !selection.isEmpty() ? new Attributes(item, bigEndian, selection)
                            : new Attributes(item, bigEndian));
    }

    /**
     * Sets fragments
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param src            source fragments
     */
    private void set(String privateCreator, int tag, Fragments src) {
        boolean toogleEndian = src.bigEndian() != bigEndian;
        VR vr = src.vr();
        Fragments dst = newFragments(privateCreator, tag, vr, src.size());
        for (Object frag : src)
            dst.add(toggleEndian(vr, frag, toogleEndian));
    }

    /**
     * Converts to a string
     *
     * @return result
     */
    @Override
    public String toString() {
        return toString(TO_STRING_LIMIT, TO_STRING_WIDTH);
    }

    /**
     * Converts to a string
     *
     * @param limit    limit
     * @param maxWidth maximum width
     * @return result
     */
    public String toString(int limit, int maxWidth) {
        return toStringBuilder(limit, maxWidth, new StringBuilder(1024)).toString();
    }

    /**
     * Converts to a string builder
     *
     * @param sb string builder
     * @return string builder
     */
    public StringBuilder toStringBuilder(StringBuilder sb) {
        return toStringBuilder(TO_STRING_LIMIT, TO_STRING_WIDTH, sb);
    }

    /**
     * Converts to a string builder
     *
     * @param limit    limit
     * @param maxWidth maximum width
     * @param sb       string builder
     * @return string builder
     */
    public StringBuilder toStringBuilder(int limit, int maxWidth, StringBuilder sb) {
        if (appendAttributes(limit, maxWidth, sb, "") > limit)
            sb.append("...\n");
        return sb;
    }

    /**
     * Appends attributes
     *
     * @param limit    limit
     * @param maxWidth maximum width
     * @param sb       string builder
     * @param prefix   prefix
     * @return line count
     */
    private int appendAttributes(int limit, int maxWidth, StringBuilder sb, String prefix) {
        if (size == 0)
            return 0;
        if (tags[0] >= 0) {
            return appendAttributes(limit, maxWidth, sb, prefix, 0, size);
        }
        int lines, index0 = -(1 + indexOf(0));
        return (lines = appendAttributes(limit, maxWidth, sb, prefix, index0, size))
                + appendAttributes(limit - lines, maxWidth, sb, prefix, 0, index0);
    }

    /**
     * Appends attributes
     *
     * @param limit    limit
     * @param maxWidth maximum width
     * @param sb       string builder
     * @param prefix   prefix
     * @param start    start index
     * @param end      end index
     * @return line count
     */
    private int appendAttributes(int limit, int maxWidth, StringBuilder sb, String prefix, int start, int end) {
        int lines = 0;
        int creatorTag = 0;
        String privateCreator = null;
        for (int i = start; i < end; i++) {
            if (++lines > limit)
                break;
            int tag = tags[i];
            if (Tag.isPrivateTag(tag)) {
                int tmp = Tag.creatorTagOf(tag);
                if (creatorTag != tmp) {
                    creatorTag = tmp;
                    privateCreator = getString(creatorTag, null);
                }
            } else {
                creatorTag = 0;
                privateCreator = null;
            }
            Object value = values[i];
            appendAttribute(privateCreator, tag, vrs[i], value, sb.length() + maxWidth, sb, prefix);
            if (value instanceof Sequence)
                lines += appendItems((Sequence) value, limit - lines, maxWidth, sb, prefix + '>');
        }
        return lines;
    }

    /**
     * Appends an item
     *
     * @param sq       sequence
     * @param limit    limit
     * @param maxWidth maximum width
     * @param sb       string builder
     * @param prefix   prefix
     * @return line count
     */
    private int appendItems(Sequence sq, int limit, int maxWidth, StringBuilder sb, String prefix) {
        int lines = 0;
        int itemNo = 0;
        for (Attributes item : sq) {
            if (++lines > limit)
                break;
            sb.append(prefix).append("Item #").append(++itemNo).append('\n');
            lines += item.appendAttributes(limit - lines, maxWidth, sb, prefix);
        }
        return lines;
    }

    /**
     * Appends attributes
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param value          value
     * @param maxLength      maximum length
     * @param sb             string builder
     * @param prefix         prefix
     * @return string builder
     */
    private StringBuilder appendAttribute(
            String privateCreator,
            int tag,
            VR vr,
            Object value,
            int maxLength,
            StringBuilder sb,
            String prefix) {
        sb.append(prefix).append(Tag.toString(tag)).append(' ').append(vr).append(" [");
        if (vr.prompt(value, bigEndian, getSpecificCharacterSet(vr), maxLength - sb.length() - 1, sb)) {
            sb.append("] ").append(ElementDictionary.keywordOf(tag, privateCreator));
            if (sb.length() > maxLength)
                sb.setLength(maxLength);
        }
        sb.append('\n');
        return sb;
    }

    /**
     * Calculates length
     *
     * @param encOpts    encoding options
     * @param explicitVR explicit VR flag
     * @return length
     */
    public int calcLength(ImageEncodingOptions encOpts, boolean explicitVR) {
        if (isEmpty())
            return 0;
        this.groupLengths = encOpts.groupLength ? new int[countGroups()] : null;
        this.length = calcLength(encOpts, explicitVR, getSpecificCharacterSet(), groupLengths);
        return this.length;
    }

    /**
     * Calculates length
     *
     * @param encOpts      encoding options
     * @param explicitVR   explicit VR flag
     * @param cs           specific character set
     * @param groupLengths Group length array
     * @return length
     */
    private int calcLength(
            ImageEncodingOptions encOpts,
            boolean explicitVR,
            SpecificCharacterSet cs,
            int[] groupLengths) {
        int len, totlen = 0;
        int groupLengthTag = -1;
        int groupLengthIndex = -1;
        VR vr;
        Object val;
        for (int i = 0; i < size; i++) {
            vr = vrs[i];
            val = values[i];
            len = explicitVR ? vr.headerLength() : 8;
            if (val instanceof Value)
                len += ((Value) val).calcLength(encOpts, explicitVR, vr);
            else {
                if (!(val instanceof byte[]))
                    values[i] = val = vr.toBytes(val, cs);
                len += (((byte[]) val).length + 1) & ~1;
            }
            totlen += len;
            if (groupLengths != null) {
                int tmp = Tag.groupLengthTagOf(tags[i]);
                if (groupLengthTag != tmp) {
                    groupLengthTag = tmp;
                    groupLengthIndex++;
                    totlen += 12;
                }
                groupLengths[groupLengthIndex] += len;
            }
        }
        return totlen;
    }

    /**
     * Calculates group count
     *
     * @return result
     */
    private int countGroups() {
        int groupLengthTag = -1;
        int count = 0;
        for (int i = 0; i < size; i++) {
            int tmp = Tag.groupLengthTagOf(tags[i]);
            if (groupLengthTag != tmp) {
                if (groupLengthTag < 0)
                    this.groupLengthIndex0 = count;
                groupLengthTag = tmp;
                count++;
            }
        }
        return count;
    }

    /**
     * Writes to the output stream
     *
     * @param out output stream
     * @throws IOException IOerror
     */
    public void writeTo(ImageOutputStream out) throws IOException {
        if (isEmpty())
            return;
        if (groupLengths == null && out.getEncodingOptions().groupLength)
            throw new IllegalStateException("groupLengths not initialized by calcLength()");
        SpecificCharacterSet cs = getSpecificCharacterSet();
        if (tags[0] < 0) {
            int index0 = -(1 + indexOf(0));
            writeTo(out, cs, index0, size, groupLengthIndex0);
            writeTo(out, cs, 0, index0, 0);
        } else {
            writeTo(out, cs, 0, size, 0);
        }
    }

    /**
     * Writes to an output stream after pixel data
     *
     * @param out output stream
     * @throws IOException IOerror
     */
    public void writePostPixelDataTo(ImageOutputStream out) throws IOException {
        if (isEmpty() || tags[0] >= 0 && tags[size - 1] <= Tag.PixelData)
            return;
        SpecificCharacterSet cs = getSpecificCharacterSet();
        int indexPostPixelData = indexOf(Tag.PixelData) + 1;
        if (indexPostPixelData < 0)
            indexPostPixelData = -indexPostPixelData;
        writeTo(out, cs, indexPostPixelData, size, 0);
        if (tags[0] < 0) {
            int index0 = -(1 + indexOf(0));
            writeTo(out, cs, 0, index0, 0);
        }
    }

    /**
     * Writes an item to the output stream
     *
     * @param out output stream
     * @throws IOException IOerror
     */
    public void writeItemTo(ImageOutputStream out) throws IOException {
        ImageEncodingOptions encOpts = out.getEncodingOptions();
        int len = getEncodedItemLength(encOpts, out.isExplicitVR());
        out.writeHeader(Tag.Item, null, len);
        writeTo(out);
        if (len == -1)
            out.writeHeader(Tag.ItemDelimitationItem, null, 0);
    }

    /**
     * Gets the encoded item length
     *
     * @param encOpts    encoding options
     * @param explicitVR explicit VR flag
     * @return result
     */
    private int getEncodedItemLength(ImageEncodingOptions encOpts, boolean explicitVR) {
        if (isEmpty())
            return encOpts.undefEmptyItemLength ? -1 : 0;
        if (encOpts.undefItemLength)
            return -1;
        if (length == -1)
            calcLength(encOpts, explicitVR);
        return length;
    }

    /**
     * Writes to the output stream
     *
     * @param out              output stream
     * @param cs               specific character set
     * @param start            start index
     * @param end              end index
     * @param groupLengthIndex group length index
     * @throws IOException IOerror
     */
    private void writeTo(ImageOutputStream out, SpecificCharacterSet cs, int start, int end, int groupLengthIndex)
            throws IOException {
        boolean groupLength = groupLengths != null;
        int groupLengthTag = -1;
        for (int i = start; i < end; i++) {
            int tag = tags[i];
            if (groupLength) {
                int tmp = Tag.groupLengthTagOf(tag);
                if (groupLengthTag != tmp) {
                    groupLengthTag = tmp;
                    out.writeGroupLength(groupLengthTag, groupLengths[groupLengthIndex++]);
                }
            }
            out.writeAttribute(tag, vrs[i], values[i], cs);
        }
    }

    /**
     * Invokes {@link Visitor#visit} for each attribute in this instance. If <code>visitor.visit()</code> returns
     * <code>false</code>, the operation is aborted.
     *
     * @param visitor             visitor
     * @param visitNestedDatasets controls whether <code>visitor.visit()</code> is also invoked for nested datasets
     * @return <code>true</code> if the operation was not aborted
     * @throws Exception error
     */
    public boolean accept(Visitor visitor, boolean visitNestedDatasets) throws Exception {
        if (isEmpty())
            return true;
        if (tags[0] < 0) {
            int index0 = -(1 + indexOf(0));
            return accept(visitor, visitNestedDatasets, index0, size)
                    && accept(visitor, visitNestedDatasets, 0, index0);
        } else {
            return accept(visitor, visitNestedDatasets, 0, size);
        }
    }

    /**
     * Accepts a visitor
     *
     * @param visitor             visitor
     * @param visitNestedDatasets whether nested datasets are visited
     * @param start               start index
     * @param end                 end index
     * @return <code>true</code> if the operation was not aborted
     * @throws Exception error
     */
    private boolean accept(Visitor visitor, boolean visitNestedDatasets, int start, int end) throws Exception {
        for (int i = start; i < end; i++) {
            if (!visitor.visit(this, tags[i], vrs[i], values[i]))
                return false;
            if (visitNestedDatasets && (values[i] instanceof Sequence)) {
                if (visitor instanceof SequenceVisitor)
                    ((SequenceVisitor) visitor).startSequence(tags[i]);
                int itemIndex = 0;
                for (Attributes item : (Sequence) values[i]) {
                    if (visitor instanceof SequenceVisitor)
                        ((SequenceVisitor) visitor).startItem(tags[i], itemIndex);
                    if (!item.accept(visitor, true))
                        return false;
                    if (visitor instanceof SequenceVisitor)
                        ((SequenceVisitor) visitor).endItem();
                    itemIndex++;
                }
                if (visitor instanceof SequenceVisitor)
                    ((SequenceVisitor) visitor).endSequence();
            }
        }
        return true;
    }

    /**
     * Writes a group to the output stream
     *
     * @param out            output stream
     * @param groupLengthTag group length tag
     * @throws IOException IOerror
     */
    public void writeGroupTo(ImageOutputStream out, int groupLengthTag) throws IOException {
        if (isEmpty())
            throw new IllegalStateException("No attributes");
        checkInGroup(0, groupLengthTag);
        checkInGroup(size - 1, groupLengthTag);
        SpecificCharacterSet cs = getSpecificCharacterSet();
        out.writeGroupLength(groupLengthTag, calcLength(out.getEncodingOptions(), out.isExplicitVR(), cs, null));
        writeTo(out, cs, 0, size, 0);
    }

    /**
     * Checks whether the index is in the group
     *
     * @param i              index
     * @param groupLengthTag group length tag
     */
    private void checkInGroup(int i, int groupLengthTag) {
        int tag = tags[i];
        if (Tag.groupLengthTagOf(tag) != groupLengthTag)
            throw new IllegalStateException(Tag.toString(tag) + " does not belong to group ("
                    + Tag.shortToHexString(Tag.groupNumber(groupLengthTag)) + ",eeee).");
    }

    /**
     * Creates DICOM file meta information for this <i>dataset</i> with the given <i>Transfer Syntax UID
     * (0002,0010)</i>, including the optional <i>Implementation Version Name (0002,0013)</i>.
     *
     * @param tsuid <i>Transfer Syntax UID (0002,0010)</i>
     * @return created DICOM file meta information
     */
    public Attributes createFileMetaInformation(String tsuid) {
        return createFileMetaInformation(tsuid, true);
    }

    /**
     * Creates DICOM file meta information for this <i>dataset</i> with the given <i>Transfer Syntax UID
     * (0002,0010)</i>.
     *
     * @param tsuid                            <i>Transfer Syntax UID (0002,0010)</i>
     * @param includeImplementationVersionName <code>true</code> to include the optional <i>Implementation Version Name
     *                                         (0002,0013)</i>; <code>false</code> to omit it.
     * @return created DICOM file meta information
     */
    public Attributes createFileMetaInformation(String tsuid, boolean includeImplementationVersionName) {
        return createFileMetaInformation(
                getString(Tag.SOPInstanceUID, null),
                getString(Tag.SOPClassUID, null),
                tsuid,
                includeImplementationVersionName);
    }

    /**
     * Creates DICOM file meta information with the given <i>Media Storage SOP Instance UID (0002,0013)</i>, <i>Media
     * Storage SOP Class UID (0002,0012)</i>, and <i>Transfer Syntax UID (0002,0010)</i>, including the optional
     * <i>Implementation Version Name (0002,0013)</i>.
     *
     * @param iuid  <i>Media Storage SOP Instance UID (0002,0013)</i>
     * @param cuid  <i>Media Storage SOP Class UID (0002,0012)</i>
     * @param tsuid <i>Transfer Syntax UID (0002,0010)</i>
     * @return created DICOM file meta information
     */
    public static Attributes createFileMetaInformation(String iuid, String cuid, String tsuid) {
        return createFileMetaInformation(iuid, cuid, tsuid, true);
    }

    /**
     * Creates DICOM file meta information.
     *
     * @param iuid                             <i>Media Storage SOP Instance UID (0002,0013)</i>
     * @param cuid                             <i>Media Storage SOP Class UID (0002,0012)</i>
     * @param tsuid                            <i>Transfer Syntax UID (0002,0010)</i>
     * @param includeImplementationVersionName <code>true</code> to include the optional <i>Implementation Version Name
     *                                         (0002,0013)</i>; <code>false</code> to omit it.
     * @return created DICOM file meta information
     */
    public static Attributes createFileMetaInformation(
            String iuid,
            String cuid,
            String tsuid,
            boolean includeImplementationVersionName) {
        if (iuid == null || iuid.isEmpty())
            throw new IllegalArgumentException("Missing SOP Instance UID");
        if (cuid == null || cuid.isEmpty())
            throw new IllegalArgumentException("Missing SOP Class UID");
        if (tsuid == null || tsuid.isEmpty())
            throw new IllegalArgumentException("Missing Transfer Syntax UID");
        Attributes fmi = new Attributes(6);
        fmi.setBytes(Tag.FileMetaInformationVersion, VR.OB, new byte[] { 0, 1 });
        fmi.setString(Tag.MediaStorageSOPClassUID, VR.UI, cuid);
        fmi.setString(Tag.MediaStorageSOPInstanceUID, VR.UI, iuid);
        fmi.setString(Tag.TransferSyntaxUID, VR.UI, tsuid);
        fmi.setString(Tag.ImplementationClassUID, VR.UI, Implementation.getClassUID());
        if (includeImplementationVersionName)
            fmi.setString(Tag.ImplementationVersionName, VR.SH, Implementation.getVersionName());
        return fmi;
    }

    /**
     * Matches attributes
     *
     * @param keys         key attribute set
     * @param ignorePNCase whether to ignore PN case
     * @param matchNoValue whether to match no value
     * @return whether matched
     */
    public boolean matches(Attributes keys, boolean ignorePNCase, boolean matchNoValue) {
        int[] keyTags = keys.tags;
        VR[] keyVrs = keys.vrs;
        Object[] keyValues = keys.values;
        int keysSize = keys.size;
        String privateCreator = null;
        int creatorTag = 0;
        for (int i = 0; i < keysSize; i++) {
            int tag = keyTags[i];
            if (Tag.isPrivateCreator(tag))
                continue;
            if (Tag.isPrivateGroup(tag)) {
                int tmp = Tag.creatorTagOf(tag);
                if (creatorTag != tmp) {
                    creatorTag = tmp;
                    privateCreator = keys.getString(creatorTag, null);
                }
            } else {
                creatorTag = 0;
                privateCreator = null;
            }
            Object keyValue = keyValues[i];
            if (isEmpty(keyValue))
                continue;
            if (keyVrs[i].isStringType()) {
                if (!matches(
                        privateCreator,
                        tag,
                        keyVrs[i],
                        ignorePNCase,
                        matchNoValue,
                        keys.getStrings(privateCreator, tag, null)))
                    return false;
            } else if (keyValue instanceof Sequence) {
                if (!matches(privateCreator, tag, ignorePNCase, matchNoValue, (Sequence) keyValue))
                    return false;
            } else {
                Logger.info(
                        false,
                        "Image",
                        "Matching Key {} with VR: vr={} not supported",
                        Tag.toString(tag),
                        keyVrs[i]);
            }
        }
        return true;
    }

    /**
     * Matches attributes
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param vr             VR
     * @param ignorePNCase   whether to ignore PN case
     * @param matchNoValue   whether to match no value
     * @param keyVals        keyValue array
     * @return whether matched
     */
    private boolean matches(
            String privateCreator,
            int tag,
            VR vr,
            boolean ignorePNCase,
            boolean matchNoValue,
            String[] keyVals) {
        String[] vals = getStrings(privateCreator, tag, null);
        if (vals == null || vals.length == 0)
            return matchNoValue;
        boolean ignoreCase = ignorePNCase && vr == VR.PN;
        for (String keyVal : keyVals) {
            DateRange dateRange = null;
            switch (vr) {
                case PN:
                    keyVal = new PersonName(keyVals[0]).toString();
                    break;

                case DA:
                case DT:
                case TM:
                    dateRange = toDateRange(keyVal, vr);
                    break;
            }
            if (Builder.containsWildCard(keyVal)) {
                Pattern pattern = Builder.compilePattern(keyVal, ignoreCase);
                for (String val : vals) {
                    if (val == null)
                        if (matchNoValue)
                            return true;
                        else
                            continue;
                    if (vr == VR.PN)
                        val = new PersonName(val).toString();
                    if (pattern.matcher(val).matches())
                        return true;
                }
            } else {
                for (String val : vals) {
                    if (val == null)
                        if (matchNoValue)
                            return true;
                        else
                            continue;
                    if (dateRange != null)
                        if (dateRange.contains(vr.toDate(val, getTimeZone(), 0, false, null, new DatePrecision())))
                            return true;
                        else
                            continue;
                    if (vr == VR.PN)
                        val = new PersonName(val).toString();
                    if (ignoreCase ? keyVal.equalsIgnoreCase(val) : keyVal.equals(val))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Matches attributes
     *
     * @param privateCreator private creator
     * @param tag            tag
     * @param ignorePNCase   whether to ignore PN case
     * @param matchNoValue   whether to match no value
     * @param keySeq         key sequence
     * @return whether matched
     */
    private boolean matches(
            String privateCreator,
            int tag,
            boolean ignorePNCase,
            boolean matchNoValue,
            Sequence keySeq) {
        int n = keySeq.size();
        if (n > 1)
            Logger.info(
                    false,
                    "Image",
                    "Matching Key {} with VR: SQ contains {} Items - only consider first Item",
                    Tag.toString(tag),
                    n);
        Attributes keys = keySeq.get(0);
        if (keys.isEmpty())
            return true;
        Object value = getValue(privateCreator, tag);
        if (value == null || isEmpty(value))
            return matchNoValue;
        if (value instanceof Sequence) {
            Sequence sq = (Sequence) value;
            for (Attributes item : sq)
                if (item.matches(keys, ignorePNCase, matchNoValue))
                    return true;
        }
        return false;
    }

    /**
     * Writes the object
     *
     * @param out output stream
     * @throws IOException IOerror
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(size);
        ImageOutputStream dout = new ImageOutputStream(out,
                bigEndian ? UID.ExplicitVRBigEndian.uid : UID.ExplicitVRLittleEndian.uid);
        dout.writeDataset(null, this);
        dout.writeHeader(Tag.ItemDelimitationItem, null, 0);
    }

    /**
     * Reads the object
     *
     * @param in parameter
     * @throws IOException            IOerror
     * @throws ClassNotFoundException error
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init(in.readInt());
        ImageInputStream din = new ImageInputStream(in,
                bigEndian ? UID.ExplicitVRBigEndian.uid : UID.ExplicitVRLittleEndian.uid);
        din.readItemValue(this, -1);
    }

    /**
     * Validates
     *
     * @param iod IOD
     * @return validation result
     */
    public ValidationResult validate(IOD iod) {
        ValidationResult result = new ValidationResult();
        HashMap<String, Boolean> resolvedConditions = new HashMap<>();
        for (IOD.DataElement el : iod) {
            validate(el, result, resolvedConditions);
        }
        return result;
    }

    /**
     * Validates
     *
     * @param el     data element
     * @param result validation result
     */
    public void validate(IOD.DataElement el, ValidationResult result) {
        validate(el, result, null);
    }

    /**
     * Validates
     *
     * @param el                  data element
     * @param result              validation result
     * @param processedConditions processed conditions
     */
    private void validate(IOD.DataElement el, ValidationResult result, Map<String, Boolean> processedConditions) {
        IOD.Condition condition = el.getCondition();
        if (condition != null) {
            String id = condition.id();
            Boolean match = id != null ? processedConditions.get(id) : null;
            if (match == null) {
                match = condition.match(this);
                if (id != null)
                    processedConditions.put(id, match);
            }
            if (!match)
                return;
        }
        int index = indexOf(el.tag);
        if (index < 0) {
            if (el.type == IOD.DataElementType.TYPE_1 || el.type == IOD.DataElementType.TYPE_2) {
                result.addMissingAttribute(el);
            }
            return;
        }
        Object value = values[index];
        if (isEmpty(value)) {
            if (el.type == IOD.DataElementType.TYPE_1) {
                result.addMissingAttributeValue(el);
            }
            return;
        }
        if (el.type == IOD.DataElementType.TYPE_0) {
            result.addNotAllowedAttribute(el);
            return;
        }
        VR vr = vrs[index];
        if (vr.isStringType()) {
            value = decodeStringValue(index);
        }
        Object validVals = el.getValues();
        if (el.vr == VR.SQ) {
            if (!(value instanceof Sequence)) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.VR);
                return;
            }
            Sequence seq = (Sequence) value;
            int seqSize = seq.size();
            if (el.maxVM > 0 && seqSize > el.maxVM) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.MultipleItems);
                return;
            }
            if (validVals instanceof Code[]) {
                boolean invalidItem = false;
                ValidationResult[] itemValidationResults = new ValidationResult[seqSize];
                for (int i = 0; i < seqSize; i++) {
                    ValidationResult itemValidationResult = validateCode(seq.get(i), (Code[]) validVals);
                    invalidItem = invalidItem || !itemValidationResult.isValid();
                    itemValidationResults[i] = itemValidationResult;
                }
                if (invalidItem) {
                    result.addInvalidAttributeValue(el, ValidationResult.Invalid.Code, itemValidationResults, null);
                }
            } else if (validVals instanceof IOD[]) {
                IOD[] itemIODs = (IOD[]) validVals;
                int[] matchingItems = new int[itemIODs.length];
                boolean invalidItem = false;
                ValidationResult[] itemValidationResults = new ValidationResult[seqSize];
                for (int i = 0; i < seqSize; i++) {
                    ValidationResult itemValidationResult = new ValidationResult();
                    HashMap<String, Boolean> resolvedItemConditions = new HashMap<>();
                    Attributes item = seq.get(i);
                    for (int j = 0; j < itemIODs.length; j++) {
                        IOD itemIOD = itemIODs[j];
                        IOD.Condition itemCondition = itemIOD.getCondition();
                        if (itemCondition != null) {
                            String id = itemCondition.id();
                            Boolean match = id != null ? resolvedItemConditions.get(id) : null;
                            if (match == null) {
                                match = itemCondition.match(item);
                                if (id != null)
                                    resolvedItemConditions.put(id, match);
                            }
                            if (!match)
                                continue;
                        }
                        matchingItems[j]++;
                        for (IOD.DataElement itemEl : itemIOD) {
                            item.validate(itemEl, itemValidationResult, resolvedItemConditions);
                        }
                    }
                    invalidItem = invalidItem || !itemValidationResult.isValid();
                    itemValidationResults[i] = itemValidationResult;
                }
                IOD[] missingItems = checkforMissingItems(matchingItems, itemIODs);
                if (invalidItem || missingItems != null) {
                    result.addInvalidAttributeValue(
                            el,
                            ValidationResult.Invalid.Item,
                            itemValidationResults,
                            missingItems);
                }
            }
            return;
        }
        if (el.maxVM > 0 || el.minVM > 1) {
            int vm = vr.vmOf(value);
            if (el.maxVM > 0 && vm > el.maxVM || el.minVM > 1 && vm < el.minVM) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.VM);
                return;
            }
        }
        if (validVals == null)
            return;
        if (validVals instanceof String[]) {
            if (!vr.isStringType()) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.VR);
                return;
            }
            if (!isValidValue(toStrings(value), el.valueNumber, (String[]) validVals)) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.Value);
            }
        } else if (validVals instanceof int[]) {
            if (vr == VR.IS)
                value = decodeISValue(index);
            else if (!vr.isIntType()) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.VR);
                return;
            }
            if (!isValidValue(vr.toInts(value, bigEndian), el.valueNumber, (int[]) validVals)) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.Value);
            }
        }
    }

    /**
     * Checks missing items
     *
     * @param matchingItems matching items
     * @param itemIODs      item IODs
     * @return missing items
     */
    private IOD[] checkforMissingItems(int[] matchingItems, IOD[] itemIODs) {
        IOD[] missingItems = new IOD[matchingItems.length];
        int n = 0;
        for (int i = 0; i < matchingItems.length; i++) {
            IOD itemIOD = itemIODs[i];
            if (matchingItems[i] == 0 && itemIOD.getType() == IOD.DataElementType.TYPE_1)
                missingItems[n++] = itemIOD;
        }
        return n > 0 ? Arrays.copyOf(missingItems, n) : null;
    }

    /**
     * Validates the code
     *
     * @param item      item
     * @param validVals valid values
     * @return validation result
     */
    private ValidationResult validateCode(Attributes item, Code[] validVals) {
        ValidationResult result = null;
        for (Code code : validVals) {
            result = item.validate(IOD.valueOf(code));
            if (result.isValid())
                break;
        }
        return result;
    }

    /**
     * Validates whether the value is valid
     *
     * @param val         value
     * @param valueNumber value number
     * @param validVals   valid values
     * @return whether valid
     */
    private boolean isValidValue(String[] val, int valueNumber, String[] validVals) {
        if (valueNumber != 0)
            return val.length < valueNumber || isOneOf(val[valueNumber - 1], validVals);
        for (int i = 0; i < val.length; i++)
            if (!isOneOf(val[i], validVals))
                return false;
        return true;
    }

    /**
     * Whether the value is one of the allowed values
     *
     * @param val value
     * @param ss  string array
     * @return Whether the value is one of the allowed values
     */
    private <T> boolean isOneOf(Object val, T[] ss) {
        if (ss == null)
            return true;
        for (T s : ss)
            if (val.equals(s))
                return true;
        return false;
    }

    /**
     * Validates whether the value is valid
     *
     * @param val         value
     * @param valueNumber value number
     * @param validVals   valid values
     * @return whether valid
     */
    private boolean isValidValue(int[] val, int valueNumber, int[] validVals) {
        if (valueNumber != 0)
            return val.length < valueNumber || isOneOf(val[valueNumber - 1], validVals);
        for (int i = 0; i < val.length; i++)
            if (!isOneOf(val[i], validVals))
                return false;
        return true;
    }

    /**
     * Whether the value is one of the allowed values
     *
     * @param val value
     * @param is  integer array
     * @return Whether the value is one of the allowed values
     */
    private boolean isOneOf(int val, int[] is) {
        if (is == null)
            return true;
        for (int i : is)
            if (val == i)
                return true;
        return false;
    }

    /**
     * Adds attributes from this dataset that are replaced in the specified other dataset to the result dataset. If no
     * result dataset is supplied, a new one is instantiated.
     *
     * @param other  other attributes
     * @param result dataset or {@code null}
     * @return result dataset
     */
    public Attributes getModified(Attributes other, Attributes result) {
        if (result == null)
            result = new Attributes(other.size);
        int creatorTag = -1;
        int prevOtherCreatorTag = -1;
        int otherCreatorTag = -1;
        String privateCreator = null;
        for (int i = 0; i < other.size; i++) {
            int tag = other.tags[i];
            if ((tag & 0x00010000) != 0) { // private group
                if ((tag & 0x0000ff00) == 0)
                    continue; // skip private creator
                otherCreatorTag = Tag.creatorTagOf(tag);
                if (prevOtherCreatorTag != otherCreatorTag) {
                    prevOtherCreatorTag = otherCreatorTag;
                    creatorTag = -1;
                    int k = other.indexOf(otherCreatorTag);
                    if (k >= 0) {
                        Object o = other.decodeStringValue(k);
                        if (o instanceof String) {
                            privateCreator = (String) o;
                            creatorTag = creatorTagOf(privateCreator, tag, false);
                        }
                    }
                }
                if (creatorTag == -1)
                    continue; // no matching Private Creator
                tag = Tag.toPrivateTag(creatorTag, tag);
            } else {
                privateCreator = null;
            }
            int j = indexOf(tag);
            if (j < 0)
                continue;
            Object origValue = values[j];
            if (origValue instanceof Value && ((Value) origValue).isEmpty())
                continue;
            if (equalValues(other, j, i))
                continue;
            if (origValue instanceof Sequence) {
                result.set(privateCreator, tag, (Sequence) origValue, null);
            } else if (origValue instanceof Fragments) {
                result.set(privateCreator, tag, (Fragments) origValue);
            } else {
                result.set(privateCreator, tag, vrs[j], origValue);
            }
        }
        return result;
    }

    /**
     * Returns attributes from this dataset that are deleted or replaced in the specified other dataset.
     *
     * @param other other attributes
     * @return result
     */
    public Attributes getRemovedOrModified(Attributes other) {
        Attributes modified = new Attributes(size);
        int creatorTag = -1;
        int prevCreatorTag = -1;
        int otherCreatorTag = 0;
        String privateCreator = null;
        for (int i = 0; i < size; i++) {
            int tag = tags[i];
            if ((tag & 0x00010000) != 0) { // private group
                if ((tag & 0x0000ff00) == 0)
                    continue; // skip private creator
                creatorTag = Tag.creatorTagOf(tag);
                if (prevCreatorTag != creatorTag) {
                    prevCreatorTag = creatorTag;
                    otherCreatorTag = -1;
                    privateCreator = null;
                    int k = indexOf(creatorTag);
                    if (k >= 0) {
                        Object o = decodeStringValue(k);
                        if (o instanceof String) {
                            privateCreator = (String) o;
                            otherCreatorTag = other.creatorTagOf(privateCreator, tag, false);
                        }
                    }
                }
                if (privateCreator == null)
                    continue; // no Private Creator
                if (otherCreatorTag >= 0)
                    tag = Tag.toPrivateTag(otherCreatorTag, tag);
            } else {
                otherCreatorTag = 0;
                privateCreator = null;
            }
            Object origValue = values[i];
            if (origValue instanceof Value && ((Value) origValue).isEmpty())
                continue;
            if (otherCreatorTag >= 0) {
                int j = other.indexOf(tag);
                if (j >= 0 && equalValues(other, i, j))
                    continue;
            }
            if (origValue instanceof Sequence) {
                modified.set(privateCreator, tag, (Sequence) origValue, null);
            } else if (origValue instanceof Fragments) {
                modified.set(privateCreator, tag, (Fragments) origValue);
            } else {
                modified.set(privateCreator, tag, vrs[i], origValue);
            }
        }
        return modified;
    }

    /**
     * Compares the selected attributes and stores differences.
     *
     * @param other     other attribute set
     * @param selection selected tags
     * @param diff      target attributes for differences
     * @return number of differences
     */
    public int diff(Attributes other, int[] selection, Attributes diff) {
        return diff(other, selection, diff, false);
    }

    /**
     * Compares the selected attributes and stores differences.
     *
     * @param other        other attribute set
     * @param selection    selected tags
     * @param diff         target attributes for differences
     * @param onlyModified whether only modified attributes are compared
     * @return number of differences
     */
    public int diff(Attributes other, int[] selection, Attributes diff, boolean onlyModified) {
        int count = 0;
        for (int tag : selection) {
            int index = indexOf(tag);
            int otherIndex = other.indexOf(tag);
            if (!equalValues(other, index, otherIndex)) {
                if (diff != null) {
                    Object value = index < 0 ? Value.NULL : values[index];
                    if (!onlyModified || value != Value.NULL) {
                        if (value instanceof Sequence) {
                            diff.set(null, tag, (Sequence) value, null);
                        } else {
                            diff.set(tag, index < 0 ? other.vrs[otherIndex] : vrs[index], value);
                        }
                    }
                }
                count++;
            }
        }
        return count;
    }

    /**
     * Unifies character sets
     *
     * @param attrsList attribute set list
     */
    public static void unifyCharacterSets(Attributes... attrsList) {
        if (attrsList.length == 0)
            return;
        SpecificCharacterSet utf8 = SpecificCharacterSet.valueOf("ISO_IR 192");
        SpecificCharacterSet commonCS = attrsList[0].getSpecificCharacterSet();
        if (!commonCS.equals(utf8)) {
            for (int i = 1; i < attrsList.length; i++) {
                SpecificCharacterSet cs = attrsList[i].getSpecificCharacterSet();
                if (!(cs.equals(commonCS) || cs.isASCII() && commonCS.containsASCII())) {
                    if (commonCS.isASCII() && cs.containsASCII())
                        commonCS = cs;
                    else {
                        commonCS = utf8;
                        break;
                    }
                }
            }
        }
        for (Attributes attrs : attrsList) {
            SpecificCharacterSet cs = attrs.getSpecificCharacterSet();
            if (!(cs.equals(commonCS))) {
                if (!cs.isASCII() || !commonCS.containsASCII())
                    attrs.decodeStringValuesUsingSpecificCharacterSet();
                attrs.setString(Tag.SpecificCharacterSet, VR.CS, commonCS.toCodes());
            }
        }
    }

    /**
     * Removes all bulk data
     *
     * @return result
     */
    public int removeAllBulkData() {
        ensureModifiable();
        int removed = 0;
        for (int i = 0; i < size; i++) {
            Object value = values[i];
            if (isBulkData(value)) {
                int srcPos = i + 1;
                int len = size - srcPos;
                System.arraycopy(tags, srcPos, tags, i, len);
                System.arraycopy(vrs, srcPos, vrs, i, len);
                System.arraycopy(values, srcPos, values, i, len);
                i--;
                size--;
                removed++;
            } else if (value instanceof Sequence) {
                for (Attributes item : (Sequence) value) {
                    removed += item.removeAllBulkData();
                }
            }
        }
        return removed;
    }

    /**
     * Whether this is bulk data
     *
     * @param value value
     * @return Whether this is bulk data
     */
    private static boolean isBulkData(Object value) {
        return value instanceof BulkData || (value instanceof Fragments && ((Fragments) value).size() > 1
                && ((Fragments) value).get(1) instanceof BulkData);
    }

    /**
     * Gets the creator index
     *
     * @param privateCreator private creator
     * @param groupNumber    group number
     * @return creator index
     */
    private int creatorIndexOf(String privateCreator, int groupNumber) {
        if ((groupNumber & 1) == 0)
            throw new IllegalArgumentException(
                    "(" + Tag.shortToHexString(groupNumber) + ",xxxx) is not a private Group");
        int group = groupNumber << 16;
        int creatorTag = group | 0x10;
        int index = indexOf(creatorTag);
        if (index < 0)
            index = -index - 1;
        while (index < size && (tags[index] & 0xffffff00) == group) {
            if (vrs[index].isStringType()) {
                Object creatorID = decodeStringValue(index);
                if (privateCreator.equals(creatorID))
                    return index;
            }
            index++;
            creatorTag++;
        }
        return -1;
    }

    /**
     * Removes private attributes
     *
     * @param privateCreator private creator
     * @param groupNumber    group number
     * @return result
     */
    public int removePrivateAttributes(String privateCreator, int groupNumber) {
        ensureModifiable();
        int privateCreatorIndex = creatorIndexOf(privateCreator, groupNumber);
        if (privateCreatorIndex < 0)
            return 0;
        int creatorTag = tags[privateCreatorIndex];
        int privateTag = (creatorTag & 0xffff0000) | ((creatorTag & 0xff) << 8);
        int srcPos = privateCreatorIndex + 1;
        int start = srcPos;
        while (start < size && tags[start] < privateTag)
            start++;
        int end = start;
        while (end < size && (tags[end] & 0xffffff00) == privateTag)
            end++;
        int len1 = start - srcPos;
        if (len1 > 0) {
            System.arraycopy(tags, srcPos, tags, privateCreatorIndex, len1);
            System.arraycopy(vrs, srcPos, vrs, privateCreatorIndex, len1);
            System.arraycopy(values, srcPos, values, privateCreatorIndex, len1);
        }
        int len2 = size - end;
        if (len2 > 0) {
            int destPos = start - 1;
            System.arraycopy(tags, end, tags, destPos, len2);
            System.arraycopy(vrs, end, vrs, destPos, len2);
            System.arraycopy(values, end, values, destPos, len2);
        }
        int removed = end - start;
        int size1 = size - removed - 1;
        Arrays.fill(tags, size1, size, 0);
        Arrays.fill(vrs, size1, size, null);
        Arrays.fill(values, size1, size, null);
        size = size1;
        return removed;
    }

    /**
     * Removes all private attributes
     *
     * @return result
     */
    public int removePrivateAttributes() {
        ensureModifiable();
        int size1 = size;
        for (int i = 0; i < size1; i++) {
            int j = i;
            while (j < size1 && Tag.isPrivateGroup(tags[j]))
                j++;
            if (j > i) {
                int len = size1 - j;
                if (len > 0) {
                    System.arraycopy(tags, j, tags, i, len);
                    System.arraycopy(vrs, j, vrs, i, len);
                    System.arraycopy(values, j, values, i, len);
                }
                size1 -= j - i;
            }
        }
        int removed = size - size1;
        if (removed > 0) {
            Arrays.fill(tags, size1, size, 0);
            Arrays.fill(vrs, size1, size, null);
            Arrays.fill(values, size1, size, null);
            size = size1;
        }
        return removed;
    }

    /**
     * Removes selected attributes
     *
     * @param selection selected tags
     */
    public void removeSelected(int... selection) {
        ensureModifiable();
        for (int i = 0; i < size; i++) {
            if (Arrays.binarySearch(selection, tags[i]) >= 0) {
                int numMoved = size - i - 1;
                if (numMoved > 0) {
                    System.arraycopy(tags, i + 1, tags, i, numMoved);
                    System.arraycopy(vrs, i + 1, vrs, i, numMoved);
                    System.arraycopy(values, i + 1, values, i, numMoved);
                }
                values[--size] = null;
                --i;
            }
        }
    }

    /**
     * Replaces selected attributes
     *
     * @param others    other attribute set
     * @param selection selected tags
     */
    public void replaceSelected(Attributes others, int... selection) {
        ensureModifiable();
        for (int i = 0; i < size; i++) {
            if (Arrays.binarySearch(selection, tags[i]) >= 0) {
                values[i] = Builder.maskNull(others.getValue(tags[i]), Value.NULL);
            }
        }
    }

    /**
     * Replaces selected UID attributes
     *
     * @param selection selected tags
     */
    public void replaceUIDSelected(int... selection) {
        ensureModifiable();
        for (int i = 0; i < size; i++) {
            if (Arrays.binarySearch(selection, tags[i]) >= 0 && values[i] != Value.NULL) {
                values[i] = replaceUIDs(decodeStringValue(i));
            }
        }
    }

    /**
     * Replaces UID
     *
     * @param val value
     * @return result
     */
    private Object replaceUIDs(Object val) {
        if (val instanceof String) {
            return UID.remapUID((String) val);
        }
        if (val instanceof String[]) {
            String[] ss = (String[]) val;
            for (int i = 0; i < ss.length; i++) {
                ss[i] = UID.remapUID(ss[i]);
            }
        }
        return val;
    }

    /**
     * Removes curve data
     *
     * @return result
     */
    public int removeCurveData() {
        ensureModifiable();
        return removeRepeatingGroup(0x50000000);
    }

    /**
     * Removes overlay data
     *
     * @return result
     */
    public int removeOverlayData() {
        ensureModifiable();
        return removeRepeatingGroup(0x60000000);
    }

    /**
     * Removes repeating groups
     *
     * @param ggxxxxxx group number
     * @return result
     */
    private int removeRepeatingGroup(int ggxxxxxx) {
        int size1 = size;
        int i = indexForInsertOf(ggxxxxxx);
        if (i < 0)
            i = -i - 1;
        while (i < size1 && (tags[i] & 0xFFE00000) == ggxxxxxx) {
            int j = i;
            while (j < size1 && (tags[j] & 0xFFE10000) == ggxxxxxx)
                j++;
            if (j > i) {
                int len = size1 - j;
                if (len > 0) {
                    System.arraycopy(tags, j, tags, i, len);
                    System.arraycopy(vrs, j, vrs, i, len);
                    System.arraycopy(values, j, values, i, len);
                }
                size1 -= j - i;
            }
            i++;
        }
        int removed = size - size1;
        if (removed > 0) {
            Arrays.fill(tags, size1, size, 0);
            Arrays.fill(vrs, size1, size, null);
            Arrays.fill(values, size1, size, null);
            size = size1;
        }
        return removed;
    }

}
