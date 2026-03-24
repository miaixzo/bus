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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class Segment extends ArrayList<Point2D> {

    private final List<Segment> children = new ArrayList<>();

    public Segment() {
        super();
    }

    public Segment(List<Point2D> point2DList) {
        this(point2DList, false);
    }

    public Segment(List<Point2D> point2DList, boolean forceClose) {
        setPoints(point2DList, forceClose);
    }

    public Segment(float[] points) {
        this(points, null, false, null);
    }

    public Segment(float[] points, AffineTransform inverse, boolean forceClose, Dimension dim) {
        setPoints(points, inverse, forceClose, dim);
    }

    public Segment(double[] pts) {
        this(pts, null, false, null);
    }

    public Segment(double[] pts, AffineTransform inverse, boolean forceClose, Dimension dim) {
        setPoints(pts, inverse, forceClose, dim);
    }

    public static double[] convertFloatToDouble(float[] floatArray) {
        double[] doubleArray = new double[floatArray.length];
        for (int i = 0; i < floatArray.length; i++) {
            doubleArray[i] = floatArray[i];
        }
        return doubleArray;
    }

    public void setPoints(List<Point2D> point2DList, boolean forceClose) {
        if (point2DList != null && !point2DList.isEmpty()) {
            addAll(point2DList);
            if (forceClose && !point2DList.get(0).equals(point2DList.get(point2DList.size() - 1))) {
                add((Point2D.Double) point2DList.get(0).clone());
            }
        }
    }

    public void setPoints(float[] points, boolean forceClose, Dimension dim) {
        setPoints(points, null, forceClose, dim);
    }

    public void setPoints(float[] points, AffineTransform inverse, boolean forceClose, Dimension dim) {
        double[] pts;
        Objects.requireNonNull(points);
        if (inverse == null) {
            pts = convertFloatToDouble(points);
        } else {
            double[] dstPoints = new double[points.length];
            inverse.transform(points, 0, dstPoints, 0, points.length / 2);
            pts = dstPoints;
        }
        addPoints(pts, forceClose, dim);
    }

    public void setPoints(double[] points, boolean forceClose, Dimension dim) {
        setPoints(points, null, forceClose, dim);
    }

    public void setPoints(double[] points, AffineTransform inverse, boolean forceClose, Dimension dim) {
        double[] pts;
        if (inverse == null) {
            pts = points;
        } else {
            double[] dstPoints = new double[points.length];
            inverse.transform(points, 0, dstPoints, 0, points.length / 2);
            pts = dstPoints;
        }
        addPoints(pts, forceClose, dim);
    }

    protected void addPoints(double[] pts, boolean forceClose, Dimension dim) {
        clear();
        if (pts == null) {
            return;
        }
        int size = pts.length / 2;
        if (size >= 2) {
            boolean resize = dim != null && dim.width > 0 && dim.height > 0;
            for (int i = 0; i < size; i++) {
                double x = resize ? pts[i * 2] * dim.width : pts[i * 2];
                double y = resize ? pts[i * 2 + 1] * dim.height : pts[i * 2 + 1];
                add(new Point2D.Double(x, y));
            }
            if (forceClose && !get(0).equals(get(size - 1))) {
                add((Point2D.Double) get(0).clone());
            }
        }
    }

    public List<Segment> getChildren() {
        return children;
    }

    public void addChild(Segment child) {
        children.add(child);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Segment point2DS = (Segment) o;
        return Objects.equals(children, point2DS.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), children);
    }

}
