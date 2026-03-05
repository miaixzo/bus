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
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageWriterFactory implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852288887239L;

    private static volatile ImageWriterFactory defaultFactory;
    private final TreeMap<String, ImageWriterParam> map = new TreeMap<>();
    private PatchJPEGLS patchJPEGLS;

    private static String nullify(String s) {
        return null == s || s.isEmpty() || s.equals(Symbol.STAR) ? null : s;
    }

    public static ImageWriterFactory getDefault() {
        if (defaultFactory == null)
            defaultFactory = init();

        return defaultFactory;
    }

    public static void setDefault(ImageWriterFactory factory) {
        if (factory == null)
            throw new NullPointerException();

        defaultFactory = factory;
    }

    public static void resetDefault() {
        defaultFactory = null;
    }

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

    public static ImageWriterParam getImageWriterParam(String tsuid) {
        return getDefault().get(tsuid);
    }

    public static ImageWriter getImageWriter(ImageWriterParam param) {
        return Boolean.getBoolean("org.miaixz.bus.image.nimble.codec.useServiceLoader")
                ? getImageWriterFromServiceLoader(param)
                : getImageWriterFromImageIOServiceRegistry(param);
    }

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
                            "No preferred Writer {} for format: {} - use {}",
                            param.className,
                            param.formatName,
                            writer.getClass().getName());
                    break;
                }
            }
        }
        return writer;
    }

    public static ImageWriter getImageWriterFromServiceLoader(ImageWriterParam param) {
        try {
            return getImageWriterSpi(param).createWriterInstance();
        } catch (IOException e) {
            throw new RuntimeException("Error instantiating Writer for format: " + param.formatName, e);
        }
    }

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
                            "No preferred Writer {} for format: {} - use {}",
                            param.className,
                            param.formatName,
                            spi.getPluginClassName());
                    break;
                }
            }
        }
        return spi;
    }

    public final PatchJPEGLS getPatchJPEGLS() {
        return patchJPEGLS;
    }

    public final void setPatchJPEGLS(PatchJPEGLS patchJPEGLS) {
        this.patchJPEGLS = patchJPEGLS;
    }

    public ImageWriterParam get(String tsuid) {
        return map.get(tsuid);
    }

    public ImageWriterParam put(String tsuid, ImageWriterParam param) {
        return map.put(tsuid, param);
    }

    public ImageWriterParam remove(String tsuid) {
        return map.remove(tsuid);
    }

    public Set<Entry<String, ImageWriterParam>> getEntries() {
        return Collections.unmodifiableMap(map).entrySet();
    }

    public void clear() {
        map.clear();
    }

    public static class ImageWriterParam implements Serializable {

        @Serial
        private static final long serialVersionUID = 2852289015819L;

        public final String formatName;
        public final String className;
        public final PatchJPEGLS patchJPEGLS;
        public final Property[] imageWriteParams;

        public ImageWriterParam(String formatName, String className, PatchJPEGLS patchJPEGLS,
                Property[] imageWriteParams) {
            this.formatName = formatName;
            this.className = nullify(className);
            this.patchJPEGLS = patchJPEGLS;
            this.imageWriteParams = imageWriteParams;
        }

        public ImageWriterParam(String formatName, String className, String patchJPEGLS, String[] imageWriteParams) {
            this(formatName, className,
                    patchJPEGLS != null && !patchJPEGLS.isEmpty() ? PatchJPEGLS.valueOf(patchJPEGLS) : null,
                    Property.valueOf(imageWriteParams));
        }

        public Property[] getImageWriteParams() {
            return imageWriteParams;
        }

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

        @Override
        public int hashCode() {
            int result = formatName.hashCode();
            result = 31 * result + (className != null ? className.hashCode() : 0);
            result = 31 * result + (patchJPEGLS != null ? patchJPEGLS.hashCode() : 0);
            result = 31 * result + Arrays.hashCode(imageWriteParams);
            return result;
        }

        @Override
        public String toString() {
            return "ImageWriterParam{" + "formatName='" + formatName + '\'' + ", className='" + className + '\''
                    + ", patchJPEGLS=" + patchJPEGLS + ", imageWriteParams=" + Arrays.toString(imageWriteParams) + '}';
        }
    }

}
