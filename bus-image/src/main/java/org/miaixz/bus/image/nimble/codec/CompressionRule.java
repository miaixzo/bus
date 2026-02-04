/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.nimble.codec;

import java.io.Serial;
import java.io.Serializable;
import java.util.EnumSet;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.metric.Property;
import org.miaixz.bus.image.nimble.Photometric;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class CompressionRule implements Comparable<CompressionRule>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852288068061L;

    private final String commonName;
    private final Condition condition;
    private final String tsuid;
    private final Property[] imageWriteParams;

    public CompressionRule(String commonName, String[] pmis, int[] bitsStored, int pixelRepresentation,
            String[] aeTitles, String[] sopClasses, String[] bodyPartExamined, String tsuid, String... params) {
        this.commonName = commonName;
        this.condition = new Condition(pmis, bitsStored, pixelRepresentation, Builder.maskNull(aeTitles),
                Builder.maskNull(sopClasses), Builder.maskNull(bodyPartExamined));
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

    private static class Condition implements Comparable<Condition>, Serializable {

        @Serial
        private static final long serialVersionUID = 2852288131317L;

        final EnumSet<Photometric> pmis;
        final int bitsStoredMask;
        final int pixelRepresentation = -1;
        final String[] aeTitles;
        final String[] sopClasses;
        final String[] bodyPartExamined;
        final int weight;

        Condition(String[] pmis, int[] bitsStored, int pixelRepresentation, String[] aeTitles, String[] sopClasses,
                String[] bodyPartExamined) {
            this.pmis = EnumSet.noneOf(Photometric.class);
            for (String pmi : pmis)
                this.pmis.add(Photometric.fromString(pmi));

            this.bitsStoredMask = toBitsStoredMask(bitsStored);
            this.aeTitles = aeTitles;
            this.sopClasses = sopClasses;
            this.bodyPartExamined = bodyPartExamined;
            this.weight = (aeTitles.length != 0 ? 4 : 0) + (sopClasses.length != 0 ? 2 : 0)
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
            return this.pixelRepresentation == -1 || this.pixelRepresentation == pixelRepresentation;
        }

        private boolean matchBitStored(int bitsStored) {
            return ((1 << bitsStored) & bitsStoredMask) != 0;
        }
    }

}
