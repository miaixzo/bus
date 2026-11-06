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
package org.miaixz.bus.opencv.core;

/**
 * A detected image feature with its location, scale, orientation, response, octave, and class identifier.
 */
public class KeyPoint {

    /**
     * Image coordinates of the detected feature.
     */
    public Point pt;
    /**
     * Feature diameter in pixels.
     */
    public float size;
    /**
     * Feature orientation in degrees; a negative value denotes an unavailable orientation.
     */
    public float angle;
    /**
     * Detector response used to rank the feature.
     */
    public float response;
    /**
     * Image-pyramid octave in which the feature was detected.
     */
    public int octave;
    /**
     * Application-defined class identifier of the feature.
     */
    public int class_id;

    /**
     * Creates a configured KeyPoint value using the supplied values.
     *
     * @param x         horizontal coordinate
     * @param y         vertical coordinate
     * @param _size     feature diameter in pixels
     * @param _angle    feature orientation in degrees, or -1 when unavailable
     * @param _response detector response used to rank this feature
     * @param _octave   pyramid octave of the feature
     * @param _class_id application-defined feature class identifier
     */
    public KeyPoint(float x, float y, float _size, float _angle, float _response, int _octave, int _class_id) {
        pt = new Point(x, y);
        size = _size;
        angle = _angle;
        response = _response;
        octave = _octave;
        class_id = _class_id;
    }

    /**
     * Creates an empty KeyPoint value.
     */
    public KeyPoint() {
        this(0, 0, 0, -1, 0, 0, -1);
    }

    /**
     * Creates a configured KeyPoint value using the supplied values.
     *
     * @param x         horizontal coordinate
     * @param y         vertical coordinate
     * @param _size     feature diameter in pixels
     * @param _angle    feature orientation in degrees, or -1 when unavailable
     * @param _response detector response used to rank this feature
     * @param _octave   pyramid octave of the feature
     */
    public KeyPoint(float x, float y, float _size, float _angle, float _response, int _octave) {
        this(x, y, _size, _angle, _response, _octave, -1);
    }

    /**
     * Creates a configured KeyPoint value using the supplied values.
     *
     * @param x         horizontal coordinate
     * @param y         vertical coordinate
     * @param _size     feature diameter in pixels
     * @param _angle    feature orientation in degrees, or -1 when unavailable
     * @param _response detector response used to rank this feature
     */
    public KeyPoint(float x, float y, float _size, float _angle, float _response) {
        this(x, y, _size, _angle, _response, 0, -1);
    }

    /**
     * Creates a configured KeyPoint value using the supplied values.
     *
     * @param x      horizontal coordinate
     * @param y      vertical coordinate
     * @param _size  feature diameter in pixels
     * @param _angle feature orientation in degrees, or -1 when unavailable
     */
    public KeyPoint(float x, float y, float _size, float _angle) {
        this(x, y, _size, _angle, 0, 0, -1);
    }

    /**
     * Creates a configured KeyPoint value using the supplied values.
     *
     * @param x     horizontal coordinate
     * @param y     vertical coordinate
     * @param _size feature diameter in pixels
     */
    public KeyPoint(float x, float y, float _size) {
        this(x, y, _size, -1, 0, 0, -1);
    }

    /**
     * Returns a textual representation of this object.
     *
     * @return the KeyPoint field values formatted as text
     */
    @Override
    public String toString() {
        return "KeyPoint [pt=" + pt + ", size=" + size + ", angle=" + angle + ", response=" + response + ", octave="
                + octave + ", class_id=" + class_id + "]";
    }

}
