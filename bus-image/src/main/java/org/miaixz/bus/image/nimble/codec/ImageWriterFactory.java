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
package org.miaixz.bus.image.nimble.codec;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.metric.Property;
import org.miaixz.bus.image.nimble.codec.jpeg.PatchJPEGLS;
import org.miaixz.bus.logger.Logger;

/**
 * Represents the ImageWriterFactory type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ImageWriterFactory implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852288887239L;

    /**
     * The default factory value.
     */
    private static volatile ImageWriterFactory defaultFactory;

    /**
     * The map value.
     */
    private final TreeMap<String, ImageWriterParam> map = new TreeMap<>();

    /**
     * The patch jpegls value.
     */
    private PatchJPEGLS patchJPEGLS;

    /**
     * Executes the nullify operation.
     *
     * @param s the s.
     * @return the operation result.
     */
    private static String nullify(String s) {
        return null == s || s.isEmpty() || s.equals(Symbol.STAR) ? null : s;
    }

    /**
     * Gets the default.
     *
     * @return the default.
     */
    public static ImageWriterFactory getDefault() {
        if (defaultFactory == null)
            defaultFactory = init();

        return defaultFactory;
    }

    /**
     * Sets the default.
     *
     * @param factory the factory.
     */
    public static void setDefault(ImageWriterFactory factory) {
        if (factory == null)
            throw new NullPointerException();

        defaultFactory = factory;
    }

    /**
     * Resets the default.
     */
    public static void resetDefault() {
        defaultFactory = null;
    }

    /**
     * Executes the init operation.
     *
     * @return the operation result.
     */
    public static ImageWriterFactory init() {
        ImageWriterFactory factory = new ImageWriterFactory();
        URL url = ResourceKit.getResourceUrl("ImageWriterFactory.properties", ImageWriterFactory.class);
        try {
            Properties props = new Properties();
            props.load(url.openStream());
            for (Entry<Object, Object> entry : props.entrySet()) {
                String[] ss = Builder.split((String) entry.getValue(), ':');
                factory.map.put(
                        (String) entry.getKey(),
                        new ImageWriterParam(ss[0], ss[1], ss[2],
                                ss.length > 3 ? Builder.split(ss[3], ';') : Normal.EMPTY_STRING_ARRAY));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Image Reader Factory configuration from: " + url.toString(), e);
        }
        return factory;
    }

    /**
     * Gets the image writer param.
     *
     * @param tsuid the tsuid.
     * @return the image writer param.
     */
    public static ImageWriterParam getImageWriterParam(String tsuid) {
        return getDefault().get(tsuid);
    }

    /**
     * Gets the image writer.
     *
     * @param param the param.
     * @return the image writer.
     */
    public static ImageWriter getImageWriter(ImageWriterParam param) {
        return Boolean.getBoolean("org.miaixz.bus.image.nimble.codec.useServiceLoader")
                ? getImageWriterFromServiceLoader(param)
                : getImageWriterFromImageIOServiceRegistry(param);
    }

    /**
     * Gets the image writer from image io service registry.
     *
     * @param param the param.
     * @return the image writer from image io service registry.
     */
    public static ImageWriter getImageWriterFromImageIOServiceRegistry(ImageWriterParam param) {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(param.formatName);
        if (!iter.hasNext())
            throw new RuntimeException("No Writer for format: " + param.formatName + " registered");

        ImageWriter writer = iter.next();
        if (param.className != null) {
            while (!param.className.equals(writer.getClass().getName())) {
                if (iter.hasNext())
                    writer = iter.next();
                else {
                    Logger.warn(
                            false,
                            "Image",
                            "No preferred Writer {} for format: format={} - use {}",
                            param.className,
                            param.formatName,
                            writer.getClass().getName());
                    break;
                }
            }
        }
        return writer;
    }

    /**
     * Gets the image writer from service loader.
     *
     * @param param the param.
     * @return the image writer from service loader.
     */
    public static ImageWriter getImageWriterFromServiceLoader(ImageWriterParam param) {
        try {
            return getImageWriterSpi(param).createWriterInstance();
        } catch (IOException e) {
            throw new RuntimeException("Error instantiating Writer for format: " + param.formatName, e);
        }
    }

    /**
     * Gets the image writer spi.
     *
     * @param param the param.
     * @return the image writer spi.
     */
    private static ImageWriterSpi getImageWriterSpi(ImageWriterParam param) {
        Iterator<ImageWriterSpi> iter = new FormatNameFilterIterator<>(
                ServiceLoader.load(ImageWriterSpi.class).iterator(), param.formatName);
        if (!iter.hasNext())
            throw new RuntimeException("No Writer for format: " + param.formatName + " registered");

        ImageWriterSpi spi = iter.next();
        if (param.className != null) {
            while (!param.className.equals(spi.getPluginClassName())) {
                if (iter.hasNext())
                    spi = iter.next();
                else {
                    Logger.warn(
                            false,
                            "Image",
                            "No preferred Writer {} for format: format={} - use {}",
                            param.className,
                            param.formatName,
                            spi.getPluginClassName());
                    break;
                }
            }
        }
        return spi;
    }

    /**
     * Gets the patch jpegls.
     *
     * @return the patch jpegls.
     */
    public final PatchJPEGLS getPatchJPEGLS() {
        return patchJPEGLS;
    }

    /**
     * Sets the patch jpegls.
     *
     * @param patchJPEGLS the patch jpegls.
     */
    public final void setPatchJPEGLS(PatchJPEGLS patchJPEGLS) {
        this.patchJPEGLS = patchJPEGLS;
    }

    /**
     * Executes the get operation.
     *
     * @param tsuid the tsuid.
     * @return the operation result.
     */
    public ImageWriterParam get(String tsuid) {
        return map.get(tsuid);
    }

    /**
     * Executes the put operation.
     *
     * @param tsuid the tsuid.
     * @param param the param.
     * @return the operation result.
     */
    public ImageWriterParam put(String tsuid, ImageWriterParam param) {
        return map.put(tsuid, param);
    }

    /**
     * Executes the remove operation.
     *
     * @param tsuid the tsuid.
     * @return the operation result.
     */
    public ImageWriterParam remove(String tsuid) {
        return map.remove(tsuid);
    }

    /**
     * Gets the entries.
     *
     * @return the entries.
     */
    public Set<Entry<String, ImageWriterParam>> getEntries() {
        return Collections.unmodifiableMap(map).entrySet();
    }

    /**
     * Executes the clear operation.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Represents the ImageWriterParam type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class ImageWriterParam implements Serializable {

        /**
         * The serial version uid value.
         */
        @Serial
        private static final long serialVersionUID = 2852289015819L;

        /**
         * The format name value.
         */
        public final String formatName;

        /**
         * The class name value.
         */
        public final String className;

        /**
         * The patch jpegls value.
         */
        public final PatchJPEGLS patchJPEGLS;

        /**
         * The image write params value.
         */
        public final Property[] imageWriteParams;

        /**
         * Creates a new instance.
         *
         * @param formatName       the format name.
         * @param className        the class name.
         * @param patchJPEGLS      the patch jpegls.
         * @param imageWriteParams the image write params.
         */
        public ImageWriterParam(String formatName, String className, PatchJPEGLS patchJPEGLS,
                Property[] imageWriteParams) {
            this.formatName = formatName;
            this.className = nullify(className);
            this.patchJPEGLS = patchJPEGLS;
            this.imageWriteParams = imageWriteParams;
        }

        /**
         * Creates a new instance.
         *
         * @param formatName       the format name.
         * @param className        the class name.
         * @param patchJPEGLS      the patch jpegls.
         * @param imageWriteParams the image write params.
         */
        public ImageWriterParam(String formatName, String className, String patchJPEGLS, String[] imageWriteParams) {
            this(formatName, className,
                    patchJPEGLS != null && !patchJPEGLS.isEmpty() ? PatchJPEGLS.valueOf(patchJPEGLS) : null,
                    Property.valueOf(imageWriteParams));
        }

        /**
         * Gets the image write params.
         *
         * @return the image write params.
         */
        public Property[] getImageWriteParams() {
            return imageWriteParams;
        }

        /**
         * Compares this instance with another object for equality.
         *
         * @param o the o.
         * @return true if the condition is met; otherwise false.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            ImageWriterParam that = (ImageWriterParam) o;

            if (!formatName.equals(that.formatName))
                return false;
            if (!Objects.equals(className, that.className))
                return false;
            if (patchJPEGLS != that.patchJPEGLS)
                return false;
            return Arrays.equals(imageWriteParams, that.imageWriteParams);

        }

        /**
         * Returns the hash code.
         *
         * @return true if the condition is met; otherwise false.
         */
        @Override
        public int hashCode() {
            int result = formatName.hashCode();
            result = 31 * result + (className != null ? className.hashCode() : 0);
            result = 31 * result + (patchJPEGLS != null ? patchJPEGLS.hashCode() : 0);
            result = 31 * result + Arrays.hashCode(imageWriteParams);
            return result;
        }

        /**
         * Returns the string representation.
         *
         * @return the string representation.
         */
        @Override
        public String toString() {
            return "ImageWriterParam{" + "formatName='" + formatName + '\'' + ", className='" + className + '\''
                    + ", patchJPEGLS=" + patchJPEGLS + ", imageWriteParams=" + Arrays.toString(imageWriteParams) + '}';
        }

    }

}
