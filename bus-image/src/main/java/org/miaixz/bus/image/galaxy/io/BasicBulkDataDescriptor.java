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
package org.miaixz.bus.image.galaxy.io;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.Property;
import org.miaixz.bus.image.galaxy.data.AttributesSelector;
import org.miaixz.bus.image.galaxy.data.ItemPointer;
import org.miaixz.bus.image.galaxy.data.VR;

import java.util.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class BasicBulkDataDescriptor implements BulkDataDescriptor {

    private final List<AttributesSelector> selectors = new ArrayList<>();
    private final EnumMap<VR, Integer> lengthsThresholdByVR = new EnumMap<>(VR.class);
    private String bulkDataDescriptorID;
    private boolean excludeDefaults;

    public BasicBulkDataDescriptor() {
    }

    public BasicBulkDataDescriptor(String bulkDataDescriptorID) {
        this.bulkDataDescriptorID = bulkDataDescriptorID;
    }

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
                return itemPointer.size() == 1
                        && itemPointer.get(0).sequenceTag == Tag.WaveformSequence;
        }
        return false;
    }

    private static boolean exeeds(int length, Integer lengthThreshold) {
        return null != lengthThreshold && length > lengthThreshold;
    }

    public String getBulkDataDescriptorID() {
        return bulkDataDescriptorID;
    }

    public void setBulkDataDescriptorID(String bulkDataDescriptorID) {
        this.bulkDataDescriptorID = bulkDataDescriptorID;
    }

    public boolean isExcludeDefaults() {
        return excludeDefaults;
    }

    public BasicBulkDataDescriptor excludeDefaults() {
        return excludeDefaults(true);
    }

    public BasicBulkDataDescriptor excludeDefaults(boolean excludeDefaults) {
        this.excludeDefaults = excludeDefaults;
        return this;
    }

    public BasicBulkDataDescriptor addAttributeSelector(AttributesSelector... selectors) {
        for (AttributesSelector selector : selectors) {
            this.selectors.add(Objects.requireNonNull(selector));
        }
        return this;
    }

    public AttributesSelector[] getAttributeSelectors() {
        return selectors.toArray(new AttributesSelector[0]);
    }

    public void setAttributeSelectorsFromStrings(String[] ss) {
        List<AttributesSelector> tmp = new ArrayList<>(ss.length);
        for (String s : ss) {
            tmp.add(AttributesSelector.valueOf(s));
        }
        selectors.clear();
        selectors.addAll(tmp);
    }

    public BasicBulkDataDescriptor addTag(int... tags) {
        for (int tag : tags) {
            this.selectors.add(new AttributesSelector(tag));
        }
        return this;
    }

    public BasicBulkDataDescriptor addTagPath(int... tagPaths) {
        if (tagPaths.length == 0)
            throw new IllegalArgumentException("tagPaths.length == 0");
        this.selectors.add(
                new AttributesSelector(tagPaths[tagPaths.length - 1], null, toItemPointers(tagPaths)));
        return this;
    }

    public BasicBulkDataDescriptor addLengthsThreshold(int threshold, VR... vrs) {
        if (vrs.length == 0)
            throw new IllegalArgumentException("Missing VR");

        for (VR vr : vrs) {
            lengthsThresholdByVR.put(vr, threshold);
        }
        return this;
    }

    public String[] getLengthsThresholdsAsStrings() {
        if (lengthsThresholdByVR.isEmpty())
            return Normal.EMPTY_STRING_ARRAY;

        Map<Integer, EnumSet<VR>> vrsByLength = new HashMap<>();
        for (Map.Entry<VR, Integer> entry : lengthsThresholdByVR.entrySet()) {
            EnumSet<VR> vrs = vrsByLength.get(entry.getValue());
            if (null == vrs)
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
                sb.append(Symbol.C_COMMA).append(vr.next());
            ss[i] = sb.append(Symbol.C_EQUAL).append(entry.getKey()).toString();
        }
        return ss;
    }

    public void setLengthsThresholdsFromStrings(String... ss) {
        EnumMap<VR, Integer> tmp = new EnumMap<>(VR.class);
        for (String s : ss) {
            String[] entry = Property.split(s, Symbol.C_EQUAL);
            if (entry.length != 2)
                throw new IllegalArgumentException(s);
            try {
                Integer length = Integer.valueOf(entry[1]);
                for (String vr : Property.split(entry[0], Symbol.C_COMMA)) {
                    tmp.put(VR.valueOf(vr), length);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(s);
            }
        }
        lengthsThresholdByVR.clear();
        lengthsThresholdByVR.putAll(tmp);
    }

    @Override
    public boolean isBulkData(List<ItemPointer> itemPointers, String privateCreator, int tag, VR vr, int length) {
        return !excludeDefaults && isStandardBulkData(itemPointers, tag)
                || selected(itemPointers, privateCreator, tag)
                || exeeds(length, lengthsThresholdByVR.get(vr));
    }

    private boolean selected(List<ItemPointer> itemPointers, String privateCreator, int tag) {
        for (AttributesSelector selector : selectors) {
            if (selector.matches(itemPointers, privateCreator, tag))
                return true;
        }
        return false;
    }

}
