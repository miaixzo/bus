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
package org.miaixz.bus.image.nimble.opencv.seg;

import java.awt.geom.Point2D;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

/**
 * Represents a contour topology containing a segment and its parent relationship. This class encapsulates the
 * hierarchical structure of contours in image processing.
 *
 * @author Kimi Liu
 */
public class ContourTopology {

    /**
     * The segment value.
     */
    private final Segment segment;

    /**
     * The parent value.
     */
    private final int parent;

    /**
     * Creates a new instance.
     *
     * @param contour the contour.
     * @param parent  the parent.
     */
    public ContourTopology(MatOfPoint contour, int parent) {
        this(toSegment(contour), parent);
    }

    /**
     * Creates a new instance.
     *
     * @param contour the contour.
     * @param parent  the parent.
     */
    public ContourTopology(MatOfPoint2f contour, int parent) {
        this(toSegment(contour), parent);
    }

    /**
     * Creates a new instance.
     *
     * @param points the points.
     * @param parent the parent.
     */
    public ContourTopology(Point[] points, int parent) {
        this(toSegment(points), parent);
    }

    /**
     * Creates a new instance.
     *
     * @param segment the segment.
     * @param parent  the parent.
     */
    private ContourTopology(Segment segment, int parent) {
        this.parent = parent;
        this.segment = segment;
    }

    /**
     * Converts the contour to a segment.
     *
     * @param contour the contour.
     * @return the segment, or null when the contour type is unsupported.
     */
    static Segment toSegment(Mat contour) {
        if (contour instanceof MatOfPoint matOfPoint) {
            return toSegment(matOfPoint);
        }
        if (contour instanceof MatOfPoint2f matOfPoint2f) {
            return toSegment(matOfPoint2f);
        }
        return null;
    }

    /**
     * Converts the integer contour to a segment.
     *
     * @param contour the contour.
     * @return the segment.
     */
    static Segment toSegment(MatOfPoint contour) {
        int count = (int) contour.total();
        var segment = newSegment(count);
        if (count > 0) {
            var data = new int[count * 2];
            contour.get(0, 0, data);
            for (int i = 0; i < data.length; i += 2) {
                segment.add(new Point2D.Double(data[i], data[i + 1]));
            }
        }
        return segment;
    }

    /**
     * Converts the floating point contour to a segment.
     *
     * @param contour the contour.
     * @return the segment.
     */
    static Segment toSegment(MatOfPoint2f contour) {
        int count = (int) contour.total();
        var segment = newSegment(count);
        if (count > 0) {
            var data = new float[count * 2];
            contour.get(0, 0, data);
            for (int i = 0; i < data.length; i += 2) {
                segment.add(new Point2D.Double(data[i], data[i + 1]));
            }
        }
        return segment;
    }

    /**
     * Converts points to a segment.
     *
     * @param points the points.
     * @return the segment.
     */
    private static Segment toSegment(Point[] points) {
        var segment = newSegment(points.length);
        for (Point point : points) {
            segment.add(new Point2D.Double(point.x, point.y));
        }
        return segment;
    }

    /**
     * Creates a segment with the expected capacity.
     *
     * @param capacity the expected capacity.
     * @return the segment.
     */
    private static Segment newSegment(int capacity) {
        var segment = new Segment();
        segment.ensureCapacity(capacity);
        return segment;
    }

    /**
     * Returns the parent.
     *
     * @return the parent.
     */
    public int getParent() {
        return parent;
    }

    /**
     * Returns the segment.
     *
     * @return the segment.
     */
    public Segment getSegment() {
        return segment;
    }

}
