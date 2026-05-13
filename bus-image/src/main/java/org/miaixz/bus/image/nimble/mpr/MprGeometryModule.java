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
package org.miaixz.bus.image.nimble.mpr;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.DicomModule;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.nimble.geometry.Vector3;

/**
 * DICOM Multi-Planar Reconstruction Geometry Module.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MprGeometryModule extends DicomModule {

    /**
     * Defines the MprThicknessType values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum MprThicknessType {
        /**
         * Constant for the thin value.
         */
        THIN,
        /**
         * Constant for the slab value.
         */
        SLAB

    }

    /**
     * Creates a new instance.
     */
    public MprGeometryModule() {
        this(new Attributes());
    }

    /**
     * Creates a new instance.
     *
     * @param dcmItems the dcm items.
     */
    public MprGeometryModule(Attributes dcmItems) {
        super(dcmItems);
    }

    /**
     * Gets the mpr style.
     *
     * @return the mpr style.
     */
    public String getMprStyle() {
        return dcmItems.getString(Tag.MultiPlanarReconstructionStyle);
    }

    /**
     * Sets the mpr style.
     *
     * @param mprStyle the mpr style.
     */
    public void setMprStyle(String mprStyle) {
        dcmItems.setString(Tag.MultiPlanarReconstructionStyle, VR.CS, mprStyle);
    }

    /**
     * Gets the mpr thickness type.
     *
     * @return the mpr thickness type.
     */
    public MprThicknessType getMprThicknessType() {
        String value = dcmItems.getString(Tag.MPRThicknessType);
        return value == null ? null : MprThicknessType.valueOf(value.toUpperCase(java.util.Locale.ROOT));
    }

    /**
     * Sets the mpr thickness type.
     *
     * @param thicknessType the thickness type.
     */
    public void setMprThicknessType(MprThicknessType thicknessType) {
        dcmItems.setString(Tag.MPRThicknessType, VR.CS, thicknessType == null ? null : thicknessType.name());
    }

    /**
     * Gets the mpr slab thickness.
     *
     * @return the mpr slab thickness.
     */
    public double getMprSlabThickness() {
        return dcmItems.getDouble(Tag.MPRSlabThickness, -1.0);
    }

    /**
     * Sets the mpr slab thickness.
     *
     * @param thickness the thickness.
     */
    public void setMprSlabThickness(double thickness) {
        dcmItems.setDouble(Tag.MPRSlabThickness, VR.FD, thickness);
    }

    /**
     * Gets the mpr view width direction.
     *
     * @return the mpr view width direction.
     */
    public Vector3 getMprViewWidthDirection() {
        return getVector3(Tag.MPRViewWidthDirection);
    }

    /**
     * Sets the mpr view width direction.
     *
     * @param direction the direction.
     */
    public void setMprViewWidthDirection(Vector3 direction) {
        setVector3(Tag.MPRViewWidthDirection, direction);
    }

    /**
     * Gets the mpr view width.
     *
     * @return the mpr view width.
     */
    public double getMprViewWidth() {
        return dcmItems.getDouble(Tag.MPRViewWidth, -1.0);
    }

    /**
     * Sets the mpr view width.
     *
     * @param width the width.
     */
    public void setMprViewWidth(double width) {
        dcmItems.setDouble(Tag.MPRViewWidth, VR.FD, width);
    }

    /**
     * Gets the mpr view height direction.
     *
     * @return the mpr view height direction.
     */
    public Vector3 getMprViewHeightDirection() {
        return getVector3(Tag.MPRViewHeightDirection);
    }

    /**
     * Sets the mpr view height direction.
     *
     * @param direction the direction.
     */
    public void setMprViewHeightDirection(Vector3 direction) {
        setVector3(Tag.MPRViewHeightDirection, direction);
    }

    /**
     * Gets the mpr view height.
     *
     * @return the mpr view height.
     */
    public double getMprViewHeight() {
        return dcmItems.getDouble(Tag.MPRViewHeight, -1.0);
    }

    /**
     * Sets the mpr view height.
     *
     * @param height the height.
     */
    public void setMprViewHeight(double height) {
        dcmItems.setDouble(Tag.MPRViewHeight, VR.FD, height);
    }

    /**
     * Gets the mpr top left hand corner.
     *
     * @return the mpr top left hand corner.
     */
    public Vector3 getMprTopLeftHandCorner() {
        return getVector3(Tag.MPRTopLeftHandCorner);
    }

    /**
     * Sets the mpr top left hand corner.
     *
     * @param topLeftCorner the top left corner.
     */
    public void setMprTopLeftHandCorner(Vector3 topLeftCorner) {
        setVector3(Tag.MPRTopLeftHandCorner, topLeftCorner);
    }

    /**
     * Gets the vector3.
     *
     * @param tag the tag.
     * @return the vector3.
     */
    private Vector3 getVector3(int tag) {
        double[] values = dcmItems.getDoubles(tag);
        return values != null && values.length == 3 ? new Vector3(values[0], values[1], values[2]) : null;
    }

    /**
     * Sets the vector3.
     *
     * @param tag    the tag.
     * @param vector the vector.
     */
    private void setVector3(int tag, Vector3 vector) {
        if (vector == null) {
            throw new IllegalArgumentException("vector cannot be null");
        }
        dcmItems.setDouble(tag, VR.FD, vector.x(), vector.y(), vector.z());
    }

}
