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
package org.miaixz.bus.core.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Utility class for coordinate system transformations. Mainstream coordinate systems include:
 * <ul>
 * <li>WGS84 Coordinate System: The Earth coordinate system, used by Google Maps outside of China.</li>
 * <li>GCJ02 Coordinate System: The Mars coordinate system, used by Gaode, Tencent, Ali, etc.</li>
 * <li>BD09 Coordinate System: The Baidu coordinate system, an encrypted version of the GCJ02 coordinate system. Used by
 * Baidu, Sogou, etc.</li>
 * </ul>
 * <p>
 * For coordinate transformation references, see: <a href="https://tool.lu/coordinate/">https://tool.lu/coordinate/</a>
 * Reference:
 * <a href="https://github.com/JourWon/coordinate-transform">https://github.com/JourWon/coordinate-transform</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Coordinate {

    /**
     * Constructs a new Coordinate. Utility class constructor for static access.
     */
    private Coordinate() {
    }

    /**
     * Coordinate transformation parameter (intermediate variable for converting between Mars and Baidu coordinate
     * systems).
     */
    public static final double X_PI = 3.1415926535897932384626433832795 * 3000.0 / 180.0;

    /**
     * Coordinate transformation parameter: π
     */
    public static final double PI = 3.1415926535897932384626433832795D;

    /**
     * Earth radius (Krasovsky 1940).
     */
    public static final double RADIUS = 6378245.0D;

    /**
     * Correction parameter (eccentricity ee).
     */
    public static final double CORRECTION_PARAM = 0.00669342162296594323D;

    /**
     * Determines if the coordinates are outside of China. The Mars coordinate system (GCJ-02) is only valid within
     * China; no conversion is needed for foreign countries.
     *
     * @param lng The longitude.
     * @param lat The latitude.
     * @return {@code true} if the coordinates are outside of China, {@code false} otherwise.
     */
    public static boolean outOfChina(final double lng, final double lat) {
        return (lng < 72.004 || lng > 137.8347) || (lat < 0.8293 || lat > 55.8271);
    }

    /**
     * Converts WGS84 to Mars coordinate system (GCJ-02).
     *
     * @param lng The longitude.
     * @param lat The latitude.
     * @return The Mars coordinates (GCJ-02).
     */
    public static Point wgs84ToGcj02(final double lng, final double lat) {
        return new Point(lng, lat).offset(offset(lng, lat, true));
    }

    /**
     * Converts WGS84 coordinates to Baidu coordinate system (BD-09) coordinates.
     *
     * @param lng The longitude.
     * @param lat The latitude.
     * @return The BD-09 coordinates.
     */
    public static Point wgs84ToBd09(final double lng, final double lat) {
        final Point gcj02 = wgs84ToGcj02(lng, lat);
        return gcj02ToBd09(gcj02.lng, gcj02.lat);
    }

    /**
     * Converts Mars coordinate system (GCJ-02) to WGS84.
     *
     * @param lng The longitude.
     * @param lat The latitude.
     * @return The WGS84 coordinates.
     */
    public static Point gcj02ToWgs84(final double lng, final double lat) {
        return new Point(lng, lat).offset(offset(lng, lat, false));
    }

    /**
     * Converts between Mars coordinate system (GCJ-02) and Baidu coordinate system (BD-09).
     *
     * @param lng The longitude.
     * @param lat The latitude.
     * @return The BD-09 coordinates.
     */
    public static Point gcj02ToBd09(final double lng, final double lat) {
        final double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * X_PI);
        final double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * X_PI);
        final double bd_lng = z * Math.cos(theta) + 0.0065;
        final double bd_lat = z * Math.sin(theta) + 0.006;
        return new Point(bd_lng, bd_lat);
    }

    /**
     * Converts between Baidu coordinate system (BD-09) and Mars coordinate system (GCJ-02). i.e., Baidu to
     * Google/Gaode.
     *
     * @param lng The longitude.
     * @param lat The latitude.
     * @return The GCJ-02 coordinates.
     */
    public static Point bd09ToGcj02(final double lng, final double lat) {
        final double x = lng - 0.0065;
        final double y = lat - 0.006;
        final double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        final double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
        final double gg_lng = z * Math.cos(theta);
        final double gg_lat = z * Math.sin(theta);
        return new Point(gg_lng, gg_lat);
    }

    /**
     * Converts between Baidu coordinate system (BD-09) and WGS84.
     *
     * @param lng The longitude.
     * @param lat The latitude.
     * @return The WGS84 coordinates.
     */
    public static Point bd09toWgs84(final double lng, final double lat) {
        final Point gcj02 = bd09ToGcj02(lng, lat);
        return gcj02ToWgs84(gcj02.lng, gcj02.lat);
    }

    /**
     * Converts WGS84 coordinates to Mercator projection.
     *
     * @param lng The longitude.
     * @param lat The latitude.
     * @return The Mercator projection.
     */
    public static Point wgs84ToMercator(final double lng, final double lat) {
        final double x = lng * 20037508.342789244 / 180;
        double y = Math.log(Math.tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180);
        y = y * 20037508.342789244 / 180;
        return new Point(x, y);
    }

    /**
     * Converts Mercator projection to WGS84 coordinates.
     *
     * @param mercatorX The Mercator X coordinate.
     * @param mercatorY The Mercator Y coordinate.
     * @return The WGS84 coordinates.
     */
    public static Point mercatorToWgs84(final double mercatorX, final double mercatorY) {
        final double x = mercatorX / 20037508.342789244 * 180;
        double y = mercatorY / 20037508.342789244 * 180;
        y = 180 / Math.PI * (2 * Math.atan(Math.exp(y * Math.PI / 180)) - Math.PI / 2);
        return new Point(x, y);
    }

    /**
     * The offset algorithm for converting between WGS84 and Mars coordinate system (GCJ-02) (non-precise).
     *
     * @param lng    The longitude.
     * @param lat    The latitude.
     * @param isPlus Whether to apply a positive offset: use positive for WGS84 to GCJ-02, otherwise use negative.
     * @return The offset coordinates.
     */
    private static Point offset(final double lng, final double lat, final boolean isPlus) {
        double dlng = transLng(lng - 105.0, lat - 35.0);
        double dlat = transLat(lng - 105.0, lat - 35.0);

        double magic = Math.sin(lat / 180.0 * PI);
        magic = 1 - CORRECTION_PARAM * magic * magic;
        final double sqrtMagic = Math.sqrt(magic);

        dlng = (dlng * 180.0) / (RADIUS / sqrtMagic * Math.cos(lat / 180.0 * PI) * PI);
        dlat = (dlat * 180.0) / ((RADIUS * (1 - CORRECTION_PARAM)) / (magic * sqrtMagic) * PI);

        if (!isPlus) {
            dlng = -dlng;
            dlat = -dlat;
        }

        return new Point(dlng, dlat);
    }

    /**
     * Calculates the longitude coordinate.
     *
     * @param lng The longitude coordinate.
     * @param lat The latitude coordinate.
     * @return The calculated longitude.
     */
    private static double transLng(final double lng, final double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * Calculates the latitude coordinate.
     *
     * @param lng The longitude.
     * @param lat The latitude.
     * @return The calculated latitude.
     */
    private static double transLat(final double lng, final double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat
                + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * Represents a point with longitude and latitude.
     */
    public static class Point implements Serializable {

        @Serial
        private static final long serialVersionUID = 2852275287953L;

        /**
         * The longitude.
         */
        private double lng;
        /**
         * The latitude.
         */
        private double lat;

        /**
         * Constructor.
         *
         * @param lng The longitude.
         * @param lat The latitude.
         */
        public Point(final double lng, final double lat) {
            this.lng = lng;
            this.lat = lat;
        }

        /**
         * Gets the longitude.
         *
         * @return The longitude.
         */
        public double getLng() {
            return lng;
        }

        /**
         * Sets the longitude.
         *
         * @param lng The longitude.
         * @return this
         */
        public Point setLng(final double lng) {
            this.lng = lng;
            return this;
        }

        /**
         * Gets the latitude.
         *
         * @return The latitude.
         */
        public double getLat() {
            return lat;
        }

        /**
         * Sets the latitude.
         *
         * @param lat The latitude.
         * @return this
         */
        public Point setLat(final double lat) {
            this.lat = lat;
            return this;
        }

        /**
         * Offsets the current coordinates by the specified amount.
         *
         * @param offset The offset.
         * @return this
         */
        public Point offset(final Point offset) {
            this.lng += offset.lng;
            this.lat += offset.lat;
            return this;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Point that = (Point) o;
            return Double.compare(that.lng, lng) == 0 && Double.compare(that.lat, lat) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(lng, lat);
        }

        @Override
        public String toString() {
            return "Point{" + "lng=" + lng + ", lat=" + lat + '}';
        }
    }

}
