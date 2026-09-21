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
package org.opencv.core;

//javadoc: KeyPoint
/**
 * Provides the {@code KeyPoint} API.
 */
public class KeyPoint {

    /**
     * Coordinates of the keypoint.
     */
    public Point pt;
    /**
     * Diameter of the useful keypoint adjacent area.
     */
    public float size;
    /**
     * Computed orientation of the keypoint (-1 if not applicable).
     */
    public float angle;
    /**
     * The response, by which the strongest keypoints have been selected. Can be used for further sorting or
     * subsampling.
     */
    public float response;
    /**
     * Octave (pyramid layer), from which the keypoint has been extracted.
     */
    public int octave;
    /**
     * Object ID, that can be used to cluster keypoints by an object they belong to.
     */
    public int class_id;

    // javadoc:KeyPoint::KeyPoint(x,y,_size,_angle,_response,_octave,_class_id)
    /**
     * Creates a new {@code KeyPoint} instance.
     *
     * @param x the {@code x} value
     * @param y the {@code y} value
     * @param _size the {@code _size} value
     * @param _angle the {@code _angle} value
     * @param _response the {@code _response} value
     * @param _octave the {@code _octave} value
     * @param _class_id the {@code _class_id} value
     */
    public KeyPoint(float x, float y, float _size, float _angle, float _response, int _octave, int _class_id) {
        pt = new Point(x, y);
        size = _size;
        angle = _angle;
        response = _response;
        octave = _octave;
        class_id = _class_id;
    }

    // javadoc: KeyPoint::KeyPoint()
    /**
     * Creates a new {@code KeyPoint} instance.
     */
    public KeyPoint() {
        this(0, 0, 0, -1, 0, 0, -1);
    }

    // javadoc: KeyPoint::KeyPoint(x, y, _size, _angle, _response, _octave)
    /**
     * Creates a new {@code KeyPoint} instance.
     *
     * @param x the {@code x} value
     * @param y the {@code y} value
     * @param _size the {@code _size} value
     * @param _angle the {@code _angle} value
     * @param _response the {@code _response} value
     * @param _octave the {@code _octave} value
     */
    public KeyPoint(float x, float y, float _size, float _angle, float _response, int _octave) {
        this(x, y, _size, _angle, _response, _octave, -1);
    }

    // javadoc: KeyPoint::KeyPoint(x, y, _size, _angle, _response)
    /**
     * Creates a new {@code KeyPoint} instance.
     *
     * @param x the {@code x} value
     * @param y the {@code y} value
     * @param _size the {@code _size} value
     * @param _angle the {@code _angle} value
     * @param _response the {@code _response} value
     */
    public KeyPoint(float x, float y, float _size, float _angle, float _response) {
        this(x, y, _size, _angle, _response, 0, -1);
    }

    // javadoc: KeyPoint::KeyPoint(x, y, _size, _angle)
    /**
     * Creates a new {@code KeyPoint} instance.
     *
     * @param x the {@code x} value
     * @param y the {@code y} value
     * @param _size the {@code _size} value
     * @param _angle the {@code _angle} value
     */
    public KeyPoint(float x, float y, float _size, float _angle) {
        this(x, y, _size, _angle, 0, 0, -1);
    }

    // javadoc: KeyPoint::KeyPoint(x, y, _size)
    /**
     * Creates a new {@code KeyPoint} instance.
     *
     * @param x the {@code x} value
     * @param y the {@code y} value
     * @param _size the {@code _size} value
     */
    public KeyPoint(float x, float y, float _size) {
        this(x, y, _size, -1, 0, 0, -1);
    }

    @Override
    public String toString() {
        return "KeyPoint [pt=" + pt + ", size=" + size + ", angle=" + angle + ", response=" + response + ", octave="
                + octave + ", class_id=" + class_id + "]";
    }

}
