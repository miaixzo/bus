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
import java.util.Arrays;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

/**
 * Represents a contour topology containing a segment and its parent relationship. This class encapsulates the
 * hierarchical structure of contours in image processing.
 *
 * @author Kimi Liu
 * @since Java 21+
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
        this(contour.toArray(), parent);
    }

    /**
     * Creates a new instance.
     *
     * @param contour the contour.
     * @param parent  the parent.
     */
    public ContourTopology(MatOfPoint2f contour, int parent) {
        this(contour.toArray(), parent);
    }

    /**
     * Creates a new instance.
     *
     * @param points the points.
     * @param parent the parent.
     */
    public ContourTopology(Point[] points, int parent) {
        this.parent = parent;
        this.segment = createSegment(points);
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

    /**
     * Creates the segment.
     *
     * @param points the points.
     * @return the operation result.
     */
    private Segment createSegment(Point[] points) {
        return new Segment(Arrays.stream(points).map(this::convertToPoint2D).toList());
    }

    /**
     * Converts the to point 2 d.
     *
     * @param point the point.
     * @return the operation result.
     */
    private Point2D.Double convertToPoint2D(Point point) {
        return new Point2D.Double(point.x, point.y);
    }

}
