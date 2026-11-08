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
package org.miaixz.bus.opencv.imgproc;

/**
 * Spatial, central, and normalized central moments of an image or contour.
 */
public class Moments {

    /**
     * Spatial image moment of order (0, 0).
     */
    public double m00;
    /**
     * Spatial image moment of order (1, 0).
     */
    public double m10;
    /**
     * Spatial image moment of order (0, 1).
     */
    public double m01;
    /**
     * Spatial image moment of order (2, 0).
     */
    public double m20;
    /**
     * Spatial image moment of order (1, 1).
     */
    public double m11;
    /**
     * Spatial image moment of order (0, 2).
     */
    public double m02;
    /**
     * Spatial image moment of order (3, 0).
     */
    public double m30;
    /**
     * Spatial image moment of order (2, 1).
     */
    public double m21;
    /**
     * Spatial image moment of order (1, 2).
     */
    public double m12;
    /**
     * Spatial image moment of order (0, 3).
     */
    public double m03;

    /**
     * Central image moment of order (2, 0).
     */
    public double mu20;
    /**
     * Central image moment of order (1, 1).
     */
    public double mu11;
    /**
     * Central image moment of order (0, 2).
     */
    public double mu02;
    /**
     * Central image moment of order (3, 0).
     */
    public double mu30;
    /**
     * Central image moment of order (2, 1).
     */
    public double mu21;
    /**
     * Central image moment of order (1, 2).
     */
    public double mu12;
    /**
     * Central image moment of order (0, 3).
     */
    public double mu03;

    /**
     * Normalized central image moment of order (2, 0).
     */
    public double nu20;
    /**
     * Normalized central image moment of order (1, 1).
     */
    public double nu11;
    /**
     * Normalized central image moment of order (0, 2).
     */
    public double nu02;
    /**
     * Normalized central image moment of order (3, 0).
     */
    public double nu30;
    /**
     * Normalized central image moment of order (2, 1).
     */
    public double nu21;
    /**
     * Normalized central image moment of order (1, 2).
     */
    public double nu12;
    /**
     * Normalized central image moment of order (0, 3).
     */
    public double nu03;

    /**
     * Creates a configured Moments value using the supplied values.
     *
     * @param m00 spatial moment of order (0, 0)
     * @param m10 spatial moment of order (1, 0)
     * @param m01 spatial moment of order (0, 1)
     * @param m20 spatial moment of order (2, 0)
     * @param m11 spatial moment of order (1, 1)
     * @param m02 spatial moment of order (0, 2)
     * @param m30 spatial moment of order (3, 0)
     * @param m21 spatial moment of order (2, 1)
     * @param m12 spatial moment of order (1, 2)
     * @param m03 spatial moment of order (0, 3)
     */
    public Moments(double m00, double m10, double m01, double m20, double m11, double m02, double m30, double m21,
            double m12, double m03) {
        this.m00 = m00;
        this.m10 = m10;
        this.m01 = m01;
        this.m20 = m20;
        this.m11 = m11;
        this.m02 = m02;
        this.m30 = m30;
        this.m21 = m21;
        this.m12 = m12;
        this.m03 = m03;
        this.completeState();
    }

    /**
     * Creates an empty Moments value.
     */
    public Moments() {
        this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Creates a configured Moments value using the supplied values.
     *
     * @param vals component values in the order used by {@code Moments}; missing array entries are initialized by this
     *             implementation
     */
    public Moments(double[] vals) {
        set(vals);
    }

    /**
     * Updates this object from the supplied values.
     *
     * @param vals component values in the order used by {@code Moments}; missing array entries are initialized by this
     *             implementation
     */
    public void set(double[] vals) {
        if (vals != null) {
            m00 = vals.length > 0 ? vals[0] : 0;
            m10 = vals.length > 1 ? vals[1] : 0;
            m01 = vals.length > 2 ? vals[2] : 0;
            m20 = vals.length > 3 ? vals[3] : 0;
            m11 = vals.length > 4 ? vals[4] : 0;
            m02 = vals.length > 5 ? vals[5] : 0;
            m30 = vals.length > 6 ? vals[6] : 0;
            m21 = vals.length > 7 ? vals[7] : 0;
            m12 = vals.length > 8 ? vals[8] : 0;
            m03 = vals.length > 9 ? vals[9] : 0;
            this.completeState();
        } else {
            m00 = 0;
            m10 = 0;
            m01 = 0;
            m20 = 0;
            m11 = 0;
            m02 = 0;
            m30 = 0;
            m21 = 0;
            m12 = 0;
            m03 = 0;
            mu20 = 0;
            mu11 = 0;
            mu02 = 0;
            mu30 = 0;
            mu21 = 0;
            mu12 = 0;
            mu03 = 0;
            nu20 = 0;
            nu11 = 0;
            nu02 = 0;
            nu30 = 0;
            nu21 = 0;
            nu12 = 0;
            nu03 = 0;
        }
    }

    /**
     * Returns a textual representation of this object.
     *
     * @return the Moments field values formatted as text
     */
    @Override
    public String toString() {
        return "Moments [ " + "\n" + "m00=" + m00 + ", " + "\n" + "m10=" + m10 + ", " + "m01=" + m01 + ", " + "\n"
                + "m20=" + m20 + ", " + "m11=" + m11 + ", " + "m02=" + m02 + ", " + "\n" + "m30=" + m30 + ", " + "m21="
                + m21 + ", " + "m12=" + m12 + ", " + "m03=" + m03 + ", " + "\n" + "mu20=" + mu20 + ", " + "mu11=" + mu11
                + ", " + "mu02=" + mu02 + ", " + "\n" + "mu30=" + mu30 + ", " + "mu21=" + mu21 + ", " + "mu12=" + mu12
                + ", " + "mu03=" + mu03 + ", " + "\n" + "nu20=" + nu20 + ", " + "nu11=" + nu11 + ", " + "nu02=" + nu02
                + ", " + "\n" + "nu30=" + nu30 + ", " + "nu21=" + nu21 + ", " + "nu12=" + nu12 + ", " + "nu03=" + nu03
                + ", " + "\n]";
    }

    /**
     * Computes central and normalized central moments from the stored spatial moments.
     */
    protected void completeState() {
        double cx = 0, cy = 0;
        double mu20, mu11, mu02;
        double inv_m00 = 0.0;

        if (Math.abs(this.m00) > 0.00000001) {
            inv_m00 = 1. / this.m00;
            cx = this.m10 * inv_m00;
            cy = this.m01 * inv_m00;
        }

        mu20 = this.m20 - this.m10 * cx;

        mu11 = this.m11 - this.m10 * cy;

        mu02 = this.m02 - this.m01 * cy;

        this.mu20 = mu20;
        this.mu11 = mu11;
        this.mu02 = mu02;

        this.mu30 = this.m30 - cx * (3 * mu20 + cx * this.m10);
        mu11 += mu11;

        this.mu21 = this.m21 - cx * (mu11 + cx * this.m01) - cy * mu20;

        this.mu12 = this.m12 - cy * (mu11 + cy * this.m10) - cx * mu02;

        this.mu03 = this.m03 - cy * (3 * mu02 + cy * this.m01);

        double inv_sqrt_m00 = Math.sqrt(Math.abs(inv_m00));
        double s2 = inv_m00 * inv_m00, s3 = s2 * inv_sqrt_m00;

        this.nu20 = this.mu20 * s2;
        this.nu11 = this.mu11 * s2;
        this.nu02 = this.mu02 * s2;
        this.nu30 = this.mu30 * s3;
        this.nu21 = this.mu21 * s3;
        this.nu12 = this.mu12 * s3;
        this.nu03 = this.mu03 * s3;

    }

    /**
     * Returns spatial moment of order (0, 0).
     *
     * @return spatial moment of order (0, 0)
     */
    public double get_m00() {
        return this.m00;
    }

    /**
     * Returns spatial moment of order (1, 0).
     *
     * @return spatial moment of order (1, 0)
     */
    public double get_m10() {
        return this.m10;
    }

    /**
     * Returns spatial moment of order (0, 1).
     *
     * @return spatial moment of order (0, 1)
     */
    public double get_m01() {
        return this.m01;
    }

    /**
     * Returns spatial moment of order (2, 0).
     *
     * @return spatial moment of order (2, 0)
     */
    public double get_m20() {
        return this.m20;
    }

    /**
     * Returns spatial moment of order (1, 1).
     *
     * @return spatial moment of order (1, 1)
     */
    public double get_m11() {
        return this.m11;
    }

    /**
     * Returns spatial moment of order (0, 2).
     *
     * @return spatial moment of order (0, 2)
     */
    public double get_m02() {
        return this.m02;
    }

    /**
     * Returns spatial moment of order (3, 0).
     *
     * @return spatial moment of order (3, 0)
     */
    public double get_m30() {
        return this.m30;
    }

    /**
     * Returns spatial moment of order (2, 1).
     *
     * @return spatial moment of order (2, 1)
     */
    public double get_m21() {
        return this.m21;
    }

    /**
     * Returns spatial moment of order (1, 2).
     *
     * @return spatial moment of order (1, 2)
     */
    public double get_m12() {
        return this.m12;
    }

    /**
     * Returns spatial moment of order (0, 3).
     *
     * @return spatial moment of order (0, 3)
     */
    public double get_m03() {
        return this.m03;
    }

    /**
     * Returns central moment of order (2, 0).
     *
     * @return central moment of order (2, 0)
     */
    public double get_mu20() {
        return this.mu20;
    }

    /**
     * Returns central moment of order (1, 1).
     *
     * @return central moment of order (1, 1)
     */
    public double get_mu11() {
        return this.mu11;
    }

    /**
     * Returns central moment of order (0, 2).
     *
     * @return central moment of order (0, 2)
     */
    public double get_mu02() {
        return this.mu02;
    }

    /**
     * Returns central moment of order (3, 0).
     *
     * @return central moment of order (3, 0)
     */
    public double get_mu30() {
        return this.mu30;
    }

    /**
     * Returns central moment of order (2, 1).
     *
     * @return central moment of order (2, 1)
     */
    public double get_mu21() {
        return this.mu21;
    }

    /**
     * Returns central moment of order (1, 2).
     *
     * @return central moment of order (1, 2)
     */
    public double get_mu12() {
        return this.mu12;
    }

    /**
     * Returns central moment of order (0, 3).
     *
     * @return central moment of order (0, 3)
     */
    public double get_mu03() {
        return this.mu03;
    }

    /**
     * Returns normalized central moment of order (2, 0).
     *
     * @return normalized central moment of order (2, 0)
     */
    public double get_nu20() {
        return this.nu20;
    }

    /**
     * Returns normalized central moment of order (1, 1).
     *
     * @return normalized central moment of order (1, 1)
     */
    public double get_nu11() {
        return this.nu11;
    }

    /**
     * Returns normalized central moment of order (0, 2).
     *
     * @return normalized central moment of order (0, 2)
     */
    public double get_nu02() {
        return this.nu02;
    }

    /**
     * Returns normalized central moment of order (3, 0).
     *
     * @return normalized central moment of order (3, 0)
     */
    public double get_nu30() {
        return this.nu30;
    }

    /**
     * Returns normalized central moment of order (2, 1).
     *
     * @return normalized central moment of order (2, 1)
     */
    public double get_nu21() {
        return this.nu21;
    }

    /**
     * Returns normalized central moment of order (1, 2).
     *
     * @return normalized central moment of order (1, 2)
     */
    public double get_nu12() {
        return this.nu12;
    }

    /**
     * Returns normalized central moment of order (0, 3).
     *
     * @return normalized central moment of order (0, 3)
     */
    public double get_nu03() {
        return this.nu03;
    }

    /**
     * Sets the m00 used by this Moments instance.
     *
     * @param m00 spatial moment of order (0, 0)
     */
    public void set_m00(double m00) {
        this.m00 = m00;
    }

    /**
     * Sets the m10 used by this Moments instance.
     *
     * @param m10 spatial moment of order (1, 0)
     */
    public void set_m10(double m10) {
        this.m10 = m10;
    }

    /**
     * Sets the m01 used by this Moments instance.
     *
     * @param m01 spatial moment of order (0, 1)
     */
    public void set_m01(double m01) {
        this.m01 = m01;
    }

    /**
     * Sets the m20 used by this Moments instance.
     *
     * @param m20 spatial moment of order (2, 0)
     */
    public void set_m20(double m20) {
        this.m20 = m20;
    }

    /**
     * Sets the m11 used by this Moments instance.
     *
     * @param m11 spatial moment of order (1, 1)
     */
    public void set_m11(double m11) {
        this.m11 = m11;
    }

    /**
     * Sets the m02 used by this Moments instance.
     *
     * @param m02 spatial moment of order (0, 2)
     */
    public void set_m02(double m02) {
        this.m02 = m02;
    }

    /**
     * Sets the m30 used by this Moments instance.
     *
     * @param m30 spatial moment of order (3, 0)
     */
    public void set_m30(double m30) {
        this.m30 = m30;
    }

    /**
     * Sets the m21 used by this Moments instance.
     *
     * @param m21 spatial moment of order (2, 1)
     */
    public void set_m21(double m21) {
        this.m21 = m21;
    }

    /**
     * Sets the m12 used by this Moments instance.
     *
     * @param m12 spatial moment of order (1, 2)
     */
    public void set_m12(double m12) {
        this.m12 = m12;
    }

    /**
     * Sets the m03 used by this Moments instance.
     *
     * @param m03 spatial moment of order (0, 3)
     */
    public void set_m03(double m03) {
        this.m03 = m03;
    }

    /**
     * Sets the mu20 used by this Moments instance.
     *
     * @param mu20 central moment of order (2, 0)
     */
    public void set_mu20(double mu20) {
        this.mu20 = mu20;
    }

    /**
     * Sets the mu11 used by this Moments instance.
     *
     * @param mu11 central moment of order (1, 1)
     */
    public void set_mu11(double mu11) {
        this.mu11 = mu11;
    }

    /**
     * Sets the mu02 used by this Moments instance.
     *
     * @param mu02 central moment of order (0, 2)
     */
    public void set_mu02(double mu02) {
        this.mu02 = mu02;
    }

    /**
     * Sets the mu30 used by this Moments instance.
     *
     * @param mu30 central moment of order (3, 0)
     */
    public void set_mu30(double mu30) {
        this.mu30 = mu30;
    }

    /**
     * Sets the mu21 used by this Moments instance.
     *
     * @param mu21 central moment of order (2, 1)
     */
    public void set_mu21(double mu21) {
        this.mu21 = mu21;
    }

    /**
     * Sets the mu12 used by this Moments instance.
     *
     * @param mu12 central moment of order (1, 2)
     */
    public void set_mu12(double mu12) {
        this.mu12 = mu12;
    }

    /**
     * Sets the mu03 used by this Moments instance.
     *
     * @param mu03 central moment of order (0, 3)
     */
    public void set_mu03(double mu03) {
        this.mu03 = mu03;
    }

    /**
     * Sets the nu20 used by this Moments instance.
     *
     * @param nu20 normalized central moment of order (2, 0)
     */
    public void set_nu20(double nu20) {
        this.nu20 = nu20;
    }

    /**
     * Sets the nu11 used by this Moments instance.
     *
     * @param nu11 normalized central moment of order (1, 1)
     */
    public void set_nu11(double nu11) {
        this.nu11 = nu11;
    }

    /**
     * Sets the nu02 used by this Moments instance.
     *
     * @param nu02 normalized central moment of order (0, 2)
     */
    public void set_nu02(double nu02) {
        this.nu02 = nu02;
    }

    /**
     * Sets the nu30 used by this Moments instance.
     *
     * @param nu30 normalized central moment of order (3, 0)
     */
    public void set_nu30(double nu30) {
        this.nu30 = nu30;
    }

    /**
     * Sets the nu21 used by this Moments instance.
     *
     * @param nu21 normalized central moment of order (2, 1)
     */
    public void set_nu21(double nu21) {
        this.nu21 = nu21;
    }

    /**
     * Sets the nu12 used by this Moments instance.
     *
     * @param nu12 normalized central moment of order (1, 2)
     */
    public void set_nu12(double nu12) {
        this.nu12 = nu12;
    }

    /**
     * Sets the nu03 used by this Moments instance.
     *
     * @param nu03 normalized central moment of order (0, 3)
     */
    public void set_nu03(double nu03) {
        this.nu03 = nu03;
    }
}
