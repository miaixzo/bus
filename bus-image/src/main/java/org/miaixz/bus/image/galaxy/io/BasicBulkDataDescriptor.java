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
package org.miaixz.bus.image.galaxy.io;

import java.util.*;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.AttributeSelector;
import org.miaixz.bus.image.galaxy.data.ItemPointer;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * Basic implementation of {@link BulkDataDescriptor} that allows configuring which attributes should be treated as bulk
 * data. It supports selection by attribute tag, tag path, and value length thresholds for specific Value
 * Representations (VRs).
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class BasicBulkDataDescriptor implements BulkDataDescriptor {

    /**
     * A list of {@link AttributeSelector} objects used to identify attributes that should be treated as bulk data.
     */
    private final List<AttributeSelector> selectors = new ArrayList<>();
    /**
     * A map storing length thresholds for different Value Representations (VRs). If an attribute's value length exceeds
     * the threshold for its VR, it will be considered bulk data.
     */
    private final EnumMap<VR, Integer> lengthsThresholdByVR = new EnumMap<>(VR.class);
    /**
     * An optional ID for this bulk data descriptor.
     */
    private String bulkDataDescriptorID;
    /**
     * A flag indicating whether default bulk data attributes should be excluded.
     */
    private boolean excludeDefaults;

    /**
     * Constructs a new {@code BasicBulkDataDescriptor} with default settings.
     */
    public BasicBulkDataDescriptor() {
    }

    /**
     * Constructs a new {@code BasicBulkDataDescriptor} with the specified bulk data descriptor ID.
     * 
     * @param bulkDataDescriptorID The ID for this bulk data descriptor.
     */
    public BasicBulkDataDescriptor(String bulkDataDescriptorID) {
        this.bulkDataDescriptorID = bulkDataDescriptorID;
    }

    /**
     * Converts an array of tag paths into a list of {@link ItemPointer} objects.
     * 
     * @param tagPaths An array of integers representing the tag path.
     * @return A list of {@link ItemPointer} objects.
     */
    private static List<ItemPointer> toItemPointers(int[] tagPaths) {
        int level = tagPaths.length - 1;
        if (level == 0)
            return Collections.emptyList();

        List<ItemPointer> itemPointers = new ArrayList<>(level);
        for (int i = 0; i < level; i++) {
            itemPointers.add(new ItemPointer(tagPaths[i]));

        }
        return itemPointers;
    }

    /**
     * Checks if a given tag, potentially within a sequence specified by item pointers, corresponds to a standard DICOM
     * bulk data element.
     * 
     * @param itemPointer A list of {@link ItemPointer} objects indicating the path to the attribute.
     * @param tag         The DICOM tag of the attribute.
     * @return {@code true} if the attribute is a standard bulk data element, {@code false} otherwise.
     */
    static boolean isStandardBulkData(List<ItemPointer> itemPointer, int tag) {
        switch (Tag.normalizeRepeatingGroup(tag)) {
            case Tag.PixelDataProviderURL:
            case Tag.AudioSampleData:
            case Tag.CurveData:
            case Tag.SpectroscopyData:
            case Tag.RedPaletteColorLookupTableData:
            case Tag.GreenPaletteColorLookupTableData:
            case Tag.BluePaletteColorLookupTableData:
            case Tag.AlphaPaletteColorLookupTableData:
            case Tag.LargeRedPaletteColorLookupTableData:
            case Tag.LargeGreenPaletteColorLookupTableData:
            case Tag.LargeBluePaletteColorLookupTableData:
            case Tag.SegmentedRedPaletteColorLookupTableData:
            case Tag.SegmentedGreenPaletteColorLookupTableData:
            case Tag.SegmentedBluePaletteColorLookupTableData:
            case Tag.SegmentedAlphaPaletteColorLookupTableData:
            case Tag.OverlayData:
            case Tag.EncapsulatedDocument:
            case Tag.FloatPixelData:
            case Tag.DoubleFloatPixelData:
            case Tag.PixelData:
                return itemPointer.isEmpty();

            case Tag.WaveformData:
                return itemPointer.size() == 1 && itemPointer.get(0).sequenceTag == Tag.WaveformSequence;
        }
        return false;
    }

    /**
     * Checks if the given length exceeds the specified length threshold.
     * 
     * @param length          The length to check.
     * @param lengthThreshold The threshold to compare against.
     * @return {@code true} if the length exceeds the threshold, {@code false} otherwise.
     */
    private static boolean exeeds(int length, Integer lengthThreshold) {
        return lengthThreshold != null && length > lengthThreshold;
    }

    /**
     * Returns the bulk data descriptor ID.
     * 
     * @return The bulk data descriptor ID.
     */
    public String getBulkDataDescriptorID() {
        return bulkDataDescriptorID;
    }

    /**
     * Sets the bulk data descriptor ID.
     * 
     * @param bulkDataDescriptorID The ID to set.
     */
    public void setBulkDataDescriptorID(String bulkDataDescriptorID) {
        this.bulkDataDescriptorID = bulkDataDescriptorID;
    }

    /**
     * Checks if default bulk data attributes are excluded.
     * 
     * @return {@code true} if default bulk data attributes are excluded, {@code false} otherwise.
     */
    public boolean isExcludeDefaults() {
        return excludeDefaults;
    }

    /**
     * Excludes default bulk data attributes.
     * 
     * @return This {@code BasicBulkDataDescriptor} instance.
     */
    public BasicBulkDataDescriptor excludeDefaults() {
        return excludeDefaults(true);
    }

    /**
     * Sets whether to exclude default bulk data attributes.
     * 
     * @param excludeDefaults {@code true} to exclude defaults, {@code false} otherwise.
     * @return This {@code BasicBulkDataDescriptor} instance.
     */
    public BasicBulkDataDescriptor excludeDefaults(boolean excludeDefaults) {
        this.excludeDefaults = excludeDefaults;
        return this;
    }

    /**
     * Adds one or more {@link AttributeSelector}s to this descriptor. These selectors define which attributes should be
     * considered bulk data.
     * 
     * @param selectors An array of {@link AttributeSelector} objects.
     * @return This {@code BasicBulkDataDescriptor} instance.
     */
    public BasicBulkDataDescriptor addAttributeSelector(AttributeSelector... selectors) {
        for (AttributeSelector selector : selectors) {
            this.selectors.add(Objects.requireNonNull(selector));
        }
        return this;
    }

    /**
     * Returns all configured {@link AttributeSelector}s.
     * 
     * @return An array of {@link AttributeSelector} objects.
     */
    public AttributeSelector[] getAttributeSelectors() {
        return selectors.toArray(new AttributeSelector[0]);
    }

    /**
     * Sets the {@link AttributeSelector}s for this descriptor from an array of their string representations.
     * 
     * @param ss An array of string representations of {@link AttributeSelector}s.
     * @throws IllegalArgumentException if any string cannot be parsed into a valid {@link AttributeSelector}.
     */
    public void setAttributeSelectorsFromStrings(String[] ss) {
        List<AttributeSelector> tmp = new ArrayList<>(ss.length);
        for (String s : ss) {
            tmp.add(AttributeSelector.valueOf(s));
        }
        selectors.clear();
        selectors.addAll(tmp);
    }

    /**
     * Adds one or more DICOM tags to be considered bulk data. These tags will be wrapped in {@link AttributeSelector}s.
     * 
     * @param tags An array of DICOM tags.
     * @return This {@code BasicBulkDataDescriptor} instance.
     */
    public BasicBulkDataDescriptor addTag(int... tags) {
        for (int tag : tags) {
            this.selectors.add(new AttributeSelector(tag));
        }
        return this;
    }

    /**
     * Adds a DICOM tag path to be considered bulk data. The last tag in the path is the target attribute, and the
     * preceding tags define the sequence path.
     * 
     * @param tagPaths An array of integers representing the tag path.
     * @return This {@code BasicBulkDataDescriptor} instance.
     * @throws IllegalArgumentException if {@code tagPaths} is empty.
     */
    public BasicBulkDataDescriptor addTagPath(int... tagPaths) {
        if (tagPaths.length == 0)
            throw new IllegalArgumentException("tagPaths.length == 0");
        this.selectors.add(new AttributeSelector(tagPaths[tagPaths.length - 1], null, toItemPointers(tagPaths)));
        return this;
    }

    /**
     * Adds length thresholds for specified Value Representations (VRs). If an attribute's value length exceeds the
     * threshold for its VR, it will be considered bulk data.
     * 
     * @param threshold The length threshold.
     * @param vrs       An array of {@link VR}s to apply the threshold to.
     * @return This {@code BasicBulkDataDescriptor} instance.
     * @throws IllegalArgumentException if no VRs are provided.
     */
    public BasicBulkDataDescriptor addLengthsThreshold(int threshold, VR... vrs) {
        if (vrs.length == 0)
            throw new IllegalArgumentException("Missing VR");

        for (VR vr : vrs) {
            lengthsThresholdByVR.put(vr, threshold);
        }
        return this;
    }

    /**
     * Returns the configured length thresholds as an array of strings. Each string represents a VR or a comma-separated
     * list of VRs with their associated threshold, e.g., "OB,OW=1024".
     * 
     * @return An array of strings representing the length thresholds.
     */
    public String[] getLengthsThresholdsAsStrings() {
        if (lengthsThresholdByVR.isEmpty())
            return Normal.EMPTY_STRING_ARRAY;

        Map<Integer, EnumSet<VR>> vrsByLength = new HashMap<>();
        for (Map.Entry<VR, Integer> entry : lengthsThresholdByVR.entrySet()) {
            EnumSet<VR> vrs = vrsByLength.get(entry.getValue());
            if (vrs == null)
                vrsByLength.put(entry.getValue(), vrs = EnumSet.noneOf(VR.class));
            vrs.add(entry.getKey());
        }
        String[] ss = new String[vrsByLength.size()];
        int i = 0;
        for (Map.Entry<Integer, EnumSet<VR>> entry : vrsByLength.entrySet()) {
            StringBuilder sb = new StringBuilder();
            Iterator<VR> vr = entry.getValue().iterator();
            sb.append(vr.next());
            while (vr.hasNext())
                sb.append(',').append(vr.next());
            ss[i] = sb.append('=').append(entry.getKey()).toString();
            i++;
        }
        return ss;
    }

    /**
     * Sets the length thresholds for this descriptor from an array of strings. Each string should be in the format
     * "VR1,VR2=threshold", where VR1, VR2 are Value Representations and threshold is an integer.
     * 
     * @param ss An array of strings representing the length thresholds.
     * @throws IllegalArgumentException if any string cannot be parsed into a valid VR and threshold.
     */
    public void setLengthsThresholdsFromStrings(String... ss) {
        EnumMap<VR, Integer> tmp = new EnumMap<>(VR.class);
        for (String s : ss) {
            String[] entry = Builder.split(s, '=');
            if (entry.length != 2)
                throw new IllegalArgumentException(s);
            try {
                Integer length = Integer.valueOf(entry[1]);
                for (String vr : Builder.split(entry[0], ',')) {
                    tmp.put(VR.valueOf(vr), length);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(s);
            }
        }
        lengthsThresholdByVR.clear();
        lengthsThresholdByVR.putAll(tmp);
    }

    /**
     * Determines if a given attribute should be treated as bulk data based on the configured selectors and thresholds.
     * 
     * @param itemPointers   A list of {@link ItemPointer} objects indicating the path to the attribute.
     * @param privateCreator The private creator of the attribute, or {@code null}.
     * @param tag            The DICOM tag of the attribute.
     * @param vr             The Value Representation (VR) of the attribute.
     * @param length         The value length of the attribute.
     * @return {@code true} if the attribute is considered bulk data, {@code false} otherwise.
     */
    @Override
    public boolean isBulkData(List<ItemPointer> itemPointers, String privateCreator, int tag, VR vr, int length) {
        return !excludeDefaults && isStandardBulkData(itemPointers, tag) || selected(itemPointers, privateCreator, tag)
                || exeeds(length, lengthsThresholdByVR.get(vr));
    }

    /**
     * Checks if the attribute specified by the item pointers, private creator, and tag matches any of the configured
     * {@link AttributeSelector}s.
     * 
     * @param itemPointers   A list of {@link ItemPointer} objects indicating the path to the attribute.
     * @param privateCreator The private creator of the attribute, or {@code null}.
     * @param tag            The DICOM tag of the attribute.
     * @return {@code true} if the attribute matches a selector, {@code false} otherwise.
     */
    private boolean selected(List<ItemPointer> itemPointers, String privateCreator, int tag) {
        for (AttributeSelector selector : selectors) {
            if (selector.matches(itemPointers, privateCreator, tag))
                return true;
        }
        return false;
    }

}
