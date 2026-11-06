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
package org.miaixz.bus.opencv.utils;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.opencv.core.CvType;
import org.miaixz.bus.opencv.core.DMatch;
import org.miaixz.bus.opencv.core.KeyPoint;
import org.miaixz.bus.opencv.core.Mat;
import org.miaixz.bus.opencv.core.MatOfByte;
import org.miaixz.bus.opencv.core.MatOfDMatch;
import org.miaixz.bus.opencv.core.MatOfInt;
import org.miaixz.bus.opencv.core.MatOfKeyPoint;
import org.miaixz.bus.opencv.core.MatOfPoint;
import org.miaixz.bus.opencv.core.MatOfPoint2f;
import org.miaixz.bus.opencv.core.MatOfPoint3f;
import org.miaixz.bus.opencv.core.Point;
import org.miaixz.bus.opencv.core.Point3;
import org.miaixz.bus.opencv.core.Rect;
import org.miaixz.bus.opencv.core.Rect2d;
import org.miaixz.bus.opencv.core.RotatedRect;
import org.miaixz.bus.opencv.core.Size;

/**
 * Conversions between native matrix encodings and Java arrays, lists, points, matches, and other value objects.
 */
public class Converters {

    /**
     * Creates a Java API container. The utility operations are available through static methods.
     */
    public Converters() {
        // No initialization required.
    }

    /**
     * Encodes a Java list of Point into a native matrix representation.
     *
     * @param pts source list of point records to encode
     * @return a matrix encoding the Point values; nonempty inputs use {@code CV_32S}
     */
    public static Mat vector_Point_to_Mat(List<Point> pts) {
        return vector_Point_to_Mat(pts, CvType.CV_32S);
    }

    /**
     * Encodes a Java list of Point2f into a native matrix representation.
     *
     * @param pts source list of point records to encode
     * @return a matrix encoding the Point2f values; nonempty inputs use {@code CV_32F}
     */
    public static Mat vector_Point2f_to_Mat(List<Point> pts) {
        return vector_Point_to_Mat(pts, CvType.CV_32F);
    }

    /**
     * Encodes a Java list of Point2d into a native matrix representation.
     *
     * @param pts source list of point records to encode
     * @return a matrix encoding the Point2d values; nonempty inputs use {@code CV_64F}
     */
    public static Mat vector_Point2d_to_Mat(List<Point> pts) {
        return vector_Point_to_Mat(pts, CvType.CV_64F);
    }

    /**
     * Encodes a Java list of Point into a native matrix representation.
     *
     * @param pts       source list of point records to encode
     * @param typeDepth coordinate element depth: CV_32S, CV_32F, or CV_64F
     * @return a matrix encoding the Point values; nonempty inputs use {@code CV_32S, CV_32SC2, CV_32F, CV_32FC2,
     *     CV_64F, CV_64FC2}
     * @throws IllegalArgumentException if typeDepth is not CV_32S, CV_32F or CV_64F
     */
    public static Mat vector_Point_to_Mat(List<Point> pts, int typeDepth) {
        Mat res;
        int count = (pts != null) ? pts.size() : 0;
        if (count > 0) {
            switch (typeDepth) {
                case CvType.CV_32S: {
                    res = new Mat(count, 1, CvType.CV_32SC2);
                    int[] buff = new int[count * 2];
                    for (int i = 0; i < count; i++) {
                        Point p = pts.get(i);
                        buff[i * 2] = (int) p.x;
                        buff[i * 2 + 1] = (int) p.y;
                    }
                    res.put(0, 0, buff);
                }
                    break;

                case CvType.CV_32F: {
                    res = new Mat(count, 1, CvType.CV_32FC2);
                    float[] buff = new float[count * 2];
                    for (int i = 0; i < count; i++) {
                        Point p = pts.get(i);
                        buff[i * 2] = (float) p.x;
                        buff[i * 2 + 1] = (float) p.y;
                    }
                    res.put(0, 0, buff);
                }
                    break;

                case CvType.CV_64F: {
                    res = new Mat(count, 1, CvType.CV_64FC2);
                    double[] buff = new double[count * 2];
                    for (int i = 0; i < count; i++) {
                        Point p = pts.get(i);
                        buff[i * 2] = p.x;
                        buff[i * 2 + 1] = p.y;
                    }
                    res.put(0, 0, buff);
                }
                    break;

                default:
                    throw new IllegalArgumentException("'typeDepth' can be CV_32S, CV_32F or CV_64F");
            }
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Encodes a Java list of Point3i into a native matrix representation.
     *
     * @param pts source list of point records to encode
     * @return a matrix encoding the Point3i values; nonempty inputs use {@code CV_32S}
     */
    public static Mat vector_Point3i_to_Mat(List<Point3> pts) {
        return vector_Point3_to_Mat(pts, CvType.CV_32S);
    }

    /**
     * Encodes a Java list of Point3f into a native matrix representation.
     *
     * @param pts source list of point records to encode
     * @return a matrix encoding the Point3f values; nonempty inputs use {@code CV_32F}
     */
    public static Mat vector_Point3f_to_Mat(List<Point3> pts) {
        return vector_Point3_to_Mat(pts, CvType.CV_32F);
    }

    /**
     * Encodes a Java list of Point3d into a native matrix representation.
     *
     * @param pts source list of point records to encode
     * @return a matrix encoding the Point3d values; nonempty inputs use {@code CV_64F}
     */
    public static Mat vector_Point3d_to_Mat(List<Point3> pts) {
        return vector_Point3_to_Mat(pts, CvType.CV_64F);
    }

    /**
     * Encodes a Java list of Point3 into a native matrix representation.
     *
     * @param pts       source list of point records to encode
     * @param typeDepth coordinate element depth: CV_32S, CV_32F, or CV_64F
     * @return a matrix encoding the Point3 values; nonempty inputs use {@code CV_32S, CV_32SC3, CV_32F,
     *     CV_32FC3, CV_64F, CV_64FC3}
     * @throws IllegalArgumentException if typeDepth is not CV_32S, CV_32F or CV_64F
     */
    public static Mat vector_Point3_to_Mat(List<Point3> pts, int typeDepth) {
        Mat res;
        int count = (pts != null) ? pts.size() : 0;
        if (count > 0) {
            switch (typeDepth) {
                case CvType.CV_32S: {
                    res = new Mat(count, 1, CvType.CV_32SC3);
                    int[] buff = new int[count * 3];
                    for (int i = 0; i < count; i++) {
                        Point3 p = pts.get(i);
                        buff[i * 3] = (int) p.x;
                        buff[i * 3 + 1] = (int) p.y;
                        buff[i * 3 + 2] = (int) p.z;
                    }
                    res.put(0, 0, buff);
                }
                    break;

                case CvType.CV_32F: {
                    res = new Mat(count, 1, CvType.CV_32FC3);
                    float[] buff = new float[count * 3];
                    for (int i = 0; i < count; i++) {
                        Point3 p = pts.get(i);
                        buff[i * 3] = (float) p.x;
                        buff[i * 3 + 1] = (float) p.y;
                        buff[i * 3 + 2] = (float) p.z;
                    }
                    res.put(0, 0, buff);
                }
                    break;

                case CvType.CV_64F: {
                    res = new Mat(count, 1, CvType.CV_64FC3);
                    double[] buff = new double[count * 3];
                    for (int i = 0; i < count; i++) {
                        Point3 p = pts.get(i);
                        buff[i * 3] = p.x;
                        buff[i * 3 + 1] = p.y;
                        buff[i * 3 + 2] = p.z;
                    }
                    res.put(0, 0, buff);
                }
                    break;

                default:
                    throw new IllegalArgumentException("'typeDepth' can be CV_32S, CV_32F or CV_64F");
            }
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into a Java list of Point2f.
     *
     * @param m   matrix containing the encoded vector representation required by this conversion
     * @param pts destination list receiving the decoded point records
     */
    public static void Mat_to_vector_Point2f(Mat m, List<Point> pts) {
        Mat_to_vector_Point(m, pts);
    }

    /**
     * Decodes a native matrix representation into a Java list of Point2d.
     *
     * @param m   matrix containing the encoded vector representation required by this conversion
     * @param pts destination list receiving the decoded point records
     */
    public static void Mat_to_vector_Point2d(Mat m, List<Point> pts) {
        Mat_to_vector_Point(m, pts);
    }

    /**
     * Decodes a native matrix representation into a Java list of Point.
     *
     * @param m   matrix containing the encoded vector representation required by this conversion
     * @param pts destination list receiving the decoded point records
     * @throws IllegalArgumentException if pts is null; or if m does not have exactly one column; or if the matrix
     *                                  coordinate type is not CV_32SC2, CV_32FC2 or CV_64FC2
     * @throws NullPointerException     if m is null
     */
    public static void Mat_to_vector_Point(Mat m, List<Point> pts) {
        if (pts == null)
            throw new IllegalArgumentException("Output List can't be null");
        int count = m.rows();
        int type = m.type();
        if (m.cols() != 1)
            throw new IllegalArgumentException("Input Mat should have one column\n" + m);

        pts.clear();
        if (type == CvType.CV_32SC2) {
            int[] buff = new int[2 * count];
            m.get(0, 0, buff);
            for (int i = 0; i < count; i++) {
                pts.add(new Point(buff[i * 2], buff[i * 2 + 1]));
            }
        } else if (type == CvType.CV_32FC2) {
            float[] buff = new float[2 * count];
            m.get(0, 0, buff);
            for (int i = 0; i < count; i++) {
                pts.add(new Point(buff[i * 2], buff[i * 2 + 1]));
            }
        } else if (type == CvType.CV_64FC2) {
            double[] buff = new double[2 * count];
            m.get(0, 0, buff);
            for (int i = 0; i < count; i++) {
                pts.add(new Point(buff[i * 2], buff[i * 2 + 1]));
            }
        } else {
            throw new IllegalArgumentException("Input Mat should be of CV_32SC2, CV_32FC2 or CV_64FC2 type\n" + m);
        }
    }

    /**
     * Decodes a native matrix representation into a Java list of Point3i.
     *
     * @param m   matrix containing the encoded vector representation required by this conversion
     * @param pts destination list receiving the decoded point records
     */
    public static void Mat_to_vector_Point3i(Mat m, List<Point3> pts) {
        Mat_to_vector_Point3(m, pts);
    }

    /**
     * Decodes a native matrix representation into a Java list of Point3f.
     *
     * @param m   matrix containing the encoded vector representation required by this conversion
     * @param pts destination list receiving the decoded point records
     */
    public static void Mat_to_vector_Point3f(Mat m, List<Point3> pts) {
        Mat_to_vector_Point3(m, pts);
    }

    /**
     * Decodes a native matrix representation into a Java list of Point3d.
     *
     * @param m   matrix containing the encoded vector representation required by this conversion
     * @param pts destination list receiving the decoded point records
     */
    public static void Mat_to_vector_Point3d(Mat m, List<Point3> pts) {
        Mat_to_vector_Point3(m, pts);
    }

    /**
     * Decodes a native matrix representation into a Java list of Point3.
     *
     * @param m   matrix containing the encoded vector representation required by this conversion
     * @param pts destination list receiving the decoded point records
     * @throws IllegalArgumentException if pts is null; or if m does not have exactly one column; or if the matrix
     *                                  coordinate type is not CV_32SC3, CV_32FC3 or CV_64FC3
     * @throws NullPointerException     if m is null
     */
    public static void Mat_to_vector_Point3(Mat m, List<Point3> pts) {
        if (pts == null)
            throw new IllegalArgumentException("Output List can't be null");
        int count = m.rows();
        int type = m.type();
        if (m.cols() != 1)
            throw new IllegalArgumentException("Input Mat should have one column\n" + m);

        pts.clear();
        if (type == CvType.CV_32SC3) {
            int[] buff = new int[3 * count];
            m.get(0, 0, buff);
            for (int i = 0; i < count; i++) {
                pts.add(new Point3(buff[i * 3], buff[i * 3 + 1], buff[i * 3 + 2]));
            }
        } else if (type == CvType.CV_32FC3) {
            float[] buff = new float[3 * count];
            m.get(0, 0, buff);
            for (int i = 0; i < count; i++) {
                pts.add(new Point3(buff[i * 3], buff[i * 3 + 1], buff[i * 3 + 2]));
            }
        } else if (type == CvType.CV_64FC3) {
            double[] buff = new double[3 * count];
            m.get(0, 0, buff);
            for (int i = 0; i < count; i++) {
                pts.add(new Point3(buff[i * 3], buff[i * 3 + 1], buff[i * 3 + 2]));
            }
        } else {
            throw new IllegalArgumentException("Input Mat should be of CV_32SC3, CV_32FC3 or CV_64FC3 type\n" + m);
        }
    }

    /**
     * Encodes a Java list of Mat into a native matrix representation.
     *
     * @param mats source matrices to encode as native handles
     * @return a matrix encoding the Mat values; nonempty inputs use {@code CV_32SC2}
     */
    public static Mat vector_Mat_to_Mat(List<Mat> mats) {
        Mat res;
        int count = (mats != null) ? mats.size() : 0;
        if (count > 0) {
            res = new Mat(count, 1, CvType.CV_32SC2);
            int[] buff = new int[count * 2];
            for (int i = 0; i < count; i++) {
                long addr = mats.get(i).nativeObj;
                buff[i * 2] = (int) (addr >> 32);
                buff[i * 2 + 1] = (int) (addr & 0xffffffff);
            }
            res.put(0, 0, buff);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into a Java list of Mat.
     *
     * @param m    matrix containing the encoded vector representation required by this conversion
     * @param mats destination list receiving matrix wrappers for the decoded native handles
     * @throws IllegalArgumentException if mats is null; or if m does not have type {@code CvType.CV_32SC2} and exactly
     *                                  one column
     * @throws NullPointerException     if m is null
     */
    public static void Mat_to_vector_Mat(Mat m, List<Mat> mats) {
        if (mats == null)
            throw new IllegalArgumentException("mats == null");
        int count = m.rows();
        if (CvType.CV_32SC2 != m.type() || m.cols() != 1)
            throw new IllegalArgumentException("CvType.CV_32SC2 != m.type() ||  m.cols()!=1\n" + m);

        mats.clear();
        int[] buff = new int[count * 2];
        m.get(0, 0, buff);
        for (int i = 0; i < count; i++) {
            long addr = (((long) buff[i * 2]) << 32) | (((long) buff[i * 2 + 1]) & 0xffffffffL);
            mats.add(new Mat(addr));
        }
    }

    /**
     * Encodes a Java list of float into a native matrix representation.
     *
     * @param fs source list of floating-point channel values
     * @return a matrix encoding the float values; nonempty inputs use {@code CV_32FC1}
     */
    public static Mat vector_float_to_Mat(List<Float> fs) {
        Mat res;
        int count = (fs != null) ? fs.size() : 0;
        if (count > 0) {
            res = new Mat(count, 1, CvType.CV_32FC1);
            float[] buff = new float[count];
            for (int i = 0; i < count; i++) {
                float f = fs.get(i);
                buff[i] = f;
            }
            res.put(0, 0, buff);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into a Java list of float.
     *
     * @param m  matrix containing the encoded vector representation required by this conversion
     * @param fs destination list of floating-point channel values
     * @throws IllegalArgumentException if fs is null; or if m does not have type {@code CvType.CV_32FC1} and exactly
     *                                  one column
     * @throws NullPointerException     if m is null
     */
    public static void Mat_to_vector_float(Mat m, List<Float> fs) {
        if (fs == null)
            throw new IllegalArgumentException("fs == null");
        int count = m.rows();
        if (CvType.CV_32FC1 != m.type() || m.cols() != 1)
            throw new IllegalArgumentException("CvType.CV_32FC1 != m.type() ||  m.cols()!=1\n" + m);

        fs.clear();
        float[] buff = new float[count];
        m.get(0, 0, buff);
        for (int i = 0; i < count; i++) {
            fs.add(buff[i]);
        }
    }

    /**
     * Encodes a Java list of uchar into a native matrix representation.
     *
     * @param bs source list of signed byte values
     * @return a matrix encoding the uchar values; nonempty inputs use {@code CV_8UC1}
     */
    public static Mat vector_uchar_to_Mat(List<Byte> bs) {
        Mat res;
        int count = (bs != null) ? bs.size() : 0;
        if (count > 0) {
            res = new Mat(count, 1, CvType.CV_8UC1);
            byte[] buff = new byte[count];
            for (int i = 0; i < count; i++) {
                byte b = bs.get(i);
                buff[i] = b;
            }
            res.put(0, 0, buff);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into a Java list of uchar.
     *
     * @param m  matrix containing the encoded vector representation required by this conversion
     * @param us destination list of unsigned-byte values represented by Java bytes
     * @throws IllegalArgumentException if us is null; or if m does not have type {@code CvType.CV_8UC1} and exactly one
     *                                  column
     * @throws NullPointerException     if m is null
     */
    public static void Mat_to_vector_uchar(Mat m, List<Byte> us) {
        if (us == null)
            throw new IllegalArgumentException("Output List can't be null");
        int count = m.rows();
        if (CvType.CV_8UC1 != m.type() || m.cols() != 1)
            throw new IllegalArgumentException("CvType.CV_8UC1 != m.type() ||  m.cols()!=1\n" + m);

        us.clear();
        byte[] buff = new byte[count];
        m.get(0, 0, buff);
        for (int i = 0; i < count; i++) {
            us.add(buff[i]);
        }
    }

    /**
     * Encodes a Java list of char into a native matrix representation.
     *
     * @param bs source list of signed byte values
     * @return a matrix encoding the char values; nonempty inputs use {@code CV_8SC1}
     */
    public static Mat vector_char_to_Mat(List<Byte> bs) {
        Mat res;
        int count = (bs != null) ? bs.size() : 0;
        if (count > 0) {
            res = new Mat(count, 1, CvType.CV_8SC1);
            byte[] buff = new byte[count];
            for (int i = 0; i < count; i++) {
                byte b = bs.get(i);
                buff[i] = b;
            }
            res.put(0, 0, buff);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Encodes a Java list of int into a native matrix representation.
     *
     * @param is source list of integer values
     * @return a matrix encoding the int values; nonempty inputs use {@code CV_32SC1}
     */
    public static Mat vector_int_to_Mat(List<Integer> is) {
        Mat res;
        int count = (is != null) ? is.size() : 0;
        if (count > 0) {
            res = new Mat(count, 1, CvType.CV_32SC1);
            int[] buff = new int[count];
            for (int i = 0; i < count; i++) {
                int v = is.get(i);
                buff[i] = v;
            }
            res.put(0, 0, buff);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into a Java list of int.
     *
     * @param m  matrix containing the encoded vector representation required by this conversion
     * @param is destination list of integer values
     * @throws IllegalArgumentException if is is null; or if m does not have type {@code CvType.CV_32SC1} and exactly
     *                                  one column
     * @throws NullPointerException     if m is null
     */
    public static void Mat_to_vector_int(Mat m, List<Integer> is) {
        if (is == null)
            throw new IllegalArgumentException("is == null");
        int count = m.rows();
        if (CvType.CV_32SC1 != m.type() || m.cols() != 1)
            throw new IllegalArgumentException("CvType.CV_32SC1 != m.type() ||  m.cols()!=1\n" + m);

        is.clear();
        int[] buff = new int[count];
        m.get(0, 0, buff);
        for (int i = 0; i < count; i++) {
            is.add(buff[i]);
        }
    }

    /**
     * Decodes a native matrix representation into a Java list of char.
     *
     * @param m  matrix containing the encoded vector representation required by this conversion
     * @param bs destination list of signed byte values
     * @throws IllegalArgumentException if bs is null; or if m does not have type {@code CvType.CV_8SC1} and exactly one
     *                                  column
     * @throws NullPointerException     if m is null
     */
    public static void Mat_to_vector_char(Mat m, List<Byte> bs) {
        if (bs == null)
            throw new IllegalArgumentException("Output List can't be null");
        int count = m.rows();
        if (CvType.CV_8SC1 != m.type() || m.cols() != 1)
            throw new IllegalArgumentException("CvType.CV_8SC1 != m.type() ||  m.cols()!=1\n" + m);

        bs.clear();
        byte[] buff = new byte[count];
        m.get(0, 0, buff);
        for (int i = 0; i < count; i++) {
            bs.add(buff[i]);
        }
    }

    /**
     * Encodes a Java list of Rect into a native matrix representation.
     *
     * @param rs source list of rectangles
     * @return a matrix encoding the Rect values; nonempty inputs use {@code CV_32SC4}
     */
    public static Mat vector_Rect_to_Mat(List<Rect> rs) {
        Mat res;
        int count = (rs != null) ? rs.size() : 0;
        if (count > 0) {
            res = new Mat(count, 1, CvType.CV_32SC4);
            int[] buff = new int[4 * count];
            for (int i = 0; i < count; i++) {
                Rect r = rs.get(i);
                buff[4 * i] = r.x;
                buff[4 * i + 1] = r.y;
                buff[4 * i + 2] = r.width;
                buff[4 * i + 3] = r.height;
            }
            res.put(0, 0, buff);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into a Java list of Rect.
     *
     * @param m  matrix containing the encoded vector representation required by this conversion
     * @param rs destination list of rectangles
     * @throws IllegalArgumentException if rs is null; or if m does not have type {@code CvType.CV_32SC4} and exactly
     *                                  one column
     * @throws NullPointerException     if m is null
     */
    public static void Mat_to_vector_Rect(Mat m, List<Rect> rs) {
        if (rs == null)
            throw new IllegalArgumentException("rs == null");
        int count = m.rows();
        if (CvType.CV_32SC4 != m.type() || m.cols() != 1)
            throw new IllegalArgumentException("CvType.CV_32SC4 != m.type() ||  m.rows()!=1\n" + m);

        rs.clear();
        int[] buff = new int[4 * count];
        m.get(0, 0, buff);
        for (int i = 0; i < count; i++) {
            rs.add(new Rect(buff[4 * i], buff[4 * i + 1], buff[4 * i + 2], buff[4 * i + 3]));
        }
    }

    /**
     * Encodes a Java list of Rect2d into a native matrix representation.
     *
     * @param rs source list of rectangles
     * @return a matrix encoding the Rect2d values; nonempty inputs use {@code CV_64FC4}
     */
    public static Mat vector_Rect2d_to_Mat(List<Rect2d> rs) {
        Mat res;
        int count = (rs != null) ? rs.size() : 0;
        if (count > 0) {
            res = new Mat(count, 1, CvType.CV_64FC4);
            double[] buff = new double[4 * count];
            for (int i = 0; i < count; i++) {
                Rect2d r = rs.get(i);
                buff[4 * i] = r.x;
                buff[4 * i + 1] = r.y;
                buff[4 * i + 2] = r.width;
                buff[4 * i + 3] = r.height;
            }
            res.put(0, 0, buff);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into a Java list of Rect2d.
     *
     * @param m  matrix containing the encoded vector representation required by this conversion
     * @param rs destination list of rectangles
     * @throws IllegalArgumentException if rs is null; or if m does not have type {@code CvType.CV_64FC4} and exactly
     *                                  one column
     * @throws NullPointerException     if m is null
     */
    public static void Mat_to_vector_Rect2d(Mat m, List<Rect2d> rs) {
        if (rs == null)
            throw new IllegalArgumentException("rs == null");
        int count = m.rows();
        if (CvType.CV_64FC4 != m.type() || m.cols() != 1)
            throw new IllegalArgumentException("CvType.CV_64FC4 != m.type() ||  m.rows()!=1\n" + m);

        rs.clear();
        double[] buff = new double[4 * count];
        m.get(0, 0, buff);
        for (int i = 0; i < count; i++) {
            rs.add(new Rect2d(buff[4 * i], buff[4 * i + 1], buff[4 * i + 2], buff[4 * i + 3]));
        }
    }

    /**
     * Encodes a Java list of Key Point into a native matrix representation.
     *
     * @param kps source list of keypoints
     * @return a matrix encoding the KeyPoint values; nonempty inputs use {@code CV_64FC(7)}
     */
    public static Mat vector_KeyPoint_to_Mat(List<KeyPoint> kps) {
        Mat res;
        int count = (kps != null) ? kps.size() : 0;
        if (count > 0) {
            res = new Mat(count, 1, CvType.CV_64FC(7));
            double[] buff = new double[count * 7];
            for (int i = 0; i < count; i++) {
                KeyPoint kp = kps.get(i);
                buff[7 * i] = kp.pt.x;
                buff[7 * i + 1] = kp.pt.y;
                buff[7 * i + 2] = kp.size;
                buff[7 * i + 3] = kp.angle;
                buff[7 * i + 4] = kp.response;
                buff[7 * i + 5] = kp.octave;
                buff[7 * i + 6] = kp.class_id;
            }
            res.put(0, 0, buff);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into a Java list of Key Point.
     *
     * @param m   matrix containing the encoded vector representation required by this conversion
     * @param kps destination list of keypoints
     * @throws IllegalArgumentException if kps is null; or if m does not have type {@code CvType.CV_64FC(7)} and exactly
     *                                  one column
     * @throws NullPointerException     if m is null
     */
    public static void Mat_to_vector_KeyPoint(Mat m, List<KeyPoint> kps) {
        if (kps == null)
            throw new IllegalArgumentException("Output List can't be null");
        int count = m.rows();
        if (CvType.CV_64FC(7) != m.type() || m.cols() != 1)
            throw new IllegalArgumentException("CvType.CV_64FC(7) != m.type() ||  m.cols()!=1\n" + m);

        kps.clear();
        double[] buff = new double[7 * count];
        m.get(0, 0, buff);
        for (int i = 0; i < count; i++) {
            kps.add(
                    new KeyPoint((float) buff[7 * i], (float) buff[7 * i + 1], (float) buff[7 * i + 2],
                            (float) buff[7 * i + 3], (float) buff[7 * i + 4], (int) buff[7 * i + 5],
                            (int) buff[7 * i + 6]));
        }
    }

    /**
     * Encodes a nested list of Mat into a native matrix representation.
     *
     * @param vecMats source collection of matrix lists
     * @param mats    source matrices to encode as native handles
     * @return a matrix encoding the nested Mat values
     * @throws NullPointerException if mats is null
     */
    public static Mat vector_vector_Mat_to_Mat(List<List<Mat>> vecMats, List<Mat> mats) {
        Mat res;
        int lCount = (vecMats != null) ? vecMats.size() : 0;
        if (lCount > 0) {
            for (List<Mat> matList : vecMats) {
                Mat mat = vector_Mat_to_Mat(matList);
                mats.add(mat);
            }
            res = vector_Mat_to_Mat(mats);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into nested list of Mat.
     *
     * @param m       matrix containing the encoded vector representation required by this conversion
     * @param vecMats destination collection of matrix lists
     * @throws IllegalArgumentException if vecMats is null; or if m is null
     */
    public static void Mat_to_vector_vector_Mat(Mat m, List<List<Mat>> vecMats) {
        if (vecMats == null)
            throw new IllegalArgumentException("Output List can't be null");

        if (m == null)
            throw new IllegalArgumentException("Input Mat can't be null");

        vecMats.clear();
        List<Mat> mats = new ArrayList<Mat>(m.rows());
        Mat_to_vector_Mat(m, mats);
        for (Mat mi : mats) {
            List<Mat> rowList = new ArrayList<Mat>(mi.rows());
            Mat_to_vector_Mat(mi, rowList);
            vecMats.add(rowList);
            mi.release();
        }
        mats.clear();
    }

    /**
     * Encodes a nested list of Point into a native matrix representation.
     *
     * @param pts  source list of point records to encode
     * @param mats source matrices to encode as native handles
     * @return a matrix encoding the nested Point values
     * @throws NullPointerException if mats is null
     */
    public static Mat vector_vector_Point_to_Mat(List<MatOfPoint> pts, List<Mat> mats) {
        Mat res;
        int lCount = (pts != null) ? pts.size() : 0;
        if (lCount > 0) {
            mats.addAll(pts);
            res = vector_Mat_to_Mat(mats);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into nested list of Point.
     *
     * @param m   matrix containing the encoded vector representation required by this conversion
     * @param pts destination list receiving the decoded point records
     * @throws IllegalArgumentException if pts is null; or if m is null
     */
    public static void Mat_to_vector_vector_Point(Mat m, List<MatOfPoint> pts) {
        if (pts == null)
            throw new IllegalArgumentException("Output List can't be null");

        if (m == null)
            throw new IllegalArgumentException("Input Mat can't be null");

        List<Mat> mats = new ArrayList<Mat>(m.rows());
        Mat_to_vector_Mat(m, mats);
        for (Mat mi : mats) {
            MatOfPoint pt = new MatOfPoint(mi);
            pts.add(pt);
            mi.release();
        }
        mats.clear();
    }

    /**
     * Decodes a native matrix representation into nested list of Point2f.
     *
     * @param m   matrix containing the encoded vector representation required by this conversion
     * @param pts destination list receiving the decoded point records
     * @throws IllegalArgumentException if pts is null; or if m is null
     */
    public static void Mat_to_vector_vector_Point2f(Mat m, List<MatOfPoint2f> pts) {
        if (pts == null)
            throw new IllegalArgumentException("Output List can't be null");

        if (m == null)
            throw new IllegalArgumentException("Input Mat can't be null");

        List<Mat> mats = new ArrayList<Mat>(m.rows());
        Mat_to_vector_Mat(m, mats);
        for (Mat mi : mats) {
            MatOfPoint2f pt = new MatOfPoint2f(mi);
            pts.add(pt);
            mi.release();
        }
        mats.clear();
    }

    /**
     * Encodes a nested list of Point2f into a native matrix representation.
     *
     * @param pts  source list of point records to encode
     * @param mats source matrices to encode as native handles
     * @return a matrix encoding the nested Point2f values
     * @throws NullPointerException if mats is null
     */
    public static Mat vector_vector_Point2f_to_Mat(List<MatOfPoint2f> pts, List<Mat> mats) {
        Mat res;
        int lCount = (pts != null) ? pts.size() : 0;
        if (lCount > 0) {
            mats.addAll(pts);
            res = vector_Mat_to_Mat(mats);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into nested list of Point3f.
     *
     * @param m   matrix containing the encoded vector representation required by this conversion
     * @param pts destination list receiving the decoded point records
     * @throws IllegalArgumentException if pts is null; or if m is null
     */
    public static void Mat_to_vector_vector_Point3f(Mat m, List<MatOfPoint3f> pts) {
        if (pts == null)
            throw new IllegalArgumentException("Output List can't be null");

        if (m == null)
            throw new IllegalArgumentException("Input Mat can't be null");

        List<Mat> mats = new ArrayList<Mat>(m.rows());
        Mat_to_vector_Mat(m, mats);
        for (Mat mi : mats) {
            MatOfPoint3f pt = new MatOfPoint3f(mi);
            pts.add(pt);
            mi.release();
        }
        mats.clear();
    }

    /**
     * Encodes a nested list of Point3f into a native matrix representation.
     *
     * @param pts  source list of point records to encode
     * @param mats source matrices to encode as native handles
     * @return a matrix encoding the nested Point3f values
     * @throws NullPointerException if mats is null
     */
    public static Mat vector_vector_Point3f_to_Mat(List<MatOfPoint3f> pts, List<Mat> mats) {
        Mat res;
        int lCount = (pts != null) ? pts.size() : 0;
        if (lCount > 0) {
            mats.addAll(pts);
            res = vector_Mat_to_Mat(mats);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Encodes a nested list of Key Point into a native matrix representation.
     *
     * @param kps  source list of keypoints
     * @param mats source matrices to encode as native handles
     * @return a matrix encoding the nested KeyPoint values
     * @throws NullPointerException if mats is null
     */
    public static Mat vector_vector_KeyPoint_to_Mat(List<MatOfKeyPoint> kps, List<Mat> mats) {
        Mat res;
        int lCount = (kps != null) ? kps.size() : 0;
        if (lCount > 0) {
            mats.addAll(kps);
            res = vector_Mat_to_Mat(mats);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into nested list of Key Point.
     *
     * @param m   matrix containing the encoded vector representation required by this conversion
     * @param kps destination list of keypoints
     * @throws IllegalArgumentException if kps is null; or if m is null
     */
    public static void Mat_to_vector_vector_KeyPoint(Mat m, List<MatOfKeyPoint> kps) {
        if (kps == null)
            throw new IllegalArgumentException("Output List can't be null");

        if (m == null)
            throw new IllegalArgumentException("Input Mat can't be null");

        List<Mat> mats = new ArrayList<Mat>(m.rows());
        Mat_to_vector_Mat(m, mats);
        for (Mat mi : mats) {
            MatOfKeyPoint vkp = new MatOfKeyPoint(mi);
            kps.add(vkp);
            mi.release();
        }
        mats.clear();
    }

    /**
     * Encodes a Java list of double into a native matrix representation.
     *
     * @param ds source list of double-precision values
     * @return a matrix encoding the double values; nonempty inputs use {@code CV_64FC1}
     */
    public static Mat vector_double_to_Mat(List<Double> ds) {
        Mat res;
        int count = (ds != null) ? ds.size() : 0;
        if (count > 0) {
            res = new Mat(count, 1, CvType.CV_64FC1);
            double[] buff = new double[count];
            for (int i = 0; i < count; i++) {
                double v = ds.get(i);
                buff[i] = v;
            }
            res.put(0, 0, buff);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into a Java list of double.
     *
     * @param m  matrix containing the encoded vector representation required by this conversion
     * @param ds destination list of double-precision values
     * @throws IllegalArgumentException if ds is null; or if m does not have type {@code CvType.CV_64FC1} and exactly
     *                                  one column
     * @throws NullPointerException     if m is null
     */
    public static void Mat_to_vector_double(Mat m, List<Double> ds) {
        if (ds == null)
            throw new IllegalArgumentException("ds == null");
        int count = m.rows();
        if (CvType.CV_64FC1 != m.type() || m.cols() != 1)
            throw new IllegalArgumentException("CvType.CV_64FC1 != m.type() ||  m.cols()!=1\n" + m);

        ds.clear();
        double[] buff = new double[count];
        m.get(0, 0, buff);
        for (int i = 0; i < count; i++) {
            ds.add(buff[i]);
        }
    }

    /**
     * Encodes a Java list of DMatch into a native matrix representation.
     *
     * @param matches source list of descriptor matches
     * @return a matrix encoding the DMatch values; nonempty inputs use {@code CV_64FC4}
     */
    public static Mat vector_DMatch_to_Mat(List<DMatch> matches) {
        Mat res;
        int count = (matches != null) ? matches.size() : 0;
        if (count > 0) {
            res = new Mat(count, 1, CvType.CV_64FC4);
            double[] buff = new double[count * 4];
            for (int i = 0; i < count; i++) {
                DMatch m = matches.get(i);
                buff[4 * i] = m.queryIdx;
                buff[4 * i + 1] = m.trainIdx;
                buff[4 * i + 2] = m.imgIdx;
                buff[4 * i + 3] = m.distance;
            }
            res.put(0, 0, buff);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into a Java list of DMatch.
     *
     * @param m       matrix containing the encoded vector representation required by this conversion
     * @param matches destination list of descriptor matches
     * @throws IllegalArgumentException if matches is null; or if m does not have type {@code CvType.CV_64FC4} and
     *                                  exactly one column
     * @throws NullPointerException     if m is null
     */
    public static void Mat_to_vector_DMatch(Mat m, List<DMatch> matches) {
        if (matches == null)
            throw new IllegalArgumentException("Output List can't be null");
        int count = m.rows();
        if (CvType.CV_64FC4 != m.type() || m.cols() != 1)
            throw new IllegalArgumentException("CvType.CV_64FC4 != m.type() ||  m.cols()!=1\n" + m);

        matches.clear();
        double[] buff = new double[4 * count];
        m.get(0, 0, buff);
        for (int i = 0; i < count; i++) {
            matches.add(
                    new DMatch((int) buff[4 * i], (int) buff[4 * i + 1], (int) buff[4 * i + 2],
                            (float) buff[4 * i + 3]));
        }
    }

    /**
     * Encodes a nested list of DMatch into a native matrix representation.
     *
     * @param lvdm nested descriptor-match lists represented by native matrix handles
     * @param mats source matrices to encode as native handles
     * @return a matrix encoding the nested DMatch values
     * @throws NullPointerException if mats is null
     */
    public static Mat vector_vector_DMatch_to_Mat(List<MatOfDMatch> lvdm, List<Mat> mats) {
        Mat res;
        int lCount = (lvdm != null) ? lvdm.size() : 0;
        if (lCount > 0) {
            mats.addAll(lvdm);
            res = vector_Mat_to_Mat(mats);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into nested list of DMatch.
     *
     * @param m    matrix containing the encoded vector representation required by this conversion
     * @param lvdm nested descriptor-match lists represented by native matrix handles
     * @throws IllegalArgumentException if lvdm is null; or if m is null
     */
    public static void Mat_to_vector_vector_DMatch(Mat m, List<MatOfDMatch> lvdm) {
        if (lvdm == null)
            throw new IllegalArgumentException("Output List can't be null");

        if (m == null)
            throw new IllegalArgumentException("Input Mat can't be null");

        List<Mat> mats = new ArrayList<Mat>(m.rows());
        Mat_to_vector_Mat(m, mats);
        lvdm.clear();
        for (Mat mi : mats) {
            MatOfDMatch vdm = new MatOfDMatch(mi);
            lvdm.add(vdm);
            mi.release();
        }
        mats.clear();
    }

    /**
     * Encodes a nested list of char into a native matrix representation.
     *
     * @param lvb  nested byte vectors represented by native matrix handles
     * @param mats source matrices to encode as native handles
     * @return a matrix encoding the nested char values
     * @throws NullPointerException if mats is null
     */
    public static Mat vector_vector_char_to_Mat(List<MatOfByte> lvb, List<Mat> mats) {
        Mat res;
        int lCount = (lvb != null) ? lvb.size() : 0;
        if (lCount > 0) {
            mats.addAll(lvb);
            res = vector_Mat_to_Mat(mats);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into nested list of char.
     *
     * @param m   matrix containing the encoded vector representation required by this conversion
     * @param llb destination collection of byte-vector lists
     * @throws IllegalArgumentException if llb is null; or if m is null
     */
    public static void Mat_to_vector_vector_char(Mat m, List<List<Byte>> llb) {
        if (llb == null)
            throw new IllegalArgumentException("Output List can't be null");

        if (m == null)
            throw new IllegalArgumentException("Input Mat can't be null");

        List<Mat> mats = new ArrayList<Mat>(m.rows());
        Mat_to_vector_Mat(m, mats);
        for (Mat mi : mats) {
            List<Byte> lb = new ArrayList<Byte>();
            Mat_to_vector_char(mi, lb);
            llb.add(lb);
            mi.release();
        }
        mats.clear();
    }

    /**
     * Encodes a Java list of Rotated Rect into a native matrix representation.
     *
     * @param rs source list of rectangles
     * @return a matrix encoding the RotatedRect values; nonempty inputs use {@code CV_32FC(5)}
     */
    public static Mat vector_RotatedRect_to_Mat(List<RotatedRect> rs) {
        Mat res;
        int count = (rs != null) ? rs.size() : 0;
        if (count > 0) {
            res = new Mat(count, 1, CvType.CV_32FC(5));
            float[] buff = new float[5 * count];
            for (int i = 0; i < count; i++) {
                RotatedRect r = rs.get(i);
                buff[5 * i] = (float) r.center.x;
                buff[5 * i + 1] = (float) r.center.y;
                buff[5 * i + 2] = (float) r.size.width;
                buff[5 * i + 3] = (float) r.size.height;
                buff[5 * i + 4] = (float) r.angle;
            }
            res.put(0, 0, buff);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into a Java list of Rotated Rect.
     *
     * @param m  matrix containing the encoded vector representation required by this conversion
     * @param rs destination list of rectangles
     * @throws IllegalArgumentException if rs is null; or if m does not have type {@code CvType.CV_32FC(5)} and exactly
     *                                  one column
     * @throws NullPointerException     if m is null
     */
    public static void Mat_to_vector_RotatedRect(Mat m, List<RotatedRect> rs) {
        if (rs == null)
            throw new IllegalArgumentException("rs == null");
        int count = m.rows();
        if (CvType.CV_32FC(5) != m.type() || m.cols() != 1)
            throw new IllegalArgumentException("CvType.CV_32FC5 != m.type() ||  m.rows()!=1\n" + m);

        rs.clear();
        float[] buff = new float[5 * count];
        m.get(0, 0, buff);
        for (int i = 0; i < count; i++) {
            rs.add(
                    new RotatedRect(new Point(buff[5 * i], buff[5 * i + 1]), new Size(buff[5 * i + 2], buff[5 * i + 3]),
                            buff[5 * i + 4]));
        }
    }

    /**
     * Encodes a Java list of Mat Shape into a native matrix representation.
     *
     * @param matOfInts source list of integer-vector matrix wrappers
     * @return a matrix encoding the MatShape values; nonempty inputs use {@code CV_32SC2}
     */
    public static Mat vector_MatShape_to_Mat(List<MatOfInt> matOfInts) {
        Mat res;
        int count = (matOfInts != null) ? matOfInts.size() : 0;
        if (count > 0) {
            res = new Mat(count, 1, CvType.CV_32SC2);
            int[] buff = new int[count * 2];
            for (int i = 0; i < count; i++) {
                long addr = matOfInts.get(i).nativeObj;
                buff[i * 2] = (int) (addr >> 32);
                buff[i * 2 + 1] = (int) (addr & 0xffffffff);
            }
            res.put(0, 0, buff);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into a Java list of Mat Shape.
     *
     * @param m         matrix containing the encoded vector representation required by this conversion
     * @param matOfInts destination list of integer-vector matrix wrappers
     * @throws IllegalArgumentException if matOfInts is null; or if m does not have type {@code CvType.CV_32SC2} and
     *                                  exactly one column
     * @throws NullPointerException     if m is null
     */
    public static void Mat_to_vector_MatShape(Mat m, List<MatOfInt> matOfInts) {
        if (matOfInts == null)
            throw new IllegalArgumentException("matOfInts == null");
        int count = m.rows();
        if (CvType.CV_32SC2 != m.type() || m.cols() != 1)
            throw new IllegalArgumentException("CvType.CV_32SC2 != m.type() ||  m.cols()!=1\n" + m);

        matOfInts.clear();
        int[] buff = new int[count * 2];
        m.get(0, 0, buff);
        for (int i = 0; i < count; i++) {
            long addr = (((long) buff[i * 2]) << 32) | (((long) buff[i * 2 + 1]) & 0xffffffffL);
            matOfInts.add(MatOfInt.fromNativeAddr(addr));
        }
    }

    /**
     * Encodes a nested list of Mat Shape into a native matrix representation.
     *
     * @param vecMatOfInts source collection of integer-vector lists
     * @param mats         source matrices to encode as native handles
     * @return a matrix encoding the nested MatShape values
     * @throws NullPointerException if mats is null
     */
    public static Mat vector_vector_MatShape_to_Mat(List<List<MatOfInt>> vecMatOfInts, List<Mat> mats) {
        Mat res;
        int lCount = (vecMatOfInts != null) ? vecMatOfInts.size() : 0;
        if (lCount > 0) {
            for (List<MatOfInt> matList : vecMatOfInts) {
                Mat mat = vector_MatShape_to_Mat(matList);
                mats.add(mat);
            }
            res = vector_Mat_to_Mat(mats);
        } else {
            res = new Mat();
        }
        return res;
    }

    /**
     * Decodes a native matrix representation into nested list of Mat Shape.
     *
     * @param m            matrix containing the encoded vector representation required by this conversion
     * @param vecMatOfInts destination collection of integer-vector lists
     * @throws IllegalArgumentException if vecMatOfInts is null; or if m is null
     */
    public static void Mat_to_vector_vector_MatShape(Mat m, List<List<MatOfInt>> vecMatOfInts) {
        if (vecMatOfInts == null)
            throw new IllegalArgumentException("Output List can't be null");

        if (m == null)
            throw new IllegalArgumentException("Input Mat can't be null");

        vecMatOfInts.clear();
        List<Mat> mats = new ArrayList<Mat>(m.rows());
        Mat_to_vector_Mat(m, mats);
        for (Mat mi : mats) {
            List<MatOfInt> rowList = new ArrayList<MatOfInt>(mi.rows());
            Mat_to_vector_MatShape(mi, rowList);
            vecMatOfInts.add(rowList);
            mi.release();
        }
        mats.clear();
    }
}
