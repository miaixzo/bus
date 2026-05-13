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
package org.miaixz.bus.image.nimble.opencv;

/**
 * Represents the SOFSegment type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
class SOFSegment {

    /**
     * The jfif value.
     */
    private final boolean jfif;

    /**
     * The marker value.
     */
    private final int marker;

    /**
     * The sample precision value.
     */
    private final int samplePrecision;

    /**
     * The lines value.
     */
    private final int lines; // height
    /**
     * The samples per line value.
     */
    private final int samplesPerLine; // width
    /**
     * The components value.
     */
    private final int components;

    /**
     * Creates a new instance.
     *
     * @param jfif            the jfif.
     * @param marker          the marker.
     * @param samplePrecision the sample precision.
     * @param lines           the lines.
     * @param samplesPerLine  the samples per line.
     * @param components      the components.
     */
    SOFSegment(boolean jfif, int marker, int samplePrecision, int lines, int samplesPerLine, int components) {
        this.jfif = jfif;
        this.marker = marker;
        this.samplePrecision = samplePrecision;
        this.lines = lines;
        this.samplesPerLine = samplesPerLine;
        this.components = components;
    }

    /**
     * Determines whether jfif.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isJFIF() {
        return jfif;
    }

    /**
     * Gets the marker.
     *
     * @return the marker.
     */
    public int getMarker() {
        return marker;
    }

    /**
     * Gets the sample precision.
     *
     * @return the sample precision.
     */
    public int getSamplePrecision() {
        return samplePrecision;
    }

    /**
     * Gets the lines.
     *
     * @return the lines.
     */
    public int getLines() {
        return lines;
    }

    /**
     * Gets the samples per line.
     *
     * @return the samples per line.
     */
    public int getSamplesPerLine() {
        return samplesPerLine;
    }

    /**
     * Gets the components.
     *
     * @return the components.
     */
    public int getComponents() {
        return components;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return String.format(
                "SOF%d[%04x, precision: %d, lines: %d, samples/line: %d]",
                marker & 0xff - 0xc0,
                marker,
                samplePrecision,
                lines,
                samplesPerLine);
    }

}
