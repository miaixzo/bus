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
 * @author Kimi Liu
 * @since Java 21+
 */
class SOFSegment {

    private final boolean jfif;
    private final int marker;
    private final int samplePrecision;
    private final int lines; // height
    private final int samplesPerLine; // width
    private final int components;

    SOFSegment(boolean jfif, int marker, int samplePrecision, int lines, int samplesPerLine, int components) {
        this.jfif = jfif;
        this.marker = marker;
        this.samplePrecision = samplePrecision;
        this.lines = lines;
        this.samplesPerLine = samplesPerLine;
        this.components = components;
    }

    public boolean isJFIF() {
        return jfif;
    }

    public int getMarker() {
        return marker;
    }

    public int getSamplePrecision() {
        return samplePrecision;
    }

    public int getLines() {
        return lines;
    }

    public int getSamplesPerLine() {
        return samplesPerLine;
    }

    public int getComponents() {
        return components;
    }

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
