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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import org.miaixz.bus.core.data.id.UUID;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;

/**
 * Represents a segmentation region containing polygonal segments with hierarchical relationships. Each region has a
 * unique identifier, a list of segments, and optional attributes for visualization.
 * <p>
 * The region can calculate its area using either pre-computed pixel counts or by calculating the area of its
 * constituent segments using the shoelace formula.
 *
 * @author Kimi Liu
 */
public class Region {

    /**
     * The uninitialized pixel count value.
     */
    private static final long UNINITIALIZED_PIXEL_COUNT = -1L;

    /**
     * The hierarchy parent index value.
     */
    private static final int HIERARCHY_PARENT_INDEX = 3;

    /**
     * The ID value.
     */
    private final String id;

    /**
     * The number of pixels value.
     */
    protected long numberOfPixels;

    /**
     * The segment list value.
     */
    protected List<Segment> segmentList;

    /**
     * The attributes value.
     */
    protected RegionAttributes attributes;

    /**
     * Creates a new instance.
     *
     * @param id the ID.
     */
    public Region(String id) {
        this(id, null);
    }

    /**
     * Creates a new instance.
     *
     * @param id          the ID.
     * @param segmentList the segment list.
     */
    public Region(String id, List<Segment> segmentList) {
        this(id, segmentList, UNINITIALIZED_PIXEL_COUNT);
    }

    /**
     * Creates a new instance.
     *
     * @param id             the ID.
     * @param segmentList    the segment list.
     * @param numberOfPixels the number of pixels.
     */
    public Region(String id, List<Segment> segmentList, long numberOfPixels) {
        this.id = generateOrValidateId(id);
        setSegmentList(segmentList, numberOfPixels);
    }

    /**
     * Generates the or validate ID.
     *
     * @param id the ID.
     * @return the operation result.
     */
    private static String generateOrValidateId(String id) {
        return StringKit.hasText(id) ? id.trim() : UUID.randomUUID().toString();
    }

    /**
     * Builds the segment list.
     *
     * @param binary the binary.
     * @return the operation result.
     */
    public static List<Segment> buildSegmentList(PlanarImage binary) {
        return buildSegmentList(binary, null);
    }

    /**
     * Builds the segment list.
     *
     * @param binary the binary.
     * @param offset the offset.
     * @return the operation result.
     */
    public static List<Segment> buildSegmentList(PlanarImage binary, Point offset) {
        if (binary == null) {
            return List.of();
        }
        var contours = new ArrayList<MatOfPoint>();
        var hierarchy = new Mat();
        try {
            findContours(binary, contours, hierarchy, offset);
            return buildSegmentList(contours, hierarchy);
        } finally {
            hierarchy.release();
            contours.forEach(Mat::release);
        }
    }

    /**
     * Finds the contours.
     *
     * @param binary    the binary.
     * @param contours  the contours.
     * @param hierarchy the hierarchy.
     * @param offset    the offset.
     */
    private static void findContours(PlanarImage binary, List<MatOfPoint> contours, Mat hierarchy, Point offset) {
        if (offset == null) {
            Imgproc.findContours(binary.toMat(), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        } else {
            Imgproc.findContours(
                    binary.toMat(),
                    contours,
                    hierarchy,
                    Imgproc.RETR_TREE,
                    Imgproc.CHAIN_APPROX_SIMPLE,
                    offset);
        }
    }

    /**
     * Builds the segment list from float.
     *
     * @param contours  the contours.
     * @param hierarchy the hierarchy.
     * @return the operation result.
     */
    public static List<Segment> buildSegmentListFromFloat(List<MatOfPoint2f> contours, Mat hierarchy) {
        return buildSegmentListFromContours(contours, hierarchy);
    }

    /**
     * Builds the segment list.
     *
     * @param contours  the contours.
     * @param hierarchy the hierarchy.
     * @return the operation result.
     */
    public static List<Segment> buildSegmentList(List<MatOfPoint> contours, Mat hierarchy) {
        return buildSegmentListFromContours(contours, hierarchy);
    }

    /**
     * Builds the segment list from contours.
     *
     * @param contours  the contours.
     * @param hierarchy the hierarchy.
     * @return the operation result.
     */
    private static List<Segment> buildSegmentListFromContours(List<? extends Mat> contours, Mat hierarchy) {
        if (contours == null || hierarchy == null || contours.isEmpty()) {
            return List.of();
        }
        int count = contours.size();
        if (CvType.depth(hierarchy.type()) != CvType.CV_32S || hierarchy.total() * hierarchy.channels() < 4L * count) {
            throw new IllegalArgumentException("Hierarchy must hold 4 int values per contour");
        }
        var hierarchyData = new int[count * 4];
        hierarchy.get(0, 0, hierarchyData);

        var segments = new Segment[count];
        for (int i = 0; i < count; i++) {
            segments[i] = ContourTopology.toSegment(contours.get(i));
        }

        var rootSegments = new ArrayList<Segment>();
        for (int i = 0; i < count; i++) {
            var segment = segments[i];
            if (segment == null) {
                continue;
            }
            int parentIndex = hierarchyData[i * 4 + HIERARCHY_PARENT_INDEX];
            if (parentIndex < 0) {
                rootSegments.add(segment);
            } else if (parentIndex < count && segments[parentIndex] != null) {
                segments[parentIndex].addChild(segment);
            }
        }
        return rootSegments;
    }

    /**
     * Calculates the area.
     *
     * @param segments the segments.
     * @param level    the level.
     * @return the operation result.
     */
    private static double calculateArea(List<Segment> segments, int level) {
        if (segments.isEmpty()) {
            return 0.0;
        }

        double totalArea = 0.0;
        for (var segment : segments) {
            double segmentArea = polygonArea(segment);
            // Alternate signs for holes: positive for even levels, negative for odd levels
            totalArea += (level % 2 == 0) ? segmentArea : -segmentArea;
            totalArea += calculateArea(segment.children, level + 1);
        }
        return totalArea;
    }

    /**
     * Executes the polygon area operation.
     *
     * @param segment the segment.
     * @return the operation result.
     */
    private static double polygonArea(Segment segment) {
        if (segment == null || segment.size() < 3) {
            return 0.0;
        }
        double area = 0.0;
        Point2D previous = segment.get(segment.size() - 1);
        for (Point2D current : segment) {
            area += previous.getX() * current.getY() - current.getX() * previous.getY();
            previous = current;
        }
        return Math.abs(area) / 2.0;
    }

    /**
     * Returns the ID.
     *
     * @return the ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the segment list.
     *
     * @return the segment list.
     */
    public List<Segment> getSegmentList() {
        return Collections.unmodifiableList(segmentList);
    }

    /**
     * Sets the segment list.
     *
     * @param segmentList the segment list.
     */
    public void setSegmentList(List<Segment> segmentList) {
        setSegmentList(segmentList, UNINITIALIZED_PIXEL_COUNT);
    }

    /**
     * Sets the segment list.
     *
     * @param segmentList    the segment list.
     * @param numberOfPixels the number of pixels.
     */
    public void setSegmentList(List<Segment> segmentList, long numberOfPixels) {
        this.segmentList = segmentList != null ? new ArrayList<>(segmentList) : new ArrayList<>();
        this.numberOfPixels = numberOfPixels > 0 ? numberOfPixels : UNINITIALIZED_PIXEL_COUNT;
    }

    /**
     * Returns the attributes.
     *
     * @return the attributes.
     */
    public RegionAttributes getAttributes() {
        return attributes;
    }

    /**
     * Sets the attributes.
     *
     * @param attributes the attributes.
     */
    public void setAttributes(RegionAttributes attributes) {
        this.attributes = attributes;
    }

    /**
     * Returns the number of pixels.
     *
     * @return the number of pixels.
     */
    public long getNumberOfPixels() {
        return numberOfPixels;
    }

    /**
     * Checks whether the valid pixel count condition is true.
     *
     * @return true if the valid pixel count condition is true; otherwise false.
     */
    public boolean hasValidPixelCount() {
        return numberOfPixels > UNINITIALIZED_PIXEL_COUNT;
    }

    /**
     * Returns the area.
     *
     * @return the area.
     */
    public double getArea() {
        return hasValidPixelCount() ? numberOfPixels : Math.round(calculateArea(segmentList, 0));
    }

    /**
     * Executes the equals operation.
     *
     * @param obj the obj.
     * @return true if the equals condition is true; otherwise false.
     */
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Region other && Objects.equals(id, other.id)
                && numberOfPixels == other.numberOfPixels && Objects.equals(segmentList, other.segmentList)
                && Objects.equals(attributes, other.attributes));
    }

    /**
     * Checks whether the hash code condition is true.
     *
     * @return true if the hash code condition is true; otherwise false.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, numberOfPixels, segmentList, attributes);
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "Region{id='%s', segments=%d, pixels=%d, hasAttributes=%s}"
                .formatted(id, segmentList.size(), numberOfPixels, attributes != null);
    }

}
