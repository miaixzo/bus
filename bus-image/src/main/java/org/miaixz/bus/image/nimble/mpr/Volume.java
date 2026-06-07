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

import java.util.Objects;

import org.miaixz.bus.image.nimble.geometry.Vector3;

/**
 * Primitive-backed 3D volume used by MPR/MIP algorithms without viewer dependencies.
 *
 * @param <T> numeric sample type
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract sealed class Volume<T extends Number>
        permits VolumeByte, VolumeDouble, VolumeFloat, VolumeInt, VolumeShort {

    /**
     * The max array length value.
     */
    private static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

    /**
     * The size value.
     */
    protected final VolumeSize size;

    /**
     * The channels value.
     */
    protected final int channels;

    /**
     * The signed value.
     */
    protected final boolean signed;

    /**
     * The pixel ratio value.
     */
    protected Vector3 pixelRatio = new Vector3(1.0, 1.0, 1.0);

    /**
     * The origin value.
     */
    protected Vector3 origin = Vector3.ZERO;

    /**
     * The axis x value.
     */
    protected Vector3 axisX = Vector3.UNIT_X;

    /**
     * The axis y value.
     */
    protected Vector3 axisY = Vector3.UNIT_Y;

    /**
     * The axis z value.
     */
    protected Vector3 axisZ = Vector3.UNIT_Z;

    /**
     * Creates a new instance.
     *
     * @param size     the size.
     * @param signed   the signed.
     * @param channels the channels.
     */
    protected Volume(VolumeSize size, boolean signed, int channels) {
        this.size = Objects.requireNonNull(size, "size");
        this.signed = signed;
        if (channels <= 0) {
            throw new IllegalArgumentException("channels must be > 0: " + channels);
        }
        this.channels = channels;
        if (size.elementCount(channels) > MAX_ARRAY_LENGTH) {
            throw new IllegalArgumentException("Volume is too large for heap-backed primitive storage");
        }
    }

    /**
     * Gets the size.
     *
     * @return the size.
     */
    public final VolumeSize getSize() {
        return size;
    }

    /**
     * Gets the channels.
     *
     * @return the channels.
     */
    public final int getChannels() {
        return channels;
    }

    /**
     * Determines whether signed.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean isSigned() {
        return signed;
    }

    /**
     * Gets the pixel ratio.
     *
     * @return the pixel ratio.
     */
    public final Vector3 getPixelRatio() {
        return pixelRatio;
    }

    /**
     * Sets the pixel ratio.
     *
     * @param pixelRatio the pixel ratio.
     */
    public final void setPixelRatio(Vector3 pixelRatio) {
        this.pixelRatio = Objects.requireNonNull(pixelRatio, "pixelRatio");
    }

    /**
     * Gets the origin.
     *
     * @return the origin.
     */
    public final Vector3 getOrigin() {
        return origin;
    }

    /**
     * Gets the axis x.
     *
     * @return the axis x.
     */
    public final Vector3 getAxisX() {
        return axisX;
    }

    /**
     * Gets the axis y.
     *
     * @return the axis y.
     */
    public final Vector3 getAxisY() {
        return axisY;
    }

    /**
     * Gets the axis z.
     *
     * @return the axis z.
     */
    public final Vector3 getAxisZ() {
        return axisZ;
    }

    /**
     * Sets the geometry.
     *
     * @param bounds the bounds.
     */
    public final void setGeometry(VolumeBounds bounds) {
        Objects.requireNonNull(bounds, "bounds");
        if (!size.equals(bounds.size())) {
            throw new IllegalArgumentException("Volume bounds size does not match volume size");
        }
        this.pixelRatio = bounds.spacing();
        this.origin = bounds.origin();
        this.axisX = bounds.rowDir();
        this.axisY = bounds.colDir();
        this.axisZ = bounds.normalDir();
    }

    /**
     * Executes the element count operation.
     *
     * @return the operation result.
     */
    public final long elementCount() {
        return size.elementCount(channels);
    }

    /**
     * Executes the linear index operation.
     *
     * @param x       the x.
     * @param y       the y.
     * @param z       the z.
     * @param channel the channel.
     * @return the operation result.
     */
    public final long linearIndex(int x, int y, int z, int channel) {
        checkCoordinates(x, y, z, channel);
        return (((long) z * size.y() + y) * size.x() + x) * channels + channel;
    }

    /**
     * Gets the voxel.
     *
     * @param x the x.
     * @param y the y.
     * @param z the z.
     * @return the voxel.
     */
    public final Voxel<T> getVoxel(int x, int y, int z) {
        Voxel<T> voxel = new Voxel<>(channels);
        for (int channel = 0; channel < channels; channel++) {
            voxel.setValue(channel, getValue(x, y, z, channel));
        }
        return voxel;
    }

    /**
     * Sets the voxel.
     *
     * @param x     the x.
     * @param y     the y.
     * @param z     the z.
     * @param voxel the voxel.
     */
    public final void setVoxel(int x, int y, int z, Voxel<T> voxel) {
        Objects.requireNonNull(voxel, "voxel");
        if (voxel.getChannels() != channels) {
            throw new IllegalArgumentException("Voxel channel count does not match volume");
        }
        for (int channel = 0; channel < channels; channel++) {
            setValue(x, y, z, channel, voxel.getValue(channel));
        }
    }

    /**
     * Gets the value.
     *
     * @param x the x.
     * @param y the y.
     * @param z the z.
     * @return the value.
     */
    public final T getValue(int x, int y, int z) {
        return getValue(x, y, z, 0);
    }

    /**
     * Gets the value.
     *
     * @param x       the x.
     * @param y       the y.
     * @param z       the z.
     * @param channel the channel.
     * @return the value.
     */
    public final T getValue(int x, int y, int z, int channel) {
        return getLinearValue(linearIndex(x, y, z, channel));
    }

    /**
     * Sets the value.
     *
     * @param x     the x.
     * @param y     the y.
     * @param z     the z.
     * @param value the value.
     */
    public final void setValue(int x, int y, int z, T value) {
        setValue(x, y, z, 0, value);
    }

    /**
     * Sets the value.
     *
     * @param x       the x.
     * @param y       the y.
     * @param z       the z.
     * @param channel the channel.
     * @param value   the value.
     */
    public final void setValue(int x, int y, int z, int channel, T value) {
        setLinearValue(linearIndex(x, y, z, channel), Objects.requireNonNull(value, "value"));
    }

    /**
     * Gets the linear value.
     *
     * @param index the index.
     * @return the linear value.
     */
    public abstract T getLinearValue(long index);

    /**
     * Sets the linear value.
     *
     * @param index the index.
     * @param value the value.
     */
    public abstract void setLinearValue(long index, T value);

    /**
     * Executes the fill operation.
     *
     * @param value the value.
     */
    public abstract void fill(T value);

    /**
     * Executes the native minimum operation.
     *
     * @return the operation result.
     */
    public abstract Number nativeMinimum();

    /**
     * Executes the native maximum operation.
     *
     * @return the operation result.
     */
    public abstract Number nativeMaximum();

    /**
     * Gets a trilinearly interpolated value from this volume.
     *
     * @param x       the fractional x coordinate.
     * @param y       the fractional y coordinate.
     * @param z       the fractional z coordinate.
     * @param channel the channel.
     * @return the interpolated value, or {@code null} when the coordinate is outside the interpolatable range.
     */
    public T getInterpolatedValueFromSource(double x, double y, double z, int channel) {
        if (x < 0 || x >= size.x() - 1 || y < 0 || y >= size.y() - 1 || z < 0 || z >= size.z() - 1) {
            return null;
        }

        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        int z0 = (int) Math.floor(z);
        int x1 = Math.min(x0 + 1, size.x() - 1);
        int y1 = Math.min(y0 + 1, size.y() - 1);
        int z1 = Math.min(z0 + 1, size.z() - 1);

        double fx = x - x0;
        double fy = y - y0;
        double fz = z - z0;

        T v000 = getValue(x0, y0, z0, channel);
        T v001 = getValue(x0, y0, z1, channel);
        T v010 = getValue(x0, y1, z0, channel);
        T v110 = getValue(x1, y1, z0, channel);
        T v100 = getValue(x1, y0, z0, channel);
        T v101 = getValue(x1, y0, z1, channel);
        T v011 = getValue(x0, y1, z1, channel);
        T v111 = getValue(x1, y1, z1, channel);

        double v00 = interpolate(v000, v100, fx);
        double v01 = interpolate(v001, v101, fx);
        double v10 = interpolate(v010, v110, fx);
        double v11 = interpolate(v011, v111, fx);

        double v0 = v00 * (1 - fy) + v10 * fy;
        double v1 = v01 * (1 - fy) + v11 * fy;
        return convertToGeneric(v0 * (1 - fz) + v1 * fz);
    }

    /**
     * Interpolates between two numeric values.
     *
     * @param first    the first value.
     * @param second   the second value.
     * @param fraction the interpolation fraction.
     * @return the interpolated value.
     */
    private double interpolate(T first, T second, double fraction) {
        return first.doubleValue() * (1 - fraction) + second.doubleValue() * fraction;
    }

    /**
     * Converts an interpolated value to the volume sample type.
     *
     * @param value the interpolated value.
     * @return the converted value.
     */
    private T convertToGeneric(double value) {
        return switch (this) {
            case VolumeByte ignored -> (T) Byte.valueOf((byte) Math.round(value));
            case VolumeShort ignored -> (T) Short.valueOf((short) Math.round(value));
            case VolumeInt ignored -> (T) Integer.valueOf((int) Math.round(value));
            case VolumeFloat ignored -> (T) Float.valueOf((float) value);
            default -> (T) Double.valueOf(value);
        };
    }

    /**
     * Executes the checked array index operation.
     *
     * @param index the index.
     * @return the operation result.
     */
    protected final int checkedArrayIndex(long index) {
        if (index < 0 || index >= elementCount()) {
            throw new IndexOutOfBoundsException("index " + index + " outside 0.." + (elementCount() - 1));
        }
        return (int) index;
    }

    /**
     * Executes the check single channel operation.
     */
    protected final void checkSingleChannel() {
        if (channels != 1) {
            throw new IllegalArgumentException("This volume type supports only one channel");
        }
    }

    /**
     * Executes the check coordinates operation.
     *
     * @param x       the x.
     * @param y       the y.
     * @param z       the z.
     * @param channel the channel.
     */
    private void checkCoordinates(int x, int y, int z, int channel) {
        if (x < 0 || x >= size.x()) {
            throw new IndexOutOfBoundsException("x " + x + " outside 0.." + (size.x() - 1));
        }
        if (y < 0 || y >= size.y()) {
            throw new IndexOutOfBoundsException("y " + y + " outside 0.." + (size.y() - 1));
        }
        if (z < 0 || z >= size.z()) {
            throw new IndexOutOfBoundsException("z " + z + " outside 0.." + (size.z() - 1));
        }
        if (channel < 0 || channel >= channels) {
            throw new IndexOutOfBoundsException("channel " + channel + " outside 0.." + (channels - 1));
        }
    }

}
