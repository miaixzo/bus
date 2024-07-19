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
package org.miaixz.bus.image.nimble.codec;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.metric.Property;
import org.miaixz.bus.image.nimble.Photometric;

import java.io.Serializable;
import java.util.EnumSet;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class CompressionRule implements Comparable<CompressionRule>, Serializable {

    private static final long serialVersionUID = -1L;

    private final String commonName;
    private final Condition condition;
    private final String tsuid;
    private final Property[] imageWriteParams;

    public CompressionRule(String commonName, String[] pmis, int[] bitsStored,
                           int pixelRepresentation, String[] aeTitles, String[] sopClasses,
                           String[] bodyPartExamined, String tsuid, String... params) {
        this.commonName = commonName;
        this.condition = new Condition(pmis, bitsStored, pixelRepresentation,
                Builder.maskNull(aeTitles),
                Builder.maskNull(sopClasses),
                Builder.maskNull(bodyPartExamined));
        this.tsuid = tsuid;
        this.imageWriteParams = Property.valueOf(params);
    }

    public final String getCommonName() {
        return commonName;
    }

    public Photometric[] getPhotometricInterpretations() {
        return condition.getPhotometricInterpretations();
    }

    public int[] getBitsStored() {
        return condition.getBitsStored();
    }

    public final int getPixelRepresentation() {
        return condition.pixelRepresentation;
    }

    public final String[] getAETitles() {
        return condition.aeTitles;
    }

    public final String[] getSOPClasses() {
        return condition.sopClasses;
    }

    public final String[] getBodyPartExamined() {
        return condition.bodyPartExamined;
    }

    public final String getTransferSyntax() {
        return tsuid;
    }

    public Property[] getImageWriteParams() {
        return imageWriteParams;
    }

    public boolean matchesCondition(String aeTitle, ImageDescriptor imageDescriptor) {
        return condition.matches(aeTitle, imageDescriptor);
    }

    @Override
    public int compareTo(CompressionRule o) {
        return condition.compareTo(o.condition);
    }

    private static class Condition
            implements Comparable<Condition>, Serializable {

        private static final long serialVersionUID = -4069284624944470710L;

        final EnumSet<Photometric> pmis;
        final int bitsStoredMask;
        final int pixelRepresentation = -1;
        final String[] aeTitles;
        final String[] sopClasses;
        final String[] bodyPartExamined;
        final int weight;

        Condition(String[] pmis, int[] bitsStored, int pixelRepresentation,
                  String[] aeTitles, String[] sopClasses, String[] bodyPartExamined) {
            this.pmis = EnumSet.noneOf(Photometric.class);
            for (String pmi : pmis)
                this.pmis.add(Photometric.fromString(pmi));

            this.bitsStoredMask = toBitsStoredMask(bitsStored);
            this.aeTitles = aeTitles;
            this.sopClasses = sopClasses;
            this.bodyPartExamined = bodyPartExamined;
            this.weight = (aeTitles.length != 0 ? 4 : 0)
                    + (sopClasses.length != 0 ? 2 : 0)
                    + (bodyPartExamined.length != 0 ? 1 : 0);
        }

        private static boolean isEmptyOrContains(Object[] a, Object o) {
            if (o == null || a.length == 0)
                return true;

            for (int i = 0; i < a.length; i++)
                if (o.equals(a[i]))
                    return true;

            return false;
        }

        private int toBitsStoredMask(int[] bitsStored) {
            int mask = 0;
            for (int i : bitsStored)
                mask |= 1 << i;

            return mask;
        }

        Photometric[] getPhotometricInterpretations() {
            return pmis.toArray(new Photometric[pmis.size()]);
        }

        int[] getBitsStored() {
            int n = 0;
            for (int i = 8; i <= 16; i++)
                if (matchBitStored(i))
                    n++;

            int[] bitsStored = new int[n];
            for (int i = 8, j = 0; i <= 16; i++)
                if (matchBitStored(i))
                    bitsStored[j++] = i;

            return bitsStored;
        }

        @Override
        public int compareTo(Condition o) {
            return o.weight - weight;
        }

        public boolean matches(String aeTitle, ImageDescriptor imageDescriptor) {
            return pmis.contains(imageDescriptor.getPhotometricInterpretation())
                    && matchBitStored(imageDescriptor.getBitsStored())
                    && matchPixelRepresentation(imageDescriptor.getPixelRepresentation())
                    && isEmptyOrContains(this.aeTitles, aeTitle)
                    && isEmptyOrContains(this.sopClasses, imageDescriptor.getSopClassUID())
                    && isEmptyOrContains(this.bodyPartExamined, imageDescriptor.getBodyPartExamined());
        }

        private boolean matchPixelRepresentation(int pixelRepresentation) {
            return this.pixelRepresentation == -1
                    || this.pixelRepresentation == pixelRepresentation;
        }

        private boolean matchBitStored(int bitsStored) {
            return ((1 << bitsStored) & bitsStoredMask) != 0;
        }
    }

}
