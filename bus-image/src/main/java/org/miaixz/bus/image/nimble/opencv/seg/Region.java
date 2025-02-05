/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.nimble.opencv.seg;

import java.awt.geom.Point2D;
import java.util.*;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Region {

    private final String id;
    protected long numberOfPixels;
    protected List<Segment> segmentList;

    protected RegionAttributes attributes;

    public Region(String id) {
        this(id, null);
    }

    public Region(String id, List<Segment> segmentList) {
        this(id, segmentList, -1);
    }

    public Region(String id, List<Segment> segmentList, int numberOfPixels) {
        this.id = StringKit.hasText(id) ? id : UUID.randomUUID().toString();
        setSegmentList(segmentList, numberOfPixels);
    }

    public static List<Segment> buildSegmentList(PlanarImage binary) {
        return buildSegmentList(binary, null);
    }

    public static List<Segment> buildSegmentList(PlanarImage binary, Point offset) {
        if (binary == null) {
            return Collections.emptyList();
        }
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        if (offset == null) {
            Imgproc.findContours(binary.toMat(), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        } else {
            Imgproc.findContours(binary.toMat(), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE,
                    offset);
        }
        return buildSegmentList(contours, hierarchy);
    }

    public static List<Segment> buildSegmentListFromFloat(List<MatOfPoint2f> contours, Mat hierarchy) {
        return buildSegmentListFromPoint(contours, hierarchy);
    }

    public static List<Segment> buildSegmentList(List<MatOfPoint> contours, Mat hierarchy) {
        return buildSegmentListFromPoint(contours, hierarchy);
    }

    protected static List<Segment> buildSegmentListFromPoint(List<? extends Mat> contours, Mat hierarchy) {
        if (contours == null || hierarchy == null) {
            return Collections.emptyList();
        }
        Map<Integer, ContourTopology> contourMap = new HashMap<>();
        int[] hierarchyData = new int[4];
        for (int i = 0; i < contours.size(); i++) {
            hierarchy.get(0, i, hierarchyData);
            if (contours.get(i) instanceof MatOfPoint pt) {
                ContourTopology contourTopology = new ContourTopology(pt, hierarchyData[3]);
                contourMap.put(i, contourTopology);
            } else if (contours.get(i) instanceof MatOfPoint2f pt2f) {
                ContourTopology contourTopology = new ContourTopology(pt2f, hierarchyData[3]);
                contourMap.put(i, contourTopology);
            }
        }

        List<Segment> segmentList = new ArrayList<>();
        for (int i = 0; i < contours.size(); i++) {
            Segment segment = buildSegment(contourMap, i);
            if (segment != null) {
                segmentList.add(segment);
            }
        }
        return segmentList;
    }

    protected static Segment buildSegment(Map<Integer, ContourTopology> contourMap, int index) {
        if (contourMap == null) {
            return null;
        }
        ContourTopology contourTopology = contourMap.get(index);
        if (contourTopology != null) {
            int parent = contourTopology.getParent();
            if (parent >= 0) {
                ContourTopology p = contourMap.get(parent);
                if (p != null) {
                    p.getSegment().addChild(contourTopology.getSegment());
                }
                return null;
            }
            return contourTopology.getSegment();
        }
        return null;
    }

    private static double calculateArea(List<Segment> segments, int level) {
        double area = 0.0;
        for (Segment segment : segments) {
            area += (level % 2 == 0 ? 1 : -1) * polygonArea(segment);
            area += calculateArea(segment.getChildren(), level + 1);
        }
        return area;
    }

    /**
     * Calculate the area of a polygon
     *
     * @param segment the polygon
     * @return the area winch is an approximation of the number of pixels inside the polygon
     */
    private static double polygonArea(Segment segment) {
        double area = 0.0;
        int n = segment.size();
        for (int i = 0; i < n; i++) {
            Point2D pt1 = segment.get(i);
            Point2D pt2 = segment.get((i + 1) % n);
            area += pt1.getX() * pt2.getY() - pt2.getX() * pt1.getY();
        }
        return Math.abs(area) / 2.0;
    }

    public String getId() {
        return id;
    }

    public List<Segment> getSegmentList() {
        return segmentList;
    }

    public void setSegmentList(List<Segment> segmentList) {
        setSegmentList(segmentList, -1L);
    }

    public void setSegmentList(List<Segment> segmentList, long numberOfPixels) {
        this.segmentList = segmentList == null ? new ArrayList<>() : segmentList;
        this.numberOfPixels = numberOfPixels;
        if (numberOfPixels <= 0) {
            this.numberOfPixels = -1L;
        }
    }

    public RegionAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(RegionAttributes attributes) {
        this.attributes = attributes;
    }

    public long getNumberOfPixels() {
        return numberOfPixels;
    }

    public double getArea() {
        if (numberOfPixels < 0) {
            return Math.round(calculateArea(getSegmentList(), 0));
        }
        return numberOfPixels;
    }

}
