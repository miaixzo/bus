/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring.banner;

import java.io.InputStream;
import java.io.PrintStream;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.IoKit;
import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

/**
 * Abstract base class for {@link Banner} implementations.
 * <p>
 * This class provides common functionality for loading banner text from resources and handling ANSI escape codes.
 * Subclasses must implement the {@link #printBanner(String)} method to define how the banner text is processed.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractBanner implements Banner {

    /**
     * The resource class used to load the banner resource.
     */
    protected Class<?> resourceClass;
    /**
     * The location of the banner resource (e.g., "/banner.txt").
     */
    protected String resourceLocation;
    /**
     * The default banner text to use if the resource cannot be loaded.
     */
    protected String defaultBanner;
    /**
     * The final banner text, after loading and processing.
     */
    protected String banner;

    /**
     * Constructs an {@code AbstractBanner} with the specified resource class, location, and default banner text.
     *
     * @param resourceClass    The class to use for loading resources.
     * @param resourceLocation The location of the banner resource.
     * @param defaultBanner    The default banner text.
     */
    public AbstractBanner(Class<?> resourceClass, String resourceLocation, String defaultBanner) {
        this.resourceClass = resourceClass;
        this.resourceLocation = resourceLocation;
        this.defaultBanner = defaultBanner;
    }

    /**
     * Initializes the banner by attempting to load it from the specified resource location. If loading fails, it falls
     * back to the default banner text.
     */
    protected void initialize() {
        InputStream inputStream = null;
        String bannerText = null;
        try {
            if (null != resourceLocation) {
                inputStream = resourceClass.getResourceAsStream(resourceLocation);
                if (inputStream != null) {
                    bannerText = IoKit.readUtf8(IoKit.toBuffered(inputStream));
                }
            }
        } catch (Exception e) {
            // Log the exception if necessary, but don't rethrow to allow startup to continue
        } finally {
            banner = printBanner(bannerText);

            if (null != inputStream) {
                IoKit.close(inputStream);
            }
        }
    }

    /**
     * Returns the processed banner text.
     *
     * @return The banner text, potentially including ANSI escape codes.
     */
    public String getBanner() {
        return banner;
    }

    /**
     * Returns the banner text with ANSI escape codes removed, resulting in plain text.
     *
     * @return The plain text banner.
     */
    public String getPlainBanner() {
        if (null != banner) {
            // Regular expression to remove ANSI escape codes
            banner = banner.replaceAll("\\u001b\\[[;\\d]*m", Normal.EMPTY);
        }
        return banner;
    }

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        out.println();
        out.println(printBanner(null));
        out.println();
    }

    /**
     * Abstract method to be implemented by subclasses to define how the banner text is processed.
     *
     * @param bannerText The loaded banner text, or {@code null} if not loaded from a resource.
     * @return The final banner string to be displayed.
     */
    protected abstract String printBanner(String bannerText);

}
