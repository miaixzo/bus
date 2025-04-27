/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org opencv.org and other contributors.         ~
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
package org.opencv.core;

//javadoc: KeyPoint
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
    public KeyPoint(float x, float y, float _size, float _angle, float _response, int _octave, int _class_id) {
        pt = new Point(x, y);
        size = _size;
        angle = _angle;
        response = _response;
        octave = _octave;
        class_id = _class_id;
    }

    // javadoc: KeyPoint::KeyPoint()
    public KeyPoint() {
        this(0, 0, 0, -1, 0, 0, -1);
    }

    // javadoc: KeyPoint::KeyPoint(x, y, _size, _angle, _response, _octave)
    public KeyPoint(float x, float y, float _size, float _angle, float _response, int _octave) {
        this(x, y, _size, _angle, _response, _octave, -1);
    }

    // javadoc: KeyPoint::KeyPoint(x, y, _size, _angle, _response)
    public KeyPoint(float x, float y, float _size, float _angle, float _response) {
        this(x, y, _size, _angle, _response, 0, -1);
    }

    // javadoc: KeyPoint::KeyPoint(x, y, _size, _angle)
    public KeyPoint(float x, float y, float _size, float _angle) {
        this(x, y, _size, _angle, 0, 0, -1);
    }

    // javadoc: KeyPoint::KeyPoint(x, y, _size)
    public KeyPoint(float x, float y, float _size) {
        this(x, y, _size, -1, 0, 0, -1);
    }

    @Override
    public String toString() {
        return "KeyPoint [pt=" + pt + ", size=" + size + ", angle=" + angle + ", response=" + response + ", octave="
                + octave + ", class_id=" + class_id + "]";
    }

}
