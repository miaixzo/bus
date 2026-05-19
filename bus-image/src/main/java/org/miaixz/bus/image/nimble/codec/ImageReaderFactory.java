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
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.metric.Property;
import org.miaixz.bus.image.nimble.codec.jpeg.PatchJPEGLS;
import org.miaixz.bus.logger.Logger;

/**
 * Represents the ImageReaderFactory type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ImageReaderFactory implements Serializable {

    /**
     * Constructs a new ImageReaderFactory instance.
     */
    public ImageReaderFactory() {
        // No initialization required.
    }

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852288677583L;

    /**
     * The default factory value.
     */
    private static volatile ImageReaderFactory defaultFactory;

    /**
     * The map value.
     */
    private final TreeMap<String, ImageReaderParam> map = new TreeMap<>();

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
    public static ImageReaderFactory getDefault() {
        if (defaultFactory == null)
            defaultFactory = init();

        return defaultFactory;
    }

    /**
     * Sets the default.
     *
     * @param factory the factory.
     */
    public static void setDefault(ImageReaderFactory factory) {
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
     * Gets the image reader param.
     *
     * @param tsuid the tsuid.
     * @return the image reader param.
     */
    public static ImageReaderParam getImageReaderParam(String tsuid) {
        return getDefault().get(tsuid);
    }

    /**
     * Determines whether decompress.
     *
     * @param tsuid the tsuid.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean canDecompress(String tsuid) {
        return getDefault().contains(tsuid);
    }

    /**
     * Gets the image reader.
     *
     * @param param the param.
     * @return the image reader.
     */
    public static ImageReader getImageReader(ImageReaderParam param) {
        return Boolean.getBoolean("org.miaixz.bus.image.nimble.codec.useServiceLoader")
                ? getImageReaderFromServiceLoader(param)
                : getImageReaderFromImageIOServiceRegistry(param);
    }

    /**
     * Gets the image reader from image io service registry.
     *
     * @param param the param.
     * @return the image reader from image io service registry.
     */
    public static ImageReader getImageReaderFromImageIOServiceRegistry(ImageReaderParam param) {
        Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName(param.formatName);
        if (!iter.hasNext())
            throw new RuntimeException("No Reader for format: " + param.formatName + " registered");

        ImageReader reader = iter.next();
        if (param.className != null) {
            while (!param.className.equals(reader.getClass().getName())) {
                if (iter.hasNext())
                    reader = iter.next();
                else {
                    Logger.warn(
                            false,
                            "Image",
                            "No preferred Reader {} for format: format={} - use {}",
                            param.className,
                            param.formatName,
                            reader.getClass().getName());
                    break;
                }
            }
        }
        return reader;
    }

    /**
     * Gets the image reader from service loader.
     *
     * @param param the param.
     * @return the image reader from service loader.
     */
    public static ImageReader getImageReaderFromServiceLoader(ImageReaderParam param) {
        try {
            return getImageReaderSpi(param).createReaderInstance();
        } catch (IOException e) {
            throw new RuntimeException("Error instantiating Reader for format: " + param.formatName, e);
        }
    }

    /**
     * Gets the image reader spi.
     *
     * @param param the param.
     * @return the image reader spi.
     */
    private static ImageReaderSpi getImageReaderSpi(ImageReaderParam param) {
        Iterator<ImageReaderSpi> iter = new FormatNameFilterIterator<>(
                ServiceLoader.load(ImageReaderSpi.class).iterator(), param.formatName);
        if (!iter.hasNext())
            throw new RuntimeException("No Reader for format: " + param.formatName + " registered");

        ImageReaderSpi spi = iter.next();
        if (param.className != null) {
            while (!param.className.equals(spi.getPluginClassName())) {
                if (iter.hasNext())
                    spi = iter.next();
                else {
                    Logger.warn(
                            false,
                            "Image",
                            "No preferred Reader {} for format: format={} - use {}",
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
     * Executes the init operation.
     *
     * @return the operation result.
     */
    public static ImageReaderFactory init() {
        ImageReaderFactory factory = new ImageReaderFactory();
        URL url = ResourceKit.getResourceUrl("ImageReaderFactory.properties", ImageReaderFactory.class);
        try {
            Properties props = new Properties();
            props.load(url.openStream());
            for (Entry<Object, Object> entry : props.entrySet()) {
                String[] ss = Builder.split((String) entry.getValue(), ':');
                factory.map.put(
                        (String) entry.getKey(),
                        new ImageReaderParam(ss[0], ss[1], ss[2],
                                ss.length > 3 ? Builder.split(ss[3], ';') : Normal.EMPTY_STRING_ARRAY));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Image Reader Factory configuration from: " + url.toString(), e);
        }
        return factory;
    }

    /**
     * Executes the get operation.
     *
     * @param tsuid the tsuid.
     * @return the operation result.
     */
    public ImageReaderParam get(String tsuid) {
        return map.get(tsuid);
    }

    /**
     * Executes the contains operation.
     *
     * @param tsuid the tsuid.
     * @return true if the condition is met; otherwise false.
     */
    public boolean contains(String tsuid) {
        return map.containsKey(tsuid);
    }

    /**
     * Executes the put operation.
     *
     * @param tsuid the tsuid.
     * @param param the param.
     * @return the operation result.
     */
    public ImageReaderParam put(String tsuid, ImageReaderParam param) {
        return map.put(tsuid, param);
    }

    /**
     * Executes the remove operation.
     *
     * @param tsuid the tsuid.
     * @return the operation result.
     */
    public ImageReaderParam remove(String tsuid) {
        return map.remove(tsuid);
    }

    /**
     * Gets the entries.
     *
     * @return the entries.
     */
    public Set<Entry<String, ImageReaderParam>> getEntries() {
        return Collections.unmodifiableMap(map).entrySet();
    }

    /**
     * Executes the clear operation.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Represents the ImageReaderParam type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class ImageReaderParam implements Serializable {

        /**
         * The serial version uid value.
         */
        @Serial
        private static final long serialVersionUID = 2852288722533L;

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
         * The image read params value.
         */
        public final Property[] imageReadParams;

        /**
         * Creates a new instance.
         *
         * @param formatName      the format name.
         * @param className       the class name.
         * @param patchJPEGLS     the patch jpegls.
         * @param imageReadParams the image read params.
         */
        public ImageReaderParam(String formatName, String className, PatchJPEGLS patchJPEGLS,
                Property[] imageReadParams) {
            this.formatName = formatName;
            this.className = nullify(className);
            this.patchJPEGLS = patchJPEGLS;
            this.imageReadParams = imageReadParams;
        }

        /**
         * Creates a new instance.
         *
         * @param formatName       the format name.
         * @param className        the class name.
         * @param patchJPEGLS      the patch jpegls.
         * @param imageWriteParams the image write params.
         */
        public ImageReaderParam(String formatName, String className, String patchJPEGLS, String... imageWriteParams) {
            this(formatName, className,
                    patchJPEGLS != null && !patchJPEGLS.isEmpty() ? PatchJPEGLS.valueOf(patchJPEGLS) : null,
                    Property.valueOf(imageWriteParams));
        }

        /**
         * Gets the image read params.
         *
         * @return the image read params.
         */
        public Property[] getImageReadParams() {
            return imageReadParams;
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

            ImageReaderParam that = (ImageReaderParam) o;

            if (!formatName.equals(that.formatName))
                return false;
            if (!Objects.equals(className, that.className))
                return false;
            if (patchJPEGLS != that.patchJPEGLS)
                return false;
            return Arrays.equals(imageReadParams, that.imageReadParams);

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
            result = 31 * result + Arrays.hashCode(imageReadParams);
            return result;
        }

        /**
         * Returns the string representation.
         *
         * @return the string representation.
         */
        @Override
        public String toString() {
            return "ImageReaderParam{" + "formatName='" + formatName + Symbol.C_SINGLE_QUOTE + ", className='"
                    + className + Symbol.C_SINGLE_QUOTE + ", patchJPEGLS=" + patchJPEGLS + ", imageReadParams="
                    + Arrays.toString(imageReadParams) + '}';
        }

    }

}
