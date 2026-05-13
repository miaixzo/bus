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
package org.miaixz.bus.image.nimble.codec;

import java.io.Serial;
import java.io.Serializable;
import java.util.EnumSet;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.metric.Property;
import org.miaixz.bus.image.nimble.Photometric;

/**
 * Represents the CompressionRule type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CompressionRule implements Comparable<CompressionRule>, Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852288068061L;

    /**
     * The common name value.
     */
    private final String commonName;

    /**
     * The condition value.
     */
    private final Condition condition;

    /**
     * The tsuid value.
     */
    private final String tsuid;

    /**
     * The image write params value.
     */
    private final Property[] imageWriteParams;

    /**
     * Creates a new instance.
     *
     * @param commonName          the common name.
     * @param pmis                the pmis.
     * @param bitsStored          the bits stored.
     * @param pixelRepresentation the pixel representation.
     * @param aeTitles            the ae titles.
     * @param sopClasses          the sop classes.
     * @param bodyPartExamined    the body part examined.
     * @param tsuid               the tsuid.
     * @param params              the params.
     */
    public CompressionRule(String commonName, String[] pmis, int[] bitsStored, int pixelRepresentation,
            String[] aeTitles, String[] sopClasses, String[] bodyPartExamined, String tsuid, String... params) {
        this.commonName = commonName;
        this.condition = new Condition(pmis, bitsStored, pixelRepresentation, Builder.maskNull(aeTitles),
                Builder.maskNull(sopClasses), Builder.maskNull(bodyPartExamined));
        this.tsuid = tsuid;
        this.imageWriteParams = Property.valueOf(params);
    }

    /**
     * Gets the common name.
     *
     * @return the common name.
     */
    public final String getCommonName() {
        return commonName;
    }

    /**
     * Gets the photometric interpretations.
     *
     * @return the photometric interpretations.
     */
    public Photometric[] getPhotometricInterpretations() {
        return condition.getPhotometricInterpretations();
    }

    /**
     * Gets the bits stored.
     *
     * @return the bits stored.
     */
    public int[] getBitsStored() {
        return condition.getBitsStored();
    }

    /**
     * Gets the pixel representation.
     *
     * @return the pixel representation.
     */
    public final int getPixelRepresentation() {
        return condition.pixelRepresentation;
    }

    /**
     * Gets the ae titles.
     *
     * @return the ae titles.
     */
    public final String[] getAETitles() {
        return condition.aeTitles;
    }

    /**
     * Gets the sop classes.
     *
     * @return the sop classes.
     */
    public final String[] getSOPClasses() {
        return condition.sopClasses;
    }

    /**
     * Gets the body part examined.
     *
     * @return the body part examined.
     */
    public final String[] getBodyPartExamined() {
        return condition.bodyPartExamined;
    }

    /**
     * Gets the transfer syntax.
     *
     * @return the transfer syntax.
     */
    public final String getTransferSyntax() {
        return tsuid;
    }

    /**
     * Gets the image write params.
     *
     * @return the image write params.
     */
    public Property[] getImageWriteParams() {
        return imageWriteParams;
    }

    /**
     * Determines whether condition.
     *
     * @param aeTitle         the ae title.
     * @param imageDescriptor the image descriptor.
     * @return true if the condition is met; otherwise false.
     */
    public boolean matchesCondition(String aeTitle, ImageDescriptor imageDescriptor) {
        return condition.matches(aeTitle, imageDescriptor);
    }

    /**
     * Executes the compare to operation.
     *
     * @param o the o.
     * @return the operation result.
     */
    @Override
    public int compareTo(CompressionRule o) {
        return condition.compareTo(o.condition);
    }

    /**
     * Represents the Condition type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static class Condition implements Comparable<Condition>, Serializable {

        /**
         * The serial version uid value.
         */
        @Serial
        private static final long serialVersionUID = 2852288131317L;

        /**
         * The pmis value.
         */
        final EnumSet<Photometric> pmis;

        /**
         * The bits stored mask value.
         */
        final int bitsStoredMask;

        /**
         * The pixel representation value.
         */
        final int pixelRepresentation = -1;

        /**
         * The ae titles value.
         */
        final String[] aeTitles;

        /**
         * The sop classes value.
         */
        final String[] sopClasses;

        /**
         * The body part examined value.
         */
        final String[] bodyPartExamined;

        /**
         * The weight value.
         */
        final int weight;

        /**
         * Creates a new instance.
         *
         * @param pmis                the pmis.
         * @param bitsStored          the bits stored.
         * @param pixelRepresentation the pixel representation.
         * @param aeTitles            the ae titles.
         * @param sopClasses          the sop classes.
         * @param bodyPartExamined    the body part examined.
         */
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

        /**
         * Determines whether empty or contains.
         *
         * @param a the a.
         * @param o the o.
         * @return true if the condition is met; otherwise false.
         */
        private static boolean isEmptyOrContains(Object[] a, Object o) {
            if (o == null || a.length == 0)
                return true;

            for (int i = 0; i < a.length; i++)
                if (o.equals(a[i]))
                    return true;

            return false;
        }

        /**
         * Converts this value to bits stored mask.
         *
         * @param bitsStored the bits stored.
         * @return the operation result.
         */
        private int toBitsStoredMask(int[] bitsStored) {
            int mask = 0;
            for (int i : bitsStored)
                mask |= 1 << i;

            return mask;
        }

        /**
         * Gets the photometric interpretations.
         *
         * @return the photometric interpretations.
         */
        Photometric[] getPhotometricInterpretations() {
            return pmis.toArray(new Photometric[pmis.size()]);
        }

        /**
         * Gets the bits stored.
         *
         * @return the bits stored.
         */
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

        /**
         * Executes the compare to operation.
         *
         * @param o the o.
         * @return the operation result.
         */
        @Override
        public int compareTo(Condition o) {
            return o.weight - weight;
        }

        /**
         * Executes the matches operation.
         *
         * @param aeTitle         the ae title.
         * @param imageDescriptor the image descriptor.
         * @return true if the condition is met; otherwise false.
         */
        public boolean matches(String aeTitle, ImageDescriptor imageDescriptor) {
            return pmis.contains(imageDescriptor.getPhotometricInterpretation())
                    && matchBitStored(imageDescriptor.getBitsStored())
                    && matchPixelRepresentation(imageDescriptor.getPixelRepresentation())
                    && isEmptyOrContains(this.aeTitles, aeTitle)
                    && isEmptyOrContains(this.sopClasses, imageDescriptor.getSopClassUID())
                    && isEmptyOrContains(this.bodyPartExamined, imageDescriptor.getBodyPartExamined());
        }

        /**
         * Executes the match pixel representation operation.
         *
         * @param pixelRepresentation the pixel representation.
         * @return true if the condition is met; otherwise false.
         */
        private boolean matchPixelRepresentation(int pixelRepresentation) {
            return this.pixelRepresentation == -1 || this.pixelRepresentation == pixelRepresentation;
        }

        /**
         * Executes the match bit stored operation.
         *
         * @param bitsStored the bits stored.
         * @return true if the condition is met; otherwise false.
         */
        private boolean matchBitStored(int bitsStored) {
            return ((1 << bitsStored) & bitsStoredMask) != 0;
        }

    }

}
