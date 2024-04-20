/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.builder.EqualsBuilder;
import org.miaixz.bus.core.builder.HashCodeBuilder;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Normal;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 地理区域坐标距离计算工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GeoKit {

    /**
     * 地球半径
     */
    private static final double EARTH_RADIUS = 6378137.0;

    private static final double EE = 0.00669342162296594323;

    /**
     * @param d 值
     * @return 弧度单位
     */
    private static double __rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * @param p1 坐标点1
     * @param p2 坐标点2
     * @return 计算两点间的距离(米)
     */
    public static double distance(Point p1, Point p2) {
        return p1.distance(p2);
    }

    /**
     * @param point    坐标点
     * @param distance 距离(米)
     * @return 返回从坐标点到直定距离的矩形范围
     */
    public static Bounds rectangle(Point point, long distance) {
        if (null == point || distance <= 0) {
            return new Bounds();
        }
        float _delta = 111000;
        if (point.getLatitude() != 0 && point.getLongitude() != 0) {
            double lng1 = point.longitude - distance / Math.abs(Math.cos(Math.toRadians(point.latitude)) * _delta);
            double lng2 = point.longitude + distance / Math.abs(Math.cos(Math.toRadians(point.latitude)) * _delta);
            double lat1 = point.latitude - (distance / _delta);
            double lat2 = point.latitude + (distance / _delta);
            return new Bounds(new Point(lng1, lat1), new Point(lng2, lat2));
        } else {
            double lng1 = point.longitude - distance / _delta;
            double lng2 = point.longitude + distance / _delta;
            double lat1 = point.latitude - (distance / _delta);
            double lat2 = point.latitude + (distance / _delta);
            return new Bounds(new Point(lng1, lat1), new Point(lng2, lat2));
        }
    }

    /**
     * 判断点是否在多边形区域内
     *
     * @param polygon 多边形区域
     * @param point   待判断点
     * @return true - 多边形包含这个点, false - 多边形未包含这个点。
     */
    public static boolean contains(Polygon polygon, Point point) {
        return contains(polygon, point, false);
    }

    public static boolean contains(Polygon polygon, Point point, boolean on) {
        if (on) {
            // 判断是否在多边形区域边界上
            return polygon.on(point);
        }
        // 判断点是否在多边形区域内
        return polygon.in(point);
    }

    /**
     * 判断点是否在圆形范围内
     *
     * @param circle 圆形区域
     * @param point  待判断点
     * @return -1 - 点在圆外, 0 - 点在圆上, 1 - 点在圆内
     */
    public static int contains(Circle circle, Point point) {
        return circle.contains(point);
    }

    /**
     * 坐标点类型
     */
    public enum PointType {
        WGS84, GCJ02, BD09
    }

    /**
     * 地理坐标点
     * <p>坐标系转换代码参考自: https://blog.csdn.net/a13570320979/article/details/51366355</p>
     */
    public static class Point implements Serializable {

        /**
         * 经度, X
         */
        private double longitude;

        /**
         * 纬度, Y
         */
        private double latitude;

        /**
         * 坐标点类型, 默认为: WGS84
         */
        private PointType type;

        /**
         * 构造器
         *
         * @param longitude 经度
         * @param latitude  纬度
         * @param type      坐标点类型, 默认为WGS84
         */
        public Point(double longitude, double latitude, PointType type) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.type = null == type ? PointType.WGS84 : type;
        }

        public Point(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.type = PointType.WGS84;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public PointType getType() {
            return type;
        }

        public void setType(PointType type) {
            this.type = type;
        }

        public Point2D.Double toPoint2D() {
            return new Point2D.Double(longitude, latitude);
        }

        /**
         * @return 将当前坐标点转换为火星坐标
         */
        public Point toGcj02() {
            Point _point;
            switch (type) {
                case BD09:
                    _point = __bd09ToGcj02();
                    break;
                case GCJ02:
                    _point = new Point(longitude, latitude, PointType.GCJ02);
                    break;
                default:
                    _point = __transform();
            }
            return _point;
        }

        /**
         * @return 将当前坐标点转换为GPS原始坐标
         */
        public Point toWgs84() {
            Point _point;
            switch (type) {
                case BD09:
                    _point = __bd09ToWgs84();
                    break;
                case GCJ02:
                    _point = __gcj02ToWgs84();
                    break;
                default:
                    _point = new Point(longitude, latitude);
            }
            return _point;
        }

        /**
         * @return 将当前坐标点转换为百度坐标
         */
        public Point toBd09() {
            Point _point;
            switch (type) {
                case BD09:
                    _point = new Point(longitude, latitude, PointType.BD09);
                    break;
                case GCJ02:
                    _point = __gcj02ToBd09();
                    break;
                default:
                    _point = __wgs84ToBd09();
            }
            return _point;
        }

        /**
         * @return 保留小数点后六位
         */
        public Point retain6() {
            return new Point(Double.valueOf(String.format("%.6f", longitude)), Double.valueOf(String.format("%.6f", latitude)), type);
        }

        /**
         * @return 是否超出中国范围
         */
        public boolean notInChina() {
            if (longitude < 72.004 || longitude > 137.8347) {
                return true;
            }
            return latitude < 0.8293 || latitude > 55.8271;
        }

        private double __transformLat() {
            double x = longitude - 105.0;
            double y = latitude - 35.0;
            //
            double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
            ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
            ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
            ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
            return ret;
        }

        private double __transformLon() {
            double x = longitude - 105.0;
            double y = latitude - 35.0;
            //
            double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
            ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
            ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
            ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
            return ret;
        }

        /**
         * @return WGS84 --> GCJ-02
         */
        private Point __transform() {
            if (notInChina()) {
                return new Point(longitude, latitude);
            }
            double dLat = __transformLat();
            double dLon = __transformLon();
            double radLat = __rad(latitude);
            double magic = Math.sin(radLat);
            magic = 1 - EE * magic * magic;
            double sqrtMagic = Math.sqrt(magic);
            dLat = (dLat * 180.0) / ((EARTH_RADIUS * (1 - EE)) / (magic * sqrtMagic) * Math.PI);
            dLon = (dLon * 180.0) / (EARTH_RADIUS / sqrtMagic * Math.cos(radLat) * Math.PI);
            double mgLat = latitude + dLat;
            double mgLon = longitude + dLon;
            //
            return new Point(mgLon, mgLat, PointType.GCJ02);
        }

        /**
         * @return GCJ-02 --> WGS84
         */
        private Point __gcj02ToWgs84() {
            Point _point = __transform();
            return new Point(longitude * 2 - _point.longitude, latitude * 2 - _point.latitude);
        }

        /**
         * @return GCJ-02 --> BD09
         */
        private Point __gcj02ToBd09() {
            double z = Math.sqrt(longitude * longitude + latitude * latitude) + 0.00002 * Math.sin(latitude * Math.PI);
            double theta = Math.atan2(latitude, longitude) + 0.000003 * Math.cos(longitude * Math.PI);
            return new Point(z * Math.cos(theta) + 0.0065, z * Math.sin(theta) + 0.006, PointType.BD09);
        }

        /**
         * @return BD09 --> GCJ-02
         */
        private Point __bd09ToGcj02() {
            double x = longitude - 0.0065, y = latitude - 0.006;
            double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * Math.PI);
            double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * Math.PI);
            return new Point(z * Math.cos(theta), z * Math.sin(theta), PointType.GCJ02);
        }

        /**
         * @return WGS84 --> BD09
         */
        private Point __wgs84ToBd09() {
            Point _point = __transform();
            return _point.toBd09();
        }

        /**
         * @return BD09 --> WGS84
         */
        private Point __bd09ToWgs84() {
            Point _point = __bd09ToGcj02();
            return _point.toWgs84();
        }

        /**
         * @param point 坐标点
         * @return 计算两点间的距离(米)
         */
        public double distance(Point point) {
            double _lat1 = __rad(latitude);
            double _lat2 = __rad(point.latitude);
            double _diff = __rad(longitude) - __rad(point.longitude);
            return Math.round(2 * Math.asin(Math.sqrt(Math.pow(Math.sin((_lat1 - _lat2) / 2), 2) + Math.cos(_lat1) * Math.cos(_lat2) * Math.pow(Math.sin(_diff / 2), 2))) * EARTH_RADIUS * 10000) / 10000;
        }

        /**
         * @return 验证是否为合法有效的经纬度
         */
        public boolean isValidCoordinate() {
            // 经度: 180° >= x >= 0°
            if (0.0 > longitude || 180.0 < longitude) {
                return false;
            }
            // 纬度: 90° >= y >= 0°
            return !(0.0 > latitude || 90.0 < latitude);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (null == o || getClass() != o.getClass()) {
                return false;
            }
            Point point = (Point) o;
            return new EqualsBuilder()
                    .append(longitude, point.longitude)
                    .append(latitude, point.latitude)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(longitude)
                    .append(latitude)
                    .toHashCode();
        }

    }

    /**
     * 地理坐标矩形区域
     */
    public static class Bounds implements Serializable {

        /**
         * 左下(西南)角坐标点
         */
        private Point southWest;

        /**
         * 右上(东北)角坐标点
         */
        private Point northEast;

        /**
         * 空矩形
         */
        public Bounds() {
        }

        /**
         * 取两个矩形区域的并集
         *
         * @param first 第一个矩形区域
         * @param other 另一个矩形区域
         */
        public Bounds(Bounds first, Bounds other) {
            if (null == first || first.isEmpty() || null == other || other.isEmpty()) {
                throw new InternalException("bounds");
            }
            this.southWest = new Point(Math.min(first.southWest.getLongitude(), other.southWest.getLongitude()), Math.min(first.southWest.getLatitude(), other.southWest.getLatitude()));
            //
            this.northEast = new Point(Math.max(first.northEast.getLongitude(), other.northEast.getLongitude()), Math.max(first.northEast.getLatitude(), other.northEast.getLatitude()));
        }

        public Bounds(Point southWest, Point northEast) {
            this.southWest = southWest;
            this.northEast = northEast;
        }

        public Point getSouthWest() {
            return southWest;
        }

        public void setSouthWest(Point southWest) {
            this.southWest = southWest;
        }

        public Point getNorthEast() {
            return northEast;
        }

        public void setNorthEast(Point northEast) {
            this.northEast = northEast;
        }

        /**
         * @return 返回矩形的中心点
         */
        public Point getCenter() {
            return new Point((southWest.longitude + northEast.longitude) / 2, (southWest.latitude + northEast.latitude) / 2);
        }

        /**
         * @return 矩形区域是否为空
         */
        public boolean isEmpty() {
            return null == southWest || null == northEast;
        }

        /**
         * @param point 地理坐标点
         * @return 地理坐标点是否位于此矩形内
         */
        public boolean contains(Point point) {
            return !isEmpty() && (point.longitude >= southWest.longitude && point.longitude <= northEast.longitude) && (point.latitude >= southWest.latitude && point.latitude <= northEast.latitude);
        }

        /**
         * @param bounds 矩形区域
         * @return 矩形区域是否完全包含于此矩形区域中
         */
        public boolean contains(Bounds bounds) {
            return contains(bounds.southWest) && contains(bounds.northEast);
        }

        /**
         * @param bounds 矩形区域
         * @return 计算与另一矩形的交集区域
         */
        public Bounds intersects(Bounds bounds) {
            if (null != bounds && !bounds.isEmpty() && !isEmpty()) {
                Bounds _merged = new Bounds(this, bounds);
                //
                double _x1 = this.southWest.longitude == _merged.southWest.longitude ? bounds.southWest.longitude : this.southWest.longitude;
                double _y1 = this.southWest.latitude == _merged.southWest.latitude ? bounds.southWest.latitude : this.southWest.latitude;
                //
                double _x2 = this.northEast.longitude == _merged.northEast.longitude ? bounds.northEast.longitude : this.northEast.longitude;
                double _y2 = this.northEast.latitude == _merged.northEast.latitude ? bounds.northEast.latitude : this.northEast.latitude;
                //
                if (_x1 < _x2 && _y1 < _y2) {
                    return new Bounds(new Point(_x1, _y1), new Point(_x2, _y2));
                }
            }
            return new Bounds();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (null == o || getClass() != o.getClass()) {
                return false;
            }
            Bounds bounds = (Bounds) o;
            return new EqualsBuilder()
                    .append(southWest, bounds.southWest)
                    .append(northEast, bounds.northEast)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(southWest)
                    .append(northEast)
                    .toHashCode();
        }

    }

    /**
     * 地理坐标圆形
     */
    public static class Circle implements Serializable {

        /**
         * 圆心
         */
        private Point center;

        /**
         * 半径
         */
        private double r;

        /**
         * 构造器
         *
         * @param center 圆心
         * @param r      半径
         */
        public Circle(Point center, double r) {
            this.center = center;
            this.r = r;
        }

        public Point getCenter() {
            return center;
        }

        public void setCenter(Point center) {
            this.center = center;
        }

        public double getR() {
            return r;
        }

        public void setR(double r) {
            this.r = r;
        }

        /**
         * 判断点是否在圆形范围内
         *
         * @param point 点
         * @return -1 - 点在圆外, 0 - 点在圆上, 1 - 点在圆内
         */
        public int contains(Point point) {
            double value = Math.hypot((point.longitude - center.longitude), (point.latitude - center.latitude));
            if (value > r) {
                // 点在圆外
                return Normal.__1;
            } else if (value < r) {
                // 点在圆内
                return 1;
            }
            // 点在圆上
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (null == o || getClass() != o.getClass()) {
                return false;
            }

            Circle circle = (Circle) o;

            return new EqualsBuilder()
                    .append(r, circle.r)
                    .append(center, circle.center)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(center)
                    .append(r)
                    .toHashCode();
        }

    }

    /**
     * 地理坐标多边形区域
     */
    public static class Polygon implements Serializable {

        /**
         * 多边形坐标点列表
         */
        private final List<Point> points = new ArrayList<>();

        public Polygon() {
        }

        public Polygon(Point[] points) {
            if (ArrayKit.isNotEmpty(points)) {
                this.points.addAll(Arrays.asList(points));
            }
        }

        public Polygon(Collection<Point> points) {
            if (null != points && !points.isEmpty()) {
                this.points.addAll(points);
            }
        }

        public boolean isEmpty() {
            return this.points.isEmpty();
        }

        public Polygon add(Point point) {
            if (null != point) {
                this.points.add(point);
            }
            return this;
        }

        public Polygon add(double longitude, double latitude) {
            this.points.add(new Point(longitude, latitude));
            return this;
        }

        public List<Point> getPoints() {
            return points;
        }

        public boolean in(Point point) {
            int nCross = 0;
            for (int i = 0; i < points.size(); i++) {
                Point p1 = points.get(i);
                Point p2 = points.get((i + 1) % points.size());
                if (p1.latitude == p2.latitude) {
                    continue;
                }
                if (point.latitude < Math.min(p1.latitude, p2.latitude)) {
                    continue;
                }
                if (point.latitude >= Math.max(p1.latitude, p2.latitude)) {
                    continue;
                }
                double x = (point.latitude - p1.latitude) * (p2.longitude - p1.longitude) / (p2.latitude - p1.latitude) + p1.longitude;
                if (x > point.longitude) {
                    nCross++;
                }
            }
            return (nCross % 2 == 1);
        }

        public boolean on(Point point) {
            for (int i = 0; i < points.size(); i++) {
                Point p1 = points.get(i);
                Point p2 = points.get((i + 1) % points.size());
                if (point.latitude < Math.min(p1.latitude, p2.latitude)) {
                    continue;
                }
                if (point.latitude > Math.max(p1.latitude, p2.latitude)) {
                    continue;
                }
                if (p1.latitude == p2.latitude) {
                    double minX = Math.min(p1.longitude, p2.longitude);
                    double maxX = Math.max(p1.longitude, p2.longitude);
                    if ((point.latitude == p1.latitude) && (point.longitude >= minX && point.longitude <= maxX)) {
                        return true;
                    }
                } else {
                    double x = (point.latitude - p1.latitude) * (p2.longitude - p1.longitude) / (p2.latitude - p1.latitude) + p1.longitude;
                    if (x == point.longitude) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

}